@echo off
REM Compila y ejecuta SENATI_ZAPATO sin depender del PATH global.
REM Usa el JDK + Maven portables de %USERPROFILE%\devtools
setlocal
set "JDK=C:\Users\Lenovo\devtools\jdk-21.0.12.1+1"
set "MVN=C:\Users\Lenovo\devtools\apache-maven-3.9.16"
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
