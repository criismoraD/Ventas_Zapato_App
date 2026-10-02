package com.mycompany.senati_zapato.modelos;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private int id;
    private String fechaHora;
    private String cajero;
    private double montoTotal;
    private String estado;
    private List<Detalle_De_Venta> detalles;
    private String metodoPago;
    private double montoRecibido;
    private double vuelto;
    private String referencia;

    public Venta() {
        this.detalles = new ArrayList<>();
        this.metodoPago = "Efectivo";
        this.referencia = "";
    }

    public int Get_Id() { return id; }
    public void Set_Id(int id) { this.id = id; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String Get_Cajero() { return cajero; }
    public void Set_Cajero(String cajero) { this.cajero = cajero; }

    public double Get_Monto_Total() { return montoTotal; }
    public void Set_Monto_Total(double montoTotal) { this.montoTotal = montoTotal; }

    public String Get_Estado() { return estado; }
    public void Set_Estado(String estado) { this.estado = estado; }

    public List<Detalle_De_Venta> Get_Detalles() { return detalles; }
    public void setDetalles(List<Detalle_De_Venta> detalles) { this.detalles = detalles; }

    public String Get_Metodo_Pago() { return metodoPago; }
    public void Set_Metodo_Pago(String metodoPago) { this.metodoPago = metodoPago; }

    public double getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(double montoRecibido) { this.montoRecibido = montoRecibido; }

    public double getVuelto() { return vuelto; }
    public void setVuelto(double vuelto) { this.vuelto = vuelto; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
