# IDE Memory Configuration Guide

## Problem
Your IDE (VS Code/Eclipse) is using small JVM heap sizes, causing OutOfMemoryError during Excel comparison.

## Root Cause
From the process list, your IDEs are using:
- **VS Code**: `-Xmx1024m` (1GB max heap) 
- **Eclipse**: `-Xmx2G` (2GB max heap)

This is insufficient for large Excel file comparisons.

## Solutions

### Option 1: Configure IDE JVM Settings (Recommended)

#### For VS Code
1. **Open Settings**: `Cmd+,` → Preferences → Settings
2. **Search**: "java.home" or "jdk"
3. **Set JVM Options**: Add to `vscode-java.server.vmOptions`
4. **Restart VS Code**

**VS Code Settings JSON**:
```json
{
    "java.server.vmOptions": [
        "-Xms512m",
        "-Xmx4g", 
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=200"
    ]
}
```

#### For Eclipse
1. **Open Preferences**: Eclipse → Preferences
2. **Navigate**: Java → Installed JREs
3. **Edit JVM Arguments**: Add to default VM arguments
4. **Restart Eclipse**

**Eclipse JVM Arguments**:
```
-Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### For IntelliJ IDEA
1. **Open Settings**: File → Settings
2. **Navigate**: Build, Execution, Deployment → Compiler
3. **Set VM Options**: Add to "Additional command line parameters"
4. **Restart IntelliJ**

**IntelliJ VM Options**:
```
-Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Option 2: Use External Terminal (Quick Fix)

Instead of running through IDE, use terminal:

```bash
# Navigate to project
cd /Users/anoopthilakss/Desktop/My\ Work/Final/excel-comparison-tool

# Run with optimized memory
./start-dynamic-memory.sh
```

### Option 3: Create IDE Launch Configuration

#### VS Code Launch Configuration
Create `.vscode/launch.json`:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Excel Comparison Tool (Optimized)",
            "request": "launch",
            "mainClass": "com.example.excelcomparison.ExcelComparisonApplication",
            "args": [],
            "vmArgs": [
                "-Xms512m",
                "-Xmx4g",
                "-XX:+UseG1GC",
                "-XX:MaxGCPauseMillis=200"
            ]
        }
    ]
}
```

### Option 4: Maven Spring Boot Run (Development)

```bash
# Run with Maven and custom JVM args
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## Application Improvements Made

### 1. Enhanced Memory Monitoring
The application now:
- **Checks available memory** before comparison
- **Estimates required memory** based on data size
- **Fails fast** with clear error messages
- **Provides specific solutions** for IDE users

### 2. Increased Upload Limits
- **File upload**: 10MB → 50MB
- **HTTP post size**: 50MB
- **Response size**: 50MB

### 3. Better Error Messages
Instead of generic OutOfMemoryError, you'll now see:
```
CRITICAL: Insufficient memory for comparison!
Available: 512MB
Required: 2048MB
SOLUTIONS:
1. Close other applications
2. Use start-dynamic-memory.sh script
3. Increase IDE JVM heap (-Xmx)
```

## Testing the Fix

### Step 1: Configure IDE
Follow Option 1 instructions for your IDE.

### Step 2: Test Memory
Run the application and check console output:
```
=== Memory Status ===
Max JVM Memory: 4096MB
Currently Used: 256MB (6.3%)
```

### Step 3: Test Comparison
Upload and compare Excel files. You should see:
- Memory usage monitoring
- Batch processing progress
- No OutOfMemoryError

## Minimum Requirements by IDE Configuration

| IDE Setting | Min RAM | Recommended JVM |
|-------------|-----------|----------------|
| VS Code (1GB) | 4GB       | -Xms512m -Xmx2g |
| Eclipse (2GB) | 6GB       | -Xms512m -Xmx4g |
| IntelliJ (2GB) | 6GB       | -Xms512m -Xmx4g |
| Optimized (4GB) | 8GB       | -Xms1g -Xmx4g   |

## Emergency: If IDE Configuration Fails

Use the external script as fallback:
```bash
./start-dynamic-memory.sh
```

This bypasses IDE limitations and ensures optimal memory configuration.

## Verification

After configuration, verify:
1. **JVM Memory**: Should show 4GB+ max heap
2. **Large Files**: Should process without errors
3. **Console Output**: Should show memory monitoring
4. **Performance**: Should be faster with G1GC

The application is now IDE-compatible with proper memory management!
