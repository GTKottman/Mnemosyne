# Builds a debug APK for installing on a physical Android device.
# Usage:
#   .\build-apk.ps1
#   .\build-apk.ps1 -Install

param(
    [switch]$Install
)

$ErrorActionPreference = "Stop"

$ProjectRoot = $PSScriptRoot
$GradleWrapper = Join-Path $ProjectRoot "gradlew.bat"
$ApkSource = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
$DistDir = Join-Path $ProjectRoot "dist"
$ApkDest = Join-Path $DistDir "mnemosyne-debug.apk"

if (-not (Test-Path $GradleWrapper)) {
    Write-Error "Gradle wrapper not found at $GradleWrapper"
}

Push-Location $ProjectRoot
try {
    Write-Host "Building debug APK..." -ForegroundColor Cyan
    & $GradleWrapper assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }

    if (-not (Test-Path $ApkSource)) {
        throw "Build finished but APK was not found at $ApkSource"
    }

    New-Item -ItemType Directory -Path $DistDir -Force | Out-Null
    Copy-Item -Path $ApkSource -Destination $ApkDest -Force

    $apkInfo = Get-Item $ApkDest
    Write-Host ""
    Write-Host "APK ready:" -ForegroundColor Green
    Write-Host "  $($apkInfo.FullName)"
    Write-Host "  $([math]::Round($apkInfo.Length / 1MB, 2)) MB"
    Write-Host ""
    Write-Host "Transfer this file to your phone and open it to install,"
    Write-Host "or run: .\build-apk.ps1 -Install"

    if ($Install) {
        $adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
        if (-not (Test-Path $adb)) {
            throw "adb not found at $adb. Install Android SDK platform-tools or add adb to PATH."
        }

        $devices = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
        if (-not $devices) {
            throw "No Android device detected. Enable USB debugging and connect your phone."
        }

        Write-Host ""
        Write-Host "Installing on connected device..." -ForegroundColor Cyan
        & $adb install -r $ApkDest
        if ($LASTEXITCODE -ne 0) {
            throw "adb install failed with exit code $LASTEXITCODE"
        }

        Write-Host "Installed successfully." -ForegroundColor Green
    }
}
finally {
    Pop-Location
}
