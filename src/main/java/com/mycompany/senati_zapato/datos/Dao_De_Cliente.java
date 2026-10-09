package com.mycompany.senati_zapato.datos;

import com.mycompany.senati_zapato.modelos.Cliente;
import java.sql.*;

public class Dao_De_Cliente {
    public Cliente buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
        String sql = "SELECT id, tipo_documento, numero_documento, nombres, telefono, correo, direccion " +
                "FROM clientes WHERE tipo_documento = ? AND numero_documento = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoDocumento);
            stmt.setString(2, numeroDocumento);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? leerCliente(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar el comprador.", e);
        }
    }

    public Cliente guardar(Cliente cliente) {
        String sql = "INSERT INTO clientes(tipo_documento, numero_documento, nombres, telefono, correo, direccion) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getTipoDocumento());
            stmt.setString(2, cliente.getNumeroDocumento());
            stmt.setString(3, cliente.getNombres());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getCorreo());
            stmt.setString(6, cliente.getDireccion());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cliente.setId(keys.getInt(1));
                    return cliente;
                }
            }
            throw new SQLException("No se obtuvo el ID del comprador.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                Cliente existente = buscarPorDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento());
                String nombre = existente == null ? "" : " (" + existente.getNombres() + ")";
                throw new PersistenciaException("Ya existe un comprador con "
                        + cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento() + nombre
                        + ". Use «Buscar comprador» para utilizarlo.", e);
            }
            throw new PersistenciaException("No se pudo registrar el comprador.", e);
        }
    }

    private Cliente leerCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setTipoDocumento(rs.getString("tipo_documento"));
        cliente.setNumeroDocumento(rs.getString("numero_documento"));
        cliente.setNombres(rs.getString("nombres"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setDireccion(rs.getString("direccion"));
        return cliente;
    }
}
