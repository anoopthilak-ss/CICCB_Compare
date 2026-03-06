#!/bin/bash

# Excel Comparison Tool - Memory Optimized Startup Script
# This script starts the application with increased heap memory to handle large Excel files

echo "=== Excel Comparison Tool - Memory Optimized ==="
echo

# Set JVM memory options
JAVA_OPTS="-Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Add memory monitoring
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log"

# Set file encoding
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# Check if JAR exists
JAR_FILE="target/ciccb-comparison-tool-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run 'mvn clean package' first."
    exit 1
fi

# Display current memory settings
echo "JVM Memory Settings:"
echo "- Initial Heap: 512MB"
echo "- Maximum Heap: 4GB" 
echo "- Garbage Collector: G1GC"
echo "- GC Pause Target: 200ms"
echo

# Show system memory
echo "System Memory:"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    TOTAL_MEM=$(sysctl -n hw.memsize | awk '{print $1/1024/1024/1024 " GB"}')
    echo "- Total Memory: $TOTAL_MEM"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    TOTAL_MEM=$(free -h | awk '/^Mem:/ {print $2}')
    echo "- Total Memory: $TOTAL_MEM"
fi
echo

# Start the application
echo "Starting Excel Comparison Tool..."
echo "Memory monitoring enabled - check gc.log for GC details"
echo

java $JAVA_OPTS -jar "$JAR_FILE"

echo
echo "Application stopped."
