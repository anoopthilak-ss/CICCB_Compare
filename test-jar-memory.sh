#!/bin/bash

# Test current JAR memory settings
echo "=== Testing Current JAR Memory Settings ==="
echo

# Check default Java memory
echo "Default Java Memory Settings:"
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E "HeapSize|MaxHeapSize" | head -2
echo

# Test with small memory (should fail for large files)
echo "Testing with 512MB max heap (current default):"
echo "This will likely fail for large Excel files..."
echo

# Show system memory
OS=$(uname -s)
if [[ "$OS" == "Darwin" ]]; then
    TOTAL_RAM_BYTES=$(sysctl -n hw.memsize)
    TOTAL_RAM_GB=$((TOTAL_RAM_BYTES / 1024 / 1024 / 1024))
    echo "System RAM: ${TOTAL_RAM_GB}GB"
elif [[ "$OS" == "Linux" ]]; then
    TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    TOTAL_RAM_GB=$((TOTAL_RAM_KB / 1024 / 1024))
    echo "System RAM: ${TOTAL_RAM_GB}GB"
fi

echo
echo "SOLUTION: Use optimized startup script:"
echo "./start-jar-optimized.sh"
echo
echo "Or manually:"
echo "java -Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target/excel-comparison-tool-1.0.0.jar"
