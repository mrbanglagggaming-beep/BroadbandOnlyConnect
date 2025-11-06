@echo off
echo Installing required tools...
echo Please wait...

:: Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Java is not installed. Please install Java JDK 8 or later.
    pause
    exit /b 1
)

:: Check if Android Studio project structure is correct
if not exist "app\src\main\java\com\broadband\onlyconnect\MainActivity.java" (
    echo Project structure is incorrect.
    pause
    exit /b 1
)

echo Project structure looks good!
echo.
echo NEXT STEPS:
echo 1. Install Android Studio
echo 2. Open the BroadbandOnlyConnect folder in Android Studio
echo 3. Click Build -> Build APK
echo 4. Your APK will be in app/build/outputs/apk/debug/
echo.
pause
