package com.mycompany.senati_zapato.utilidades;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cargador centralizado de configuración.
 *
 * <p>Prioridad de búsqueda para cada variable:
 * <ol>
 *   <li>Variable de entorno del sistema ({@code System.getenv})</li>
 *   <li>Propiedad del sistema JVM ({@code -DGEMINI_API_KEY=...})</li>
 *   <li>Archivo {@code .env} ubicado junto al ejecutable / raíz del proyecto</li>
 * </ol>
 * El archivo {@code .env} NUNCA debe subirse a git (ver .gitignore).
 */
public final class Configuracion {

    // ===== Modelos (IDs reales de API) =====
    // Chat principal: Gemini 3.5 Flash Lite (rapido y barato).
    public static final String MODELO_CHAT_PRINCIPAL = "gemini-3.5-flash-lite";
    // Chat secundarios: familia Gemma 4.
    public static final String MODELO_CHAT_SECUNDARIO_1 = "gemma-4-26b-a4b-it";
    public static final String MODELO_CHAT_SECUNDARIO_2 = "gemma-4-31b-it";
    // Voz Live principal: Gemini 3.8 Live + fallback 3.1 por si la cuenta no lo tiene.
    public static final String MODELO_VOZ_PRINCIPAL = "models/gemini-3.8-live";
    public static final String MODELO_VOZ_FALLBACK = "models/gemini-3.1-flash-live-preview";

    public static final String[] MODELOS_CHAT = {
        MODELO_CHAT_PRINCIPAL, MODELO_CHAT_SECUNDARIO_1, MODELO_CHAT_SECUNDARIO_2
    };

    private static final Map<String, String> VALORES_DOTENV = new HashMap<>();
    private static final Map<String, String> SOBREESCRITURAS = new HashMap<>();
    private static File archivoEnvDetectado = null;

    static {
        Cargar_Dotenv_Si_Existe();
    }

    private Configuracion() {
    }

    private static void Cargar_Dotenv_Si_Existe() {
        // 1. Junto al ejecutable / donde se lanza la JVM (user.dir)
        // 2. Directorio padre (cuando se ejecuta desde SENATI_ZAPATO/target/...)
        // 3. Directorio de trabajo del proyecto en desarrollo
        String[] candidatos = {
            System.getProperty("user.dir") + File.separator + ".env",
            new File(System.getProperty("user.dir")).getParent() + File.separator + ".env",
            ".env"
        };
        for (String ruta : candidatos) {
            try {
                File f = new File(ruta);
                if (f.exists() && f.isFile()) {
                    Cargar_Archivo(f);
                    archivoEnvDetectado = f.getAbsoluteFile();
                    break;
                }
            } catch (Exception ex) {
                System.err.println("[Configuracion] No se pudo leer " + ruta + ": " + ex.getMessage());
            }
        }
        if (archivoEnvDetectado == null) {
            archivoEnvDetectado = new File(System.getProperty("user.dir") + File.separator + ".env").getAbsoluteFile();
        }
    }

    private static void Cargar_Archivo(File archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                // Soporta: export KEY=valor  /  KEY=valor  /  KEY="valor con espacios"
                if (linea.startsWith("export ")) {
                    linea = linea.substring(7).trim();
                }
                int idx = linea.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String clave = linea.substring(0, idx).trim();
                String valor = linea.substring(idx + 1).trim();
                // Quita comillas simples/dobles envolventes
                if (valor.length() >= 2
                        && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'")))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                if (!clave.isEmpty() && !VALORES_DOTENV.containsKey(clave)) {
                    VALORES_DOTENV.put(clave, valor);
                }
            }
        } catch (Exception ex) {
            System.err.println("[Configuracion] Error leyendo .env: " + ex.getMessage());
        }
    }

    /**
     * Obtiene una variable con la prioridad: entorno -&gt; -Dpropiedad -&gt; .env -&gt; defecto.
     */
    public static String Obtener(String nombre, String porDefecto) {
        String v = SOBREESCRITURAS.get(nombre);
        if (v != null && !v.isBlank()) {
            return v.trim();
        }
        v = System.getenv(nombre);
        if (v != null && !v.isBlank()) {
            return v.trim();
        }
        v = System.getProperty(nombre);
        if (v != null && !v.isBlank()) {
            return v.trim();
        }
        v = VALORES_DOTENV.get(nombre);
        if (v != null && !v.isBlank()) {
            return v.trim();
        }
        return porDefecto;
    }

    /**
     * Retorna la API Key de Gemini o lanza una excepción con instrucciones claras.
     */
    public static String Obtener_Gemini_Api_Key() {
        String key = Obtener("GEMINI_API_KEY", "");
        if (key == null || key.isBlank() || key.contains("PEGA_AQUI") || key.contains("TU_API_KEY")) {
            throw new IllegalStateException(
                "Falta la variable GEMINI_API_KEY.\n"
                + "1) Crea un archivo .env junto al .jar/.exe con:\n"
                + "     GEMINI_API_KEY=AIza...\n"
                + "   Consigue tu key gratis en: https://aistudio.google.com/apikey\n"
                + "2) O expórtala como variable de entorno de Windows:\n"
                + "     setx GEMINI_API_KEY \"AIza...\"  (reinicia la terminal/IDE)\n"
                + "3) O ejecútalo con: java -DGEMINI_API_KEY=AIza... -jar tu-app.jar");
        }
        return key;
    }

    /**
     * true si hay una key utilizable (para degradar el chatbot con un mensaje amable).
     */
    public static boolean Hay_Gemini_Api_Key() {
        try {
            Obtener_Gemini_Api_Key();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String Obtener_Modelo_Chat() {
        return Obtener("GEMINI_MODEL", MODELO_CHAT_PRINCIPAL);
    }

    public static java.util.List<String> Obtener_Modelos_Chat_Fallback() {
        String csv = Obtener("GEMINI_MODELS", String.join(",", MODELOS_CHAT));
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String s : csv.split(",")) {
            s = s.trim();
            if (!s.isEmpty() && !out.contains(s)) {
                out.add(s);
            }
        }
        if (out.isEmpty()) {
            for (String m : MODELOS_CHAT) {
                out.add(m);
            }
        }
        return out;
    }

    public static String Obtener_Modelo_Voz() {
        return Obtener("GEMINI_LIVE_MODEL", MODELO_VOZ_PRINCIPAL);
    }

    public static boolean Usar_Busqueda_Web() {
        return "true".equalsIgnoreCase(Obtener("GEMINI_SEARCH", "false"));
    }

    public static String Obtener_Thinking_Level() {
        String v = Obtener("GEMINI_THINKING", "LOW").trim().toUpperCase();
        if (!v.equals("MINIMAL") && !v.equals("LOW") && !v.equals("MEDIUM") && !v.equals("HIGH")) {
            return "LOW";
        }
        return v;
    }

    public static void Fijar_En_Memoria(String nombre, String valor) {
        if (valor == null || valor.isBlank()) {
            SOBREESCRITURAS.remove(nombre);
        } else {
            SOBREESCRITURAS.put(nombre, valor.trim());
        }
    }

    public static String Ruta_Env() {
        return archivoEnvDetectado.getAbsolutePath();
    }

    public static String Enmascarar(String key) {
        if (key == null || key.isBlank()) {
            return "(sin configurar)";
        }
        key = key.trim();
        if (key.length() <= 10) {
            return "****";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 3);
    }

    public static synchronized void Guardar_En_Env(java.util.Map<String, String> cambios) throws Exception {
        java.util.Map<String, String> nuevos = new java.util.LinkedHashMap<>(cambios);
        java.util.List<String> lineas = new java.util.ArrayList<>();
        if (archivoEnvDetectado.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivoEnvDetectado))) {
                String l;
                while ((l = br.readLine()) != null) {
                    String t = l.trim();
                    boolean reemplazada = false;
                    if (!t.isEmpty() && !t.startsWith("#")) {
                        String cuerpo = t.startsWith("export ") ? t.substring(7).trim() : t;
                        int idx = cuerpo.indexOf('=');
                        if (idx > 0) {
                            String clave = cuerpo.substring(0, idx).trim();
                            if (nuevos.containsKey(clave)) {
                                lineas.add(clave + "=" + nuevos.remove(clave));
                                reemplazada = true;
                            }
                        }
                    }
                    if (!reemplazada) {
                        lineas.add(l);
                    }
                }
            }
        }
        for (java.util.Map.Entry<String, String> e : nuevos.entrySet()) {
            lineas.add(e.getKey() + "=" + e.getValue());
        }
        File padre = archivoEnvDetectado.getParentFile();
        if (padre != null && !padre.exists()) {
            padre.mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoEnvDetectado, false))) {
            for (String l : lineas) {
                pw.println(l);
            }
        }
        VALORES_DOTENV.clear();
        Cargar_Archivo(archivoEnvDetectado);
        for (java.util.Map.Entry<String, String> e : cambios.entrySet()) {
            Fijar_En_Memoria(e.getKey(), e.getValue());
        }
    }
}
