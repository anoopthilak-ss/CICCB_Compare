#!/bin/bash

# Test JAR functionality
echo "=== Testing Excel Comparison Tool JAR ==="
echo

# Check if JAR exists
JAR_FILE="jar/ciccb-comparison-tool-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

echo "✅ JAR file found: $JAR_FILE"

# Check JAR size
JAR_SIZE=$(stat -f%z "$JAR_FILE" 2>/dev/null || stat -c%s "$JAR_FILE" 2>/dev/null)
JAR_SIZE_MB=$((JAR_SIZE / 1024 / 1024))
echo "✅ JAR size: ${JAR_SIZE_MB}MB"

# Check Java version
echo "✅ Java version:"
java -version

echo
echo "=== Startup Scripts Available ==="
echo "📄 Quick Start (2GB heap):"
echo "   ./jar/quick-start.sh"
echo "   jar\\quick-start.bat (Windows)"
echo
echo "📄 Optimized Start (auto-detect memory):"
echo "   ./jar/start.sh"
echo "   jar\\start.bat (Windows)"
echo
echo "=== Usage Instructions ==="
echo "1. Run one of the startup scripts above"
echo "2. Open browser to: http://localhost:8080"
echo "3. Upload Excel files and compare"
echo
echo "=== Memory Configuration ==="
echo "• Quick Start: 2GB heap (good for most files)"
echo "• Optimized: Auto-detects system RAM"
echo "• Manual: java -Xms512m -Xmx4g -jar $JAR_FILE"
echo
echo "✅ JAR package ready for distribution!"
