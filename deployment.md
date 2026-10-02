# Guía de Despliegue: SENATI_ZAPATO

Este documento describe cómo preparar, empaquetar y desplegar la aplicación de escritorio Java **SENATI_ZAPATO**.

## Análisis de la Estructura Actual

El proyecto es una aplicación de escritorio desarrollada en **Java 22** con interfaz gráfica usando **Java Swing** y el tema **FlatLaf**. Utiliza **Maven** para la gestión de dependencias y una base de datos local **SQLite** (`senati_zapato.db`).

## Requisitos Previos
- **Java Development Kit (JDK) 22** o superior instalado.
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
Actualmente, el `pom.xml` genera un JAR estándar que no incluye las librerías externas (`flatlaf` y `sqlite-jdbc`). Para facilitar la distribución en un único archivo ejecutable, se recomienda configurar el `maven-assembly-plugin`.

**Pasos:**
1. (Si no está configurado) Agregar `maven-assembly-plugin` en la sección `<build>` del `pom.xml`.
2. Compilar el ejecutable:
   ```bash
   mvn clean compile assembly:single
   ```
3. El archivo resultante (ej: `SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar`) se generará en la carpeta `target/`.
4. El usuario final puede ejecutarlo desde la terminal:
   ```bash
   java -jar target/SENATI_ZAPATO-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

### Opción 3: Instalador Nativo para Windows (.exe / .msi) usando `jpackage`
Para una experiencia profesional en sistemas Windows (el SO objetivo predominante), se puede crear un instalador que incluya una versión reducida de Java (JRE), por lo que el usuario final no necesitará instalar Java por separado.

**Pasos Generales:**
1. Construir el proyecto y obtener el JAR con sus dependencias (Fat JAR).
2. Ejecutar la herramienta `jpackage` (incluida en el JDK 22):
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

- **Con un Fat JAR**: El archivo `.db` debe acompañar al `.jar` en el mismo directorio si la conexión JDBC apunta a una ruta relativa (`jdbc:sqlite:senati_zapato.db`).
- **Con un instalador nativo**: Es recomendable modificar la ruta de conexión a SQLite en el código fuente para apuntar al directorio de datos de la aplicación del usuario (ej. `System.getenv("APPDATA") + "\\SenatiZapato\\senati_zapato.db"`) de forma que el programa tenga permisos de escritura garantizados. En el primer inicio, si el archivo de base de datos no existe, la aplicación debe estar programada para crearlo con la estructura de tablas inicial.
