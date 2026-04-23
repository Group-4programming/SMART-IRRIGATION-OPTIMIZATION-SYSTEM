// SmartIrrigation.cpp
#include <iostream>
#include <string>
using namespace std;

// System state variables (global for simplicity in Milestone 1)
double soilMoisture = 35.0;      // Percentage (%)
int cropGrowthStage = 30;        // Days after planting
double rainfallForecast = 5.0;   // mm expected in next 24 hours
double temperature = 28.0;       // Celsius

// Function to compute evapotranspiration rate (basic arithmetic)
double computeEvapotranspiration(double temp, int growthStage) {
    double baseET = 3.0;  // mm/day base rate
    double tempFactor = 1.0 + (temp - 25.0) * 0.05;
    double growthFactor = 1.0 + (growthStage / 100.0);
    return baseET * tempFactor * growthFactor;
}

// Function to check if irrigation is needed (conditional logic)
bool needsIrrigation(double moisture, double et, double rain) {
    double effectiveRain = rain * 0.7;  // 70% of rain is usable
    double moistureAfterRain = moisture + (effectiveRain * 0.5);
    
    if (moistureAfterRain < 25.0) {
        return true;
    } else if (moistureAfterRain < 40.0 && et > 4.0) {
        return true;
    } else {
        return false;
    }
}

int main() {
    cout << "=== Smart Irrigation Optimization System ===" << endl;
    cout << "Milestone 1: Basic Program Structure" << endl << endl;
    
    // System initialization
    cout << "System Initialization..." << endl;
    cout << "Initial Soil Moisture: " << soilMoisture << "%" << endl;
    cout << "Crop Growth Stage: Day " << cropGrowthStage << endl;
    cout << "Rainfall Forecast: " << rainfallForecast << " mm" << endl;
    cout << "Temperature: " << temperature << "°C" << endl << endl;
    
    // Loop-based simulation of 7 days
    cout << "--- 7-Day Simulation ---" << endl;
    
    for (int day = 1; day <= 7; day++) {
        cout << "\nDay " << day << ":" << endl;
        
        // Arithmetic computation for evapotranspiration
        double evapotranspiration = computeEvapotranspiration(temperature, cropGrowthStage);
        cout << "  Evapotranspiration rate: " << evapotranspiration << " mm/day" << endl;
        
        // Conditional decision for irrigation
        bool irrigate = needsIrrigation(soilMoisture, evapotranspiration, rainfallForecast);
        
        if (irrigate) {
            double irrigationAmount = 15.0;  // mm
            cout << "  Decision: IRRIGATE with " << irrigationAmount << " mm" << endl;
            // Update soil moisture (basic arithmetic)
            soilMoisture = soilMoisture + (irrigationAmount * 0.4);
        } else {
            cout << "  Decision: NO IRRIGATION needed" << endl;
        }
        
        // Apply daily changes (evaporation, crop uptake)
        double moistureLoss = evapotranspiration * 0.6;
        soilMoisture = soilMoisture - moistureLoss;
        
        // Add rainfall effect
        double rainEffect = rainfallForecast * 0.5;
        soilMoisture = soilMoisture + rainEffect;
        
        // Crop growth advances
        cropGrowthStage = cropGrowthStage + 1;
        
        // Ensure soil moisture stays within bounds
        if (soilMoisture < 10.0) soilMoisture = 10.0;
        if (soilMoisture > 80.0) soilMoisture = 80.0;
        
        cout << "  New Soil Moisture: " << soilMoisture << "%" << endl;
        cout << "  Crop Growth Stage: Day " << cropGrowthStage << endl;
    }
    
    cout << "\n=== Simulation Complete ===" << endl;
    return 0;
}
