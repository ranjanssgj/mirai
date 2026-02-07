#!/bin/bash
# Gradle wrapper script for the Unified Otaku project

# Set JAVA_HOME if necessary  
# export JAVA_HOME=/usr/lib/jvm/default

# Resolve project directory
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Download and run gradle wrapper if not present
if [ ! -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p "$APP_HOME/gradle/wrapper"
    curl -L -o "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
        "https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
fi

# Run gradle with wrapper  
exec java -Dorg.gradle.appname="gradlew" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
