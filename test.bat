@echo off
title Qveto Database Integrity and Transaction Test Runner
cd /d "%~dp0"

echo ========================================================
echo   Running Qveto Database Integrity and Rollback Tests
echo ========================================================
echo.

REM Compile all modules including verification test suite
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Running automated verification test suite...
echo.

set "CP=bin"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

REM Execute the DatabaseIntegrityTest suite
java -cp "%CP%" com.qveto.ecommerce.util.DatabaseIntegrityTest

echo.
pause

