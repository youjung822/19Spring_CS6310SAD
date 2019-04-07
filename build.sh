#!/bin/sh
cd ./dist
rm -rf *
cp ../src/*.java .
javac *.java
cp -r ../images .
jar cfe osmowsis.jar Main *.class images
zip -r osmowsis_sources.zip *.java images
rm -rf *.java
rm -rf *.class
rm -rf images
