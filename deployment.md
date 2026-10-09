# Guía de Despliegue: SENATI_ZAPATO

Este documento describe cómo preparar, empaquetar y desplegar la aplicación de escritorio Java **SENATI_ZAPATO**.

## Análisis de la Estructura Actual

El proyecto es una aplicación de escritorio desarrollada en **Java 21** con interfaz gráfica usando **Java Swing** y el tema **FlatLaf**. Utiliza **Maven** para la gestión de dependencias y una base de datos local **SQLite** (`senati_zapato.db`).

## Requisitos Previos
- **Java Development Kit (JDK) 21** o superior instalado.
- **Apache Maven** instalado.
- Sistema Operativo: Entorno compatible con Java (Windows, Linux, macOS), aunque el instalador se orientará a Windows.

---

## Opciones de Despliegue

### Opción 1: Ejecución Directa (Desarrollo / Pruebas)
Para ejecutar la aplicación localmente en el entorno de desarrollo, utiliza el plugin de ejecución de Maven:
```bash
mvn clean compile exec:java
```

### Opción 2: Empaquetado en un "Fat JAR" (Distribución Simple)
El `pom.xml` configura `maven-assembly-plugin` para generar un JAR con las dependencias incluidas.

**Pasos:**
1. Compilar el ejecutable:
   ```bash
   mvn clean compile assembly:single
   ```
2. El archivo resultante (ej: `SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar`) se generará en la carpeta `target/`.
3. El usuario final puede ejecutarlo desde la terminal:
   ```bash
   java -jar target/SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### Opción 3: Instalador Nativo para Windows (.exe / .msi) usando `jpackage`
Para una experiencia profesional en sistemas Windows (el SO objetivo predominante), se puede crear un instalador que incluya una versión reducida de Java (JRE), por lo que el usuario final no necesitará instalar Java por separado.

**Pasos Generales:**
1. Construir el proyecto y obtener el JAR con sus dependencias (Fat JAR).
2. Ejecutar la herramienta `jpackage` (incluida en el JDK 21):
   ```bash
   jpackage --type exe \
            --name "SenatiZapato" \
            --input target/ \
            --main-jar SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar \
            --main-class com.mycompany.senati_zapato.Arranque_Del_Sistema \
            --win-shortcut \
            --win-menu
   ```
3. Se generará un instalador `SenatiZapato-1.0.exe` que instalará la aplicación en los Archivos de Programa de Windows.

---

## Gestión de la Base de Datos (`senati_zapato.db`)

La persistencia de la aplicación se maneja mediante SQLite de manera local.

- La aplicación usa `Datos_SenatiZapato\\senati_zapato.db` relativo al directorio de ejecución.
- Si no existe esa base, en el primer inicio puede migrar una copia desde la raíz del proyecto o desde `%USERPROFILE%\\SenatiZapato`.
- Cuando la base portable ya existe, no se sobrescribe automáticamente con otra copia. Esto evita perder ventas o cambios del inventario.
