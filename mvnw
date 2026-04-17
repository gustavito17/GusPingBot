#!/bin/sh
# Maven Wrapper script for Unix/Mac
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(dirname "$0")}"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    if command -v wget > /dev/null; then
        wget "$WRAPPER_URL" -O "$WRAPPER_JAR" || exit 1
    elif command -v curl > /dev/null; then
        curl -o "$WRAPPER_JAR" "$WRAPPER_URL" -f || exit 1
    else
        echo "ERROR: wget or curl required to download maven-wrapper.jar"
        exit 1
    fi
fi

exec "$JAVA_HOME/bin/java" \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
