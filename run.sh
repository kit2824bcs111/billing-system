#!/bin/bash
echo "Compiling..."
mkdir -p out
javac -cp lib/mysql-connector-j.jar src/BillingSystem.java -d out
[ $? -ne 0 ] && echo "Compilation failed." && exit 1
echo "Running..."
java -cp out:lib/mysql-connector-j.jar BillingSystem
