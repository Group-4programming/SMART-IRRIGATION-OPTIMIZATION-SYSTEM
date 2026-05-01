#include <iostream>
#include <string>
using namespace std;

class SmartIrrigation {
private:
    string cropType;
    double moisture;
    double temperature;

public:
    // Setter method
    void setData(string crop, double m, double t) {
        cropType = crop;
        moisture = m;
        temperature = t;
    }

    // Display system data
    void displayData() {
        cout << "\nCrop Type: " << cropType << endl;
        cout << "Soil Moisture: " << moisture << endl;
        cout << "Temperature: " << temperature << endl;
    }

    // Decision logic
    void makeDecision() {
        if(moisture < 30) {
            cout << "Decision: Irrigation ON\n";
        } 
        else if(moisture >= 30 && moisture <= 70) {
            cout << "Decision: Moderate Irrigation\n";
        } 
        else {
            cout << "Decision: Irrigation OFF\n";
        }
    }
};

int main() {
    SmartIrrigation system;
    string crop;
    double moisture, temperature;

    cout << "=== SMART IRRIGATION SYSTEM: MILESTONE 4 ===\n";

    cout << "Enter crop type: ";
    cin >> crop;

    cout << "Enter soil moisture: ";
    cin >> moisture;

    cout << "Enter temperature: ";
    cin >> temperature;

    // Set data into object
    system.setData(crop, moisture, temperature);

    // Display + decision
    system.displayData();
    system.makeDecision();

    return 0;
}