#!/usr/bin/env bash

rm -f *.class

for file in *java; do
    javac -cp ".:../lib/weka.jar" "$file"
done
