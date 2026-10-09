#!/bin/sh
set -e
rm -rf out
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
cp -r src/main/resources/. out/
java -cp out pasta.Main
