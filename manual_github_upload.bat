@echo off
chcp 65001
title Broadband Only Connect - Manual GitHub Upload

echo.
echo ================================================
echo    MANUAL GITHUB UPLOAD - BROADBAND APP
echo ================================================
echo.

:: Check Git installation
set GIT_PATH=C:\Program Files\Git\bin\git.exe
if not exist "%GIT_PATH%" (
    echo ? Git not found at default location
    echo ?? Using alternative upload method...
    goto :ALTERNATIVE
)

echo ? Git found! Starting upload process...
echo.

:: Step 1: Initialize Git
"%GIT_PATH%" init
if errorlevel 1 (
    echo ? Git init failed
    goto :ALTERNATIVE
)

:: Step 2: Add files
"%GIT_PATH%" add .
echo Files added to Git...

:: Step 3: Commit
"%GIT_PATH%" config user.email "upload@example.com"
"%GIT_PATH%" config user.name "Broadband User"
"%GIT_PATH%" commit -m "Initial commit: Broadband Only Connect Android App"
echo Commit created...

echo.
echo ================================================
echo    GITHUB REPOSITORY SETUP REQUIRED
echo ================================================
echo.
echo PLEASE CREATE GITHUB REPOSITORY FIRST:
echo.
echo 1. Go to: https://github.com
echo 2. Login/Signup
echo 3. Click '+' -> New repository
echo 4. Name: BroadbandOnlyConnect
echo 5. Keep PUBLIC, UNCHECK all options
echo 6. Click Create repository
echo.
echo After creation, paste the URL below...
echo.

set /p REPO_URL="Paste GitHub URL: "

:: Step 4: Connect to GitHub
"%GIT_PATH%" remote add origin "%REPO_URL%"
"%GIT_PATH%" branch -M main

:: Step 5: Push to GitHub
echo Uploading to GitHub...
"%GIT_PATH%" push -u origin main

if errorlevel 0 (
    echo.
    echo ? SUCCESS! Project uploaded to GitHub!
    echo ?? Your project: %REPO_URL%
    goto :SUCCESS
) else (
    echo.
    echo ? Git push failed
    goto :ALTERNATIVE
)

:ALTERNATIVE
echo.
echo ================================================
echo    ALTERNATIVE UPLOAD METHOD
echo ================================================
echo.
echo Since Git is not working properly, use these methods:
echo.
echo METHOD 1: Manual ZIP Upload
echo 1. Right-click BroadbandOnlyConnect folder
echo 2. Send to -> Compressed (zipped) folder
echo 3. Go to GitHub repository
echo 4. Click 'Add file' -> 'Upload files'
echo 5. Drag and drop the ZIP file
echo.
echo METHOD 2: Use GitHub Desktop
echo 1. Download GitHub Desktop from: https://desktop.github.com/
echo 2. Install and login
echo 3. Drag project folder to GitHub Desktop
echo 4. Publish to GitHub
echo.
echo METHOD 3: Use Android Studio built-in Git
echo 1. Install Android Studio
echo 2. Open project
echo 3. VCS -> Enable Version Control
echo 4. VCS -> Import into Version Control -> Share on GitHub
echo.

:SUCCESS
echo.
echo ?? Your Android project is ready for APK building!
echo.
pause
