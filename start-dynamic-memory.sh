#!/bin/bash

# Dynamic Memory Configuration Script
# Automatically configures JVM memory based on available system RAM

echo "=== Excel Comparison Tool - Dynamic Memory Configuration ==="
echo

# Detect system information
OS=$(uname -s)
TOTAL_RAM_BYTES=0

if [[ "$OS" == "Darwin" ]]; then
    # macOS
    TOTAL_RAM_BYTES=$(sysctl -n hw.memsize)
elif [[ "$OS" == "Linux" ]]; then
    # Linux
    TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    TOTAL_RAM_BYTES=$((TOTAL_RAM_KB * 1024))
else
    echo "Unsupported OS: $OS"
    exit 1
fi

# Convert to GB for display
TOTAL_RAM_GB=$((TOTAL_RAM_BYTES / 1024 / 1024 / 1024))

echo "System Information:"
echo "- OS: $OS"
echo "- Total RAM: ${TOTAL_RAM_GB}GB"
echo

# Calculate optimal heap sizes based on available RAM
if [[ $TOTAL_RAM_GB -le 4 ]]; then
    # Systems with 4GB or less
    INITIAL_HEAP="256m"
    MAX_HEAP="1g"
    echo "Low memory system detected (<=4GB)"
elif [[ $TOTAL_RAM_GB -le 8 ]]; then
    # Systems with 4-8GB
    INITIAL_HEAP="512m"
    MAX_HEAP="2g"
    echo "Medium memory system detected (4-8GB)"
elif [[ $TOTAL_RAM_GB -le 16 ]]; then
    # Systems with 8-16GB
    INITIAL_HEAP="1g"
    MAX_HEAP="4g"
    echo "High memory system detected (8-16GB)"
else
    # Systems with 16GB+
    INITIAL_HEAP="2g"
    MAX_HEAP="8g"
    echo "Very high memory system detected (>16GB)"
fi

# Calculate safe heap (75% of max to leave room for OS)
SAFE_MAX_HEAP=$((TOTAL_RAM_GB * 3 / 4))
if [[ $SAFE_MAX_HEAP -lt 2 ]]; then
    SAFE_MAX_HEAP="1g"
elif [[ $SAFE_MAX_HEAP -lt 4 ]]; then
    SAFE_MAX_HEAP="2g"
elif [[ $SAFE_MAX_HEAP -lt 8 ]]; then
    SAFE_MAX_HEAP="4g"
else
    SAFE_MAX_HEAP="8g"
fi

echo "Memory Configuration:"
echo "- Initial Heap: $INITIAL_HEAP"
echo "- Maximum Heap: $MAX_HEAP"
echo "- Safe Maximum: $SAFE_MAX_HEAP (75% of total RAM)"
echo

# Build JVM arguments
JVM_ARGS="-Xms$INITIAL_HEAP -Xmx$SAFE_MAX_HEAP -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# Add memory monitoring for debugging
JVM_ARGS="$JVM_ARGS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log"

echo "JVM Arguments:"
echo "$JVM_ARGS"
echo

# Check if JAR exists
JAR_FILE="target/ciccb-comparison-tool-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run 'mvn clean package' first."
    exit 1
fi

# Show current Java memory settings
echo "Current Java Configuration:"
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E "HeapSize|MaxHeapSize" | head -2
echo

# Start the application
echo "Starting Excel Comparison Tool with optimized memory settings..."
echo "GC logs will be written to gc.log"
echo

java $JVM_ARGS -jar "$JAR_FILE"

echo
echo "Application stopped."
echo "Check gc.log for garbage collection details if memory issues persist."
