export MAVEN_OPTS="-XX:MaxPermSize=256m -Xmx1g -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn hpi:run -Djava.awt.headless=true 
