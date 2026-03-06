#!/bin/bash

# Excel Comparison Tool - Quick Start Script
# Simple startup with basic memory optimization

echo "Starting Excel Comparison Tool..."
echo "Access at: http://localhost:8080"
echo

# Start with 2GB heap (good for most systems)
java -Xms512m -Xmx2g -XX:+UseG1GC -jar jar/ciccb-comparison-tool-1.0.0.jar
