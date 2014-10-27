echo $JAVA_HOME;
echo $M2_HOME;
export M3_HOME=Documents/apache-maven-3.2.3;
export M3=$M3_HOME/bin;
export PATH=$M3:$PATH;
export JAVA_HOME=$(/usr/libexec/java_home -v 1.7);
mvn --version;
