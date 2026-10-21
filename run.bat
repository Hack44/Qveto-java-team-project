@echo off
title Qveto E-Commerce System
cd /d "%~dp0"

echo ========================================================
echo   Launching Qveto E-Commerce & Inventory Management GUI
echo ========================================================

REM Recompile if needed
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

REM Run the compiled classes from the bin folder
java -cp bin com.qveto.ecommerce.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo If 'java' was not found, please close and reopen this window.
    pause
)
