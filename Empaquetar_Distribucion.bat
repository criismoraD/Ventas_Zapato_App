@echo off
setlocal
echo ========================================================
echo   Empaquetador de Distribucion Portable - Sello Masculino
echo ========================================================

set "DIST_DIR=Distribucion_Portable"
set "DATA_DIR=%DIST_DIR%\Datos_SenatiZapato"
set "OLD_DATA_DIR=%USERPROFILE%\SenatiZapato"

:: Crear directorios
if exist "%DIST_DIR%" rd /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%"
mkdir "%DATA_DIR%"
mkdir "%DATA_DIR%\imagenes"
mkdir "%DATA_DIR%\comprobantes"

:: 1. Copiar el Ejecutable
echo.
echo [1/3] Copiando ejecutable...
if exist "target\SENATI_ZAPATO.exe" (
    copy "target\SENATI_ZAPATO.exe" "%DIST_DIR%\" >nul
    echo   OK - SENATI_ZAPATO.exe copiado.
) else (
    echo   ERROR: No se encontro target\SENATI_ZAPATO.exe. Compila primero el proyecto.
    goto end
)

:: 2. Copiar Datos Locales Nuevos (si existen)
echo.
echo [2/3] Buscando base de datos portable...
if exist "Datos_SenatiZapato\senati_zapato.db" (
    copy "Datos_SenatiZapato\senati_zapato.db" "%DATA_DIR%\" >nul
    echo   OK - DB Portable copiada.
) else (
    :: Si no hay db portable, buscar la antigua en user.home
    echo   No hay DB portable. Buscando DB antigua...
    if exist "%OLD_DATA_DIR%\senati_zapato.db" (
        copy "%OLD_DATA_DIR%\senati_zapato.db" "%DATA_DIR%\" >nul
        echo   OK - DB Antigua migrada a la distribucion.
    ) else (
        echo   AVISO: No se encontro ninguna base de datos. Se generara una nueva vacia al abrir el EXE.
    )
)

:: 3. Copiar Imagenes (si existen en modo portable)
echo.
echo [3/3] Copiando imagenes...
if exist "Datos_SenatiZapato\imagenes\*" (
    xcopy "Datos_SenatiZapato\imagenes\*" "%DATA_DIR%\imagenes\" /s /e /q /y >nul
    echo   OK - Imagenes copiadas.
) else (
    echo   AVISO: No hay imagenes en la carpeta portable aun.
)

echo.
echo ========================================================
echo   EMPAQUETADO FINALIZADO
echo ========================================================
echo.
echo Puedes encontrar tu programa listo para llevar en la carpeta:
echo %CD%\%DIST_DIR%
echo.
echo Copia la carpeta "%DIST_DIR%" entera a tu USB.
echo.
pause
