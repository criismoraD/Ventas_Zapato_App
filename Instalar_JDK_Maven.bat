@echo off
REM Instalador portable JDK 21 + Maven - doble clic, sin admin. Se instala en %USERPROFILE%\devtools
setlocal
set "BASE=%USERPROFILE%\devtools"
if not exist "%BASE%" mkdir "%BASE%"
cd /d "%BASE%"

echo === [1/4] Descargando Microsoft OpenJDK 21 (~190MB, puede tardar) ===
if exist jdk21.zip (
  echo ZIP ya existe, reutilizando...
) else (
  powershell -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip' -OutFile 'jdk21.zip' -UseBasicParsing"
  if errorlevel 1 ( echo ERROR descargando JDK. Revisa tu internet. & pause & exit /b 1 )
)
if not exist "%BASE%\jdk-21*" (
  echo Extrayendo JDK...
  powershell -Command "Expand-Archive -Path 'jdk21.zip' -DestinationPath '%BASE%' -Force"
)
for /d %%D in ("%BASE%\jdk-21*") do set "JDKDIR=%%D"
echo JDK en: %JDKDIR%
"%JDKDIR%\bin\java.exe" -version
if errorlevel 1 ( echo ERROR: no se pudo ejecutar java. & pause & exit /b 1 )

echo.
echo === [2/4] Descargando Apache Maven 3.9.16 (~10MB) ===
if exist maven.zip (
  echo ZIP ya existe, reutilizando...
) else (
  powershell -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip' -OutFile 'maven.zip' -UseBasicParsing"
  if errorlevel 1 ( echo ERROR descargando Maven. & pause & exit /b 1 )
)
if not exist "%BASE%\apache-maven-3.9.16" (
  echo Extrayendo Maven...
  powershell -Command "Expand-Archive -Path 'maven.zip' -DestinationPath '%BASE%' -Force"
)
set "MVNDIR=%BASE%\apache-maven-3.9.16"
"%MVNDIR%\bin\mvn.cmd" -version
if errorlevel 1 ( echo ERROR: no se pudo ejecutar mvn. & pause & exit /b 1 )

echo.
echo === [3/4] Configurando JAVA_HOME y PATH de usuario ===
setx JAVA_HOME "%JDKDIR%" >nul
setx MAVEN_HOME "%MVNDIR%" >nul
setx PATH "%JDKDIR%\bin;%MVNDIR%\bin;%PATH%" >nul
set "JAVA_HOME=%JDKDIR%"
set "MAVEN_HOME=%MVNDIR%"
set "PATH=%JDKDIR%\bin;%MVNDIR%\bin;%PATH%"

echo.
echo === [4/4] Verificacion ===
java -version
mvn -version
echo.
echo ========================================================
echo  OK. Ahora CIERRA y REABRE Antigravity/terminal.
echo  Luego ejecuta:  mvn -version
echo  JAVA_HOME=%JDKDIR%
echo ========================================================
pause
