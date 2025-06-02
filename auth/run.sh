#!/bin/bash
clear
set -e

TOMCAT_HOME="/home/hridaykh/Code/tomcat-11.0.7"
WAR_NAME="auth.war"
TARGET_WAR="target/$WAR_NAME"
DEPLOY_PATH="$TOMCAT_HOME/webapps/$WAR_NAME"

echo "🧹 Building WAR (skip tests)..."
# Skip cleaning if not needed, just package
mvn package -DskipTests

echo "📦 Deploying new WAR..."
# Use mv instead of rm+cp for atomic replace (if on same filesystem)
mv -f "$TARGET_WAR" "$DEPLOY_PATH"

#echo "🚀 Starting Tomcat..."
#exec "$TOMCAT_HOME/bin/catalina.sh" run
