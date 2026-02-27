# Memory Optimization for Large Excel Files

The Excel Comparison Tool has been optimized to handle large Excel files without running out of memory.

## Problem Solved
- **Error**: `OutOfMemoryError: Java heap space` during comparison
- **Cause**: Large Excel files consuming excessive memory during processing

## Solutions Implemented

### 1. JVM Memory Configuration
- **Initial Heap**: 512MB (`-Xms512m`)
- **Maximum Heap**: 2GB (`-Xmx2g`) 
- **Garbage Collector**: G1GC (`-XX:+UseG1GC`)
- **GC Pause Target**: 200ms (`-XX:MaxGCPauseMillis=200`)

### 2. Code Optimizations

#### Excel Service (`ExcelService.java`)
- **Pre-sized Collections**: ArrayList with initial capacity based on estimated row count
- **Memory Monitoring**: Logs memory usage before/after parsing
- **Batch Processing**: Triggers garbage collection every 1000 rows
- **Resource Management**: Proper workbook closure

#### Comparison Service (`ComparisonService.java`)
- **Batch Result Processing**: Processes results in batches of 1000 records
- **Memory Tracking**: Monitors memory usage during comparison
- **Optimized Data Structures**: LinkedHashMap with initial capacity
- **Periodic GC**: Suggests garbage collection during large operations

### 3. Memory Monitoring
The application now logs:
- Memory usage before/after operations
- Number of records processed
- Processing progress for large files

## Usage

### Method 1: Startup Script (Recommended)
```bash
./start-with-memory.sh
```

### Method 2: Manual Java Command
```bash
java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target/excel-comparison-tool-1.0.0.jar
```

### Method 3: Maven with Memory Settings
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx4g -XX:+UseG1GC"
```

## Performance Tips

### For Very Large Files (>50MB)
1. **Increase Maximum Heap**: Use `-Xmx8g` for 8GB maximum
2. **Use 64-bit Java**: Ensure you're running 64-bit JVM
3. **Close Other Applications**: Free up system memory
4. **Consider File Splitting**: Split very large files into smaller chunks

### Memory Requirements by File Size
| Excel File Size | Recommended Max Heap |
|----------------|---------------------|
| < 10MB         | 1GB                 |
| 10-50MB        | 2GB                 |
| 50-100MB       | 4GB                 |
| > 100MB        | 8GB+                |

### Monitoring Memory Usage
Check the console logs for:
```
Memory before Excel parsing: XXXMB
Memory after Excel parsing: XXXMB
Memory used for parsing: XXXMB
Processed 1000 rows...
```

## Troubleshooting

### Still Getting OutOfMemoryError?
1. **Increase Max Heap**: Change `-Xmx2g` to `-Xmx4g` or `-Xmx8g`
2. **Check System Memory**: Ensure enough RAM is available
3. **Use 64-bit Java**: `java -version` should show "64-Bit"
4. **Reduce Concurrent Operations**: Process one comparison at a time

### Performance Issues
1. **Use G1GC**: Already enabled, best for large heaps
2. **Monitor GC Logs**: Check `gc.log` for garbage collection patterns
3. **Consider SSD**: Faster disk I/O helps with file processing

## System Requirements

### Minimum Requirements
- **RAM**: 4GB
- **Java**: 8+ (64-bit recommended)
- **Disk Space**: 100MB + Excel file sizes

### Recommended for Large Files
- **RAM**: 8GB+
- **Java**: 11+ (64-bit)
- **SSD**: For faster file processing

## Example Output
```
Memory before Excel parsing: 256MB
Estimated rows to process: 15000
Processed 1000 rows...
Processed 2000 rows...
...
Memory after Excel parsing: 512MB
Memory used for parsing: 256MB

Memory before comparison: 512MB
File1 values count: 15000
File2 values count: 12000
Processed 1000 records...
Memory after comparison: 768MB
Memory used: 256MB
```
