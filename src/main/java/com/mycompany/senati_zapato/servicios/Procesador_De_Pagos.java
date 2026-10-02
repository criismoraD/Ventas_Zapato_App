package com.mycompany.senati_zapato.servicios;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.sql.*;

public class Procesador_De_Pagos {
    
    /**
     * Procesa el pago de una venta de forma transaccional (ACID).
     * @param venta El objeto de la venta conteniendo los detalles.
     * @param metodoPago El método de pago ('Efectivo', 'Tarjeta (Débito/Crédito)', 'Yape', 'Plin', 'Transferencia BCP/Interbank').
     * @param montoRecibido El monto entregado por el cliente.
     * @param vuelto El vuelto calculado para el cliente.
     * @param referencia Código o número de transacción/referencia para pagos electrónicos o tarjetas.
     * @return true si la transacción se completó con éxito, false en caso contrario.
     */
    public static boolean Procesar_Pago(Venta venta, String metodoPago, double montoRecibido, double vuelto, String referencia) {
        venta.Set_Metodo_Pago(metodoPago);
        venta.setMontoRecibido(montoRecibido);
        venta.setVuelto(vuelto);
        venta.setReferencia(referencia);
        venta.Set_Estado("Completado");

        Connection conn = null;
        try {
            conn = Conexion_A_Base_De_Datos.Get_Conexion();
            conn.setAutoCommit(false); // INICIO DE LA TRANSACCIÓN (ACID)

            // 1. Validar stock en la base de datos (concurrencia)
            String sqlCheckStock = "SELECT stock, nombre FROM productos WHERE id = ?";
            for (Detalle_De_Venta detalle : venta.Get_Detalles()) {
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckStock)) {
                    pstmtCheck.setInt(1, detalle.Get_Producto_Id());
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            int stockActual = rs.getInt("stock");
                            String nombre = rs.getString("nombre");
                            if (stockActual < detalle.Get_Cantidad()) {
                                throw new SQLException("Stock insuficiente para: " + nombre + " (Disponible: " + stockActual + ", Solicitado: " + detalle.Get_Cantidad() + ")");
                            }
                        } else {
                            throw new SQLException("El producto con ID " + detalle.Get_Producto_Id() + " no existe.");
                        }
                    }
                }
            }

            // 2. Insertar venta principal con los campos extendidos
            String sqlVenta = "INSERT INTO ventas(cajero, monto_total, estado, metodo_pago, monto_recibido, vuelto, referencia) VALUES(?, ?, ?, ?, ?, ?, ?)";
            int ventaId = 0;
            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenta.setString(1, venta.Get_Cajero());
                pstmtVenta.setDouble(2, venta.Get_Monto_Total());
                pstmtVenta.setString(3, venta.Get_Estado());
                pstmtVenta.setString(4, venta.Get_Metodo_Pago());
                pstmtVenta.setDouble(5, venta.getMontoRecibido());
                pstmtVenta.setDouble(6, venta.getVuelto());
                pstmtVenta.setString(7, venta.getReferencia());
                
                pstmtVenta.executeUpdate();
                
                try (ResultSet rsKeys = pstmtVenta.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        ventaId = rsKeys.getInt(1);
                        venta.Set_Id(ventaId);
                    } else {
                        throw new SQLException("Error al obtener el ID autogenerado de la venta.");
                    }
                }
            }

            // 3. Insertar detalles de la venta
            String sqlDetalle = "INSERT INTO detalles_venta(venta_id, producto_id, cantidad, precio_unitario, subtotal) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle)) {
                for (Detalle_De_Venta detalle : venta.Get_Detalles()) {
                    pstmtDetalle.setInt(1, ventaId);
                    pstmtDetalle.setInt(2, detalle.Get_Producto_Id());
                    pstmtDetalle.setInt(3, detalle.Get_Cantidad());
                    pstmtDetalle.setDouble(4, detalle.Get_Precio_Unitario());
                    pstmtDetalle.setDouble(5, detalle.Get_Subtotal());
                    pstmtDetalle.executeUpdate();
                }
            }

            // 4. Descontar stock y actualizar estado "Agotado" si el stock llega a 0
            String sqlUpdateStock = "UPDATE productos SET stock = stock - ? WHERE id = ?";
            String sqlUpdateEstado = "UPDATE productos SET estado = 'Agotado' WHERE id = ? AND stock <= 0";
            
            try (PreparedStatement pstmtStock = conn.prepareStatement(sqlUpdateStock);
                 PreparedStatement pstmtEstado = conn.prepareStatement(sqlUpdateEstado)) {
                for (Detalle_De_Venta detalle : venta.Get_Detalles()) {
                    // Descontar
                    pstmtStock.setInt(1, detalle.Get_Cantidad());
                    pstmtStock.setInt(2, detalle.Get_Producto_Id());
                    pstmtStock.executeUpdate();

                    // Cambiar a Agotado si stock llega a 0
                    pstmtEstado.setInt(1, detalle.Get_Producto_Id());
                    pstmtEstado.executeUpdate();
                }
            }

            conn.commit(); // CONFIRMACIÓN DE LA TRANSACCIÓN (ACID)
            return true;
        } catch (SQLException e) {
            System.err.println("Transacción abortada. Rollback ejecutado. Motivo: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // DESHACER CAMBIOS (ACID)
                } catch (SQLException ex) {
                    System.err.println("Error al ejecutar rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException(e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.err.println("Error al restaurar auto-commit: " + ex.getMessage());
                }
            }
        }
    }
}
