TOMCAT_HOME="/home/hridaykh/Code/Servers/apache-tomcat-11.0.13-custom"

# cd first so log files are created in the right place
cd $TOMCAT_HOME

echo "🚀 Starting Tomcat..."
exec "$TOMCAT_HOME/bin/catalina.sh" run
