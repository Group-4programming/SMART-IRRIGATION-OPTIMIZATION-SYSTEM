# SmartIrrigation.py

class SmartIrrigation:
    """Smart Irrigation Optimization System - Milestone 1"""
    
    def __init__(self):
        """System initialization"""
        print("=== Smart Irrigation Optimization System ===")
        print("Milestone 1: Basic Program Structure\n")
        
        # System state variables
        self.soil_moisture = 35.0      # Percentage (%)
        self.crop_growth_stage = 30    # Days after planting
        self.rainfall_forecast = 5.0   # mm expected
        self.temperature = 28.0        # Celsius
        
        print("System Initialization Complete:")
        print(f"  Soil Moisture: {self.soil_moisture}%")
        print(f"  Crop Growth Stage: Day {self.crop_growth_stage}")
        print(f"  Rainfall Forecast: {self.rainfall_forecast} mm")
        print(f"  Temperature: {self.temperature}°C\n")
    
    def compute_evapotranspiration(self):
        """Basic arithmetic computation"""
        base_et = 3.0  # mm/day base rate
        temp_factor = 1.0 + (self.temperature - 25.0) * 0.05
        growth_factor = 1.0 + (self.crop_growth_stage / 100.0)
        return base_et * temp_factor * growth_factor
    
    def needs_irrigation(self, moisture, evapotranspiration, rainfall):
        """Simple conditional logic"""
        effective_rain = rainfall * 0.7
        moisture_after_rain = moisture + (effective_rain * 0.5)
        
        if moisture_after_rain < 25.0:
            return True
        elif moisture_after_rain < 40.0 and evapotranspiration > 4.0:
            return True
        else:
            return False
    
    def run_simulation(self):
        """Main simulation loop"""
        print("--- Starting Daily Simulation ---")
        
        # User input for simulation duration
        try:
            simulation_days = int(input("How many days to simulate? "))
        except ValueError:
            print("Invalid input. Using default: 7 days")
            simulation_days = 7
        
        # Loop-based simulation
        for day in range(1, simulation_days + 1):
            print(f"\n{'=' * 10} Day {day} {'=' * 10}")
            
            # Basic arithmetic
            evapotranspiration = self.compute_evapotranspiration()
            print(f"  Evapotranspiration rate: {evapotranspiration:.2f} mm/day")
            
            # Conditional decision
            irrigate = self.needs_irrigation(
                self.soil_moisture, 
                evapotranspiration, 
                self.rainfall_forecast
            )
            
            if irrigate:
                irrigation_amount = 15.0  # mm
                print(f"  Decision: IRRIGATE with {irrigation_amount} mm")
                self.soil_moisture += irrigation_amount * 0.4
            else:
                print("  Decision: NO IRRIGATION needed")
            
            # Apply daily changes (basic arithmetic operations)
            moisture_loss = evapotranspiration * 0.6
            rain_effect = self.rainfall_forecast * 0.5
            
            self.soil_moisture = self.soil_moisture - moisture_loss + rain_effect
            self.crop_growth_stage += 1
            
            # Bounds checking
            if self.soil_moisture < 10.0:
                self.soil_moisture = 10.0
            if self.soil_moisture > 80.0:
                self.soil_moisture = 80.0
            
            print(f"  New Soil Moisture: {self.soil_moisture:.1f}%")
            print(f"  Crop Growth Stage: Day {self.crop_growth_stage}")
            
            # Weekly forecast update (simple control flow)
            if day % 7 == 0:
                print("\n  --- Weekly Update ---")
                try:
                    self.rainfall_forecast = float(input("  New rainfall forecast (mm): "))
                    self.temperature = float(input("  New temperature (°C): "))
                except ValueError:
                    print("  Invalid input. Keeping previous values.")
        
        print("\n=== Simulation Complete ===")

# Main entry point
if __name__ == "__main__":
    system = SmartIrrigation()
    system.run_simulation()
