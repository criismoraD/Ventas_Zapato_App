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
    public void Set_Tallas(String tallas) { this.tallas = tallas; }

    public String Get_Url_Imagen() { return urlImagen; }
    public void Set_Url_Imagen(String urlImagen) { this.urlImagen = urlImagen; }

    public String Get_Estado() { return estado; }
    public void Set_Estado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return nombre;
    }
}

