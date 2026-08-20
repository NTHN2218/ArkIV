@echo off
setlocal EnableDelayedExpansion

title Universal Java Compiler

echo.
echo ============================================================
echo              UNIVERSAL JAVA COMPILER
echo ============================================================
echo.

set "PROJECT=%~dp0"
set "PROJECT=%PROJECT:~0,-1%"

echo Project:
echo %PROJECT%
echo.

echo Enter path to the MAIN Java file.
echo You can use a path relative to this folder or an absolute path.
echo.
set /p "MAIN=Main Java file: "

if "%MAIN%"=="" (
    echo.
    echo [ERROR] No file was entered.
    goto :ERROR
)

REM ============================================================
REM Resolve main file
REM ============================================================

if exist "%MAIN%" (
    set "MAIN_FILE=%MAIN%"
) else if exist "%PROJECT%\%MAIN%" (
    set "MAIN_FILE=%PROJECT%\%MAIN%"
) else (
    echo.
    echo [ERROR] Main Java file was not found:
    echo         %MAIN%
    goto :ERROR
)

for %%F in ("%MAIN_FILE%") do set "MAIN_FILE=%%~fF"

echo.
echo [OK] Main file:
echo      %MAIN_FILE%

REM ============================================================
REM Check Java
REM ============================================================

echo.
echo [INFO] Checking Java...

where javac >nul 2>&1

if errorlevel 1 (
    echo [ERROR] javac was not found in PATH.
    goto :ERROR
)

echo [OK] javac found.

REM ============================================================
REM Prepare OUT directory
REM ============================================================

set "OUT=%PROJECT%\out"

echo.
echo [INFO] Output directory:
echo        %OUT%

if exist "%OUT%" (
    echo [INFO] Removing old out directory...
    rmdir /s /q "%OUT%"
)

mkdir "%OUT%"

if not exist "%OUT%" (
    echo [ERROR] Could not create out directory.
    goto :ERROR
)

echo [OK] Output directory ready.

REM ============================================================
REM Build library classpath
REM ============================================================

set "LIB_CP="

if exist "%PROJECT%\lib\" (

    echo.
    echo [INFO] Found 'lib' directory.
    echo [INFO] Searching for JAR files...

    for %%J in ("%PROJECT%\lib\*.jar") do (

        if exist "%%~fJ" (

            echo        %%~nxJ

            if defined LIB_CP (
                set "LIB_CP=!LIB_CP!;%%~fJ"
            ) else (
                set "LIB_CP=%%~fJ"
            )
        )
    )

    if not defined LIB_CP (
        echo [INFO] No JAR files found in lib.
    )

) else (

    echo.
    echo [INFO] No 'lib' directory found.
    echo        Compiling without external libraries.
)

REM ============================================================
REM Find all Java source files
REM ============================================================

echo.
echo [INFO] Searching for Java source files...
echo.

set "SOURCE_LIST=%TEMP%\java_sources_%RANDOM%.txt"

if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"

for /r "%PROJECT%" %%F in (*.java) do (

    REM Skip anything inside .git
    echo %%~dpF | findstr /i "\\.git\\" >nul

    if errorlevel 1 (

        REM Convert Windows backslashes to forward slashes
        set "SOURCE=%%~fF"
        set "SOURCE=!SOURCE:\=/!"

        REM Quote the path because it may contain spaces
        echo "!SOURCE!">>"%SOURCE_LIST%"
    )
)

REM ============================================================
REM Count source files
REM ============================================================

set /a SOURCE_COUNT=0

for /f "usebackq delims=" %%F in ("%SOURCE_LIST%") do (
    set /a SOURCE_COUNT+=1
)

echo [INFO] Found !SOURCE_COUNT! Java source files.

if !SOURCE_COUNT! EQU 0 (
    echo.
    echo [ERROR] No Java source files were found.
    goto :ERROR_CLEAN
)

REM ============================================================
REM Compile
REM ============================================================

echo.
echo ============================================================
echo                     COMPILING
echo ============================================================
echo.

if defined LIB_CP (

    javac ^
        -encoding UTF-8 ^
        -cp "%LIB_CP%" ^
        -d "%OUT%" ^
        @"%SOURCE_LIST%"

) else (

    javac ^
        -encoding UTF-8 ^
        -d "%OUT%" ^
        @"%SOURCE_LIST%"
)

set "COMPILE_RESULT=%ERRORLEVEL%"

REM ============================================================
REM Cleanup
REM ============================================================

if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"

REM ============================================================
REM Result
REM ============================================================

echo.

if not "%COMPILE_RESULT%"=="0" (
    goto :COMPILE_FAILED
)

echo ============================================================
echo                 COMPILATION SUCCESSFUL
echo ============================================================
echo.
echo Output:
echo %OUT%
echo.
echo Main source:
echo %MAIN_FILE%
echo.
echo You can now run:
echo run.bat
echo ============================================================
echo.

pause
exit /b 0


REM ============================================================
REM Errors
REM ============================================================

:ERROR_CLEAN

if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"

:ERROR

echo.
echo ============================================================
echo                  COMPILATION FAILED
echo ============================================================
echo.

pause
exit /b 1


:COMPILE_FAILED

echo ============================================================
echo                  COMPILATION FAILED
echo ============================================================
echo.
echo javac exited with code %COMPILE_RESULT%.
echo.

pause
exit /b %COMPILE_RESULT%