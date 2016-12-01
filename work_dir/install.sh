#!/bin/bash
# compiles all java files for linux 
echo Linux only, JDBC required
export CLASSPATH=".:/oracle/jdbc/lib/ojdbc6.jar"
javac *.java
echo Completed Installation.