// Milestone6_SmartIrrigationFX.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ============================================
// ENUMERATIONS (Reused from Milestone 5)
// ============================================

enum GrowthStageFX {
    SEEDLING(0, 20, 2.5, "#4CAF50", "Early growth"),
    VEGETATIVE(21, 60, 5.0, "#2196F3", "Rapid growth"),
    FLOWERING(61, 90, 4.0, "#FF9800", "Critical period"),
    MATURATION(91, 120, 2.0, "#9C27B0", "Ripening");
    
    final int startDay, endDay;
    final double waterRequirement;
    final String color, description;
    
    GrowthStageFX(int start, int end, double water, String color, String desc) {
        this.startDay = start; this.endDay = end;
        this.waterRequirement = water; this.color = color; this.description = desc;
    }
    
    static GrowthStageFX fromDay(int day) {
        if (day <= SEEDLING.endDay) return SEEDLING;
        if (day <= VEGETATIVE.endDay) return VEGETATIVE;
        if (day <= FLOWERING.endDay) return FLOWERING;
        return MATURATION;
    }
}

// ============================================
// MAIN GUI APPLICATION
// ============================================

public class Milestone6_SmartIrrigationFX extends Application {
    
    // Simulation components
    private ExecutorService executorService;
    private final List<FieldData> fields = new ArrayList<>();
    private final ObservableList<String> logMessages = FXCollections.observableArrayList();
    private final AtomicInteger globalIrrigationTotal = new AtomicInteger(0);
    
    // GUI Components
    private LineChart<String, Number> moistureChart;
    private BarChart<String, Number> irrigationChart;
    private ListView<String> logListView;
    private ProgressIndicator simulationProgress;
    private Label statusLabel;
    private Label irrigationLabel;
    private Label completionLabel;
    private Timeline refreshTimeline;
    private Button startButton, pauseButton, resetButton;
    
    // Simulation control
    private volatile boolean simulationRunning = false;
    private volatile boolean simulationPaused = false;
    private int currentDay = 0;
    private final int totalDays = 60;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Irrigation Optimization System - Complete Solution");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");
        
        // Add menu bar
        mainLayout.setTop(createMenuBar());
        
        // Add dashboard (center)
        mainLayout.setCenter(createDashboard());
        
        // Add control panel (right)
        mainLayout.setRight(createControlPanel());
        
        // Add status bar (bottom)
        mainLayout.setBottom(createStatusBar());
        
        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initialize fields
        initializeFields();
        
        // Start auto-refresh for charts
        startAutoRefresh();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem exportLog = new MenuItem("Export Simulation Log");
        exportLog.setOnAction(e -> exportLog());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(exportLog, new SeparatorMenuItem(), exitItem);
        
        // Simulation Menu
        Menu simMenu = new Menu("Simulation");
        MenuItem startSim = new MenuItem("Start");
        startSim.setOnAction(e -> startSimulation());
        MenuItem pauseSim = new MenuItem("Pause");
        pauseSim.setOnAction(e -> pauseSimulation());
        MenuItem resetSim = new MenuItem("Reset");
        resetSim.setOnAction(e -> resetSimulation());
        simMenu.getItems().addAll(startSim, pauseSim, resetSim);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        ToggleMenuItem showCharts = new ToggleMenuItem("Show Real-time Charts", true);
        showCharts.setOnAction(e -> {
            // Toggle chart visibility
        });
        viewMenu.getItems().add(showCharts);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, simMenu, viewMenu, helpMenu);
        return menuBar;
    }
    
    private GridPane createDashboard() {
        GridPane dashboard = new GridPane();
        dashboard.setPadding(new Insets(15));
        dashboard.setHgap(15);
        dashboard.setVgap(15);
        
        // Title
        Label titleLabel = new Label("📊 FIELD MONITORING DASHBOARD");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.valueOf("#2E7D32"));
        GridPane.setColumnSpan(titleLabel, 2);
        dashboard.add(titleLabel, 0, 0);
        
        // Soil Moisture Chart (Line Chart)
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (Days)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Soil Moisture (%)");
        yAxis.setRange(0, 100);
        
        moistureChart = new LineChart<>(xAxis, yAxis);
        moistureChart.setTitle("Soil Moisture Trends by Field");
        moistureChart.setPrefHeight(350);
        moistureChart.setPrefWidth(700);
        dashboard.add(moistureChart, 0, 1);
        
        // Irrigation Summary Chart (Bar Chart)
        CategoryAxis xAxis2 = new CategoryAxis();
        xAxis2.setLabel("Field");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("Total Irrigation (mm)");
        
        irrigationChart = new BarChart<>(xAxis2, yAxis2);
        irrigationChart.setTitle("Cumulative Irrigation Applied");
        irrigationChart.setPrefHeight(350);
        irrigationChart.setPrefWidth(500);
        dashboard.add(irrigationChart, 1, 1);
        
        // Log Panel
        Label logLabel = new Label("📋 Simulation Log");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dashboard.add(logLabel, 0, 2);
        
        logListView = new ListView<>(logMessages);
        logListView.setPrefHeight(250);
        logListView.setPrefWidth(700);
        dashboard.add(logListView, 0, 3);
        
        // Field Status Cards
        GridPane statusCards = createFieldStatusCards();
        dashboard.add(statusCards, 1, 2);
        GridPane.setRowSpan(statusCards, 2);
        
        return dashboard;
    }
    
    private GridPane createFieldStatusCards() {
        GridPane cardsPane = new GridPane();
        cardsPane.setHgap(10);
        cardsPane.setVgap(10);
        cardsPane.setPadding(new Insets(5));
        
        String[] fieldNames = {"North Field", "South Field", "East Field", "West Field", "Center Field"};
        Color[] colors = {Color.web("#4CAF50"), Color.web("#FF9800"), Color.web("#2196F3"), 
                          Color.web("#9C27B0"), Color.web("#F44336")};
        
        for (int i = 0; i < fieldNames.length; i++) {
            VBox card = createStatusCard(fieldNames[i], colors[i]);
            cardsPane.add(card, i % 2, i / 2);
        }
        
        return cardsPane;
    }
    
    private VBox createStatusCard(String fieldName, Color color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + toHex(color) + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setPrefWidth(180);
        
        Label nameLabel = new Label(fieldName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(color);
        
        Label moistureLabel = new Label("Moisture: --%");
        moistureLabel.setFont(Font.font("Arial", 12));
        
        Label stageLabel = new Label("Stage: --");
        stageLabel.setFont(Font.font("Arial", 12));
        
        Label statusLabel = new Label("Status: --");
        statusLabel.setFont(Font.font("Arial", 12));
        
        ProgressBar moistureBar = new ProgressBar(0);
        moistureBar.setPrefWidth(150);
        
        card.getChildren().addAll(nameLabel, moistureLabel, moistureBar, stageLabel, statusLabel);
        
        // Store references for updates
        fieldCards.put(fieldName, new CardData(moistureLabel, stageLabel, statusLabel, moistureBar));
        
        return card;
    }
    
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setStyle("-fx-background-color: #e8f5e9; -fx-border-color: #c8e6c9; -fx-border-width: 1;");
        controlPanel.setPrefWidth(250);
        
        Label controlLabel = new Label("🎮 SIMULATION CONTROL");
        controlLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        controlLabel.setTextFill(Color.valueOf("#2E7D32"));
        
        // Control buttons
        startButton = new Button("▶ START SIMULATION");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setPrefWidth(200);
        startButton.setOnAction(e -> startSimulation());
        
        pauseButton = new Button("⏸ PAUSE SIMULATION");
        pauseButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        pauseButton.setPrefWidth(200);
        pauseButton.setOnAction(e -> pauseSimulation());
        pauseButton.setDisable(true);
        
        resetButton = new Button("🔄 RESET SIMULATION");
        resetButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setPrefWidth(200);
        resetButton.setOnAction(e -> resetSimulation());
        
        // Progress indicator
        simulationProgress = new ProgressIndicator(0);
        simulationProgress.setPrefSize(80, 80);
        
        Label progressLabel = new Label("Simulation Progress");
        
        // Statistics
        Separator separator = new Separator();
        
        Label statsLabel = new Label("📈 STATISTICS");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        irrigationLabel = new Label("Total Irrigation: 0 mm");
        completionLabel = new Label("Days Completed: 0");
        
        // Quick settings
        Label settingsLabel = new Label("⚙ QUICK SETTINGS");
        settingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Slider speedSlider = new Slider(0.5, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        Label speedLabel = new Label("Simulation Speed: 1.0x");
        
        speedSlider.valueProperty().addListener((obs, old, val) -> {
            speedLabel.setText(String.format("Simulation Speed: %.1fx", val.doubleValue()));
            updateSimulationSpeed(val.doubleValue());
        });
        
        controlPanel.getChildren().addAll(
            controlLabel, startButton, pauseButton, resetButton,
            simulationProgress, progressLabel, separator,
            statsLabel, irrigationLabel, completionLabel, separator,
            settingsLabel, speedSlider, speedLabel
        );
        
        return controlPanel;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(15);
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.setStyle("-fx-background-color: #2E7D32;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("System Ready");
        statusLabel.setTextFill(Color.WHITE);
        
        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeLabel.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBar.getChildren().addAll(statusLabel, spacer, timeLabel);
        
        return statusBar;
    }
    
    // ============================================
    // CORE FIELD DATA CLASS
    // ============================================
    
    class FieldData {
        String name;
        double moisture;
        GrowthStageFX stage;
        int totalIrrigation;
        XYChart.Series<String, Number> moistureSeries;
        XYChart.Data<String, Number> lastDataPoint;
        
        FieldData(String name, double initialMoisture) {
            this.name = name;
            this.moisture = initialMoisture;
            this.stage = GrowthStageFX.SEEDLING;
            this.totalIrrigation = 0;
            this.moistureSeries = new XYChart.Series<>();
            this.moistureSeries.setName(name);
        }
        
        void update(int day, double rain, double temperature) {
            // Calculate water loss
            double loss = stage.waterRequirement * (0.5 + moisture / 100.0);
            loss = Math.min(loss, moisture);
            
            // Determine irrigation need
            double irrigation = 0;
            if (moisture < 25) {
                irrigation = 20.0;
                addLog(name + ": CRITICAL - Emergency irrigation (" + irrigation + "mm)");
            } else if (moisture < 35) {
                irrigation = 12.0;
                addLog(name + ": WARNING - Standard irrigation (" + irrigation + "mm)");
            } else if (moisture < 45 && stage.waterRequirement > 4) {
                irrigation = 6.0;
            }
            
            totalIrrigation += irrigation;
            globalIrrigationTotal.addAndGet((int)irrigation);
            
            // Apply water balance
            double effectiveRain = rain * 0.7;
            double effectiveIrrigation = irrigation * 0.8;
            moisture = moisture - loss + effectiveRain + effectiveIrrigation;
            
            // Bounds checking
            moisture = Math.max(10, Math.min(80, moisture));
            
            // Update growth stage
            stage = GrowthStageFX.fromDay(day);
            
            // Record for chart
            if (moistureSeries.getData().size() < 60) {
                XYChart.Data<String, Number> point = new XYChart.Data<>(String.valueOf(day), moisture);
                moistureSeries.getData().add(point);
                lastDataPoint = point;
            }
        }
    }
    
    // Storage for UI updates
    private final Map<String, CardData> fieldCards = new HashMap<>();
    
    class CardData {
        Label moistureLabel, stageLabel, statusLabel;
        ProgressBar moistureBar;
        
        CardData(Label m, Label s, Label st, ProgressBar b) {
            this.moistureLabel = m; this.stageLabel = s; this.statusLabel = st; this.moistureBar = b;
        }
    }
    
    private void initializeFields() {
        fields.clear();
        fields.add(new FieldData("North Field", 45));
        fields.add(new FieldData("South Field", 32));
        fields.add(new FieldData("East Field", 50));
        fields.add(new FieldData("West Field", 38));
        fields.add(new FieldData("Center Field", 28));
        
        // Add series to chart
        moistureChart.getData().clear();
        for (FieldData field : fields) {
            moistureChart.getData().add(field.moistureSeries);
        }
    }
    
    private void addLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message;
        
        Platform.runLater(() -> {
            logMessages.add(0, logEntry);
            if (logMessages.size() > 100) {
                logMessages.remove(logMessages.size() - 1);
            }
        });
    }
    
    private void startSimulation() {
        if (simulationRunning && !simulationPaused) return;
        
        simulationRunning = true;
        simulationPaused = false;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        statusLabel.setText("Simulation Running...");
        addLog("=== SIMULATION STARTED ===");
        
        // Run simulation in background thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::runSimulation);
    }
    
    private void pauseSimulation() {
        simulationPaused = true;
        pauseButton.setDisable(true);
        startButton.setDisable(false);
        statusLabel.setText("Simulation Paused");
        addLog("=== SIMULATION PAUSED ===");
    }
    
    private void resetSimulation() {
        simulationRunning = false;
        simulationPaused = false;
        currentDay = 0;
        globalIrrigationTotal.set(0);
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        
        // Reset fields
        initializeFields();
        logMessages.clear();
        
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        statusLabel.setText("System Reset");
        simulationProgress.setProgress(0);
        irrigationLabel.setText("Total Irrigation: 0 mm");
        completionLabel.setText("Days Completed: 0");
        
        addLog("=== SIMULATION RESET ===");
    }
    
    private void runSimulation() {
        Random random = new Random();
        
        for (currentDay = 1; currentDay <= totalDays && simulationRunning; currentDay++) {
            // Handle pause
            while (simulationPaused && simulationRunning) {
                try { Thread.sleep(100); } catch (InterruptedException e) { break; }
            }
            
            final int day = currentDay;
            double rain = 2 + random.nextDouble() * 8;  // 2-10mm rain
            double temperature = 22 + random.nextDouble() * 12;  // 22-34°C
            
            // Update all fields
            for (FieldData field : fields) {
                field.update(day, rain, temperature);
            }
            
            // Update UI
            Platform.runLater(() -> {
                updateFieldCards(day);
                updateIrrigationChart();
                updateProgress(day);
                
                irrigationLabel.setText(String.format("Total Irrigation: %d mm", globalIrrigationTotal.get()));
                completionLabel.setText(String.format("Days Completed: %d / %d", day, totalDays));
            });
            
            // Add daily log
            if (day % 5 == 0) {
                addLog(String.format("Day %d: Rain=%.1fmm, Temp=%.1f°C", day, rain, temperature));
            }
            
            // Control simulation speed
            try { Thread.sleep(200); } catch (InterruptedException e) { break; }
        }
        
        simulationRunning = false;
        Platform.runLater(() -> {
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            statusLabel.setText("Simulation Complete!");
            addLog("=== SIMULATION COMPLETED SUCCESSFULLY ===");
            addLog(String.format("Total Irrigation Applied: %d mm across all fields", globalIrrigationTotal.get()));
            simulationProgress.setProgress(1.0);
        });
    }
    
    private void updateFieldCards(int day) {
        for (FieldData field : fields) {
            CardData card = fieldCards.get(field.name);
            if (card != null) {
                card.moistureLabel.setText(String.format("Moisture: %.1f%%", field.moisture));
                card.moistureBar.setProgress(field.moisture / 100.0);
                card.stageLabel.setText(String.format("Stage: %s", field.stage.name()));
                
                String status;
                if (field.moisture < 25) status = "⚠ CRITICAL";
                else if (field.moisture < 35) status = "⚠ DRY";
                else if (field.moisture < 60) status = "✓ OPTIMAL";
                else status = "💧 SATURATED";
                card.statusLabel.setText("Status: " + status);
            }
        }
    }
    
    private void updateIrrigationChart() {
        irrigationChart.getData().clear();
        
        CategoryAxis xAxis = (CategoryAxis) irrigationChart.getXAxis();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Irrigation (mm)");
        
        for (FieldData field : fields) {
            series.getData().add(new XYChart.Data<>(field.name, field.totalIrrigation));
        }
        
        irrigationChart.getData().add(series);
    }
    
    private void updateProgress(int day) {
        double progress = (double) day / totalDays;
        simulationProgress.setProgress(progress);
    }
    
    private void updateSimulationSpeed(double speed) {
        // Speed control implementation
        // Would adjust sleep duration in simulation loop
    }
    
    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (simulationRunning && !simulationPaused) {
                // Auto-refresh logic
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
    
    private void exportLog() {
        // Export log messages to file
        addLog("Log export requested - would save to file");
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Smart Irrigation System");
        alert.setHeaderText("Smart Irrigation Optimization System");
        alert.setContentText(
            "Version: 2.0 Complete\n" +
            "Developed for: Biosystems Engineering\n\n" +
            "Features:\n" +
            "• Real-time field monitoring\n" +
            "• Multi-threaded simulation\n" +
            "• Interactive dashboard\n" +
            "• Data visualization\n\n" +
            "© 2024 All Rights Reserved"
        );
        alert.showAndWait();
    }
    
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    @Override
    public void stop() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// Helper ToggleMenuItem class
class ToggleMenuItem extends MenuItem {
    private boolean selected;
    
    public ToggleMenuItem(String text, boolean initial) {
        super(text);
        this.selected = initial;
        setOnAction(e -> {
            selected = !selected;
            if (selected) {
                setStyle("-fx-font-weight: bold;");
            } else {
                setStyle(null);
            }
        });
        if (selected) setStyle("-fx-font-weight: bold;");
    }
}
