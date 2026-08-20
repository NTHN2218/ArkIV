@echo off
setlocal

title Universal Java Runner

:: ============================================================
:: Universal Java Runner - BAT launcher
:: ============================================================

set "ROOT=%~dp0"
set "SCRIPT=%ROOT%java_runner.py"

echo.
echo ============================================================
echo              UNIVERSAL JAVA RUNNER
echo ============================================================
echo.
echo Project: %ROOT%
echo.

:: ------------------------------------------------------------
:: Check Python
:: ------------------------------------------------------------

where python >nul 2>&1

if errorlevel 1 (
    echo [ERROR] Python was not found in PATH.
    echo.
    pause
    exit /b 1
)

:: ------------------------------------------------------------
:: Run Python runner
:: ------------------------------------------------------------

python "%SCRIPT%" %*

set "EXIT_CODE=%ERRORLEVEL%"

echo.
echo ============================================================
echo Runner exited with code %EXIT_CODE%.
echo ============================================================
echo.

pause

exit /b %EXIT_CODE%