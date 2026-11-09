@echo off
title Qveto Automated Verification Test Runner
cd /d "%~dp0"

REM Ensure Java 21 compiler and runtime are in PATH
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe" (
        set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;%PATH%"
    )
)

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
echo [1/2] Executing Core Database & Persistence Integrity Test Suite...
echo --------------------------------------------------------
java -cp "%CP%" com.qveto.ecommerce.util.DatabaseIntegrityTest

echo.
echo --------------------------------------------------------
echo [2/2] Executing End-to-End Store Workflow & Business Integration Test Suite...
echo --------------------------------------------------------
java -cp "%CP%" com.qveto.ecommerce.StoreWorkflowIntegrationTest

echo.
pause
