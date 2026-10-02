package com.mycompany.senati_zapato.modelos;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

public class Detalle_De_Venta {
    private int id;
    private int ventaId;
    private int productoId;
    private String nombreProducto; // util para mostrar
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public Detalle_De_Venta() {}

    public Detalle_De_Venta(int productoId, String nombreProducto, int cantidad, double precioUnitario, double subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int Get_Id() { return id; }
    public void Set_Id(int id) { this.id = id; }

    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }

    public int Get_Producto_Id() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String Get_Nombre_Producto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int Get_Cantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double Get_Precio_Unitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double Get_Subtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
