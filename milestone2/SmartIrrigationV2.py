# SmartIrrigationV2.py
import re

class SoilSensor:
    """Encapsulated soil moisture sensor"""
    
    def __init__(self, field_capacity=45.0, wilting_point=15.0):
        self._field_capacity = field_capacity
        self._wilting_point = wilting_point
        self._moisture = 35.0  # percentage
    
    @property
    def moisture(self):
        return self._moisture
    
    @moisture.setter
    def moisture(self, value):
        if value < self._wilting_point:
            self._moisture = self._wilting_point
        elif value > self._field_capacity:
            self._moisture = self._field_capacity
        else:
            self._moisture = value
    
    def is_dry(self):
        return self._moisture < 25.0
    
    def is_critically_dry(self):
        return self._moisture < self._wilting_point + 5.0
    
    def water_deficit(self):
        return self._field_capacity - self._moisture


class Crop:
    """Encapsulated crop growth model"""
    
    def __init__(self, crop_type, initial_stage=30):
        self._crop_type = crop_type
        self._growth_stage = initial_stage
        self._water_requirement = 0.0
        self._update_water_requirement()
    
    def advance_day(self):
        self._growth_stage += 1
        self._update_water_requirement()
    
    @property
    def growth_stage(self):
        return self._growth_stage
    
    @property
    def crop_type(self):
        return self._crop_type
    
    @property
    def water_requirement(self):
        return self._water_requirement
    
    def _update_water_requirement(self):
        """Encapsulated decision logic for water needs by growth stage"""
        if self._growth_stage < 20:
            self._water_requirement = 3.0
        elif self._growth_stage < 60:
            self._water_requirement = 5.0
        elif self._growth_stage < 90:
            self._water_requirement = 4.0
        else:
            self._water_requirement = 2.5


class Weather:
    """Encapsulated weather data"""
    
    def __init__(self, temperature=28.0, rainfall_forecast=5.0):
        self._temperature = temperature
        self._rainfall_forecast = rainfall_forecast
        self._evapotranspiration = 0.0
        self._compute_evapotranspiration()
    
    def update(self, temperature, rainfall_forecast):
        self._temperature = temperature
        self._rainfall_forecast = rainfall_forecast
        self._compute_evapotranspiration()
    
    @property
    def temperature(self):
        return self._temperature
    
    @property
    def rainfall_forecast(self):
        return self._rainfall_forecast
    
    @property
    def evapotranspiration(self):
        return self._evapotranspiration
    
    def _compute_evapotranspiration(self):
        base_et = 3.0
        temp_factor = 1.0 + (self._temperature - 25.0) * 0.05
        self._evapotranspiration = base_et * temp_factor


class IrrigationOptimizer:
    """Main decision system with encapsulated state and behavior"""
    
    def __init__(self, crop_type, irrigation_efficiency=0.7):
        self._sensor = SoilSensor()
        self._crop = Crop(crop_type)
        self._weather = Weather()
        self._irrigation_efficiency = irrigation_efficiency
        self._decision_log = []
        self._decision_log.append(f"System Initialized: {crop_type}")
    
    def make_decision(self):
        """Core decision-making system with conditional logic"""
        moisture = self._sensor.moisture
        crop_need = self._crop.water_requirement
        et = self._weather.evapotranspiration
        rain = self._weather.rainfall_forecast
        
        # Multi-level conditional decision tree
        if self._sensor.is_critically_dry():
            return "EMERGENCY: Immediate heavy irrigation required!"
        
        if self._sensor.is_dry():
            return "WARNING: Irrigation recommended (soil moisture low)"
        
        # Calculate water balance
        effective_rain = rain * 0.7
        total_water_available = moisture + effective_rain * 0.5
        water_deficit = crop_need * 1.2 - total_water_available
        
        # Conditional logic for irrigation decision
        if water_deficit > 10.0:
            irrigation_amount = water_deficit / self._irrigation_efficiency
            return f"IRRIGATE: {irrigation_amount:.0f} mm needed"
        elif water_deficit > 5.0 and et > 4.0:
            return f"LIGHT IRRIGATION: {water_deficit:.0f} mm"
        elif rain > 15.0:
            return "SKIP IRRIGATION: Significant rain expected"
        elif moisture > 40.0 and crop_need < 4.0:
            return "NO IRRIGATION: Adequate moisture for current stage"
        else:
            return "MONITOR ONLY: No immediate action needed"
    
    def execute_irrigation(self, decision):
        """Execute irrigation if decision requires it"""
        if "IRRIGATE" in decision or "EMERGENCY" in decision:
            # Extract amount from decision string
            amount = 15.0  # default
            match = re.search(r'(\d+)\s*mm', decision)
            if match:
                amount = float(match.group(1))
            
            moisture_increase = amount * self._irrigation_efficiency * 0.3
            self._sensor.moisture = self._sensor.moisture + moisture_increase
            self._decision_log.append(f"Irrigation applied: {amount} mm")
    
    def simulate(self, days):
        """Loop-based simulation cycle"""
        print(f"\n=== Starting {days}-Day Simulation ===\n")
        
        for day in range(1, days + 1):
            print(f"========== DAY {day} ==========")
            print(f" Soil Moisture: {self._sensor.moisture:.1f}%")
            print(f" Crop Stage: Day {self._crop.growth_stage}")
            print(f" Crop Need: {self._crop.water_requirement:.1f} mm/day")
            print(f" Temperature: {self._weather.temperature:.1f}°C")
            print(f" Rain Forecast: {self._weather.rainfall_forecast:.1f} mm")
            
            # Make and execute decision
            decision = self.make_decision()
            print(f" DECISION: {decision}")
            self.execute_irrigation(decision)
            
            # Apply environmental changes (basic arithmetic)
            moisture_loss = self._weather.evapotranspiration * 0.6
            rain_effect = self._weather.rainfall_forecast * 0.3
            self._sensor.moisture = self._sensor.moisture - moisture_loss + rain_effect
            
            # Advance crop growth
            self._crop.advance_day()
            
            # Weekly weather update with input validation
            if day % 7 == 0:
                print("\n--- Weekly Forecast Update ---")
                new_temp = self._get_validated_input("Enter temperature for next week (°C): ", -10, 60, float)
                new_rain = self._get_validated_input("Enter rainfall forecast (mm): ", 0, 200, float)
                self._weather.update(new_temp, new_rain)
            
            print("----------------------------------------\n")
        
        # Display summary
        print("\n=== SIMULATION SUMMARY ===")
        print(f"Total days simulated: {days}")
        print(f"Final growth stage: Day {self._crop.growth_stage}")
        print(f"Final soil moisture: {self._sensor.moisture:.1f}%")
    
    def _get_validated_input(self, prompt, min_val, max_val, data_type=float):
        """Input validation helper"""
        while True:
            try:
                value = data_type(input(prompt))
                if min_val <= value <= max_val:
                    return value
                else:
                    print(f"Value must be between {min_val} and {max_val}")
            except ValueError:
                print(f"Invalid input. Please enter a number.")


# Main execution
if __name__ == "__main__":
    print("=== SMART IRRIGATION OPTIMIZATION SYSTEM ===")
    print("Milestone 2: Structured Decision Systems\n")
    
    # User interaction
    crop_type = input("Enter crop type (e.g., Maize, Wheat, Tomato): ")
    
    # Input validation for days
    while True:
        try:
            simulation_days = int(input("Enter number of days to simulate (1-365): "))
            if 1 <= simulation_days <= 365:
                break
            else:
                print("Please enter a value between 1 and 365")
        except ValueError:
            print("Invalid input. Please enter a number.")
    
    # Create optimizer object and run simulation
    optimizer = IrrigationOptimizer(crop_type)
    optimizer.simulate(simulation_days)
