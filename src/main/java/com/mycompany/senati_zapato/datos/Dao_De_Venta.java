package com.mycompany.senati_zapato.datos;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dao_De_Venta {

    public List<Venta> Obtener_Ventas_Recientes(int limite) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY fecha_hora DESC LIMIT ?";
        try (PreparedStatement pstmt = Conexion_A_Base_De_Datos.Get_Conexion().prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.Set_Id(rs.getInt("id"));
                    // SQLite datetime string
                    v.setFechaHora(rs.getString("fecha_hora"));
                    v.Set_Cajero(rs.getString("cajero"));
                    v.Set_Monto_Total(rs.getDouble("monto_total"));
                    v.Set_Estado(rs.getString("estado"));
                    
                    String metodo = "Efectivo";
                    try { metodo = rs.getString("metodo_pago"); } catch (Exception e) {}
                    v.Set_Metodo_Pago(metodo);

                    double recibido = 0.0;
                    try { recibido = rs.getDouble("monto_recibido"); } catch (Exception e) {}
                    v.setMontoRecibido(recibido);

                    double vueltoVal = 0.0;
                    try { vueltoVal = rs.getDouble("vuelto"); } catch (Exception e) {}
                    v.setVuelto(vueltoVal);

                    String ref = "";
                    try { ref = rs.getString("referencia"); } catch (Exception e) {}
                    v.setReferencia(ref);

                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas recientes: " + e.getMessage());
        }
        return lista;
    }

    public double[] obtenerKpisDelDia() {
        // [0] = Ingresos del día, [1] = Zapatos vendidos hoy, [2] = Cantidad de transacciones hoy
        double[] kpis = new double[3];
        String sql = "SELECT SUM(monto_total) AS ingresos, COUNT(*) as cantidad FROM ventas WHERE date(fecha_hora) = date('now', 'localtime')";
        String sqlZapatos = "SELECT SUM(dv.cantidad) as total_zapatos FROM detalles_venta dv JOIN ventas v ON dv.venta_id = v.id WHERE date(v.fecha_hora) = date('now', 'localtime')";
        
        try (Statement stmt = Conexion_A_Base_De_Datos.Get_Conexion().createStatement()) {
            
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    kpis[0] = rs.getDouble("ingresos");
                    kpis[2] = rs.getDouble("cantidad");
                }
            }
            
            try (ResultSet rs = stmt.executeQuery(sqlZapatos)) {
                if (rs.next()) {
                    kpis[1] = rs.getDouble("total_zapatos");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener KPIs: " + e.getMessage());
        }
        return kpis;
    }

    public List<Object[]> Obtener_Top_Productos(int limite) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT p.codigo, p.nombre, p.categoria, SUM(dv.cantidad) as total_vendido " +
                     "FROM detalles_venta dv " +
                     "JOIN productos p ON dv.producto_id = p.id " +
                     "GROUP BY dv.producto_id " +
                     "ORDER BY total_vendido DESC LIMIT ?";
        try (PreparedStatement pstmt = Conexion_A_Base_De_Datos.Get_Conexion().prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("total_vendido")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener top productos: " + e.getMessage());
        }
        return lista;
    }

    public List<Object[]> obtenerVentasUltimos7Dias() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT date(fecha_hora) as dia, SUM(monto_total) as total " +
                     "FROM ventas " +
                     "WHERE date(fecha_hora) >= date('now', 'localtime', '-6 days') " +
                     "GROUP BY date(fecha_hora) " +
                     "ORDER BY date(fecha_hora) ASC";
        try (Statement stmt = Conexion_A_Base_De_Datos.Get_Conexion().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("dia"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas de últimos 7 días: " + e.getMessage());
        }
        return lista;
    }

    public List<Object[]> obtenerDetallesPorVenta(int ventaId) {
        List<Object[]> detalles = new ArrayList<>();
        String sql = "SELECT p.nombre, p.precio, dv.cantidad, dv.subtotal " +
                     "FROM detalles_venta dv " +
                     "JOIN productos p ON dv.producto_id = p.id " +
                     "WHERE dv.venta_id = ?";
        try (PreparedStatement pstmt = Conexion_A_Base_De_Datos.Get_Conexion().prepareStatement(sql)) {
            pstmt.setInt(1, ventaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new Object[]{
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad"),
                        rs.getDouble("subtotal")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta: " + e.getMessage());
        }
        return detalles;
    }
}
