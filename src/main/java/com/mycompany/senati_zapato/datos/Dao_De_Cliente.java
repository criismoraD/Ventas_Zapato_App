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
                    registrarDiagnostico("INSERT OK", cliente, "id=" + cliente.getId());
                    return cliente;
                }
            }
            throw new SQLException("No se obtuvo el ID del comprador.");
        } catch (SQLException e) {
            String detalle = e.getMessage() == null ? e.toString() : e.getMessage();
            Cliente existente = buscarPorDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento());
            registrarDiagnostico("INSERT FALLA", cliente,
                    "sql=" + detalle + (existente == null ? " | sin registro previo" : " | previo id=" + existente.getId()));
            if (detalle.toLowerCase().contains("unique")) {
                if (existente != null) {
                    return existente;
                }
                throw new PersistenciaException("No se pudo registrar el comprador.\n"
                        + "Detalle técnico: " + detalle, e);
            }
            throw new PersistenciaException("No se pudo registrar el comprador.", e);
        }
    }

    public Cliente actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombres = ?, telefono = ?, correo = ?, direccion = ? WHERE id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombres());
            stmt.setString(2, cliente.getTelefono());
            stmt.setString(3, cliente.getCorreo());
            stmt.setString(4, cliente.getDireccion());
            stmt.setInt(5, cliente.getId());
            stmt.executeUpdate();
            registrarDiagnostico("UPDATE OK", cliente, "id=" + cliente.getId());
            return cliente;
        } catch (SQLException e) {
            registrarDiagnostico("UPDATE FALLA", cliente, e.getMessage());
            throw new PersistenciaException("No se pudo actualizar el comprador.", e);
        }
    }

    private void registrarDiagnostico(String accion, Cliente cliente, String detalle) {
        try {
            java.nio.file.Path log = java.nio.file.Paths.get(System.getProperty("user.dir"),
                    "Datos_SenatiZapato", "diagnostico_clientes.log");
            java.nio.file.Files.createDirectories(log.getParent());
            String linea = java.time.LocalDateTime.now()
                    + " | cwd=" + System.getProperty("user.dir")
                    + " | " + accion
                    + " | doc=" + cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento()
                    + " | nombres=" + cliente.getNombres()
                    + " | " + detalle + System.lineSeparator();
            java.nio.file.Files.write(log, linea.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignorada) {
            // El diagnostico nunca debe interrumpir el registro.
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
