@echo off
REM Compila y ejecuta SENATI_ZAPATO sin depender del PATH global.
REM Usa el JDK + Maven portables de %USERPROFILE%\devtools
setlocal
for /d %%D in ("%USERPROFILE%\devtools\jdk-21*") do set "JDK=%%D"
set "MVN=C:\Users\Lenovo\devtools\apache-maven-3.9.16"
if not defined JDK (
    echo ERROR: No se encontro un JDK 21 en "%USERPROFILE%\devtools".
    echo Ejecuta Instalar_JDK_Maven.bat primero.
    pause
    exit /b 1
)
if not exist "%MVN%\bin\mvn.cmd" (
    echo ERROR: No se encontro Maven en "%MVN%".
    echo Ejecuta Instalar_JDK_Maven.bat primero.
    pause
    exit /b 1
)
set "JAVA_HOME=%JDK%"
set "MAVEN_HOME=%MVN%"
set "PATH=%JDK%\bin;%MVN%\bin;%PATH%"
cd /d "%~dp0"
echo JAVA_HOME=%JAVA_HOME%
java -version
call mvn -version
if errorlevel 1 ( echo ERROR en Maven. & pause & exit /b 1 )
echo.
echo === Compilando (mvn clean package -DskipTests) ===
call mvn clean package -DskipTests
if errorlevel 1 ( echo ERROR al compilar. Revisa el .env y los errores de arriba. & pause & exit /b 1 )
echo.
echo === Ejecutando ===
java -jar "target\SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar"
pause
