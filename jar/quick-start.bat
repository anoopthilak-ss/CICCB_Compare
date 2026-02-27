@echo off
REM Excel Comparison Tool - Quick Start Script for Windows

echo Starting Excel Comparison Tool...
echo Access at: http://localhost:8080
echo.

REM Start with 2GB heap (good for most systems)
java -Xms512m -Xmx2g -XX:+UseG1GC -jar jar\excel-comparison-tool-1.0.0.jar

pause
