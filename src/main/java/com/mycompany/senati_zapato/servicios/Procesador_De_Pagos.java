package com.mycompany.senati_zapato.servicios;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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
        validarPago(venta, metodoPago, montoRecibido, vuelto, referencia);
        venta.Set_Metodo_Pago(metodoPago);
        venta.setMontoRecibido(montoRecibido);
        venta.setVuelto(vuelto);
        venta.setReferencia(referencia);
        venta.Set_Estado("Completado");
        venta.setEstadoPago("CONFIRMADO_MANUALMENTE");

        Connection conn = null;
        try {
            conn = Conexion_A_Base_De_Datos.Get_Conexion();
            conn.setAutoCommit(false); // INICIO DE LA TRANSACCIÓN (ACID)

            // 1. Validar stock en la base de datos (concurrencia)
            String sqlCheckStock = "SELECT stock, nombre FROM productos WHERE id = ?";
            Map<Integer, Integer> cantidadesPorProducto = agruparCantidades(venta);
            for (Map.Entry<Integer, Integer> entrada : cantidadesPorProducto.entrySet()) {
                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckStock)) {
                    pstmtCheck.setInt(1, entrada.getKey());
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            int stockActual = rs.getInt("stock");
                            String nombre = rs.getString("nombre");
                            if (stockActual < entrada.getValue()) {
                                throw new SQLException("Stock insuficiente para: " + nombre + " (Disponible: " + stockActual + ", Solicitado: " + entrada.getValue() + ")");
                            }
                        } else {
                            throw new SQLException("El producto con ID " + entrada.getKey() + " no existe.");
                        }
                    }
                }
            }

            // 2. Insertar venta principal con los campos extendidos
            String sqlVenta = "INSERT INTO ventas(cajero, monto_total, estado, metodo_pago, monto_recibido, vuelto, referencia, estado_pago, terminal_id, cliente_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int ventaId = 0;
            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenta.setString(1, venta.Get_Cajero());
                pstmtVenta.setDouble(2, venta.Get_Monto_Total());
                pstmtVenta.setString(3, venta.Get_Estado());
                pstmtVenta.setString(4, venta.Get_Metodo_Pago());
                pstmtVenta.setDouble(5, venta.getMontoRecibido());
                pstmtVenta.setDouble(6, venta.getVuelto());
                pstmtVenta.setString(7, venta.getReferencia());
                pstmtVenta.setString(8, venta.getEstadoPago());
                pstmtVenta.setString(9, venta.getTerminalId());
                if (venta.getCliente() == null) {
                    pstmtVenta.setNull(10, Types.INTEGER);
                } else {
                    pstmtVenta.setInt(10, venta.getCliente().getId());
                }
                
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
            String sqlUpdateStock = "UPDATE productos SET stock = stock - ?, " +
                    "estado = CASE WHEN stock - ? <= 0 THEN 'Agotado' ELSE estado END " +
                    "WHERE id = ? AND stock >= ?";

            try (PreparedStatement pstmtStock = conn.prepareStatement(sqlUpdateStock)) {
                for (Map.Entry<Integer, Integer> entrada : cantidadesPorProducto.entrySet()) {
                    pstmtStock.setInt(1, entrada.getValue());
                    pstmtStock.setInt(2, entrada.getValue());
                    pstmtStock.setInt(3, entrada.getKey());
                    pstmtStock.setInt(4, entrada.getValue());
                    if (pstmtStock.executeUpdate() != 1) {
                        throw new SQLException("El stock cambió durante la venta para el producto "
                                + entrada.getKey() + ".");
                    }
                }
            }

            conn.commit(); // CONFIRMACIÓN DE LA TRANSACCIÓN (ACID)
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
            String mensaje = e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")
                    ? "El código de operación ya fue utilizado para este método de pago."
                    : "No se pudo procesar el pago. La venta fue revertida.";
            throw new RuntimeException(mensaje, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeError) {
                    System.err.println("No se pudo cerrar la conexión de pago: " + closeError.getMessage());
                }
            }
        }
    }

    private static void validarPago(Venta venta, String metodoPago, double montoRecibido,
                double vuelto, String referencia) {
            if (venta == null || venta.Get_Detalles() == null || venta.Get_Detalles().isEmpty()) {
                throw new IllegalArgumentException("La venta debe contener al menos un producto.");
            }
            if (!Double.isFinite(venta.Get_Monto_Total()) || venta.Get_Monto_Total() <= 0) {
                throw new IllegalArgumentException("El total de la venta debe ser mayor que cero.");
            }
            if (metodoPago == null || metodoPago.isBlank()) {
                throw new IllegalArgumentException("Debe seleccionar un método de pago.");
            }
            if (!Double.isFinite(montoRecibido) || montoRecibido < 0
                    || !Double.isFinite(vuelto) || vuelto < 0) {
                throw new IllegalArgumentException("Los importes del pago no son válidos.");
            }
            if ("Efectivo".equalsIgnoreCase(metodoPago)) {
                if (montoRecibido < venta.Get_Monto_Total()) {
                    throw new IllegalArgumentException("El monto recibido es menor que el total.");
                }
                double vueltoEsperado = montoRecibido - venta.Get_Monto_Total();
                if (Math.abs(vuelto - vueltoEsperado) > 0.01) {
                    throw new IllegalArgumentException("El vuelto calculado no coincide con el monto recibido.");
                }
            } else if (referencia == null || referencia.isBlank()) {
                throw new IllegalArgumentException("Debe registrar el código de operación o autorización.");
            }
            for (Detalle_De_Venta detalle : venta.Get_Detalles()) {
                if (detalle.Get_Cantidad() <= 0 || detalle.Get_Precio_Unitario() < 0
                        || !Double.isFinite(detalle.Get_Subtotal())) {
                    throw new IllegalArgumentException("La venta contiene un detalle inválido.");
                }
            }

    }

    private static Map<Integer, Integer> agruparCantidades(Venta venta) {
        Map<Integer, Integer> cantidades = new HashMap<>();
        for (Detalle_De_Venta detalle : venta.Get_Detalles()) {
            cantidades.merge(detalle.Get_Producto_Id(), detalle.Get_Cantidad(), Integer::sum);
        }
        return cantidades;
    }
}
