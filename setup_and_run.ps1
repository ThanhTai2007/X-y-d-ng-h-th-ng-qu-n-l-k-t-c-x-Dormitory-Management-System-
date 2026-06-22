# Automation Script to Setup Dependencies and Compile the KTX Manager Application
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$projectDir = "C:\Users\ADMIN\IdeaProjects\Dormitory_Management_System"
$libDir = "$projectDir\lib"
$jfxDir = "C:\Users\ADMIN\javafx-sdk-17.0.14"

# 1. Create lib directory
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir | Out-Null
    Write-Host "[OK] Created lib directory: $libDir"
}

# 2. Download Apache POI & jBCrypt
$urls = @(
    "https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.3/poi-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.3/poi-ooxml-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-lite/5.2.3/poi-ooxml-lite-5.2.3.jar",
    "https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.1.1/xmlbeans-5.1.1.jar",
    "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar",
    "https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar",
    "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.18.0/log4j-api-2.18.0.jar",
    "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.18.0/log4j-core-2.18.0.jar"
)

Write-Host "Downloading missing JARs (jBCrypt, Apache POI)..."
foreach ($url in $urls) {
    $fileName = Split-Path $url -Leaf
    $dest = Join-Path $libDir $fileName
    if (-not (Test-Path $dest)) {
        Write-Host "   - Downloading $fileName..."
        Invoke-WebRequest -Uri $url -OutFile $dest
    }
}
Write-Host "[OK] All JARs downloaded to $libDir"

# 3. Check JavaFX
if (-not (Test-Path $jfxDir)) {
    Write-Host "JavaFX SDK 17 is missing. Downloading to C:\Users\ADMIN..."
    Invoke-WebRequest -Uri "https://download2.gluonhq.com/openjfx/17.0.14/openjfx-17.0.14_windows-x64_bin-sdk.zip" -OutFile "C:\Users\ADMIN\javafx.zip"
    Write-Host "Extracting JavaFX..."
    Expand-Archive -Path "C:\Users\ADMIN\javafx.zip" -DestinationPath "C:\Users\ADMIN" -Force
    Remove-Item "C:\Users\ADMIN\javafx.zip"
    Write-Host "[OK] JavaFX extracted to $jfxDir"
} else {
    Write-Host "[OK] JavaFX SDK 17 is ready at $jfxDir"
}

# 4. Compile Project
Write-Host "Compiling the Project..."
if (-not (Test-Path "$projectDir\out")) { New-Item -ItemType Directory -Path "$projectDir\out" | Out-Null }

# Get all Java files
$javaFiles = Get-ChildItem -Path "$projectDir\src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
if ($javaFiles.Count -eq 0) {
    Write-Host "[ERROR] No Java files found in src/"
    exit 1
}

# Construct Classpath
$cpJars = @()
if (Test-Path $libDir) {
    $cpJars = Get-ChildItem -Path $libDir -Filter "*.jar" | Select-Object -ExpandProperty FullName
}
$mssqlJars = Get-ChildItem -Path "C:\Users\ADMIN\Downloads\*mssql-jdbc*.jar" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if ($mssqlJars.Count -gt 0) {
    $cpJars += $mssqlJars[0]
}
$cpString = ($cpJars -join ";") + ";$projectDir\src"

# Execute javac
$javacPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr\bin\javac.exe"
if (-not (Test-Path $javacPath)) {
    $javacPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr\bin\javac.exe"
}

& $javacPath -d "$projectDir\out" -cp $cpString --module-path "$jfxDir\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation successful! No syntax errors."
    Write-Host "To run the app, you can use IntelliJ IDEA or run:"
    
    # Copy FXML/CSS resources
    Write-Host "   (Copying resources to out\ directory...)"
    Copy-Item -Path "$projectDir\src\view" -Destination "$projectDir\out\view" -Recurse -Force
    
    $javaPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr\bin\java.exe"
    if (-not (Test-Path $javaPath)) { $javaPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr\bin\java.exe" }
    
    Write-Host ""
    Write-Host "   & `"$javaPath`" -cp `"$cpString;$projectDir\out`" --module-path `"$jfxDir\lib`" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base App"
} else {
    Write-Host "[ERROR] Compilation failed. Check the errors above."
}
