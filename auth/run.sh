#!/bin/bash
clear
set -e

TOMCAT_HOME="/home/hridaykh/Code/tomcat-11.0.7"
WAR_NAME="auth"
TARGET_WAR="target/$WAR_NAME.war"
DEPLOY_PATH="$TOMCAT_HOME/webapps/$WAR_NAME.war"

echo "🧹 Building WAR (skip tests)..."
# Skip cleaning if not needed, just package
mvn package -DskipTests

echo "📦 Deploying new WAR..."
# Use mv instead of rm+cp for atomic replace (if on same filesystem)
rm -rf $TOMCAT_HOME/webapps/$WAR_NAME.war
rm -rf $TOMCAT_HOME/webapps/$WAR_NAME
mv -f "$TARGET_WAR" "$DEPLOY_PATH"

#echo "🚀 Starting Tomcat..."
#exec "$TOMCAT_HOME/bin/catalina.sh" run
