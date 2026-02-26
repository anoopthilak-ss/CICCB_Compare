# Multi-Version Java Compatibility

This project is configured to work with multiple Java versions (8, 11, 17, and 21+).

## Supported Java Versions

- **Java 8** (LTS) - Default compatibility
- **Java 11** (LTS) 
- **Java 17** (LTS)
- **Java 21+** (Latest LTS)

## How It Works

The project uses Maven profiles to automatically detect the Java version and apply the appropriate compiler settings:

### Automatic Profile Activation
Maven will automatically activate the correct profile based on the detected JDK version:

- Java 8: Uses `java8` profile (default)
- Java 11: Uses `java11` profile  
- Java 17: Uses `java17` profile
- Java 21+: Uses `java21+` profile

### Manual Profile Selection
You can also manually specify a profile:

```bash
# Compile with Java 8 compatibility
mvn clean compile -Pjava8

# Compile with Java 11 compatibility  
mvn clean compile -Pjava11

# Compile with Java 17 compatibility
mvn clean compile -Pjava17

# Compile with Java 21+ compatibility
mvn clean compile -Pjava21+
```

## Building the Project

### With Current JDK
```bash
mvn clean package
```

### With Specific Java Version (if multiple JDKs installed)
```bash
# Using JAVA_HOME
export JAVA_HOME=/path/to/jdk-11
mvn clean package

# Or using Maven Toolchains (if configured)
mvn clean package -Djdk.version=11
```

## Runtime Requirements

The compiled JAR will run on any Java version that is **equal to or higher** than the target compilation version:

- If compiled with Java 8: Runs on Java 8, 11, 17, 21+
- If compiled with Java 11: Runs on Java 11, 17, 21+
- If compiled with Java 17: Runs on Java 17, 21+
- If compiled with Java 21+: Runs on Java 21+

## Recommendation

For maximum compatibility, compile using Java 8:
```bash
mvn clean package -Pjava8
```

This will create a JAR that runs on all supported Java versions (8+).

## Dependencies

All project dependencies are compatible with Java 8+:
- Spring Boot 2.7.18 (supports Java 8-19)
- Apache POI 5.2.4 (supports Java 8+)

## Notes

- The project codebase uses only Java 8 compatible features
- No Java 9+ specific features (modules, var keyword, etc.) are used
- Spring Boot's auto-configuration handles version differences automatically
- The application will take advantage of newer JVM features when available (performance improvements, garbage collection, etc.)
