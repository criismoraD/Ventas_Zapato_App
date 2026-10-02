$ErrorActionPreference = 'Stop'
# Instalador portable JDK 21 (Microsoft) + Maven 3.9.16 — sin admin, carpeta %USERPROFILE%\devtools
$base = Join-Path $env:USERPROFILE 'devtools'
New-Item -ItemType Directory -Force -Path $base | Out-Null
Set-Location $base

Write-Host '=== [1/4] Descargando Microsoft OpenJDK 21 (zip portable) ==='
$jdkUrl = 'https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip'
$jdkZip = Join-Path $base 'jdk21.zip'
if (-not (Test-Path $jdkZip)) {
  [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
  Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing
} else { Write-Host 'ZIP JDK ya existe, reutilizando.' }
$jdkDir = Get-ChildItem $base -Directory | Where-Object { $_.Name -like 'jdk-21*' } | Select-Object -First 1
if (-not $jdkDir) {
  Write-Host 'Extrayendo JDK...'
  Expand-Archive -Path $jdkZip -DestinationPath $base -Force
  $jdkDir = Get-ChildItem $base -Directory | Where-Object { $_.Name -like 'jdk-21*' } | Select-Object -First 1
}
if (-not $jdkDir) { throw 'No se pudo extraer el JDK' }
Write-Host "JDK en: $($jdkDir.FullName)"
& "$($jdkDir.FullName)\bin\java.exe" -version

Write-Host '=== [2/4] Descargando Apache Maven 3.9.16 (zip portable) ==='
$mvnUrl = 'https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip'
$mvnZip = Join-Path $base 'maven.zip'
if (-not (Test-Path $mvnZip)) {
  Invoke-WebRequest -Uri $mvnUrl -OutFile $mvnZip -UseBasicParsing
} else { Write-Host 'ZIP Maven ya existe, reutilizando.' }
$mvnDir = Join-Path $base 'apache-maven-3.9.16'
if (-not (Test-Path $mvnDir)) {
  Write-Host 'Extrayendo Maven...'
  Expand-Archive -Path $mvnZip -DestinationPath $base -Force
}
Write-Host "Maven en: $mvnDir"
& "$mvnDir\bin\mvn.cmd" -version

Write-Host '=== [3/4] Configurando entorno de usuario (JAVA_HOME + PATH) ==='
[Environment]::SetEnvironmentVariable('JAVA_HOME', $jdkDir.FullName, 'User')
[Environment]::SetEnvironmentVariable('MAVEN_HOME', $mvnDir, 'User')
$uPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if (-not $uPath) { $uPath = '' }
$entries = @("$($jdkDir.FullName)\bin", "$mvnDir\bin")
foreach ($e in $entries) {
  if ($uPath -notlike "*$e*") { $uPath = $e + ';' + $uPath }
}
[Environment]::SetEnvironmentVariable('Path', $uPath, 'User')
$env:JAVA_HOME = $jdkDir.FullName
$env:MAVEN_HOME = $mvnDir
$env:Path = "$($jdkDir.FullName)\bin;$mvnDir\bin;" + $env:Path

Write-Host '=== [4/4] Verificacion ==='
java -version
mvn -version
Write-Host ''
Write-Host 'OK. Cierra y reabre la terminal/Antigravity para usar java/mvn en cualquier carpeta.'
Write-Host "JAVA_HOME=$($jdkDir.FullName)"
Write-Host "MAVEN_HOME=$mvnDir"
