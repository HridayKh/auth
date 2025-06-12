TOMCAT_HOME="/home/hridaykh/Code/tomcat-11.0.7"

# cd first so log files are created in the right place
cd $TOMCAT_HOME

echo "🚀 Starting Tomcat..."
exec "$TOMCAT_HOME/bin/catalina.sh" run
