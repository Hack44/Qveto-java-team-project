@echo off
title Qveto E-Commerce System
cd /d "%~dp0"

echo ========================================================
echo   Launching Qveto E-Commerce and Inventory Management GUI
echo ========================================================

REM Recompile all modules including DAO and UI layers
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

set "CP=bin"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

REM Run the compiled classes from the bin folder
java -cp "%CP%" com.qveto.ecommerce.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo If 'java' was not found, please close and reopen this window.
    pause
)
