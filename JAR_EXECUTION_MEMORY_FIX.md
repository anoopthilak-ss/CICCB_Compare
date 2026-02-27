# JAR Execution Memory Fix Guide

## Problem
Running the JAR file with default Java settings causes OutOfMemoryError during Excel comparison.

## Root Cause
Default Java heap size is too small (typically 256MB-512MB) for Excel file processing.

## Quick Solutions

### Option 1: Use Optimized Startup Script (Recommended)

#### For macOS/Linux
```bash
./start-jar-optimized.sh
```

#### For Windows
```cmd
start-jar-optimized.bat
```

This script automatically:
- Detects system RAM
- Sets optimal heap size (2GB-8GB based on available memory)
- Enables G1GC garbage collector
- Adds memory monitoring and debugging

### Option 2: Manual Java Command

#### Basic Fix (2GB heap)
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar target/excel-comparison-tool-1.0.0.jar
```

#### Advanced Fix (4GB heap)
```bash
java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -jar target/excel-comparison-tool-1.0.0.jar
```

#### Maximum Fix (8GB heap)
```bash
java -Xms1g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heap-dump.hprof -jar target/excel-comparison-tool-1.0.0.jar
```

## Memory Requirements by File Size

| Excel File Size | Min JVM Heap | Recommended JVM |
|-----------------|---------------|-----------------|
| <5MB           | 1GB           | -Xms256m -Xmx1g |
| 5-20MB         | 2GB           | -Xms512m -Xmx2g |
| 20-50MB        | 4GB           | -Xms512m -Xmx4g |
| >50MB          | 8GB           | -Xms1g -Xmx8g   |

## Testing the Fix

### Step 1: Check Current Memory
```bash
./test-jar-memory.sh
```

### Step 2: Run with Optimized Settings
```bash
./start-jar-optimized.sh
```

### Step 3: Verify Memory Usage
Check console output:
```
=== Excel Comparison Tool - JAR Startup ===
System RAM: 16GB
High memory system detected (8-16GB)
JVM Memory Configuration:
- Initial Heap: 1g
- Maximum Heap: 6g
```

### Step 4: Test Excel Comparison
1. Access: http://localhost:8080
2. Upload Excel files
3. Perform comparison
4. Monitor console for memory status

## JVM Arguments Explained

| Argument | Purpose |
|----------|---------|
| `-Xms512m` | Initial heap size (512MB) |
| `-Xmx4g` | Maximum heap size (4GB) |
| `-XX:+UseG1GC` | Use G1 garbage collector (better for large heaps) |
| `-XX:MaxGCPauseMillis=200` | Target GC pause time (200ms) |
| `-XX:+UseStringDeduplication` | Reduce memory usage for duplicate strings |
| `-XX:+HeapDumpOnOutOfMemoryError` | Create heap dump on OOM for debugging |
| `-XX:HeapDumpPath=./heap-dump.hprof` | Location for heap dump file |

## Troubleshooting

### Still Getting OutOfMemoryError?

1. **Increase heap size**:
   ```bash
   java -Xms1g -Xmx8g -jar target/excel-comparison-tool-1.0.0.jar
   ```

2. **Check system memory**:
   ```bash
   # macOS
   sysctl -n hw.memsize
   
   # Linux
   free -h
   ```

3. **Close other applications** to free memory

4. **Use 64-bit Java** (required for >2GB heaps):
   ```bash
   java -version
   # Should show "64-Bit Server VM"
   ```

### Performance Issues

1. **Use G1GC**: Already enabled in optimized scripts
2. **Monitor GC logs**: Check `gc-jar.log` for garbage collection patterns
3. **Adjust heap size**: Balance between performance and system stability

## Application Features

The application now includes:
- **Memory monitoring** before comparison
- **Early failure detection** with clear error messages
- **Batch processing** for large datasets
- **Progress logging** during operations
- **Automatic garbage collection** suggestions

## Example Output

With optimized settings, you'll see:
```
=== Memory Status ===
Max JVM Memory: 4096MB
Currently Used: 512MB (12.5%)
File1 values count: 15000
File2 values count: 12000
Processed 1000 records...
Processed 2000 records...
Memory after comparison: 768MB
Memory used: 256MB
```

## Success Indicators

✅ **No OutOfMemoryError** during comparison  
✅ **Memory monitoring** shows heap usage  
✅ **Large files** process successfully  
✅ **Console shows** progress and memory status  
✅ **Application remains responsive**  

The optimized JAR execution will handle large Excel file comparisons without memory issues!
