@echo off
REM Excel Comparison Tool - Optimized JAR Startup Script for Windows

echo === Excel Comparison Tool - JAR Startup ===
echo.

REM Check if JAR exists
if not exist "target\ciccb-comparison-tool-1.0.0.jar" (
    echo Error: JAR file not found: target\ciccb-comparison-tool-1.0.0.jar
    echo Please run 'mvn clean package' first.
    pause
    exit /b 1
)

REM Set optimized JVM memory settings
set JVM_ARGS=-Xms512m -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication

REM Add memory monitoring and debugging
set JVM_ARGS=%JVM_ARGS% -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc-jar.log
set JVM_ARGS=%JVM_ARGS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=heap-dump.hprof
set JVM_ARGS=%JVM_ARGS% -Dfile.encoding=UTF-8

echo JVM Memory Configuration:
echo - Initial Heap: 512m
echo - Maximum Heap: 4g
echo - Garbage Collector: G1GC
echo.
echo JVM Arguments: %JVM_ARGS%
echo.
echo Starting Excel Comparison Tool...
echo Access at: http://localhost:8080
echo GC logs: gc-jar.log
echo Heap dump (if OOM): heap-dump.hprof
echo.

REM Start the application with optimized memory
java %JVM_ARGS% -jar target\ciccb-comparison-tool-1.0.0.jar

echo.
echo Application stopped.
pause
