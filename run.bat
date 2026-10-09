@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo javac was not found. Install JDK 17 or newer and add it to PATH.
    pause
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out
if exist sources.txt del sources.txt

for /r "src\main\java" %%f in (*.java) do (
    set "p=%%f"
    set "p=!p:\=/!"
    echo "!p!">>sources.txt
)

javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo Compilation failed.
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

xcopy /e /i /y "src\main\resources" "out" >nul
java -cp out pasta.Main
if errorlevel 1 pause
