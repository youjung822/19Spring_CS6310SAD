#!/bin/sh
cd ./dist
rm -rf *
cp ../src/*.java .
javac *.java
jar cfe osmowsis.jar Main *.class
zip osmowsis_sources.zip *.java
rm -rf *.java
rm -rf *.class
