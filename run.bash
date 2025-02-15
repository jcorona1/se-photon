#!/bin/bash

# Remove all class files
echo "Cleaning up old class files..."
rm -f *.class

# Compile all Java files
echo "Compiling Java files..."
javac *.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful. Running Game..."
    java Game
else
    echo "Compilation failed. Fix errors and try again."
fi
