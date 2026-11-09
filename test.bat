@echo off
title Qveto Automated Verification Test Runner
cd /d "%~dp0"

echo ========================================================
echo   Running Qveto Automated Verification Test Suites
echo ========================================================
echo.

REM Compile all modules including verification test suite
javac -d bin src/main/java/com/qveto/ecommerce/model/*.java src/main/java/com/qveto/ecommerce/dao/*.java src/main/java/com/qveto/ecommerce/util/*.java src/main/java/com/qveto/ecommerce/ui/*.java src/main/java/com/qveto/ecommerce/*.java src/test/java/com/qveto/ecommerce/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Running automated verification test suites...
echo.

set "CP=bin;src/main/resources"
if exist "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" (
    set "CP=bin;src/main/resources;%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar"
)

echo --------------------------------------------------------
echo [1/2] Executing DatabaseIntegrityTest (Member 1)...
echo --------------------------------------------------------
java -cp "%CP%" com.qveto.ecommerce.util.DatabaseIntegrityTest

echo.
echo --------------------------------------------------------
echo [2/2] Executing StoreWorkflowIntegrationTest (Member 2)...
echo --------------------------------------------------------
java -cp "%CP%" com.qveto.ecommerce.StoreWorkflowIntegrationTest

echo.
pause
