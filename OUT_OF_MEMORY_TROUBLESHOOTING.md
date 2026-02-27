# OutOfMemoryError Troubleshooting Guide

## Problem
Application works on some computers but fails with `OutOfMemoryError: Java heap space` on others.

## Root Cause Analysis
The issue occurs when:
1. **System has insufficient RAM** for the JVM heap size
2. **JVM heap is too small** for the Excel file size
3. **Other applications** are consuming available memory
4. **32-bit Java** is being used on large files

## Solutions Implemented

### 1. Dynamic Memory Detection
Created `start-dynamic-memory.sh` that:
- **Detects system RAM** automatically
- **Calculates optimal heap sizes** based on available memory
- **Uses safe limits** (75% of total RAM)
- **Adjusts per system**:
  - ≤4GB RAM: 256m initial, 1g max
  - 4-8GB RAM: 512m initial, 2g max  
  - 8-16GB RAM: 1g initial, 4g max
  - >16GB RAM: 2g initial, 8g max

### 2. Early Warning System
Added memory monitoring that:
- **Checks memory usage** before operations
- **Shows warnings** at 75-80% usage
- **Forces garbage collection** when needed
- **Provides guidance** to users

### 3. Optimized Processing
- **Batch processing** for large datasets
- **Pre-sized collections** to reduce memory allocation
- **Periodic GC** during processing
- **Resource cleanup** after operations

## Usage Instructions

### For Problematic Systems
Use the dynamic memory script:

```bash
./start-dynamic-memory.sh
```

This will:
1. Detect your system's RAM
2. Set appropriate memory limits
3. Show memory configuration
4. Start with optimal settings

### Manual Testing
If the script doesn't work, test manually:

```bash
# Try with 1GB max heap
java -Xms256m -Xmx1g -jar target/excel-comparison-tool-1.0.0.jar

# Try with 2GB max heap  
java -Xms512m -Xmx2g -jar target/excel-comparison-tool-1.0.0.jar

# Try with 4GB max heap
java -Xms1g -Xmx4g -jar target/excel-comparison-tool-1.0.0.jar
```

## System Requirements Check

### Check Your System
```bash
# Check total RAM
# macOS:
sysctl -n hw.memsize

# Linux:
free -h

# Check Java version
java -version

# Check if 64-bit Java
java -d64 -version
```

### Minimum Requirements
- **RAM**: 4GB (8GB recommended for large files)
- **Java**: 8+ 64-bit (required for >2GB heaps)
- **Free Disk**: 200MB + Excel file sizes

## Debugging Steps

### 1. Check Memory Monitoring
The application now shows:
```
=== Memory Status ===
Max JVM Memory: 2048MB
Currently Used: 512MB (25.0%)
```

If you see warnings like:
```
WARNING: High memory usage detected! Consider:
1. Using smaller Excel files
2. Increasing JVM max heap (-Xmx)
3. Closing other applications
```

### 2. Monitor GC Logs
Check `gc.log` for:
- Frequent Full GC cycles
- Long GC pause times
- OutOfMemoryError before crash

### 3. Test File Size Limits
```bash
# Test with small file first (1-2MB)
# Then medium file (5-10MB)  
# Finally large file (20MB+)
```

## Common Issues & Fixes

### Issue: Works on 8GB RAM, fails on 4GB
**Fix**: Use dynamic script or reduce max heap to 1g

### Issue: 32-bit Java with large heap
**Error**: `Could not reserve enough space for object heap`
**Fix**: Install 64-bit Java:
```bash
# macOS
brew install openjdk@11

# Linux  
sudo apt install openjdk-11-jdk-amd64
```

### Issue: Still OutOfMemory with 4GB heap
**Causes**:
- Excel file >50MB
- System has many background processes
- Fragmented memory

**Fixes**:
1. Close all other applications
2. Restart computer
3. Use smaller Excel files
4. Increase to 8GB heap if system has 16GB+ RAM

## Performance Optimization

### For Large Excel Files (>20MB)
1. **Pre-process files**: Remove empty rows/columns
2. **Split files**: Break into smaller chunks
3. **Use SSD**: Faster disk I/O helps
4. **Close browsers**: Free up memory

### Memory Usage by File Size
| File Size | Min RAM | Recommended JVM |
|------------|-----------|----------------|
| <5MB       | 2GB       | -Xms256m -Xmx1g |
| 5-20MB     | 4GB       | -Xms512m -Xmx2g |
| 20-50MB    | 8GB       | -Xms1g -Xmx4g   |
| >50MB       | 16GB+     | -Xms2g -Xmx8g   |

## Emergency Recovery

If application crashes:
1. **Check logs** for memory warnings
2. **Reduce file size** by 50%
3. **Increase heap** if system allows
4. **Restart application** with new settings
5. **Monitor system memory** during operation

## Contact Support

If issues persist:
1. Run `./start-dynamic-memory.sh` and copy output
2. Check `gc.log` for error patterns  
3. Note system specifications (RAM, OS, Java version)
4. Provide Excel file size and row count
