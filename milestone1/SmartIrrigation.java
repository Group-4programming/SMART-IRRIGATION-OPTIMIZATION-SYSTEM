// SmartIrrigation.java
import java.util.Scanner;

public class SmartIrrigation {
    // System state variables
    private double soilMoisture;
    private int cropGrowthStage;
    private double rainfallForecast;
    private double temperature;
    
    // Constructor for system initialization
    public SmartIrrigation() {
        System.out.println("=== Smart Irrigation Optimization System ===");
        System.out.println("Milestone 1: Basic Program Structure\n");
        
        // Initialize state variables
        this.soilMoisture = 35.0;      // Percentage
        this.cropGrowthStage = 30;     // Days after planting
        this.rainfallForecast = 5.0;   // mm
        this.temperature = 28.0;       // Celsius
        
        System.out.println("System Initialization Complete:");
        System.out.println("  Soil Moisture: " + soilMoisture + "%");
        System.out.println("  Crop Growth Stage: Day " + cropGrowthStage);
        System.out.println("  Rainfall Forecast: " + rainfallForecast + " mm");
        System.out.println("  Temperature: " + temperature + "°C\n");
    }
    
    // Basic arithmetic computation: evapotranspiration
    public double computeEvapotranspiration() {
        double baseET = 3.0;
        double tempFactor = 1.0 + (temperature - 25.0) * 0.05;
        double growthFactor = 1.0 + (cropGrowthStage / 100.0);
        return baseET * tempFactor * growthFactor;
    }
    
    // Conditional logic: determine irrigation need
    public boolean needsIrrigation(double moisture, double et, double rain) {
        double effectiveRain = rain * 0.7;
        double moistureAfterRain = moisture + (effectiveRain * 0.5);
        
        if (moistureAfterRain < 25.0) {
            return true;
        } else if (moistureAfterRain < 40.0 && et > 4.0) {
            return true;
        } else {
            return false;
        }
    }
    
    // Simple control flow: update system with user interaction
    public void runSimulation() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("--- Starting Daily Simulation ---");
        System.out.print("How many days to simulate? ");
        int simulationDays = scanner.nextInt();
        
        // Loop-based simulation
        for (int day = 1; day <= simulationDays; day++) {
            System.out.println("\n========== Day " + day + " ==========");
            
            // Compute current evapotranspiration
            double evapotranspiration = computeEvapotranspiration();
            System.out.printf("  Evapotranspiration: %.2f mm/day%n", evapotranspiration);
            
            // Conditional decision
            boolean irrigate = needsIrrigation(soilMoisture, evapotranspiration, rainfallForecast);
            
            if (irrigate) {
                double irrigationAmount = 15.0;
                System.out.println("  Decision: IRRIGATE (" + irrigationAmount + " mm)");
                soilMoisture += irrigationAmount * 0.4;
            } else {
                System.out.println("  Decision: NO IRRIGATION");
            }
            
            // Apply environmental changes (basic arithmetic)
            double moistureLoss = evapotranspiration * 0.6;
            double rainEffect = rainfallForecast * 0.5;
            
            soilMoisture = soilMoisture - moistureLoss + rainEffect;
            cropGrowthStage++;
            
            // Bounds checking (conditional logic)
            if (soilMoisture < 10.0) soilMoisture = 10.0;
            if (soilMoisture > 80.0) soilMoisture = 80.0;
            
            System.out.printf("  Updated Soil Moisture: %.1f%%%n", soilMoisture);
            System.out.println("  Crop Growth Stage: Day " + cropGrowthStage);
            
            // Ask user for new forecast each week (interaction)
            if (day % 7 == 0) {
                System.out.print("  Enter new rainfall forecast (mm): ");
                rainfallForecast = scanner.nextDouble();
                System.out.print("  Enter current temperature (°C): ");
                temperature = scanner.nextDouble();
            }
        }
        
        System.out.println("\n=== Simulation Complete ===");
        scanner.close();
    }
    
    // Main entry point
    public static void main(String[] args) {
        SmartIrrigation system = new SmartIrrigation();
        system.runSimulation();
    }
}
