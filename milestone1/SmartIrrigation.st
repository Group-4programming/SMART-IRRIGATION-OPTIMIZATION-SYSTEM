"SmartIrrigation Class - Milestone 1"
Object subclass: #SmartIrrigation
    instanceVariableNames: 'soilMoisture cropGrowthStage rainfallForecast temperature'
    classVariableNames: ''
    poolDictionaries: ''
    category: 'IrrigationSystem'

"System initialization method"
SmartIrrigation >> initialize
    "Constructor - sets up initial system state"
    Transcript show: '=== Smart Irrigation Optimization System ==='; cr.
    Transcript show: 'Milestone 1: Basic Program Structure'; cr; cr.
    
    "Initialize state variables"
    soilMoisture := 35.0.        "Percentage"
    cropGrowthStage := 30.       "Days after planting"
    rainfallForecast := 5.0.     "mm"
    temperature := 28.0.         "Celsius"
    
    Transcript show: 'System Initialization Complete:'; cr.
    Transcript show: '  Soil Moisture: ', soilMoisture printString, '%'; cr.
    Transcript show: '  Crop Growth Stage: Day ', cropGrowthStage printString; cr.
    Transcript show: '  Rainfall Forecast: ', rainfallForecast printString, ' mm'; cr.
    Transcript show: '  Temperature: ', temperature printString, '°C'; cr; cr.
    ^self

"Basic arithmetic computation - evapotranspiration"
SmartIrrigation >> computeEvapotranspiration
    | baseET tempFactor growthFactor |
    baseET := 3.0.
    tempFactor := 1.0 + ((temperature - 25.0) * 0.05).
    growthFactor := 1.0 + (cropGrowthStage / 100.0).
    ^baseET * tempFactor * growthFactor

"Conditional logic - determine if irrigation is needed"
SmartIrrigation >> needsIrrigation: moisture evapotranspiration: et rainfall: rain
    | effectiveRain moistureAfterRain |
    effectiveRain := rain * 0.7.
    moistureAfterRain := moisture + (effectiveRain * 0.5).
    
    (moistureAfterRain < 25.0) ifTrue: [^true].
    ((moistureAfterRain < 40.0) and: [et > 4.0]) ifTrue: [^true].
    ^false

"Simple control flow - daily update"
SmartIrrigation >> applyDailyChanges
    | evapotranspiration irrigate irrigationAmount moistureLoss rainEffect |
    
    "Compute evapotranspiration"
    evapotranspiration := self computeEvapotranspiration.
    Transcript show: '  Evapotranspiration rate: ', evapotranspiration printString, ' mm/day'; cr.
    
    "Decision logic (conditional)"
    irrigate := self needsIrrigation: soilMoisture 
                       evapotranspiration: evapotranspiration 
                       rainfall: rainfallForecast.
    
    irrigate ifTrue: [
        irrigationAmount := 15.0.
        Transcript show: '  Decision: IRRIGATE with ', irrigationAmount printString, ' mm'; cr.
        soilMoisture := soilMoisture + (irrigationAmount * 0.4)
    ] ifFalse: [
        Transcript show: '  Decision: NO IRRIGATION needed'; cr.
    ].
    
    "Apply environmental changes (basic arithmetic)"
    moistureLoss := evapotranspiration * 0.6.
    rainEffect := rainfallForecast * 0.5.
    
    soilMoisture := soilMoisture - moistureLoss + rainEffect.
    cropGrowthStage := cropGrowthStage + 1.
    
    "Bounds checking"
    soilMoisture < 10.0 ifTrue: [soilMoisture := 10.0].
    soilMoisture > 80.0 ifTrue: [soilMoisture := 80.0].
    
    Transcript show: '  New Soil Moisture: ', soilMoisture printString, '%'; cr.
    Transcript show: '  Crop Growth Stage: Day ', cropGrowthStage printString; cr.
    ^self

"Main simulation loop"
SmartIrrigation >> runSimulation: numDays
    | day |
    Transcript show: '--- Starting Daily Simulation ---'; cr.
    
    "Loop-based simulation"
    day := 1.
    [day <= numDays] whileTrue: [
        Transcript show: ' '; cr.
        Transcript show: '========== Day ', day printString, ' =========='; cr.
        
        "Apply daily changes"
        self applyDailyChanges.
        
        "Weekly forecast update (conditional)"
        (day \\ 7 = 0) ifTrue: [
            Transcript show: '  --- Weekly Update ---'; cr.
            "In Smalltalk, user input would typically be via dialog or workspace"
            "For demonstration, we'll simulate an update"
            rainfallForecast := 8.0.
            temperature := 26.0.
            Transcript show: '  Updated forecast: Rain=', rainfallForecast printString, 'mm, Temp=', temperature printString, '°C'; cr.
        ].
        
        day := day + 1.
    ].
    
    Transcript show: ' '; cr.
    Transcript show: '=== Simulation Complete ==='; cr.
    ^self

"Main entry point - class-side method"
SmartIrrigation class >> main
    "Program execution starts here"
    | system |
    system := SmartIrrigation new initialize.
    "Simulate 7 days (loop demonstrated in runSimulation:)"
    system runSimulation: 7.
    ^system

"To run in Pharo/Squeak Workspace:
SmartIrrigation main.
"
