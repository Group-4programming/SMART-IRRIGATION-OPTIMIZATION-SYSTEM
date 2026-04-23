// SmartIrrigationV2.cpp
#include <iostream>
#include <string>
#include <vector>
#include <limits>
using namespace std;

// Class representing soil sensor (encapsulation)
class SoilSensor {
private:
    double moisture;        // Encapsulated state
    double fieldCapacity;
    double wiltingPoint;
    
public:
    SoilSensor(double fc = 45.0, double wp = 15.0) 
        : fieldCapacity(fc), wiltingPoint(wp) {
        moisture = 35.0;  // Initial moisture
    }
    
    // Encapsulated behavior
    double getMoisture() const { return moisture; }
    void setMoisture(double m) { 
        if (m < wiltingPoint) moisture = wiltingPoint;
        else if (m > fieldCapacity) moisture = fieldCapacity;
        else moisture = m;
    }
    
    bool isDry() const { return moisture < 25.0; }
    bool isCriticallyDry() const { return moisture < wiltingPoint + 5.0; }
    double getWaterDeficit() const { return fieldCapacity - moisture; }
};

// Class representing crop (encapsulation)
class Crop {
private:
    string cropType;
    int growthStage;        // days after planting
    double waterRequirement; // mm/day
    
public:
    Crop(string type, int initialStage = 30) 
        : cropType(type), growthStage(initialStage) {
        updateWaterRequirement();
    }
    
    void advanceDay() {
        growthStage++;
        updateWaterRequirement();
    }
    
    int getGrowthStage() const { return growthStage; }
    string getCropType() const { return cropType; }
    double getWaterRequirement() const { return waterRequirement; }
    
private:
    void updateWaterRequirement() {
        // Encapsulated decision logic
        if (growthStage < 20) waterRequirement = 3.0;      // Early growth
        else if (growthStage < 60) waterRequirement = 5.0; // Vegetative
        else if (growthStage < 90) waterRequirement = 4.0; // Flowering
        else waterRequirement = 2.5;                       // Maturation
    }
};

// Class representing weather data (encapsulation)
class Weather {
private:
    double temperature;
    double rainfallForecast;
    double evapotranspiration;
    
public:
    Weather(double temp = 28.0, double rain = 5.0) 
        : temperature(temp), rainfallForecast(rain), evapotranspiration(0.0) {}
    
    void update(double temp, double rain) {
        temperature = temp;
        rainfallForecast = rain;
        computeEvapotranspiration();
    }
    
    double getTemperature() const { return temperature; }
    double getRainfallForecast() const { return rainfallForecast; }
    double getEvapotranspiration() const { return evapotranspiration; }
    
    void computeEvapotranspiration() {
        // Basic arithmetic encapsulated
        double baseET = 3.0;
        double tempFactor = 1.0 + (temperature - 25.0) * 0.05;
        evapotranspiration = baseET * tempFactor;
    }
};

// Main decision system class (encapsulation + decision logic)
class IrrigationOptimizer {
private:
    SoilSensor sensor;
    Crop crop;
    Weather weather;
    double irrigationEfficiency;
    vector<string> decisionLog;
    
public:
    IrrigationOptimizer(string cropType, double efficiency = 0.7) 
        : crop(cropType), weather(), irrigationEfficiency(efficiency) {
        decisionLog.push_back("System Initialized: " + cropType);
    }
    
    // Core decision-making system (conditional logic)
    string makeDecision() {
        double moisture = sensor.getMoisture();
        double cropNeed = crop.getWaterRequirement();
        double et = weather.getEvapotranspiration();
        double rain = weather.getRainfallForecast();
        
        // Multi-level decision logic
        if (sensor.isCriticallyDry()) {
            return "EMERGENCY: Immediate heavy irrigation required!";
        }
        
        if (sensor.isDry()) {
            return "WARNING: Irrigation recommended (soil moisture low)";
        }
        
        // Calculate water balance (basic arithmetic)
        double effectiveRain = rain * 0.7;
        double totalWaterAvailable = moisture + effectiveRain * 0.5;
        double waterDeficit = cropNeed * 1.2 - totalWaterAvailable; // 20% safety margin
        
        // Conditional decision tree
        if (waterDeficit > 10.0) {
            double irrigationAmount = waterDeficit / irrigationEfficiency;
            return "IRRIGATE: " + to_string(int(irrigationAmount)) + " mm needed";
        } 
        else if (waterDeficit > 5.0 && et > 4.0) {
            return "LIGHT IRRIGATION: " + to_string(int(waterDeficit)) + " mm";
        }
        else if (rain > 15.0) {
            return "SKIP IRRIGATION: Significant rain expected";
        }
        else if (moisture > 40.0 && cropNeed < 4.0) {
            return "NO IRRIGATION: Adequate moisture for current stage";
        }
        else {
            return "MONITOR ONLY: No immediate action needed";
        }
    }
    
    // Execute irrigation and update state
    void executeIrrigation(string decision) {
        if (decision.find("IRRIGATE") != string::npos || 
            decision.find("EMERGENCY") != string::npos) {
            
            // Parse irrigation amount from decision string
            double amount = 15.0; // default
            size_t mmPos = decision.find("mm");
            if (mmPos != string::npos) {
                string amountStr = decision.substr(decision.find_last_of(' ') + 1, mmPos - decision.find_last_of(' ') - 1);
                amount = stod(amountStr);
            }
            
            double moistureIncrease = amount * irrigationEfficiency * 0.3;
            sensor.setMoisture(sensor.getMoisture() + moistureIncrease);
            decisionLog.push_back("Irrigation applied: " + to_string(amount) + " mm");
        }
    }
    
    // Simulation cycle (loop-based)
    void simulate(int days) {
        cout << "\n=== Starting " << days << "-Day Simulation ===\n" << endl;
        
        for (int day = 1; day <= days; day++) {
            cout << "========== DAY " << day << " ==========" << endl;
            
            // Display current state
            cout << " Soil Moisture: " << sensor.getMoisture() << "%" << endl;
            cout << " Crop Stage: Day " << crop.getGrowthStage() << endl;
            cout << " Crop Need: " << crop.getWaterRequirement() << " mm/day" << endl;
            cout << " Temperature: " << weather.getTemperature() << "°C" << endl;
            cout << " Rain Forecast: " << weather.getRainfallForecast() << " mm" << endl;
            
            // Make decision (conditional logic)
            string decision = makeDecision();
            cout << " DECISION: " << decision << endl;
            
            // Execute if irrigation needed
            executeIrrigation(decision);
            
            // Apply environmental changes
            double moistureLoss = weather.getEvapotranspiration() * 0.6;
            double rainEffect = weather.getRainfallForecast() * 0.3;
            sensor.setMoisture(sensor.getMoisture() - moistureLoss + rainEffect);
            
            // Advance crop growth
            crop.advanceDay();
            
            // Weekly weather update with user input (input validation)
            if (day % 7 == 0) {
                cout << "\n--- Weekly Forecast Update ---" << endl;
                double newTemp, newRain;
                
                // Input validation loop
                while (true) {
                    cout << "Enter temperature for next week (°C): ";
                    cin >> newTemp;
                    if (cin.fail() || newTemp < -10 || newTemp > 60) {
                        cin.clear();
                        cin.ignore(numeric_limits<streamsize>::max(), '\n');
                        cout << "Invalid! Enter temperature between -10 and 60: ";
                    } else {
                        break;
                    }
                }
                
                while (true) {
                    cout << "Enter rainfall forecast (mm): ";
                    cin >> newRain;
                    if (cin.fail() || newRain < 0 || newRain > 200) {
                        cin.clear();
                        cin.ignore(numeric_limits<streamsize>::max(), '\n');
                        cout << "Invalid! Enter rainfall between 0 and 200: ";
                    } else {
                        break;
                    }
                }
                
                weather.update(newTemp, newRain);
            }
            
            cout << "----------------------------------------\n" << endl;
        }
        
        // Display summary
        cout << "\n=== SIMULATION SUMMARY ===" << endl;
        cout << "Total days simulated: " << days << endl;
        cout << "Final growth stage: Day " << crop.getGrowthStage() << endl;
        cout << "Final soil moisture: " << sensor.getMoisture() << "%" << endl;
    }
};

// Main entry point
int main() {
    cout << "=== SMART IRRIGATION OPTIMIZATION SYSTEM ===" << endl;
    cout << "Milestone 2: Structured Decision Systems\n" << endl;
    
    // User interaction and input validation
    string cropType;
    cout << "Enter crop type (e.g., Maize, Wheat, Tomato): ";
    cin >> cropType;
    
    int simulationDays;
    while (true) {
        cout << "Enter number of days to simulate (1-365): ";
        cin >> simulationDays;
        if (cin.fail() || simulationDays < 1 || simulationDays > 365) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter 1-365." << endl;
        } else {
            break;
        }
    }
    
    // Create optimizer object (class instantiation)
    IrrigationOptimizer optimizer(cropType);
    
    // Run simulation loop
    optimizer.simulate(simulationDays);
    
    return 0;
}
