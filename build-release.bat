@echo off
setlocal enabledelayedexpansion

REM ===================================================================
REM Dynoplayer Build Script - Automated Distribution Creator
REM Creates two versions: standalone and with ffmpeg/yt-dlp
REM ===================================================================

set APP_NAME=Dynoplayer
set APP_VERSION=2.0.69
set BUILD_DIR=%~dp0target\jpackage-build
set OUTPUT_DIR=%~dp0releases
set ICON_PATH=%~dp0src\main\resources\static\image\Dynoplayer.ico
set TOOLS_DIR=%~dp0tools

echo.
echo ===== %APP_NAME% v%APP_VERSION% Distribution Builder =====
echo.

REM Create output directory
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Step 1: Run Maven package
echo [1/5] Running Maven build...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed!
    exit /b 1
)

REM Step 2: Run jpackage
echo [2/5] Creating app-image with jpackage...
jpackage ^
  --type app-image ^
  --input target\ ^
  --main-jar Dynoplayer.jar ^
  --main-class dynoplayer.Launcher ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --vendor "Dynosaurs" ^
  --icon "%ICON_PATH%" ^
  --dest "%BUILD_DIR%"

if errorlevel 1 (
    echo ERROR: jpackage failed!
    exit /b 1
)

REM Step 3: Create standalone version
echo [3/5] Creating standalone version ^(without ffmpeg/yt-dlp^)...
set CLEAN_DIST=%BUILD_DIR%\%APP_NAME%-clean
if exist "%CLEAN_DIST%" rmdir /s /q "%CLEAN_DIST%"
xcopy "%BUILD_DIR%\%APP_NAME%" "%CLEAN_DIST%\" /e /i /y >nul

cd /d "%BUILD_DIR%"
powershell -NoProfile -Command "Compress-Archive -Path '%APP_NAME%-clean' -DestinationPath '%OUTPUT_DIR%\%APP_NAME%-v%APP_VERSION%-standalone.zip' -Force"
echo Created: %APP_NAME%-v%APP_VERSION%-standalone.zip

REM Step 4: Create version with dependencies
echo [4/5] Creating version with ffmpeg and yt-dlp...
set FULL_DIST=%BUILD_DIR%\%APP_NAME%-full
if exist "%FULL_DIST%" rmdir /s /q "%FULL_DIST%"
xcopy "%BUILD_DIR%\%APP_NAME%" "%FULL_DIST%\" /e /i /y >nul

set BIN_DIR=%FULL_DIST%\app\bin
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

REM Check and copy ffmpeg/yt-dlp from tools directory
set TOOLS_FOUND=0
if exist "%TOOLS_DIR%\ffmpeg.exe" (
    echo   Copying ffmpeg.exe...
    copy "%TOOLS_DIR%\ffmpeg.exe" "%BIN_DIR%\" >nul
    set TOOLS_FOUND=1
)

if exist "%TOOLS_DIR%\ffprobe.exe" (
    echo   Copying ffprobe.exe...
    copy "%TOOLS_DIR%\ffprobe.exe" "%BIN_DIR%\" >nul
    set TOOLS_FOUND=1
)

if exist "%TOOLS_DIR%\yt-dlp.exe" (
    echo   Copying yt-dlp.exe...
    copy "%TOOLS_DIR%\yt-dlp.exe" "%BIN_DIR%\" >nul
    set TOOLS_FOUND=1
)

REM Create ZIP with dependencies
cd /d "%BUILD_DIR%"
powershell -NoProfile -Command "Compress-Archive -Path '%APP_NAME%-full' -DestinationPath '%OUTPUT_DIR%\%APP_NAME%-v%APP_VERSION%-with-dependencies.zip' -Force"
echo Created: %APP_NAME%-v%APP_VERSION%-with-dependencies.zip

REM Step 5: Cleanup
echo [5/5] Cleaning up...
rmdir /s /q "%BUILD_DIR%"

echo.
echo ===== Build Complete =====
echo.
echo Output files created in: %OUTPUT_DIR%
echo.
echo   1. %APP_NAME%-v%APP_VERSION%-standalone.zip
echo      ^- No ffmpeg or yt-dlp ^(users must install separately^)
echo.
echo   2. %APP_NAME%-v%APP_VERSION%-with-dependencies.zip
if %TOOLS_FOUND% equ 1 (
    echo      ^- Includes ffmpeg and/or yt-dlp from %TOOLS_DIR%
) else (
    echo      ^- No tools found. Place ffmpeg.exe, ffprobe.exe, yt-dlp.exe
    echo        in the %TOOLS_DIR% directory to include them.
)
echo.
echo To prepare dependencies:
echo   1. Download ffmpeg from: https://ffmpeg.org/download.html
echo   2. Download yt-dlp from: https://github.com/yt-dlp/yt-dlp/releases
echo   3. Place the .exe files in: %TOOLS_DIR%
echo   4. Run this script again
echo.
pause
