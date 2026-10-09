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
        String sql = "SELECT v.*, c.tipo_documento, c.numero_documento, c.nombres, c.telefono, c.correo, c.direccion " +
                "FROM ventas v LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "ORDER BY v.fecha_hora DESC LIMIT ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                    String estadoPago = "CONFIRMADO_MANUALMENTE";
                    try { estadoPago = rs.getString("estado_pago"); } catch (Exception e) {}
                    v.setEstadoPago(estadoPago);
                    String terminalId = "";
                    try { terminalId = rs.getString("terminal_id"); } catch (Exception e) {}
                    v.setTerminalId(terminalId);
                    if (rs.getString("numero_documento") != null) {
                        Cliente cliente = new Cliente();
                        cliente.setId(rs.getInt("cliente_id"));
                        cliente.setTipoDocumento(rs.getString("tipo_documento"));
                        cliente.setNumeroDocumento(rs.getString("numero_documento"));
                        cliente.setNombres(rs.getString("nombres"));
                        cliente.setTelefono(rs.getString("telefono"));
                        cliente.setCorreo(rs.getString("correo"));
                        cliente.setDireccion(rs.getString("direccion"));
                        v.setCliente(cliente);
                    }

                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron obtener las ventas recientes.", e);
        }
        return lista;
    }

    public double[] obtenerKpisDelDia() {
        // [0] = Ingresos del día, [1] = Zapatos vendidos hoy, [2] = Cantidad de transacciones hoy
        double[] kpis = new double[3];
        String sql = "SELECT SUM(monto_total) AS ingresos, COUNT(*) as cantidad FROM ventas WHERE date(fecha_hora) = date('now', 'localtime')";
        String sqlZapatos = "SELECT SUM(dv.cantidad) as total_zapatos FROM detalles_venta dv JOIN ventas v ON dv.venta_id = v.id WHERE date(v.fecha_hora) = date('now', 'localtime')";
        
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             Statement stmt = conn.createStatement()) {
            
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
            throw new PersistenciaException("No se pudieron obtener los indicadores del día.", e);
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
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            throw new PersistenciaException("No se pudieron obtener los productos más vendidos.", e);
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
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("dia"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron obtener las ventas de los últimos 7 días.", e);
        }
        return lista;
    }

    public List<Object[]> obtenerDetallesPorVenta(int ventaId) {
        List<Object[]> detalles = new ArrayList<>();
        String sql = "SELECT p.nombre, p.precio, dv.cantidad, dv.subtotal " +
                     "FROM detalles_venta dv " +
                     "JOIN productos p ON dv.producto_id = p.id " +
                     "WHERE dv.venta_id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            throw new PersistenciaException("No se pudieron obtener los detalles de la venta " + ventaId + ".", e);
        }
        return detalles;
    }
}
