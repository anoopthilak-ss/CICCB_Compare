# Excel Comparison Tool - JAR Distribution

## Overview
This folder contains the executable JAR file and startup scripts for the Excel Comparison Tool.

## Files Included
- `excel-comparison-tool-1.0.0.jar` - Main application JAR file
- `start.sh` - Optimized startup script (macOS/Linux)
- `start.bat` - Optimized startup script (Windows)
- `quick-start.sh` - Quick startup script (macOS/Linux)
- `quick-start.bat` - Quick startup script (Windows)

## Quick Start

### Option 1: Quick Start (Recommended for most users)
#### macOS/Linux
```bash
./quick-start.sh
```

#### Windows
```cmd
quick-start.bat
```

### Option 2: Optimized Start (Recommended for large files)
#### macOS/Linux
```bash
./start.sh
```

#### Windows
```cmd
start.bat
```

## Manual Java Command

If scripts don't work, run manually:

```bash
# Basic (2GB heap)
java -Xms512m -Xmx2g -XX:+UseG1GC -jar excel-comparison-tool-1.0.0.jar

# Advanced (4GB heap)
java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -jar excel-comparison-tool-1.0.0.jar
```

## System Requirements

### Minimum Requirements
- **Java**: 8+ (64-bit recommended)
- **RAM**: 4GB
- **Disk Space**: 100MB

### Recommended for Large Files
- **Java**: 11+ (64-bit)
- **RAM**: 8GB+
- **SSD**: For faster file processing

## Memory Configuration

The optimized scripts automatically detect system memory and configure:

| System RAM | Heap Size | Use Case |
|------------|-----------|----------|
| ≤4GB       | 2GB       | Small files (<5MB) |
| 4-8GB      | 4GB       | Medium files (5-20MB) |
| 8-16GB     | 6GB       | Large files (20-50MB) |
| >16GB      | 8GB       | Very large files (>50MB) |

## Features

### Application Features
- **Multi-version Java compatibility** (Java 8+)
- **Memory optimization** for large Excel files
- **Batch processing** for performance
- **Memory monitoring** and early warnings
- **Cross-platform support** (Windows, macOS, Linux)

### Supported Excel Formats
- `.xlsx` (Excel 2007+)
- `.xls` (Excel 97-2003)
- Large files up to 50MB+

## Usage Instructions

1. **Start the application** using one of the startup scripts
2. **Open browser** to http://localhost:8080
3. **Upload Excel files** using the web interface
4. **Select sheets** and columns for comparison
5. **View results** with matched and mismatched records

## Troubleshooting

### OutOfMemoryError
If you get memory errors:
1. Use the optimized startup script (`./start.sh`)
2. Close other applications
3. Increase heap size manually:
   ```bash
   java -Xms1g -Xmx8g -jar excel-comparison-tool-1.0.0.jar
   ```

### Port Already in Use
If port 8080 is busy:
```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9

# Or use different port
java -jar excel-comparison-tool-1.0.0.jar --server.port=8081
```

### Java Not Found
Install Java 11+:
- **macOS**: `brew install openjdk@11`
- **Linux**: `sudo apt install openjdk-11-jdk`
- **Windows**: Download from Oracle.com

## Performance Tips

### For Large Excel Files
1. **Use optimized startup script** (`./start.sh`)
2. **Close other applications** to free memory
3. **Use SSD storage** for faster I/O
4. **Consider file size** - split very large files

### Memory Monitoring
The application logs memory usage:
```
=== Memory Status ===
Max JVM Memory: 4096MB
Currently Used: 512MB (12.5%)
```

## Version Information
- **Version**: 1.0.0
- **Build Date**: 2026-02-27
- **Java Compatibility**: 8, 11, 17, 21+
- **Spring Boot**: 2.7.18

## Support

For issues and questions:
1. Check the console output for error messages
2. Review the troubleshooting section
3. Ensure Java is properly installed
4. Verify system meets minimum requirements

## License
This application is built with Spring Boot and Apache POI for Excel processing.
