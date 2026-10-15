package com.mycompany.senati_zapato.servicios;

import com.mycompany.senati_zapato.utilidades.Configuracion;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Consulta datos de personas por DNI (RENIEC) y empresas por RUC (SUNAT)
 * usando Decolecta API. Documentación: https://decolecta.gitbook.io/docs
 */
public class Servicio_De_DNI {

    private static final String URL_DNI = "https://api.decolecta.com/v1/reniec/dni?numero=";
    private static final String URL_RUC = "https://api.decolecta.com/v1/sunat/ruc?numero=";

    // Cliente HTTP compartido (reutiliza conexiones, más rápido)
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();

    // Último DNI/RUC consultado con éxito (para pre-llenar el formulario)
    private static volatile String ultimoDNI = null;
    private static volatile String ultimosNombres = null;

    // DNI/RUC pendiente que el bot guardó (sin consultar aún)
    private static volatile String dniPendiente = null;

    public static String Obtener_Ultimo_DNI() { return ultimoDNI; }
    public static String Obtener_Ultimos_Nombres() { return ultimosNombres; }
    public static void Limpiar() { ultimoDNI = null; ultimosNombres = null; }

    /**
     * El bot guarda el DNI/RUC aquí SIN esperar la consulta.
     * El formulario lo lee y lanza la consulta en un thread aparte.
     */
    public static void Guardar_DNI_Para_Formulario(String documento) {
        dniPendiente = documento;
    }

    public static String Obtener_DNI_Pendiente() {
        String d = dniPendiente;
        dniPendiente = null;
        return d;
    }

    public static java.util.List<String> Obtener_Tokens_Decolecta() {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        String primary = Configuracion.Obtener_Decoelecta_Api_Key();
        if (primary != null && !primary.isBlank()) {
            tokens.add(primary.trim());
        }
        String claves = Configuracion.Obtener("DECOLECTA_CLAVES", "");
        if (claves != null && !claves.isBlank()) {
            for (String k : claves.split(",")) {
                k = k.trim();
                if (!k.isEmpty() && !tokens.contains(k)) {
                    tokens.add(k);
                }
            }
        }
        return tokens;
    }

    private static String Hacer_Get(String url) throws Exception {
        java.util.List<String> tokens = Obtener_Tokens_Decolecta();
        if (tokens.isEmpty()) return null;

        for (String token : tokens) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/json")
                        .timeout(java.time.Duration.ofSeconds(5))
                        .GET().build();
                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    System.err.println("[Decolecta] Token devolvió estado " + response.statusCode() + ". Probando siguiente...");
                }
            } catch (Exception ex) {
                System.err.println("[Decolecta] Error en solicitud: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Consulta un DNI (8 dígitos) y devuelve "APELLIDOS, NOMBRES".
     */
    public static String Consultar_Nombres(String dni) {
        try {
            if (Obtener_Tokens_Decolecta().isEmpty() || dni == null || !dni.matches("\\d{8}")) return null;

            String body = Hacer_Get(URL_DNI + dni);
            if (body == null) return null;

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String nombres = json.has("first_name") ? json.get("first_name").getAsString() : "";
            String apP = json.has("first_last_name") ? json.get("first_last_name").getAsString() : "";
            String apM = json.has("second_last_name") ? json.get("second_last_name").getAsString() : "";

            if (nombres.isEmpty() && apP.isEmpty()) return null;

            String resultado = (apP + " " + apM).trim() + ", " + nombres;
            ultimoDNI = dni;
            ultimosNombres = resultado;
            return resultado;
        } catch (Exception e) {
            System.err.println("Error consultando DNI " + dni + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Consulta un RUC (11 dígitos) y devuelve la razón social.
     */
    public static String Consultar_Razon_Social(String ruc) {
        try {
            if (Obtener_Tokens_Decolecta().isEmpty() || ruc == null || !ruc.matches("\\d{11}")) return null;

            String body = Hacer_Get(URL_RUC + ruc);
            if (body == null) return null;

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String razonSocial = json.has("razon_social") ? json.get("razon_social").getAsString() : "";

            if (razonSocial.isEmpty()) return null;

            ultimoDNI = ruc;
            ultimosNombres = razonSocial;
            return razonSocial;
        } catch (Exception e) {
            System.err.println("Error consultando RUC " + ruc + ": " + e.getMessage());
            return null;
        }
    }
}
