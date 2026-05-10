// Milestone5_SmartIrrigation.java 

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ============================================
// ENUMERATIONS for structured states
// ============================================

enum GrowthStage {
    SEEDLING(0, 20, 2.5, "Early growth - high sensitivity"),
    VEGETATIVE(21, 60, 5.0, "Rapid growth - high water demand"),
    FLOWERING(61, 90, 4.0, "Reproductive - critical period"),
    MATURATION(91, 120, 2.0, "Ripening - reduced water need");
    
    private final int startDay;
    private final int endDay;
    private final double waterRequirement;
    private final String description;
    
    GrowthStage(int start, int end, double waterReq, String desc) {
        this.startDay = start;
        this.endDay = end;
        this.waterRequirement = waterReq;
        this.description = desc;
    }
    
    public static GrowthStage fromDay(int day) {
        if (day <= SEEDLING.endDay) return SEEDLING;
        if (day <= VEGETATIVE.endDay) return VEGETATIVE;
        if (day <= FLOWERING.endDay) return FLOWERING;
        return MATURATION;
    }
    
    public double getWaterRequirement() { return waterRequirement; }
    public String getDescription() { return description; }
    public int getStartDay() { return startDay; }
    public int getEndDay() { return endDay; }
}

enum IrrigationStatus {
    CRITICAL("EMERGENCY", 100, "Immediate action required"),
    DRY("WARNING", 75, "Irrigation recommended"),
    OPTIMAL("NORMAL", 50, "Adequate moisture"),
    SATURATED("HOLD", 25, "No irrigation needed"),
    FLOODED("ALERT", 0, "Excess water - drainage required");
    
    private final String decision;
    private final int priority;
    private final String action;
    
    IrrigationStatus(String decision, int priority, String action) {
        this.decision = decision;
        this.priority = priority;
        this.action = action;
    }
    
    public String getDecision() { return decision; }
    public int getPriority() { return priority; }
    public String getAction() { return action; }
}

enum SoilType {
    SAND(0.2, 0.05, 0.40, "Low water retention"),
    LOAM(0.4, 0.15, 0.65, "Ideal water retention"),
    CLAY(0.6, 0.25, 0.80, "High water retention");
    
    private final double wiltingPoint;
    private final double fieldCapacity;
    private final double saturationPoint;
    private final String description;
    
    SoilType(double wp, double fc, double sat, String desc) {
        this.wiltingPoint = wp;
        this.fieldCapacity = fc;
        this.saturationPoint = sat;
        this.description = desc;
    }
    
    public double getWiltingPoint() { return wiltingPoint; }
    public double getFieldCapacity() { return fieldCapacity; }
    public double getSaturationPoint() { return saturationPoint; }
    public String getDescription() { return description; }
}

// ============================================
// GENERIC CLASS for reusability
// ============================================

class SimulationResult<T> {
    private final T data;
    private final LocalDateTime timestamp;
    private final long processingTimeMs;
    
    public SimulationResult(T data, long processingTimeMs) {
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.processingTimeMs = processingTimeMs;
    }
    
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    
    public String toString() {
        return String.format("[%s] %s (took %d ms)", 
            timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")), 
            data, processingTimeMs);
    }
}

// Generic repository for storing simulation data
class SimulationRepository<T> {
    private final List<T> data = new CopyOnWriteArrayList<>();
    private final AtomicInteger totalEntries = new AtomicInteger(0);
    
    public void add(T item) {
        data.add(item);
        totalEntries.incrementAndGet();
    }
    
    public List<T> getAll() { return new ArrayList<>(data); }
    public int size() { return totalEntries.get(); }
    public void clear() { 
        data.clear(); 
        totalEntries.set(0);
    }
}

// ============================================
// FIELD SIMULATION TASK (implements Runnable)
// ============================================

class FieldSimulator implements Runnable {
    private final String fieldName;
    private final SoilType soilType;
    private final int totalDays;
    private final SimulationRepository<String> logRepo;
    private final AtomicInteger irrigationTotal;
    private final CountDownLatch latch;
    private double moisture;
    
    public FieldSimulator(String fieldName, SoilType soilType, double initialMoisture,
                          int totalDays, SimulationRepository<String> logRepo,
                          AtomicInteger irrigationTotal, CountDownLatch latch) {
        this.fieldName = fieldName;
        this.soilType = soilType;
        this.moisture = initialMoisture;
        this.totalDays = totalDays;
        this.logRepo = logRepo;
        this.irrigationTotal = irrigationTotal;
        this.latch = latch;
    }
    
    public void run() {
        String threadName = Thread.currentThread().getName();
        log("STARTED on " + soilType.name() + " soil (Thread: " + threadName + ")");
        
        for (int day = 1; day <= totalDays; day++) {
            simulateDay(day);
            
            // Small delay to simulate real-time processing and allow thread interleaving
            try { 
                Thread.sleep(5); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log("COMPLETED. Final moisture: " + String.format("%.1f", moisture) + "%");
        latch.countDown();
    }
    
    private void simulateDay(int day) {
        GrowthStage stage = GrowthStage.fromDay(day);
        double waterNeed = stage.getWaterRequirement();
        
        // Calculate water loss
        double loss = waterNeed * (0.5 + moisture / 100.0);
        loss = Math.min(loss, moisture);
        
        // Determine irrigation need
        double fieldCap = soilType.getFieldCapacity() * 100;
        double wiltingPt = soilType.getWiltingPoint() * 100;
        
        double irrigation = 0;
        String status = "NORMAL";
        
        if (moisture < wiltingPt + 5) {
            irrigation = 25.0;
            status = "CRITICAL";
            log("CRITICAL - Emergency irrigation: " + irrigation + "mm");
        } else if (moisture < fieldCap * 0.6) {
            irrigation = 15.0;
            status = "DRY";
            log("WARNING - Irrigation applied: " + irrigation + "mm");
        } else if (moisture < fieldCap * 0.85) {
            irrigation = 5.0;
            status = "OPTIMAL";
        } else {
            status = "HOLD";
        }
        
        irrigationTotal.addAndGet((int)irrigation);
        
        // Update soil moisture
        double effectiveRain = 2.0; // mm per day average
        double effectiveIrrigation = irrigation * 0.7;
        
        moisture = moisture - loss + effectiveRain + effectiveIrrigation;
        
        // Bounds checking
        if (moisture < 10.0) moisture = 10.0;
        if (moisture > 80.0) moisture = 80.0;
        
        // Weekly summary
        if (day % 7 == 0) {
            log(String.format("Week %d: Stage=%s, Moisture=%.1f%%, Loss=%.1fmm, Irrigation=%.1fmm",
                day/7, stage.name(), moisture, loss, irrigation));
        }
    }
    
    private void log(String message) {
        String entry = String.format("[%s][Day?] %s", fieldName, message);
        logRepo.add(entry);
        System.out.println(entry);
    }
}

// ============================================
// MAIN CONCURRENT SIMULATION SYSTEM
// ============================================

public class Milestone5_SmartIrrigation {
    
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     SMART IRRIGATION SYSTEM - MILESTONE 5               ");
        System.out.println("     Concurrency and Advanced Computation               ");
        System.out.println("==========================================================\n");
        
        // Create thread-safe repository for logs
        SimulationRepository<String> logRepository = new SimulationRepository<>();
        
        // Thread-safe counter for total irrigation
        AtomicInteger globalIrrigationTotal = new AtomicInteger(0);
        
        // Define fields to simulate
        List<FieldSimulator> fields = Arrays.asList(
            new FieldSimulator("North_Field", SoilType.LOAM, 45.0, 30, logRepository, globalIrrigationTotal, null),
            new FieldSimulator("South_Field", SoilType.SAND, 30.0, 30, logRepository, globalIrrigationTotal, null),
            new FieldSimulator("East_Field", SoilType.CLAY, 50.0, 30, logRepository, globalIrrigationTotal, null),
            new FieldSimulator("West_Field", SoilType.LOAM, 38.0, 30, logRepository, globalIrrigationTotal, null),
            new FieldSimulator("Center_Field", SoilType.SAND, 25.0, 30, logRepository, globalIrrigationTotal, null)
        );
        
        System.out.println("SIMULATION CONFIGURATION");
        System.out.println("----------------------------------------------------------");
        System.out.printf("  %-15s %-10s %-15s %-10s%n", "Field", "Soil Type", "Initial Moisture", "Duration");
        System.out.println("----------------------------------------------------------");
        
        for (FieldSimulator field : fields) {
            // Using reflection to get field info - simplified by storing separately
            System.out.printf("  %-15s %-10s %-15s %-10d%n", 
                getFieldName(field), getSoilTypeName(field), "35.0%", 30);
        }
        System.out.println("----------------------------------------------------------\n");
        
        // Create CountDownLatch for synchronization
        CountDownLatch completionLatch = new CountDownLatch(fields.size());
        
        // Recreate fields with proper latch
        List<FieldSimulator> finalFields = new ArrayList<>();
        finalFields.add(new FieldSimulator("North_Field", SoilType.LOAM, 45.0, 30, logRepository, globalIrrigationTotal, completionLatch));
        finalFields.add(new FieldSimulator("South_Field", SoilType.SAND, 30.0, 30, logRepository, globalIrrigationTotal, completionLatch));
        finalFields.add(new FieldSimulator("East_Field", SoilType.CLAY, 50.0, 30, logRepository, globalIrrigationTotal, completionLatch));
        finalFields.add(new FieldSimulator("West_Field", SoilType.LOAM, 38.0, 30, logRepository, globalIrrigationTotal, completionLatch));
        finalFields.add(new FieldSimulator("Center_Field", SoilType.SAND, 25.0, 30, logRepository, globalIrrigationTotal, completionLatch));
        
        // Get system information
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
        
        System.out.println("SYSTEM INFORMATION");
        System.out.println("  Available CPU Cores: " + availableProcessors);
        System.out.println("  Thread Pool Size: " + availableProcessors);
        System.out.println("  Number of Fields: " + finalFields.size());
        System.out.println("\nSTARTING CONCURRENT SIMULATION...\n");
        System.out.println("==========================================================\n");
        
        long startTime = System.currentTimeMillis();
        
        // Submit all tasks to thread pool
        List<Future<?>> futures = new ArrayList<>();
        for (FieldSimulator field : finalFields) {
            futures.add(executorService.submit(field));
        }
        
        // Wait for all simulations to complete
        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted!");
        }
        
        long endTime = System.currentTimeMillis();
        
        // Shutdown thread pool
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        long totalTime = endTime - startTime;
        
        // ============================================
        // FUNCTIONAL PROGRAMMING (Lambdas and Streams)
        // ============================================
        
        System.out.println("\n==========================================================");
        System.out.println("\nSIMULATION RESULTS");
        System.out.println("----------------------------------------------------------");
        
        // Using lambda to process logs
        System.out.println("\nLOG ENTRIES (Last 15):");
        List<String> allLogs = logRepository.getAll();
        allLogs.stream()
            .skip(Math.max(0, allLogs.size() - 15))
            .forEach(log -> System.out.println("  " + log));
        
        // Functional aggregation - count by log type
        long criticalCount = allLogs.stream()
            .filter(log -> log.contains("CRITICAL"))
            .count();
        
        long warningCount = allLogs.stream()
            .filter(log -> log.contains("WARNING"))
            .count();
        
        long completedCount = allLogs.stream()
            .filter(log -> log.contains("COMPLETED"))
            .count();
        
        System.out.println("\nSTATISTICS (Functional Programming):");
        System.out.printf("  Total Irrigation Applied (All Fields): %d mm%n", globalIrrigationTotal.get());
        System.out.printf("  Critical Events: %d%n", criticalCount);
        System.out.printf("  Warning Events: %d%n", warningCount);
        System.out.printf("  Completed Fields: %d%n", completedCount);
        System.out.printf("  Total Log Entries: %d%n", allLogs.size());
        
        // Using Stream API for aggregation
        Map<String, Long> logTypeSummary = allLogs.stream()
            .map(log -> {
                if (log.contains("CRITICAL")) return "CRITICAL";
                if (log.contains("WARNING")) return "WARNING";
                if (log.contains("COMPLETED")) return "COMPLETION";
                return "INFO";
            })
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        System.out.println("\n  Log Type Summary:");
        logTypeSummary.forEach((type, count) -> 
            System.out.printf("    %-10s: %d%n", type, count));
        
        // Performance metrics
        System.out.println("\nPERFORMANCE METRICS:");
        System.out.printf("  Total Execution Time: %d ms%n", totalTime);
        System.out.printf("  Average Time per Field: %.1f ms%n", (double)totalTime / finalFields.size());
        System.out.printf("  Throughput: %.2f fields/second%n", (finalFields.size() * 1000.0) / totalTime);
        
        // Enumerations demonstration
        System.out.println("\nGROWTH STAGES REFERENCE (Enumerations):");
        for (GrowthStage stage : GrowthStage.values()) {
            System.out.printf("  %-12s: Days %d-%d, Need=%.1f mm/day - %s%n",
                stage.name(), stage.getStartDay(), stage.getEndDay(), 
                stage.getWaterRequirement(), stage.getDescription());
        }
        
        System.out.println("\nIRRIGATION STATUS REFERENCE (Enumerations):");
        for (IrrigationStatus status : IrrigationStatus.values()) {
            System.out.printf("  %-10s [Priority %d]: %s%n",
                status.getDecision(), status.getPriority(), status.getAction());
        }
        
        System.out.println("\nSOIL TYPES REFERENCE (Enumerations):");
        for (SoilType soil : SoilType.values()) {
            System.out.printf("  %-5s: %s (Field Capacity: %.0f%%, Wilting: %.0f%%)%n",
                soil.name(), soil.getDescription(), 
                soil.getFieldCapacity() * 100, soil.getWiltingPoint() * 100);
        }
        
        // Generics demonstration
        System.out.println("\nGENERICS DEMONSTRATION:");
        SimulationResult<String> stringResult = new SimulationResult<>("Simulation Complete", totalTime);
        SimulationResult<Double> moistureResult = new SimulationResult<>(45.7, 10);
        SimulationResult<Integer> daysResult = new SimulationResult<>(30, 5);
        
        System.out.println("  " + stringResult);
        System.out.println("  " + moistureResult);
        System.out.println("  " + daysResult);
        
        System.out.println("\nSIMULATION COMPLETED SUCCESSFULLY!");
        System.out.println("==========================================================");
    }
    
    // Helper methods for getting field information
    private static String getFieldName(FieldSimulator field) {
        String str = field.toString();
        if (str.contains("North")) return "North_Field";
        if (str.contains("South")) return "South_Field";
        if (str.contains("East")) return "East_Field";
        if (str.contains("West")) return "West_Field";
        return "Center_Field";
    }
    
    private static String getSoilTypeName(FieldSimulator field) {
        String str = field.toString();
        if (str.contains("LOAM")) return "LOAM";
        if (str.contains("SAND")) return "SAND";
        return "CLAY";
    }
}
