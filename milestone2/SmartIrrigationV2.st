"========================================="
"SmartIrrigationV2 Class - Milestone 2"
"Object-Oriented with Encapsulation"
"========================================="

"--------- SoilSensor Class ---------"
Object subclass: #SoilSensor
    instanceVariableNames: 'moisture fieldCapacity wiltingPoint'
    classVariableNames: ''
    poolDictionaries: ''
    category: 'IrrigationSystem'

SoilSensor >> initializeWithFieldCapacity: fc wiltingPoint: wp
    fieldCapacity := fc.
    wiltingPoint := wp.
    moisture := 35.0.
    ^self

SoilSensor >> moisture
    ^moisture

SoilSensor >> setMoisture: newMoisture
    newMoisture < wiltingPoint 
        ifTrue: [moisture := wiltingPoint]
        ifFalse: [
            newMoisture > fieldCapacity 
                ifTrue: [moisture := fieldCapacity]
                ifFalse: [moisture := newMoisture]
        ].
    ^self

SoilSensor >> isDry
    ^moisture < 25.0

SoilSensor >> isCriticallyDry
    ^moisture < (wiltingPoint + 5.0)

SoilSensor >> waterDeficit
    ^fieldCapacity - moisture


"--------- Crop Class ---------"
Object subclass: #Crop
    instanceVariableNames: 'cropType growthStage waterRequirement'
    classVariableNames: ''
    poolDictionaries: ''
    category: 'IrrigationSystem'

Crop >> initializeWithType: aType initialStage: stage
    cropType := aType.
    growthStage := stage.
    self updateWaterRequirement.
    ^self

Crop >> advanceDay
    growthStage := growthStage + 1.
    self updateWaterRequirement.
    ^self

Crop >> growthStage
    ^growthStage

Crop >> cropType
    ^cropType

Crop >> waterRequirement
    ^waterRequirement

Crop >> updateWaterRequirement
    "Encapsulated decision logic"
    growthStage < 20 
        ifTrue: [waterRequirement := 3.0]
        ifFalse: [
            growthStage < 60 
                ifTrue: [waterRequirement := 5.0]
                ifFalse: [
                    growthStage < 90 
                        ifTrue: [waterRequirement := 4.0]
                        ifFalse: [waterRequirement := 2.5]
                ]
        ].
    ^self


"--------- Weather Class ---------"
Object subclass: #Weather
    instanceVariableNames: 'temperature rainfallForecast evapotranspiration'
    classVariableNames: ''
    poolDictionaries: ''
    category: 'IrrigationSystem'

Weather >> initializeWithTemp: temp rain: rain
    temperature := temp.
    rainfallForecast := rain.
    self computeEvapotranspiration.
    ^self

Weather >> updateTemp: temp rain: rain
    temperature := temp.
    rainfallForecast := rain.
    self computeEvapotranspiration.
    ^self

Weather >> temperature
    ^temperature

Weather >> rainfallForecast
    ^rainfallForecast

Weather >> evapotranspiration
    ^evapotranspiration

Weather >> computeEvapotranspiration
    | baseET tempFactor |
    baseET := 3.0.
    tempFactor := 1.0 + ((temperature - 25.0) * 0.05).
    evapotranspiration := baseET * tempFactor.
    ^self


"--------- IrrigationOptimizer Class (Main Decision System) ---------"
Object subclass: #IrrigationOptimizer
    instanceVariableNames: 'sensor crop weather irrigationEfficiency decisionLog'
    classVariableNames: ''
    poolDictionaries: ''
    category: 'IrrigationSystem'

"Initialization"
IrrigationOptimizer >> initializeWithCropType: cropType efficiency: efficiency
    sensor := SoilSensor new initializeWithFieldCapacity: 45.0 wiltingPoint: 15.0.
    crop := Crop new initializeWithType: cropType initialStage: 30.
    weather := Weather new initializeWithTemp: 28.0 rain: 5.0.
    irrigationEfficiency := efficiency.
    decisionLog := OrderedCollection new.
    decisionLog add: ('System Initialized: ', cropType).
    ^self

"Core Decision-Making System (Conditional Logic)"
IrrigationOptimizer >> makeDecision
    | moisture cropNeed et rain effectiveRain totalWaterAvailable waterDeficit irrigationAmount |
    
    moisture := sensor moisture.
    cropNeed := crop waterRequirement.
    et := weather evapotranspiration.
    rain := weather rainfallForecast.
    
    "Multi-level conditional decision tree"
    (sensor isCriticallyDry) ifTrue: [
        ^'EMERGENCY: Immediate heavy irrigation required!'
    ].
    
    (sensor isDry) ifTrue: [
        ^'WARNING: Irrigation recommended (soil moisture low)'
    ].
    
    "Calculate water balance"
    effectiveRain := rain * 0.7.
    totalWaterAvailable := moisture + (effectiveRain * 0.5).
    waterDeficit := (cropNeed * 1.2) - totalWaterAvailable.
    
    "Conditional irrigation logic"
    waterDeficit > 10.0 ifTrue: [
        irrigationAmount := waterDeficit / irrigationEfficiency.
        ^'IRRIGATE: ', irrigationAmount rounded printString, ' mm needed'
    ].
    
    (waterDeficit > 5.0 and: [et > 4.0]) ifTrue: [
        ^'LIGHT IRRIGATION: ', waterDeficit rounded printString, ' mm'
    ].
    
    rain > 15.0 ifTrue: [
        ^'SKIP IRRIGATION: Significant rain expected'
    ].
    
    (moisture > 40.0 and: [cropNeed < 4.0]) ifTrue: [
        ^'NO IRRIGATION: Adequate moisture for current stage'
    ].
    
    ^'MONITOR ONLY: No immediate action needed'

"Irrigation execution"
IrrigationOptimizer >> executeIrrigation: decision
    | amount moistureIncrease |
    
    ((decision includesSubString: 'IRRIGATE') or: [decision includesSubString: 'EMERGENCY']) 
        ifTrue: [
            amount := 15.0.  "default"
            "Parse amount from decision string"
            (decision findString: 'mm') > 0 ifTrue: [
                | tokens |
                tokens := decision substrings.
                tokens do: [:token |
                    (token isAllDigits) ifTrue: [
                        amount := token asNumber.
                    ]
                ]
            ].
            moistureIncrease := amount * irrigationEfficiency * 0.3.
            sensor setMoisture: (sensor moisture + moistureIncrease).
            decisionLog add: ('Irrigation applied: ', amount printString, ' mm')
        ].
    ^self

"Apply daily environmental changes"
IrrigationOptimizer >> applyDailyChanges
    | moistureLoss rainEffect |
    moistureLoss := weather evapotranspiration * 0.6.
    rainEffect := weather rainfallForecast * 0.3.
    sensor setMoisture: (sensor moisture - moistureLoss + rainEffect).
    crop advanceDay.
    ^self

"User input with validation"
IrrigationOptimizer >> getValidatedNumber: prompt min: minVal max: maxVal
    | input isValid |
    isValid := false.
    [isValid] whileFalse: [
        Transcript show: prompt.
        input := FileStream stdin nextLine.
        input := input asNumber.
        (input isNumber and: [input between: minVal and: maxVal]) 
            ifTrue: [isValid := true]
            ifFalse: [
                Transcript show: 'Invalid! Enter between ', minVal printString, ' and ', maxVal printString; cr.
            ]
    ].
    ^input

"Main simulation loop"
IrrigationOptimizer >> simulate: numDays
    | day decision |
    
    Transcript show: '=== Starting ', numDays printString, '-Day Simulation ==='; cr; cr.
    
    day := 1.
    [day <= numDays] whileTrue: [
        Transcript show: '========== DAY ', day printString, ' =========='; cr.
        Transcript show: ' Soil Moisture: ', (sensor moisture) printString, '%'; cr.
        Transcript show: ' Crop Stage: Day ', (crop growthStage) printString; cr.
        Transcript show: ' Crop Need: ', (crop waterRequirement) printString, ' mm/day'; cr.
        Transcript show: ' Temperature: ', (weather temperature) printString, '°C'; cr.
        Transcript show: ' Rain Forecast: ', (weather rainfallForecast) printString, ' mm'; cr.
        
        "Make and execute decision"
        decision := self makeDecision.
        Transcript show: ' DECISION: ', decision; cr.
        self executeIrrigation: decision.
        
        "Apply changes"
        self applyDailyChanges.
        
        "Weekly weather update with user interaction"
        (day \\ 7 = 0) ifTrue: [
            Transcript show: ' '; cr.
            Transcript show: '--- Weekly Forecast Update ---'; cr.
            | newTemp newRain |
            newTemp := self getValidatedNumber: 'Enter temperature for next week (°C): ' min: -10 max: 60.
            newRain := self getValidatedNumber: 'Enter rainfall forecast (mm): ' min: 0 max: 200.
            weather updateTemp: newTemp rain: newRain.
        ].
        
        Transcript show: '----------------------------------------'; cr; cr.
        day := day + 1.
    ].
    
    "Summary"
    Transcript show: ' '; cr.
    Transcript show: '=== SIMULATION SUMMARY ==='; cr.
    Transcript show: 'Total days simulated: ', numDays printString; cr.
    Transcript show: 'Final growth stage: Day ', (crop growthStage) printString; cr.
    Transcript show: 'Final soil moisture: ', (sensor moisture) printString, '%'; cr.
    ^self

"Class-side main entry point"
IrrigationOptimizer class >> main
    | optimizer cropType simulationDays |
    
    Transcript show: '=== SMART IRRIGATION OPTIMIZATION SYSTEM ==='; cr.
    Transcript show: 'Milestone 2: Structured Decision Systems'; cr; cr.
    
    "User interaction"
    Transcript show: 'Enter crop type (e.g., Maize, Wheat, Tomato): '.
    cropType := FileStream stdin nextLine.
    
    simulationDays := 0.
    [simulationDays between: 1 and: 365] whileFalse: [
        Transcript show: 'Enter number of days to simulate (1-365): '.
        simulationDays := FileStream stdin nextLine asNumber.
        (simulationDays between: 1 and: 365) ifFalse: [
            Transcript show: 'Invalid input. Please enter 1-365.'; cr.
        ]
    ].
    
    "Create optimizer and run simulation"
    optimizer := self new initializeWithCropType: cropType efficiency: 0.7.
    optimizer simulate: simulationDays.
    ^optimizer

"To run in Pharo/Squeak Workspace:
IrrigationOptimizer main.
"
