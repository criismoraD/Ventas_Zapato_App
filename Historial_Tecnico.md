# Historial Técnico - SENATI ZAPATO

## Estado
- **Última modificación:** 2026-05-29
- **Versión JDK:** 22
- **Build System:** Maven (pom.xml) / Eclipse (.classpath)

## Completado

### 2026-05-31
- **Actualización de Icono de la Aplicación:**
  - Copiado `icono.jpg` a la ruta de recursos `/images/icono.jpg`.
  - Modificado `Panel_Principal.java` para usar `setIconImage()` y asignar el `icono.jpg` como icono de la ventana (JFrame).
- **Mejora del Chatbot (Navegación Híbrida Inteligente):**
  - Restaurada la lógica de navegación local ultrarrápida basada en Regex (`detectNavigationTarget`) en `Panel_De_Chatbot.java` para comandos explícitos.
  - Mantenida la herramienta `navegar_modulo` y el callback `onNavigate` en `Servicio_De_Gemini.java` como mecanismo de *Fallback*. 
  - Esto implementa una **doble validación**: si el usuario comete un error tipográfico severo (Ej: "incio") y el Regex falla, la petición se envía a la IA, la cual entiende la intención y ejecuta la navegación de todas formas invocando su *Function Call*, logrando un sistema tolerante a fallos y responsivo.
- **Corrección de Modo Voz en Vivo (WebSocket Error):**
  - Eliminada la declaración duplicada de la herramienta `navegar_modulo` en `Servicio_De_Voz_En_Vivo.java` (`buildLiveTools`), la cual causaba que la API de Gemini rechazara la conexión WebSocket con un error interno, restableciendo el correcto funcionamiento del asistente de voz.
- **Mejora Visual (Captura de Pantalla Proactiva):**
  - Ampliados los activadores (triggers) en el chat escrito (`shouldSendScreenContext`) para detectar variaciones coloquiales (ej. "qué hay en la pantalla").
  - Implementado un interceptor en la transcripción en tiempo real de `Servicio_De_Voz_En_Vivo.java` que invoca el método público `Capturar_Pantalla_Actual()` de `Panel_Principal.java` y envía la imagen a la sesión *Live* (vía `mediaChunks`) de manera invisible para el usuario. Ahora el Agente puede "ver" la interfaz tanto si se le habla como si se le escribe.
- **Modo Portable y Generación de EXE (Launch4j):**
  - Añadido `launch4j-maven-plugin` a `pom.xml` para generar el ejecutable nativo (`.exe`) con icono personalizado.
  - Modificado el almacenamiento de la base de datos (`Conexion_A_Base_De_Datos.java`) y PDFs (`Generador_De_Pdf.java`) para que se guarden en la carpeta `Datos_SenatiZapato` justo al lado del ejecutable (`user.dir`), haciéndolo 100% portable.
  - Se incluyó lógica de migración automática de DB antigua desde `user.home`.
  - **Imágenes de catálogo portables:** Al seleccionar imagen de producto en `Panel_De_Inventario.java`, ahora se copia automáticamente a `Datos_SenatiZapato/imagenes/` y se guarda solo el nombre del archivo en la DB. La carga de imágenes en `Panel_De_Ventas.java` y la preview del formulario buscan primero en la carpeta portable, luego ruta absoluta, luego recursos del classpath.

### 2026-05-30
- **Refactorización Masiva Pascal_Snake_Case y Reestructuración de Arquitectura:**
  - **Reorganización de Paquetes:** Creados los paquetes `ui`, `modelos`, `datos`, `servicios` y `utilidades` para separar la interfaz gráfica de la lógica de negocio y base de datos, eliminando la sobresaturada carpeta `db`.
  - **Renombrado General:** Convertidos **21 archivos Java** a la convención `Pascal_Snake_Case` en español solicitada (Ej. `VentasPanel.java` -> `Panel_De_Ventas.java`, `LiveVoiceService.java` -> `Servicio_De_Voz_En_Vivo.java`).
  - **Refactorización de Código:** Actualizadas todas las referencias de clases (imports y variables tipadas) a sus nuevos nombres hispanizados.
  - **Métodos y Propiedades:** Convertidos todos los métodos públicos y getters/setters a `Pascal_Snake_Case` en todas las clases y modelos (Ej. `getId()` -> `Get_Id()`, `switchTab()` -> `Cambiar_Pestana()`).
  - **Limpieza y Estabilización:** Borrado seguro de carpetas y archivos antiguos. Actualizado `pom.xml` para reflejar la nueva clase principal `Arranque_Del_Sistema`.

### 2026-05-29
- **Corrección de Glifos Invisibles y Unificación Vectorial de Iconos:**
  - **ElegantIcon.java**: Expandido el enum y la implementación con `Graphics2D` para soportar nuevos iconos vectoriales de lujo: `CASH` (billete elegante), `SAVE` (disquete clásico) y `PRINT` (impresora de POS).
  - **PagoDialog.java (Punto de Venta)**:
    - Corregidas las rutas de recursos case-sensitive de las imágenes de métodos de pago (`Plin.png`, `YAPE.png`, `Transferencia.png` y `tarjeta-bancaria.png`), solucionando las excepciones de carga offline.
    - Vinculada la forma de pago en efectivo directamente al nuevo icono vectorial nativo `CASH` (eliminando la dependencia de imágenes externas inexistentes y el fallback erróneo `HOME`).
    - Reemplazados los emojis de tarjeta `💳` y cruz `❌` de los botones de confirmación y cancelación por los iconos vectoriales de alta fidelidad `ElegantIcon.Type.CREDIT_CARD` y `CLOSE` en color blanco y neutro respectivamente. Esto soluciona los cuadrados vacíos (`[]`) causados por fuentes de sistema incompatibles con emojis en Swing.
  - **TicketDialog.java (Boleta)**: Modernizados los botones de acción inferior de la boleta (`Guardar TXT`, `Imprimir Ticket`, `Cerrar (Esc)`) reemplazando los emojis por `ElegantIcon` de 16-18px. Estos se integran al Look & Feel de FlatLaf y cambian de color de forma natural entre chocolate y crema según el tema (claro/oscuro) de la aplicación.
  - **Montos Rápidos de Efectivo Inteligentes y Dinámicos**:
    - Reemplazadas las denominaciones estáticas inútiles (como `S/ 10`, `S/ 20` cuando el total supera los S/ 400) por una base calculada dinámicamente que inicia en `S/ 200.00` o la siguiente escala lógica del total de la compra (`Math.ceil(total / 50) * 50`). Esto provee botones de cobro siempre superiores al total, asegurando su utilidad en transacciones reales.
- **Implementación de Casillas de Selección y Botón Dinámico de Selección Masiva en Inventario**:
  - **GestorPanel.java (Módulo Inventario)**:
    - Implementada una columna de casillas de verificación (`Boolean`) al extremo izquierdo de la tabla para soportar la selección masiva/individual.
    - Creado un botón reactivo y premium "Seleccionar Todo" (`btnSelectAll`) que se integra en el panel superior CRUD y que aparece y desaparece dinámicamente según haya al menos un producto seleccionado.
    - Implementado un `TableModelListener` robusto que monitorea los estados de las casillas en la columna `0` y actualiza reactivamente el botón, alternándolo automáticamente entre "Seleccionar Todo" (con icono de adición/check) y "Deseleccionar Todo" (con icono de cierre/cruz) según corresponda.
    - Ajustado el renderizador de celdas para omitir la columna `0`, delegando la visualización del checkbox directamente en el motor nativo y responsivo de FlatLaf.
    - Adaptados los índices de columna para la obtención del identificador de base de datos (`ID DB`) desplazándolos del índice `0` al índice `1` en las operaciones de edición y eliminación masiva.

### 2026-05-27
- **Corrección de Errores de Compilación y Ejecución:**
  - Actualizado `.classpath` reemplazando la ruta absoluta del repositorio Maven local que apuntaba a un usuario inexistente (`criis`) por el usuario activo (`Usuario`), solucionando errores generalizados en el IDE/VS Code.
  - Añadido el driver JDBC de SQLite (`sqlite-jdbc`) de forma explícita al `.classpath` para evitar fallos de resolución de dependencias offline.
  - Modificado `DatabaseConnection.java` para registrar dinámicamente el controlador de base de datos SQLite con `Class.forName("org.sqlite.JDBC")`, resolviendo el error runtime `No suitable driver found`.
- **Optimización Radical de Renderizado de Calzado en Catálogo (Modulo Ventas):**
  - Movidas todas las imágenes de alta resolución del calzado desde la carpeta externa `zapatos_imagenes/` a la carpeta de recursos del sistema (`src/main/resources/images/`), integrándolas en el classpath y limpiando la raíz del proyecto.
  - **Redimensionado Masivo de Imágenes:** Ejecutado script batch que redujo de manera segura las 22 imágenes gigantes del calzado (que llegaban hasta 6240x4160 px y pesaban más de 2.8MB por archivo) a un tamaño óptimo y cuadrado de **160x160 px**. Esto redujo el tamaño de los archivos a escasos kilobytes y disminuyó el consumo de RAM en la JVM de **103 MB por imagen a solo 102 KB (un ahorro del 99.9%)**.
  - Implementada una memoria caché estática (`IMAGE_CACHE`) mediante un `ConcurrentHashMap` en `VentasPanel.java` que almacena las imágenes escaladas (80x80 px) para evitar accesos redundantes al disco y decodificaciones de JPEGs.
  - Reemplazada la recarga y escalado repetido de la imagen de zapato por defecto por una versión estática en caché (`getDefaultShoeIcon`).
  - Sustituido el lanzamiento descontrolado de hilos individuales (`new Thread`) por un pool de hilos de ejecución concurrentes fijo y optimizado (`IMAGE_LOAD_EXECUTOR` con 4 workers).
  - Desarrollado un resolvedor inteligente de rutas que mapea automáticamente rutas relativas antiguas (`zapatos_imagenes/...`) a recursos de classpath integrados (`/images/...`) de manera dinámica sin necesidad de alterar los registros existentes de la base de datos local SQLite.

### 2026-05-26
- **Rediseño Estético Completo (Estilo Cuero y Costuras):**
  - Generadas imágenes con IA (fondo de cuero liso 16:9 con detalles laterales, imágenes de catálogo y avatar). Reemplazo de textura de fondo principal a una más sutil.
  - Arreglados errores de compilación (`cannot find symbol` y visibilidad de `NAV_BG`) importando librerías gráficas faltantes y ajustando modificadores de acceso en `PANEL_PRINCIPAL`.
  - `PANEL_PRINCIPAL.java` y `TabButton.java`: Creada textura de cuero de fondo, barra superior crema con pestaña seleccionada en Georgia con indicador terracota e inactive en café.
  - Eliminado `VectorIcon.java` por solicitud y purgadas las referencias restantes en `VentasPanel.java` (reemplazado por emoji nativo).
  - Nuevo fondo de pantalla generado (`bg_parchment_desk`) imitando la referencia con tela, madera y textura suave.
  - Generado avatar estilo Toon/Pixar (robot bronce simpaticón sin gorro) para Chatbot. Fondo verde removido vía chroma-key. Botón flotante: fondo terracota con borde crema, icono 50px.
  - Añadidos iconos (emojis) al panel superior de navegación en `PANEL_PRINCIPAL`.
  - Aumentado significativamente el tamaño de fuente en los títulos y descripciones de las tarjetas (`InicioPanel`) y en los globos/textos del chatbot (`ChatbotPanel`).
  - Implementación de Chatbot Flotante con Drag & Drop global usando coordenadas de pantalla (`getXOnScreen`). Animación Fade-In+Scale-Up desde esquina inferior derecha. Drop Shadow difuminado y esquinas redondeadas.
  - Corrección visual del Chatbot: Scroll visual nativo ocultado (width=0). Auto-scroll fiable mediante doble `invokeLater` y referencia directa a `chatScroll` como campo de instancia. Flecha hacia abajo interactiva como botón de cierre. Marco decorativo (stitching) de cuero.
  - Fondo general restaurado: Revertido el `contentPanel` a panel transparente simple.
  - Tarjetas de `InicioPanel` rediseñadas y escaladas: Aplicada proporción 1.4x1 (380x532), imagen de portada expandida significativamente (a 320px) para evitar recortes, y mayor espaciado vertical (`subY += 24`) para mejorar la legibilidad de la descripción en la zona inferior.
  - Sombra del navegador superior: `topNavBar` modificado para tener 80px de altura y reservar los últimos 10px inferiores para renderizar una sombra difuminada progresiva proyectada hacia el contenido (evitando recorte por los límites de bounds del componente Swing).
  - Cabecera del Chatbot: Eliminado círculo blanco detrás del avatar, texto "Asistente Virtual" centrado verticalmente con `FontMetrics`. Icono flotante agrandado a 80px/96px sin fondo circular.
  - Corregido el renderizado de iconos en las pestañas (`TabButton.java`) mediante una implementación de `Icon` personalizada forzando la fuente `Segoe UI Emoji`, eliminando los cuadrados (caracteres no soportados en Georgia).
  - Ajustado el `Margin` del botón cerrar (esquina superior derecha) a `0` para que la "X" se dibuje completa y no como "...".
  - `VentasPanel.java`, `GestorPanel.java`, `ReportesPanel.java`: Tablas y paneles estilo marfil, botones de cobro en verde oliva premium y tarjetas de productos con relieve y costuras.

### Tareas Antiguas (Resumen)
- **Eliminación de VentasFrame.java:**
  - Clase `JFrame` duplicada de `VentasPanel`, no referenciada por ningún otro archivo
  - Eliminado `VentasFrame.java` (351 líneas) + `VentasFrame.form`
  - Compilación limpia: 9 archivos, 0 errores
