package com.mycompany.senati_zapato.modelos;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private String categoria;
    private int stock;
    private double precio;
    private String tallas;
    private String urlImagen;
    private String estado;

    public Producto() {
        this.estado = "Disponible";
    }

    public Producto(int id, String codigo, String nombre, String categoria, int stock, double precio) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stock = stock;
        this.precio = precio;
        this.tallas = "38,39,40,41,42";
        this.urlImagen = "";
        this.estado = "Disponible";
    }

    public Producto(int id, String codigo, String nombre, String categoria, int stock, double precio, String tallas, String urlImagen) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stock = stock;
        this.precio = precio;
        this.tallas = tallas;
        this.urlImagen = urlImagen;
        this.estado = "Disponible";
    }

    public Producto(int id, String codigo, String nombre, String categoria, int stock, double precio, String tallas, String urlImagen, String estado) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.stock = stock;
        this.precio = precio;
        this.tallas = tallas;
        this.urlImagen = urlImagen;
        this.estado = (estado == null || estado.isEmpty()) ? "Disponible" : estado;
    }

    public int Get_Id() { return id; }
    public void Set_Id(int id) { this.id = id; }

    public String Get_Codigo() { return codigo; }
    public void Set_Codigo(String codigo) { this.codigo = codigo; }

    public String Get_Nombre() { return nombre; }
    public void Set_Nombre(String nombre) { this.nombre = nombre; }

    public String Get_Categoria() { return categoria; }
    public void Set_Categoria(String categoria) { this.categoria = categoria; }

    public int Get_Stock() { return stock; }
    public void Set_Stock(int stock) { this.stock = stock; }

    public double Get_Precio() { return precio; }
    public void Set_Precio(double precio) { this.precio = precio; }

    public String Get_Tallas() { return tallas; }
    public void Set_Tallas(String tallas) {
        this.tallas = tallas;
        // Si la cadena contiene desglose con ':', recalcular el stock total
        if (tallas != null && tallas.contains(":")) {
            int suma = 0;
            java.util.Map<String, Integer> mapa = Get_Mapa_Stock_Tallas();
            for (int cant : mapa.values()) {
                suma += cant;
            }
            if (suma > 0) {
                this.stock = suma;
            }
        }
    }

    public String Get_Url_Imagen() { return urlImagen; }
    public void Set_Url_Imagen(String urlImagen) { this.urlImagen = urlImagen; }

    public String Get_Estado() { return estado; }
    public void Set_Estado(String estado) { this.estado = estado; }

    // =========================================================================
    // GESTIÓN INTELIGENTE DE STOCK POR TALLAS (Ej: "38:5, 39:10, 40:15, 44:20")
    // =========================================================================

    /**
     * Retorna el mapa ordenado de Talla -> Cantidad de pares disponibles.
     */
    public java.util.Map<String, Integer> Get_Mapa_Stock_Tallas() {
        java.util.Map<String, Integer> mapa = new java.util.LinkedHashMap<>();
        if (tallas == null || tallas.trim().isEmpty()) {
            return mapa;
        }

        String[] tokens = tallas.split("[,;]+");
        boolean tieneFormatoClaveValor = tallas.contains(":");

        if (tieneFormatoClaveValor) {
            for (String token : tokens) {
                String t = token.trim();
                if (t.isEmpty()) continue;
                if (t.contains(":")) {
                    String[] partes = t.split(":");
                    String tallaNom = partes[0].trim();
                    int cant = 0;
                    try {
                        cant = Integer.parseInt(partes[1].trim());
                    } catch (NumberFormatException ignored) {}
                    mapa.put(tallaNom, cant);
                } else {
                    mapa.put(t, 0);
                }
            }
        } else {
            // Compatibilidad con formato antiguo "38,39,40,41,42":
            // Distribuir el stock total entre las tallas disponibles
            java.util.List<String> listaTallas = new java.util.ArrayList<>();
            for (String tok : tokens) {
                String t = tok.trim();
                if (!t.isEmpty()) listaTallas.add(t);
            }
            if (!listaTallas.isEmpty()) {
                int cantBase = stock / listaTallas.size();
                int resto = stock % listaTallas.size();
                for (int i = 0; i < listaTallas.size(); i++) {
                    int cant = cantBase + (i == 0 ? resto : 0);
                    mapa.put(listaTallas.get(i), Math.max(0, cant));
                }
            }
        }
        return mapa;
    }

    /**
     * Retorna la cantidad exacta de pares para una talla específica (ej: "44").
     * Si no existe o no hay stock, retorna 0.
     */
    public int Get_Stock_De_Talla(String tallaBuscada) {
        if (tallaBuscada == null) return 0;
        String limpia = tallaBuscada.replaceAll("(?i)talla", "").trim();
        java.util.Map<String, Integer> mapa = Get_Mapa_Stock_Tallas();
        for (java.util.Map.Entry<String, Integer> e : mapa.entrySet()) {
            if (e.getKey().equalsIgnoreCase(limpia) || e.getKey().equalsIgnoreCase(tallaBuscada.trim())) {
                return e.getValue();
            }
        }
        return 0;
    }

    /**
     * Verifica si hay al menos una cantidad dada en la talla solicitada.
     */
    public boolean Tiene_Talla(String tallaBuscada) {
        return Get_Stock_De_Talla(tallaBuscada) > 0;
    }

    /**
     * Devuelve una cadena formateada con el desglose por tallas:
     * "Talla 38: 5 pares | Talla 39: 10 pares | Talla 44: 20 pares"
     */
    public String Get_Resumen_Tallas_Con_Stock() {
        java.util.Map<String, Integer> mapa = Get_Mapa_Stock_Tallas();
        if (mapa.isEmpty()) {
            return "Sin tallas registradas";
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Integer> e : mapa.entrySet()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Talla ").append(e.getKey()).append(": ").append(e.getValue()).append(" pares");
        }
        return sb.toString();
    }

    /**
     * Devuelve solo los números de tallas disponibles (con stock > 0): "38, 39, 40, 44"
     */
    public String Get_Tallas_Solo_Numeros() {
        java.util.Map<String, Integer> mapa = Get_Mapa_Stock_Tallas();
        if (mapa.isEmpty()) {
            return tallas != null ? tallas : "";
        }
        java.util.List<String> activas = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : mapa.entrySet()) {
            activas.add(e.getKey());
        }
        return String.join(", ", activas);
    }

    /**
     * Actualiza el stock de una talla y recalcula el stock total.
     */
    public void Set_Stock_De_Talla(String talla, int cantidad) {
        String limpia = talla.replaceAll("(?i)talla", "").trim();
        java.util.Map<String, Integer> mapa = Get_Mapa_Stock_Tallas();
        mapa.put(limpia, Math.max(0, cantidad));

        StringBuilder sb = new StringBuilder();
        int nuevoTotal = 0;
        for (java.util.Map.Entry<String, Integer> e : mapa.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append(":").append(e.getValue());
            nuevoTotal += e.getValue();
        }
        this.tallas = sb.toString();
        this.stock = nuevoTotal;
        if (nuevoTotal == 0) {
            this.estado = "Agotado";
        } else if ("Agotado".equalsIgnoreCase(this.estado)) {
            this.estado = "Disponible";
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}

