// SmartIrrigationV2.java
import java.util.ArrayList;
import java.util.Scanner;

// Encapsulated class: SoilSensor
class SoilSensor {
    private double moisture;
    private final double fieldCapacity;
    private final double wiltingPoint;
    
    public SoilSensor(double fc, double wp) {
        this.fieldCapacity = fc;
        this.wiltingPoint = wp;
        this.moisture = 35.0;
    }
    
    public double getMoisture() { return moisture; }
    
    public void setMoisture(double m) {
        if (m < wiltingPoint) this.moisture = wiltingPoint;
        else if (m > fieldCapacity) this.moisture = fieldCapacity;
        else this.moisture = m;
    }
    
    public boolean isDry() { return moisture < 25.0; }
    public boolean isCriticallyDry() { return moisture < wiltingPoint + 5.0; }
    public double getWaterDeficit() { return fieldCapacity - moisture; }
}

// Encapsulated class: Crop
class Crop {
    private String cropType;
    private int growthStage;
    private double waterRequirement;
    
    public Crop(String type, int initialStage) {
        this.cropType = type;
        this.growthStage = initialStage;
        updateWaterRequirement();
    }
    
    public void advanceDay() {
        growthStage++;
        updateWaterRequirement();
    }
    
    public int getGrowthStage() { return growthStage; }
    public String getCropType() { return cropType; }
    public double getWaterRequirement() { return waterRequirement; }
    
    private void updateWaterRequirement() {
        // Decision logic inside encapsulation
        if (growthStage < 20) waterRequirement = 3.0;
        else if (growthStage < 60) waterRequirement = 5.0;
        else if (growthStage < 90) waterRequirement = 4.0;
        else waterRequirement = 2.5;
    }
}

// Encapsulated class: Weather
class Weather {
    private double temperature;
    private double rainfallForecast;
    private double evapotranspiration;
    
    public Weather(double temp, double rain) {
        this.temperature = temp;
        this.rainfallForecast = rain;
        computeEvapotranspiration();
    }
    
    public void update(double temp, double rain) {
        this.temperature = temp;
        this.rainfallForecast = rain;
        computeEvapotranspiration();
    }
    
    public double getTemperature() { return temperature; }
    public double getRainfallForecast() { return rainfallForecast; }
    public double getEvapotranspiration() { return evapotranspiration; }
    
    private void computeEvapotranspiration() {
        double baseET = 3.0;
        double tempFactor = 1.0 + (temperature - 25.0) * 0.05;
        this.evapotranspiration = baseET * tempFactor;
    }
}

// Main decision system class
class IrrigationOptimizer {
    private SoilSensor sensor;
    private Crop crop;
    private Weather weather;
    private double irrigationEfficiency;
    private ArrayList<String> decisionLog;
    
    public IrrigationOptimizer(String cropType, double efficiency) {
        this.sensor = new SoilSensor(45.0, 15.0);
        this.crop = new Crop(cropType, 30);
        this.weather = new Weather(28.0, 5.0);
        this.irrigationEfficiency = efficiency;
        this.decisionLog = new ArrayList<>();
        decisionLog.add("System Initialized: " + cropType);
    }
    
    // Core decision-making with conditional logic
    public String makeDecision() {
        double moisture = sensor.getMoisture();
        double cropNeed = crop.getWaterRequirement();
        double et = weather.getEvapotranspiration();
        double rain = weather.getRainfallForecast();
        
        // Multi-level decision tree
        if (sensor.isCriticallyDry()) {
            return "EMERGENCY: Immediate heavy irrigation required!";
        }
        
        if (sensor.isDry()) {
            return "WARNING: Irrigation recommended (soil moisture low)";
        }
        
        double effectiveRain = rain * 0.7;
        double totalWaterAvailable = moisture + effectiveRain * 0.5;
        double waterDeficit = cropNeed * 1.2 - totalWaterAvailable;
        
        // Conditional decision structure
        if (waterDeficit > 10.0) {
            double irrigationAmount = waterDeficit / irrigationEfficiency;
            return String.format("IRRIGATE: %.0f mm needed", irrigationAmount);
        } else if (waterDeficit > 5.0 && et > 4.0) {
            return String.format("LIGHT IRRIGATION: %.0f mm", waterDeficit);
        } else if (rain > 15.0) {
            return "SKIP IRRIGATION: Significant rain expected";
        } else if (moisture > 40.0 && cropNeed < 4.0) {
            return "NO IRRIGATION: Adequate moisture for current stage";
        } else {
            return "MONITOR ONLY: No immediate action needed";
        }
    }
    
    public void executeIrrigation(String decision) {
        if (decision.contains("IRRIGATE") || decision.contains("EMERGENCY")) {
            double amount = 15.0;
            // Parse amount from decision string
            if (decision.contains("mm")) {
                String[] parts = decision.split(" ");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        amount = Double.parseDouble(part);
                        break;
                    }
                }
            }
            double moistureIncrease = amount * irrigationEfficiency * 0.3;
            sensor.setMoisture(sensor.getMoisture() + moistureIncrease);
            decisionLog.add("Irrigation applied: " + amount + " mm");
        }
    }
    
    // Loop-based simulation
    public void simulate(int days, Scanner scanner) {
        System.out.println("\n=== Starting " + days + "-Day Simulation ===\n");
        
        for (int day = 1; day <= days; day++) {
            System.out.println("========== DAY " + day + " ==========");
            System.out.printf(" Soil Moisture: %.1f%%\n", sensor.getMoisture());
            System.out.println(" Crop Stage: Day " + crop.getGrowthStage());
            System.out.printf(" Crop Need: %.1f mm/day\n", crop.getWaterRequirement());
            System.out.printf(" Temperature: %.1f°C\n", weather.getTemperature());
            System.out.printf(" Rain Forecast: %.1f mm\n", weather.getRainfallForecast());
            
            String decision = makeDecision();
            System.out.println(" DECISION: " + decision);
            
            executeIrrigation(decision);
            
            // Apply environmental changes
            double moistureLoss = weather.getEvapotranspiration() * 0.6;
            double rainEffect = weather.getRainfallForecast() * 0.3;
            sensor.setMoisture(sensor.getMoisture() - moistureLoss + rainEffect);
            crop.advanceDay();
            
            // Weekly update with input validation
            if (day % 7 == 0) {
                System.out.println("\n--- Weekly Forecast Update ---");
                double newTemp = getValidatedDouble(scanner, "Enter temperature for next week (°C): ", -10, 60);
                double newRain = getValidatedDouble(scanner, "Enter rainfall forecast (mm): ", 0, 200);
                weather.update(newTemp, newRain);
            }
            
            System.out.println("----------------------------------------\n");
        }
        
        // Summary output
        System.out.println("\n=== SIMULATION SUMMARY ===");
        System.out.println("Total days simulated: " + days);
        System.out.println("Final growth stage: Day " + crop.getGrowthStage());
        System.out.printf("Final soil moisture: %.1f%%\n", sensor.getMoisture());
    }
    
    private double getValidatedDouble(Scanner scanner, String prompt, double min, double max) {
        double value;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextDouble()) {
                value = scanner.nextDouble();
                if (value >= min && value <= max) {
                    return value;
                }
            } else {
                scanner.next(); // clear invalid input
            }
            System.out.printf("Invalid! Enter a value between %.0f and %.0f: ", min, max);
        }
    }
}

// Main class
public class SmartIrrigationV2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SMART IRRIGATION OPTIMIZATION SYSTEM ===");
        System.out.println("Milestone 2: Structured Decision Systems\n");
        
        // User interaction
        System.out.print("Enter crop type (e.g., Maize, Wheat, Tomato): ");
        String cropType = scanner.nextLine();
        
        int simulationDays = 0;
        while (true) {
            System.out.print("Enter number of days to simulate (1-365): ");
            if (scanner.hasNextInt()) {
                simulationDays = scanner.nextInt();
                if (simulationDays >= 1 && simulationDays <= 365) {
                    break;
                }
            } else {
                scanner.next();
            }
            System.out.println("Invalid input. Please enter 1-365.");
        }
        
        // Create optimizer object
        IrrigationOptimizer optimizer = new IrrigationOptimizer(cropType, 0.7);
        optimizer.simulate(simulationDays, scanner);
        
        scanner.close();
    }
}
