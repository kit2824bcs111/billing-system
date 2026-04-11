@echo off
echo Compiling...
if not exist out mkdir out
javac -cp lib\mysql-connector-j.jar src\BillingSystem.java -d out
if %ERRORLEVEL% NEQ 0 (echo Compilation failed. & pause & exit /b)
echo Running...
java -cp out;lib\mysql-connector-j.jar BillingSystem
pause
