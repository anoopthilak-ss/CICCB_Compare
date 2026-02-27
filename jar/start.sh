#!/bin/bash

# Excel Comparison Tool - Optimized JAR Startup Script
# This script runs the JAR with proper memory settings to prevent OutOfMemoryError

echo "=== Excel Comparison Tool - JAR Startup ==="
echo

# Check if JAR exists
JAR_FILE="jar/excel-comparison-tool-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please ensure the JAR file is in the jar/ directory."
    exit 1
fi

# Detect system memory
OS=$(uname -s)
TOTAL_RAM_BYTES=0

if [[ "$OS" == "Darwin" ]]; then
    TOTAL_RAM_BYTES=$(sysctl -n hw.memsize)
elif [[ "$OS" == "Linux" ]]; then
    TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    TOTAL_RAM_BYTES=$((TOTAL_RAM_KB * 1024))
fi

TOTAL_RAM_GB=$((TOTAL_RAM_BYTES / 1024 / 1024 / 1024))
echo "System RAM: ${TOTAL_RAM_GB}GB"

# Calculate optimal heap size for JAR execution
if [[ $TOTAL_RAM_GB -le 4 ]]; then
    INITIAL_HEAP="512m"
    MAX_HEAP="2g"
    echo "Low memory system detected (<=4GB)"
elif [[ $TOTAL_RAM_GB -le 8 ]]; then
    INITIAL_HEAP="512m"
    MAX_HEAP="4g"
    echo "Medium memory system detected (4-8GB)"
elif [[ $TOTAL_RAM_GB -le 16 ]]; then
    INITIAL_HEAP="1g"
    MAX_HEAP="6g"
    echo "High memory system detected (8-16GB)"
else
    INITIAL_HEAP="2g"
    MAX_HEAP="8g"
    echo "Very high memory system detected (>16GB)"
fi

echo "JVM Memory Configuration:"
echo "- Initial Heap: $INITIAL_HEAP"
echo "- Maximum Heap: $MAX_HEAP"
echo

# Build JVM arguments for JAR execution
JVM_ARGS="-Xms$INITIAL_HEAP -Xmx$MAX_HEAP -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Add memory monitoring and debugging
JVM_ARGS="$JVM_ARGS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:jar/gc-jar.log"
JVM_ARGS="$JVM_ARGS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./jar/heap-dump.hprof"
JVM_ARGS="$JVM_ARGS -Dfile.encoding=UTF-8"

echo "JVM Arguments: $JVM_ARGS"
echo
echo "Starting Excel Comparison Tool..."
echo "Access at: http://localhost:8080"
echo "GC logs: jar/gc-jar.log"
echo "Heap dump (if OOM): jar/heap-dump.hprof"
echo

# Start the application with optimized memory
java $JVM_ARGS -jar "$JAR_FILE"

echo
echo "Application stopped."
