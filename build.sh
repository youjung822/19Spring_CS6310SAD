#!/bin/sh
cd ./dist
rm -rf *
cp ../src/*.java .
javac *.java
cp -r ../images .
jar cfe osmowsis.jar Main *.class images
zip -r source_code.zip *.java images
rm -rf *.java
rm -rf *.class
rm -rf images
