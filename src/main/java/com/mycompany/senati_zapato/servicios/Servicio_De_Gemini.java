package com.mycompany.senati_zapato.servicios;

import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.ui.Gestor_De_Temas;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Servicio_De_Gemini {

    private Consumer<String> onNavigate;
    private java.util.function.BiFunction<String, Integer, String> onAddCart;
    private java.util.function.Supplier<String> onCheckout;
    private java.util.function.Supplier<String> onCancel;
    private java.util.function.Consumer<Boolean> onSetDarkMode;

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void setOnAddCart(java.util.function.BiFunction<String, Integer, String> onAddCart) {
        this.onAddCart = onAddCart;
    }

    public void setOnCheckout(java.util.function.Supplier<String> onCheckout) {
        this.onCheckout = onCheckout;
    }

    public void setOnCancel(java.util.function.Supplier<String> onCancel) {
        this.onCancel = onCancel;
    }

    public void setOnSetDarkMode(java.util.function.Consumer<Boolean> onSetDarkMode) {
        this.onSetDarkMode = onSetDarkMode;
    }

    // =========================================================================
    // ÁRBOL BINARIO DE BÚSQUEDA (BST) — indexa productos por código SKU
    // Permite búsqueda en O(log n) en lugar de O(n) con for plano
    // =========================================================================
    private static class NodoBST {
        Producto producto;
        NodoBST izquierdo, derecho;
        NodoBST(Producto producto) { this.producto = producto; }
    }

    private static class ArbolBST_Productos {
        private NodoBST raiz;

        /** Inserta un producto ordenado por código SKU (ignorando mayúsculas) */
        public void insertar(Producto p) {
            raiz = insertarRec(raiz, p);
        }

        private NodoBST insertarRec(NodoBST nodo, Producto p) {
            if (nodo == null) return new NodoBST(p);
            String codigoNuevo = p.Get_Codigo() != null ? p.Get_Codigo().toLowerCase() : "";
            String codigoNodo  = nodo.producto.Get_Codigo() != null ? nodo.producto.Get_Codigo().toLowerCase() : "";
            int cmp = codigoNuevo.compareTo(codigoNodo);
            if (cmp < 0)
                nodo.izquierdo = insertarRec(nodo.izquierdo, p);
            else if (cmp > 0)
                nodo.derecho = insertarRec(nodo.derecho, p);
            // Si cmp == 0 (mismo código) no se duplica
            return nodo;
        }

        /**
         * Búsqueda exacta por código SKU — O(log n)
         * Retorna el producto si lo encuentra, null si no existe
         */
        public Producto buscarPorCodigo(String codigo) {
            return buscarRec(raiz, codigo.toLowerCase());
        }

        private Producto buscarRec(NodoBST nodo, String codigo) {
            if (nodo == null) return null;
            String codigoNodo = nodo.producto.Get_Codigo() != null ? nodo.producto.Get_Codigo().toLowerCase() : "";
            int cmp = codigo.compareTo(codigoNodo);
            if (cmp == 0) return nodo.producto;
            if (cmp < 0)  return buscarRec(nodo.izquierdo, codigo);
            return buscarRec(nodo.derecho, codigo);
        }

        /**
         * Recorrido INORDER del BST — devuelve todos los productos ordenados por código
         * Usado en búsquedas parciales: recorre el árbol y filtra por nombre/categoría
         */
        public void inorder(NodoBST nodo, List<Producto> resultado) {
            if (nodo == null) return;
            inorder(nodo.izquierdo, resultado);   // visitar subárbol izquierdo
            resultado.add(nodo.producto);          // visitar nodo actual
            inorder(nodo.derecho, resultado);      // visitar subárbol derecho
        }

        public NodoBST getRaiz() { return raiz; }
    }

    // =========================================================================
    // ÁRBOL DE CATEGORÍAS — organiza productos en nodos por categoría
    // El recorrido transversal (inorder por nombre) genera el listado agrupado
    // =========================================================================
    private static class NodoCategoria {
        String nombre;
        List<Producto> productos;
        NodoCategoria izquierdo, derecho; // BST de categorías ordenado alfabéticamente

        NodoCategoria(String nombre) {
            this.nombre = nombre;
            this.productos = new ArrayList<>();
        }
    }

    private static class ArbolCategorias {
        private NodoCategoria raiz;

        /** Inserta una categoría en el árbol ordenado alfabéticamente */
        public void insertarCategoria(String nombre) {
            raiz = insertarCatRec(raiz, nombre);
        }

        private NodoCategoria insertarCatRec(NodoCategoria nodo, String nombre) {
            if (nodo == null) return new NodoCategoria(nombre);
            int cmp = nombre.compareToIgnoreCase(nodo.nombre);
            if (cmp < 0)
                nodo.izquierdo = insertarCatRec(nodo.izquierdo, nombre);
            else if (cmp > 0)
                nodo.derecho = insertarCatRec(nodo.derecho, nombre);
            return nodo;
        }

        /** Asigna cada producto al nodo de su categoría */
        public void asignarProducto(Producto p) {
            asignarRec(raiz, p);
        }

        private void asignarRec(NodoCategoria nodo, Producto p) {
            if (nodo == null) return;
            if (nodo.nombre.equalsIgnoreCase(p.Get_Categoria())) {
                nodo.productos.add(p);
                return;
            }
            int cmp = (p.Get_Categoria() != null ? p.Get_Categoria() : "").compareToIgnoreCase(nodo.nombre);
            if (cmp < 0) asignarRec(nodo.izquierdo, p);
            else         asignarRec(nodo.derecho, p);
        }

        /**
         * Recorrido INORDER del árbol de categorías
         * Genera el listado agrupado y ordenado alfabéticamente por categoría
         */
        public void inorderCategorias(NodoCategoria nodo, StringBuilder sb, String filtro) {
            if (nodo == null) return;
            inorderCategorias(nodo.izquierdo, sb, filtro);  // subárbol izquierdo
            // Visitar nodo actual: agregar productos de esta categoría según filtro
            for (Producto p : nodo.productos) {
                boolean incluir = false;
                switch (filtro.toLowerCase()) {
                    case "disponibles":  incluir = p.Get_Stock() > 5 && "Disponible".equalsIgnoreCase(p.Get_Estado()); break;
                    case "bajo_stock":   incluir = p.Get_Stock() > 0 && p.Get_Stock() <= 5; break;
                    case "agotados":     incluir = p.Get_Stock() == 0; break;
                    default:             incluir = true;
                }
                if (incluir) {
                    sb.append("[").append(nodo.nombre).append("] ")
                      .append(p.Get_Nombre())
                      .append(" | Código: ").append(p.Get_Codigo())
                      .append(" | Stock: ").append(p.Get_Stock())
                      .append(" | S/ ").append(String.format("%.2f", p.Get_Precio()))
                      .append(" | Estado: ").append(p.Get_Estado()).append("\n");
                }
            }
            inorderCategorias(nodo.derecho, sb, filtro);    // subárbol derecho
        }

        /** Cuenta productos que cumplen el filtro recorriendo el árbol */
        public int contarFiltrados(NodoCategoria nodo, String filtro) {
            if (nodo == null) return 0;
            int count = 0;
            for (Producto p : nodo.productos) {
                switch (filtro.toLowerCase()) {
                    case "disponibles": if (p.Get_Stock() > 5 && "Disponible".equalsIgnoreCase(p.Get_Estado())) count++; break;
                    case "bajo_stock":  if (p.Get_Stock() > 0 && p.Get_Stock() <= 5) count++; break;
                    case "agotados":    if (p.Get_Stock() == 0) count++; break;
                    default:            count++; break;
                }
            }
            return count + contarFiltrados(nodo.izquierdo, filtro) + contarFiltrados(nodo.derecho, filtro);
        }

        public NodoCategoria getRaiz() { return raiz; }
    }

    // =========================================================================
    // API KEY — se lee desde entorno / -Dpropiedad / archivo .env (ver Configuracion).
    // Nunca hardcodear keys en el código.
    // =========================================================================

    public static String Obtener_Api_Key() {
        return com.mycompany.senati_zapato.utilidades.Configuracion.Obtener_Gemini_Api_Key();
    }

    /** @deprecated Usa {@link #Obtener_Api_Key()} (lee de GEMINI_API_KEY en entorno/.env). */
    @Deprecated
    public static final String GEMINI_API_KEY = null;

    private static final String SYSTEM_PROMPT =
        "Eres el asistente virtual interno de Sello Masculino, zapatería de caballero. " +
        "Ayudas a empleados y cajeros a usar el sistema de punto de venta. " +
        "Puedes:\n" +
        "- Buscar productos por nombre, código o categoría\n" +
        "- Verificar stock y precios\n" +
        "- Consultar ventas, reportes y KPIs del día directamente desde la base de datos\n" +
        "- Revisar tablas de la base de datos con consultas SELECT seguras\n" +
        "- Registrar nuevos productos en el inventario\n" +
        "- Listar productos disponibles o con stock bajo\n" +
        "- Entender la pantalla actual cuando recibes una captura como contexto\n" +
        "- Explicar cómo funciona el sistema (ventas, reportes, gestión)\n" +
        "- Responder preguntas generales sobre atención al cliente\n\n" +
        "Si preguntan cuánto se vendió hoy, ventas recientes, productos más vendidos, " +
        "stock o información de la base de datos, usa tus herramientas antes de responder. " +
        "No digas que no tienes acceso a reportes: sí puedes consultar la base de datos. " +
        "Si el usuario te pide añadir, poner o agregar un producto al carrito, usa obligatoriamente la herramienta agregar_carrito.\n" +
        "Si te envían una imagen o captura, úsala como referencia visual temporal para responder.\n" +
        "Categorías válidas: Mocasines, Botas, Zapatillas, Sandalias, Deportivos, Urbanos, Elegantes.\n" +
        "Tallas por defecto si no se especifican: 38,39,40,41,42.\n" +
        "Responde en español con máximo 2 frases cortas. " +
        "No uses Markdown, no uses asteriscos, no uses negritas y no hagas listas largas. " +
        "Si el usuario pide ir, cambiar o navegar a Ventas, Inventario, Reportes o Inicio, usa obligatoriamente la herramienta navegar_modulo y responde confirmando. " +
        "Si el usuario te pide cobrar o pagar, usa la herramienta procesar_pago. Si pide cancelar o vaciar carrito, usa la herramienta cancelar_orden. " +
        "Si el usuario pide activar o desactivar el modo oscuro, modo noche o modo claro, usa la herramienta cambiar_modo_oscuro.";

    private Client client;
    // Modelo efectivo actual (principal o fallback vigente). Se re-lee de Configuracion al reconectar.
    private String model;
    // Historial SOLO de la llamada en curso (LLM stateless por llamada, sin bot interno persistente).
    // Se descarta al terminar cada mensaje para no arrastrar contexto viejo ni entrenamientos.
    private final List<Content> history = new ArrayList<>();
    private List<Tool> tools;
    private final Dao_De_Producto productoDAO;
    private final Dao_De_Venta ventaDAO;
    private String apiKeyError;

    public Servicio_De_Gemini() {
        this.productoDAO = new Dao_De_Producto();
        this.ventaDAO = new Dao_De_Venta();
        Reconectar();
    }

    /** (Re)lee API key + modelos desde Configuracion y reconstruye client/tools. */
    public final void Reconectar() {
        String keyOrError;
        Client c = null;
        String err = null;
        try {
            keyOrError = Obtener_Api_Key();
            c = Client.builder().apiKey(keyOrError).build();
        } catch (IllegalStateException ex) {
            err = ex.getMessage();
            System.err.println("[Gemini] " + err);
        }
        this.client = c;
        this.apiKeyError = err;
        this.model = com.mycompany.senati_zapato.utilidades.Configuracion.Obtener_Modelo_Chat();
        this.history.clear();
        this.tools = Construir_Herramientas();
    }



    public List<Tool> Construir_Herramientas() {
        List<Tool> toolList = new ArrayList<>();

        // buscar_producto
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("buscar_producto")
                        .description("Busca productos por nombre, código o categoría")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "query", Schema.builder().type(new Type(Type.Known.STRING)).description("Término de búsqueda (nombre, código o categoría)").build()
                                ))
                                .required(List.of("query"))
                                .build())
                        .build())
                .build());

        // verificar_stock
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("verificar_stock")
                        .description("Verifica el stock y precio de un producto específico por su código o nombre")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "producto", Schema.builder().type(new Type(Type.Known.STRING)).description("Nombre o código del producto").build()
                                ))
                                .required(List.of("producto"))
                                .build())
                        .build())
                .build());

        // listar_productos
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("listar_productos")
                        .description("Lista productos. Puede filtrar por estado: todos, disponibles, bajo_stock, agotados")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "filtro", Schema.builder().type(new Type(Type.Known.STRING)).description("Filtro: todos, disponibles, bajo_stock, agotados").build()
                                ))
                                .required(List.of("filtro"))
                                .build())
                        .build())
                .build());

        // agregar_producto
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("agregar_producto")
                        .description("Registra un nuevo producto en el inventario de la base de datos")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "codigo", Schema.builder().type(new Type(Type.Known.STRING)).description("Código único del producto (SKU)").build(),
                                        "nombre", Schema.builder().type(new Type(Type.Known.STRING)).description("Nombre del producto").build(),
                                        "categoria", Schema.builder().type(new Type(Type.Known.STRING)).description("Categoría: Mocasines, Botas, Zapatillas, Sandalias, Deportivos, Urbanos, Elegantes").build(),
                                        "stock", Schema.builder().type(new Type(Type.Known.INTEGER)).description("Cantidad en stock").build(),
                                        "precio", Schema.builder().type(new Type(Type.Known.NUMBER)).description("Precio en soles (S/)").build(),
                                        "tallas", Schema.builder().type(new Type(Type.Known.STRING)).description("Tallas disponibles separadas por coma. Ej: 38,39,40,41,42").build(),
                                        "url_imagen", Schema.builder().type(new Type(Type.Known.STRING)).description("Ruta o URL de la imagen del producto (opcional)").build()
                                ))
                                .required(List.of("codigo", "nombre", "categoria", "stock", "precio"))
                                .build())
                        .build())
                .build());

        // ventas_hoy
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("ventas_hoy")
                        .description("Obtiene ingresos, pares vendidos y cantidad de transacciones de hoy")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of())
                                .build())
                        .build())
                .build());

        // ventas_recientes
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("ventas_recientes")
                        .description("Lista las ventas más recientes con fecha, cajero, total, estado y método de pago")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "limite", Schema.builder().type(new Type(Type.Known.INTEGER)).description("Cantidad máxima de ventas a listar").build()
                                ))
                                .build())
                        .build())
                .build());

        // detalle_venta
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("detalle_venta")
                        .description("Muestra los productos vendidos en una venta específica")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "venta_id", Schema.builder().type(new Type(Type.Known.INTEGER)).description("ID de la venta").build()
                                ))
                                .required(List.of("venta_id"))
                                .build())
                        .build())
                .build());

        // top_productos
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("top_productos")
                        .description("Obtiene los productos más vendidos")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "limite", Schema.builder().type(new Type(Type.Known.INTEGER)).description("Cantidad máxima de productos").build()
                                ))
                                .build())
                        .build())
                .build());

        // ventas_ultimos_7_dias
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("ventas_ultimos_7_dias")
                        .description("Resume las ventas totales de los últimos 7 días")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of())
                                .build())
                        .build())
                .build());

        // resumen_base_datos
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("resumen_base_datos")
                        .description("Muestra tablas disponibles y conteos principales de la base de datos")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of())
                                .build())
                        .build())
                .build());

        // consultar_base_datos
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("consultar_base_datos")
                        .description("Ejecuta una consulta SQL segura de solo lectura (SELECT) sobre SQLite")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "sql", Schema.builder().type(new Type(Type.Known.STRING)).description("Consulta SELECT. No se permiten INSERT, UPDATE, DELETE, DROP ni PRAGMA").build(),
                                        "limite", Schema.builder().type(new Type(Type.Known.INTEGER)).description("Máximo de filas a devolver").build()
                                ))
                                .required(List.of("sql"))
                                .build())
                        .build())
                .build());

        // navegar_modulo
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("navegar_modulo")
                        .description("Cambia la vista de la aplicación al módulo especificado por el usuario.")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "modulo", Schema.builder().type(new Type(Type.Known.STRING)).description("Módulo destino. Valores permitidos: Inicio, Ventas, Gestor, Reportes").build()
                                ))
                                .required(List.of("modulo"))
                                .build())
                        .build())
                .build());

        // agregar_carrito
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("agregar_carrito")
                        .description("Agrega un producto al carrito de compras del módulo Ventas")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "producto", Schema.builder().type(new Type(Type.Known.STRING)).description("Nombre o código del producto a agregar").build(),
                                        "cantidad", Schema.builder().type(new Type(Type.Known.INTEGER)).description("Cantidad a agregar (por defecto 1)").build()
                                ))
                                .required(List.of("producto"))
                                .build())
                        .build())
                .build());

        // procesar_pago
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("procesar_pago")
                        .description("Abre el panel de cobro/pago para finalizar la venta actual")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of())
                                .build())
                        .build())
                .build());

        // cancelar_orden
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("cancelar_orden")
                        .description("Vacía el carrito de compras, cancelando la orden actual")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of())
                                .build())
                        .build())
                .build());

        // cambiar_modo_oscuro
        toolList.add(Tool.builder()
                .functionDeclarations(FunctionDeclaration.builder()
                        .name("cambiar_modo_oscuro")
                        .description("Activa o desactiva el modo oscuro o modo noche en el sistema")
                        .parameters(Schema.builder()
                                .type(new Type(Type.Known.OBJECT))
                                .properties(Map.of(
                                        "activar", Schema.builder().type(new Type(Type.Known.BOOLEAN)).description("True para activar modo oscuro, false para desactivarlo").build()
                                ))
                                .required(List.of("activar"))
                                .build())
                        .build())
                .build());

        return toolList;
    }

    public String Ejecutar_Funcion(String name, Map<String, Object> args) {
        switch (name) {
            case "buscar_producto": {
                String query = (String) args.get("query");
                if (query == null) return "Error: falta el parámetro query";
                List<Producto> todos = productoDAO.Obtener_Todos();

                // --- ÁRBOL BINARIO DE BÚSQUEDA ---
                // 1. Construir el BST con todos los productos (ordenado por código SKU)
                ArbolBST_Productos bst = new ArbolBST_Productos();
                for (Producto p : todos) bst.insertar(p);

                // 2. Intentar búsqueda exacta por código SKU en O(log n)
                Producto exacto = bst.buscarPorCodigo(query);
                if (exacto != null) {
                    return "Se encontraron 1 producto(s):\n"
                        + "- " + exacto.Get_Nombre()
                        + " | Código: " + exacto.Get_Codigo()
                        + " | Stock: " + exacto.Get_Stock()
                        + " | Precio: S/ " + String.format("%.2f", exacto.Get_Precio())
                        + " | Estado: " + exacto.Get_Estado() + "\n";
                }

                // 3. Si no hay coincidencia exacta, recorrer el BST en INORDER
                //    para obtener todos los productos ordenados y filtrar por nombre/categoría
                List<Producto> ordenados = new ArrayList<>();
                bst.inorder(bst.getRaiz(), ordenados); // recorrido transversal inorder

                List<Producto> encontrados = new ArrayList<>();
                String q = query.toLowerCase();
                for (Producto p : ordenados) {
                    if (p.Get_Nombre().toLowerCase().contains(q)
                            || (p.Get_Codigo() != null && p.Get_Codigo().toLowerCase().contains(q))
                            || (p.Get_Categoria() != null && p.Get_Categoria().toLowerCase().contains(q))) {
                        encontrados.add(p);
                    }
                }
                if (encontrados.isEmpty()) return "No se encontraron productos con: " + query;
                StringBuilder sb = new StringBuilder();
                sb.append("Se encontraron ").append(encontrados.size()).append(" producto(s):\n");
                for (Producto p : encontrados) {
                    sb.append("- ").append(p.Get_Nombre())
                      .append(" | Código: ").append(p.Get_Codigo())
                      .append(" | Stock: ").append(p.Get_Stock())
                      .append(" | Precio: S/ ").append(String.format("%.2f", p.Get_Precio()))
                      .append(" | Estado: ").append(p.Get_Estado()).append("\n");
                }
                return sb.toString();
            }
            case "verificar_stock": {
                String prod = (String) args.get("producto");
                if (prod == null) return "Error: falta el parámetro producto";
                List<Producto> todos = productoDAO.Obtener_Todos();

                // --- ÁRBOL BINARIO DE BÚSQUEDA ---
                // Construir BST y buscar primero por código exacto en O(log n)
                ArbolBST_Productos bst = new ArbolBST_Productos();
                for (Producto p : todos) bst.insertar(p);

                Producto encontrado = bst.buscarPorCodigo(prod);

                // Si no hay coincidencia exacta por código, recorrer inorder y buscar por nombre
                if (encontrado == null) {
                    List<Producto> ordenados = new ArrayList<>();
                    bst.inorder(bst.getRaiz(), ordenados);
                    String q = prod.toLowerCase();
                    for (Producto p : ordenados) {
                        if (p.Get_Nombre().toLowerCase().contains(q)) {
                            encontrado = p;
                            break;
                        }
                    }
                }

                if (encontrado == null) return "No se encontró el producto: " + prod;
                return "Producto: " + encontrado.Get_Nombre() + "\n"
                        + "Código: " + encontrado.Get_Codigo() + "\n"
                        + "Stock: " + encontrado.Get_Stock() + " pares\n"
                        + "Precio: S/ " + String.format("%.2f", encontrado.Get_Precio()) + "\n"
                        + "Tallas: " + encontrado.Get_Tallas() + "\n"
                        + "Estado: " + encontrado.Get_Estado();
            }
            case "listar_productos": {
                String filtro = (String) args.get("filtro");
                if (filtro == null) filtro = "todos";
                List<Producto> todos = productoDAO.Obtener_Todos();

                // --- ÁRBOL DE CATEGORÍAS + RECORRIDO TRANSVERSAL INORDER ---
                // 1. Construir el árbol de categorías (BST ordenado alfabéticamente)
                ArbolCategorias arbolCat = new ArbolCategorias();
                String[] categorias = {"Botas", "Deportivos", "Elegantes", "Mocasines", "Sandalias", "Urbanos", "Zapatillas"};
                for (String cat : categorias) arbolCat.insertarCategoria(cat);

                // 2. Asignar cada producto a su nodo de categoría
                for (Producto p : todos) arbolCat.asignarProducto(p);

                // 3. Recorrido INORDER del árbol: genera listado agrupado y ordenado por categoría
                StringBuilder sb = new StringBuilder();
                int total = arbolCat.contarFiltrados(arbolCat.getRaiz(), filtro);
                if (total == 0) return "No hay productos con filtro: " + filtro;
                sb.append("Productos (").append(filtro).append("): ").append(total).append("\n");
                arbolCat.inorderCategorias(arbolCat.getRaiz(), sb, filtro);
                return sb.toString();
            }
            case "agregar_producto": {
                String codigo = (String) args.get("codigo");
                String nombre = (String) args.get("nombre");
                String categoria = (String) args.get("categoria");
                Number stock = (Number) args.get("stock");
                Number precio = (Number) args.get("precio");
                String tallas = args.containsKey("tallas") ? (String) args.get("tallas") : "38,39,40,41,42";
                String urlImagen = args.containsKey("url_imagen") ? (String) args.get("url_imagen") : "";

                if (codigo == null || nombre == null || categoria == null || stock == null || precio == null) {
                    return "Error: faltan campos obligatorios (codigo, nombre, categoria, stock, precio)";
                }

                Producto p = new Producto(0, codigo, nombre, categoria, stock.intValue(), precio.doubleValue(), tallas, urlImagen);
                try {
                    productoDAO.insertar(p);
                    return "Producto registrado exitosamente:\n"
                            + "- Nombre: " + nombre + "\n"
                            + "- Código: " + codigo + "\n"
                            + "- Categoría: " + categoria + "\n"
                            + "- Stock: " + stock.intValue() + " pares\n"
                            + "- Precio: S/ " + String.format("%.2f", precio.doubleValue());
                } catch (Exception e) {
                    return "Error al registrar producto: " + e.getMessage();
                }
            }
            case "ventas_hoy": {
                double[] kpis = ventaDAO.obtenerKpisDelDia();
                return "Ventas de hoy:\n"
                        + "- Ingresos: S/ " + String.format("%.2f", kpis[0]) + "\n"
                        + "- Pares vendidos: " + (int) kpis[1] + "\n"
                        + "- Transacciones: " + (int) kpis[2];
            }
            case "ventas_recientes": {
                int limite = getIntArg(args, "limite", 5);
                limite = Math.max(1, Math.min(limite, 20));
                List<Venta> ventas = ventaDAO.Obtener_Ventas_Recientes(limite);
                if (ventas.isEmpty()) return "No hay ventas registradas.";
                StringBuilder sb = new StringBuilder("Ventas recientes:\n");
                for (Venta v : ventas) {
                    sb.append("- #").append(v.Get_Id())
                      .append(" | ").append(v.getFechaHora())
                      .append(" | Cajero: ").append(v.Get_Cajero())
                      .append(" | Total: S/ ").append(String.format("%.2f", v.Get_Monto_Total()))
                      .append(" | Pago: ").append(v.Get_Metodo_Pago())
                      .append(" | Estado: ").append(v.Get_Estado()).append("\n");
                }
                return sb.toString();
            }
            case "detalle_venta": {
                int ventaId = getIntArg(args, "venta_id", -1);
                if (ventaId <= 0) return "Error: falta venta_id válido";
                List<Object[]> detalles = ventaDAO.obtenerDetallesPorVenta(ventaId);
                if (detalles.isEmpty()) return "No se encontraron detalles para la venta #" + ventaId;
                StringBuilder sb = new StringBuilder("Detalle de venta #").append(ventaId).append(":\n");
                for (Object[] d : detalles) {
                    sb.append("- ").append(d[0])
                      .append(" | Cantidad: ").append(d[2])
                      .append(" | Precio: S/ ").append(String.format("%.2f", (Double) d[1]))
                      .append(" | Subtotal: S/ ").append(String.format("%.2f", (Double) d[3])).append("\n");
                }
                return sb.toString();
            }
            case "top_productos": {
                int limite = getIntArg(args, "limite", 5);
                limite = Math.max(1, Math.min(limite, 20));
                List<Object[]> top = ventaDAO.Obtener_Top_Productos(limite);
                if (top.isEmpty()) return "Aún no hay productos vendidos para calcular el top.";
                StringBuilder sb = new StringBuilder("Productos más vendidos:\n");
                for (Object[] row : top) {
                    sb.append("- ").append(row[1])
                      .append(" | Código: ").append(row[0])
                      .append(" | Categoría: ").append(row[2])
                      .append(" | Vendidos: ").append(row[3]).append("\n");
                }
                return sb.toString();
            }
            case "ventas_ultimos_7_dias": {
                List<Object[]> rows = ventaDAO.obtenerVentasUltimos7Dias();
                if (rows.isEmpty()) return "No hay ventas registradas en los últimos 7 días.";
                StringBuilder sb = new StringBuilder("Ventas de los últimos 7 días:\n");
                for (Object[] row : rows) {
                    sb.append("- ").append(row[0])
                      .append(": S/ ").append(String.format("%.2f", (Double) row[1])).append("\n");
                }
                return sb.toString();
            }
            case "resumen_base_datos": {
                return buildDatabaseSummary();
            }
            case "consultar_base_datos": {
                String sql = (String) args.get("sql");
                int limite = getIntArg(args, "limite", 20);
                return executeReadOnlyQuery(sql, limite);
            }
            case "navegar_modulo": {
                String modulo = (String) args.get("modulo");
                if (modulo == null) return "Error: falta el parámetro modulo";
                if (onNavigate != null) {
                    onNavigate.accept(modulo);
                    return "Navegación al módulo " + modulo + " ejecutada con éxito.";
                }
                return "Error: callback de navegación no configurado.";
            }
            case "agregar_carrito": {
                String producto = (String) args.get("producto");
                int cantidad = 1;
                Object rawCant = args.get("cantidad");
                if (rawCant instanceof Number) {
                    cantidad = ((Number) rawCant).intValue();
                } else if (rawCant instanceof String) {
                    try { cantidad = Integer.parseInt((String) rawCant); } catch (Exception ignored) {}
                }
                if (onAddCart != null) {
                    return onAddCart.apply(producto, Math.max(1, cantidad));
                }
                return "Error: callback de agregar al carrito no configurado.";
            }
            case "procesar_pago": {
                if (onCheckout != null) {
                    return onCheckout.get();
                }
                return "Error: callback de procesar pago no configurado.";
            }
            case "cancelar_orden": {
                if (onCancel != null) {
                    return onCancel.get();
                }
                return "Error: callback de cancelar orden no configurado.";
            }
            case "cambiar_modo_oscuro": {
                Boolean activar = (Boolean) args.get("activar");
                if (activar == null) {
                    activar = !Gestor_De_Temas.isDarkMode();
                }
                if (onSetDarkMode != null) {
                    onSetDarkMode.accept(activar);
                    return "Modo oscuro " + (activar ? "activado" : "desactivado") + " con éxito.";
                }
                return "Error: callback de modo oscuro no configurado.";
            }
            default:
                return "Función desconocida: " + name;
        }
    }

    private int getIntArg(Map<String, Object> args, String key, int fallback) {
        Object value = args.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException e) { return fallback; }
        }
        return fallback;
    }

    private String buildDatabaseSummary() {
        StringBuilder sb = new StringBuilder("Base de datos Sello Masculino:\n");
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                sb.append("Tablas:\n");
                while (rs.next()) {
                    sb.append("- ").append(rs.getString("name")).append("\n");
                }
            }
            sb.append("Conteos:\n");
            sb.append("- productos: ").append(countRows(stmt, "productos")).append("\n");
            sb.append("- ventas: ").append(countRows(stmt, "ventas")).append("\n");
            sb.append("- detalles_venta: ").append(countRows(stmt, "detalles_venta")).append("\n");
        } catch (SQLException e) {
            return "Error al leer la base de datos: " + e.getMessage();
        }
        return sb.toString();
    }

    public static String Get_Prompt_Del_Sistema_En_Vivo() {
        return SYSTEM_PROMPT + " Estás en modo voz en vivo: conversa de forma natural, breve y útil. "
                + "Si el usuario pide agregar, poner o añadir un producto al carrito, usa la herramienta agregar_carrito.";
    }

    private int countRows(Statement stmt, String table) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String executeReadOnlyQuery(String sql, int limit) {
        if (sql == null || sql.trim().isEmpty()) return "Error: falta la consulta SQL.";
        String normalized = sql.trim().replaceAll(";+$", "");
        String lower = normalized.toLowerCase();
        if (!lower.startsWith("select ") || lower.contains(";")
                || lower.matches(".*\\b(insert|update|delete|drop|alter|create|replace|truncate|pragma|attach|detach|vacuum)\\b.*")) {
            return "Solo puedo ejecutar consultas SELECT de lectura.";
        }
        limit = Math.max(1, Math.min(limit, 50));
        String limitedSql = lower.matches("(?s).*\\blimit\\s+\\d+\\b.*") ? normalized : normalized + " LIMIT " + limit;

        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(limitedSql);
             ResultSet rs = pstmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            StringBuilder sb = new StringBuilder("Resultado:\n");
            int rows = 0;
            while (rs.next() && rows < limit) {
                rows++;
                sb.append("- ");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) sb.append(" | ");
                    sb.append(meta.getColumnLabel(i)).append(": ").append(rs.getString(i));
                }
                sb.append("\n");
            }
            if (rows == 0) return "La consulta no devolvió filas.";
            return sb.toString();
        } catch (SQLException e) {
            return "Error en la consulta: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArgs(Map<String, Object> raw) {
        return raw != null ? raw : new HashMap<>();
    }

    public void Enviar_Mensaje_Asincrono(String userMessage, Consumer<String> onChunk, Runnable onComplete) {
        Enviar_Mensaje_Asincrono(userMessage, null, onChunk, onComplete);
    }

    public void Enviar_Mensaje_Asincrono(String userMessage, byte[] imageData, Consumer<String> onChunk, Runnable onComplete) {
        Enviar_Mensaje_Asincrono(userMessage, imageData, imageData == null ? null : "image/jpeg", onChunk, onComplete);
    }

    public boolean Esta_Configurado() {
        return client != null;
    }

    public String Obtener_Error_Configuracion() {
        return apiKeyError;
    }

    public void Enviar_Mensaje_Asincrono(String userMessage, byte[] imageData, String imageMimeType, Consumer<String> onChunk, Runnable onComplete) {
        if (client == null) {
            new Thread(() -> {
                onChunk.accept("No tengo configurada la API Key de Gemini. Crea un archivo .env junto al programa con GEMINI_API_KEY=tu_key (consíguela en https://aistudio.google.com/apikey) y reinicia la app.");
                onComplete.run();
            }).start();
            return;
        }
        new Thread(() -> {
            // Build user parts
            List<Part> userParts = new ArrayList<>();
            if (imageData != null) {
                userParts.add(Part.fromBytes(imageData, imageMimeType == null ? "image/jpeg" : imageMimeType));
            }
            userParts.add(Part.fromText(userMessage));

            // El nivel de thinking se respeta la configuracion del usuario (GEMINI_THINKING).
            // Antes estaba fijo en "MINIMAL", ignorando el .env; con HIGH el modelo razona
            // mas lento pero las consultas de inventario/flujo necesitan ese margen.
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .thinkingConfig(ThinkingConfig.builder().thinkingLevel(ConfiguracionThinking()).build())
                    .systemInstruction(Content.builder()
                            .parts(List.of(Part.fromText(SYSTEM_PROMPT)))
                            .build())
                    // FIX 400: Built-in tools (GoogleSearch) + Function calling NO pueden ir juntos
                    // sin toolConfig.includeServerSideToolInvocations=true. Por defecto usamos
                    // SOLO funciones internas (lo que necesita la tienda). GoogleSearch solo si
                    // el usuario lo activa en Config (GEMINI_SEARCH=true).
                    .toolConfig(ToolConfig.builder()
                            .includeServerSideToolInvocations(true)
                            .build())
                    .tools(Construir_Tools_Efectivas())
                    .build();

            // --- RECURSIVIDAD ---
            // El procesamiento de function calls de Gemini se maneja de forma recursiva.
            // Cada llamada procesa una ronda: si Gemini devuelve function calls,
            // las ejecuta y se llama a si misma para la siguiente ronda (hasta 5 niveles).
            // Se prueba principal -> secundarios y se guarda el modelo que funciono.
            // LLM stateless por llamada: historial fresco SOLO con este mensaje.
            List<String> candidatos = com.mycompany.senati_zapato.utilidades.Configuracion.Obtener_Modelos_Chat_Fallback();
            // Pon el modelo actual primero para no reprobar el que ya funciona.
            if (model != null && candidatos.contains(model)) {
                candidatos.remove(model);
                candidatos.add(0, model);
            }
            boolean ok = false;
            StringBuilder todoTexto = new StringBuilder();
            List<String> chunksOk = new ArrayList<>();
            String errorFinal = null;
            for (String m : candidatos) {
                List<Content> conversacion = new ArrayList<>();
                conversacion.add(Content.builder().role("user").parts(userParts).build());
                todoTexto.setLength(0);
                chunksOk.clear();
                // El intento 1 (modelo vigente) emite EN VIVO para que la UI vea progreso y su
                // watchdog de inactividad se reinicie. Los reintentos se bufferean y solo se
                // sueltan si tienen exito, para no duplicar texto si el modelo caido ya emitio.
                final boolean esIntentoPrincipal = (m == candidatos.get(0));
                final boolean[] yaEmitioEnVivo = {false};
                try {
                    procesarRondaRecursiva(m, conversacion, config, chunk -> {
                        todoTexto.append(chunk);
                        chunksOk.add(chunk);
                        if (esIntentoPrincipal) {
                            yaEmitioEnVivo[0] = true;
                            onChunk.accept(chunk);
                        }
                    }, 0);
                    // Si hubo texto o tool-calls (la conversacion crecio), se considera exito.
                    if (todoTexto.length() > 0 || conversacion.size() > 1) {
                        if (!yaEmitioEnVivo[0]) {
                            for (String c : chunksOk) {
                                onChunk.accept(c);
                            }
                        }
                        if (!m.equals(model)) {
                            model = m;
                            com.mycompany.senati_zapato.utilidades.Configuracion.Fijar_En_Memoria("GEMINI_MODEL", m);
                        }
                        ok = true;
                        break;
                    }
                } catch (Exception ex) {
                    errorFinal = ex.getMessage();
                    System.err.println("[Gemini] Modelo " + m + " fallo: " + errorFinal + ". Probando siguiente...");
                }
            }
            if (!ok) {
                onChunk.accept("\nError: no se pudo contactar a ningun modelo (" + String.join(", ", candidatos) + ")."
                    + (errorFinal != null ? " Detalle: " + errorFinal : "")
                    + " Revisa tu API key en Configuracion y tu internet.");
            }
            onComplete.run();
        }).start();
    }

    private String ConfiguracionThinking() {
        try {
            return com.mycompany.senati_zapato.utilidades.Configuracion.Obtener_Thinking_Level();
        } catch (Exception e) {
            return "LOW";
        }
    }

    private List<Tool> Construir_Tools_Efectivas() {
        // Por defecto SOLO funciones internas (compatibles con function calling).
        // GoogleSearch (built-in) solo si se activa en Config, y con toolConfig
        // includeServerSideToolInvocations=true (ya puesto arriba).
        List<Tool> efectivas = new ArrayList<>(tools);
        try {
            if (com.mycompany.senati_zapato.utilidades.Configuracion.Usar_Busqueda_Web()) {
                efectivas.add(0, Tool.builder()
                        .googleSearch(GoogleSearch.builder().build())
                        .build());
            }
        } catch (Exception e) {
            System.err.println("[Gemini] No se pudo agregar GoogleSearch: " + e.getMessage());
        }
        return efectivas;
    }

    public String Obtener_Modelo_Actual() {
        return model;
    }

    /**
     * Procesa una ronda de comunicación con Gemini de forma RECURSIVA.
     * Stateless por llamada: opera sobre la lista conversacion que se le pasa,
     * NO sobre un historial persistente (sin bot interno entrenado).
     */
    private void procesarRondaRecursiva(String modelo, List<Content> conversacion, GenerateContentConfig config, Consumer<String> onChunk, int ronda) {
        // CASO BASE: límite de rondas alcanzado
        if (ronda >= 5) return;

        StringBuilder fullResponse = new StringBuilder();
        List<FunctionCall> functionCalls = new ArrayList<>();

        try (ResponseStream<GenerateContentResponse> stream = client.models.generateContentStream(modelo, conversacion, config)) {
            for (GenerateContentResponse res : stream) {
                if (res.candidates().isEmpty() || res.candidates().get().get(0).content().isEmpty()
                        || res.candidates().get().get(0).content().get().parts().isEmpty()) {
                    continue;
                }
                List<Part> parts = res.candidates().get().get(0).content().get().parts().get();
                for (Part part : parts) {
                    if (part.text().isPresent()) {
                        String chunk = part.text().get();
                        fullResponse.append(chunk);
                        onChunk.accept(chunk);
                    }
                    if (part.functionCall().isPresent()) {
                        functionCalls.add(part.functionCall().get());
                    }
                }
            }
        } catch (Exception e) {
            // Propaga para que el llamador pruebe el siguiente modelo fallback.
            throw new RuntimeException("[" + modelo + "] " + e.getMessage(), e);
        }

        // Agregar respuesta del modelo a la conversacion en curso
        if (fullResponse.length() > 0 || !functionCalls.isEmpty()) {
            List<Part> modelParts = new ArrayList<>();
            if (fullResponse.length() > 0) {
                modelParts.add(Part.fromText(fullResponse.toString()));
            }
            for (FunctionCall fc : functionCalls) {
                modelParts.add(Part.fromFunctionCall(fc.name().orElse(""), fc.args().orElse(Map.of())));
            }
            conversacion.add(Content.builder().role("model").parts(modelParts).build());
        }

        // CASO BASE: no hay function calls → Gemini ya respondió al usuario, fin
        if (functionCalls.isEmpty()) return;

        // CASO RECURSIVO: ejecutar las function calls y llamarse para la siguiente ronda
        List<Part> responseParts = new ArrayList<>();
        for (FunctionCall fc : functionCalls) {
            String funcName = fc.name().orElse("");
            Map<String, Object> fcArgs = parseArgs(fc.args().orElse(null));
            String result = Ejecutar_Funcion(funcName, fcArgs);
            responseParts.add(Part.fromFunctionResponse(funcName, Map.of("result", result)));
        }

        // Agregar resultados de las funciones a la conversacion en curso
        conversacion.add(Content.builder()
                .role("user")
                .parts(responseParts)
                .build());

        // Llamada recursiva para procesar la siguiente ronda con los resultados
        procesarRondaRecursiva(modelo, conversacion, config, onChunk, ronda + 1);
    }

    /** Limpia el buffer interno (por compatibilidad; ya es stateless por llamada). */
    public void Limpiar_Historial() {
        history.clear();
    }
}
