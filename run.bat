@echo off
title Qveto E-Commerce System
cd /d "%~dp0"

REM Ensure Java 21 compiler and runtime are in PATH
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe" (
        set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;%PATH%"
    )
)

echo ========================================================
echo   Launching Qveto E-Commerce and Inventory Management GUI
echo ========================================================

REM Recompile all modules including DAO and UI layers
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

set "CP=bin;src/main/resources"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;src/main/resources;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

REM Run the compiled classes from the bin folder
java -cp "%CP%" com.qveto.ecommerce.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo If 'java' was not found, please close and reopen this window.
    pause
)
