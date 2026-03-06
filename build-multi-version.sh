#!/bin/bash

# Multi-Version Java Build Script
# This script demonstrates building the project with different Java versions

echo "=== Excel Comparison Tool - Multi-Version Java Build ==="
echo

# Function to build with specific Java version
build_with_java() {
    local java_version=$1
    local profile_id=$2
    local java_home_path=$3
    
    echo "Building with Java $java_version..."
    
    if [ ! -z "$java_home_path" ]; then
        export JAVA_HOME="$java_home_path"
        echo "JAVA_HOME set to: $JAVA_HOME"
    fi
    
    java -version
    echo
    
    mvn clean package -DskipTests -P$profile_id
    
    if [ $? -eq 0 ]; then
        echo "✅ Build successful with Java $java_version"
        echo "Created JAR: target/ciccb-comparison-tool-1.0.0.jar"
        echo "This JAR will run on Java $java_version and higher"
    else
        echo "❌ Build failed with Java $java_version"
    fi
    
    echo "----------------------------------------"
    echo
}

# Check available Java versions
echo "Checking available Java versions..."
/usr/libexec/java_home -V
echo

# Build with different Java versions if available
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk" ]; then
    build_with_java "17" "java17" "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
fi

if [ -d "/Library/Java/JavaVirtualMachines/jdk-11.jdk" ]; then
    build_with_java "11" "java11" "/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home"
fi

# Build with default Java (current system)
echo "Building with default system Java..."
java -version
echo

mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful with default Java"
    echo "Created JAR: target/ciccb-comparison-tool-1.0.0.jar"
else
    echo "❌ Build failed with default Java"
fi

echo
echo "=== Build Complete ==="
echo "The project is now configured for multi-version Java compatibility!"
echo "See JAVA_VERSION_COMPATIBILITY.md for usage instructions."
