package com.mycompany.senati_zapato.datos;

import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dao_De_Producto {

    private Producto mapRowToProducto(ResultSet rs) throws SQLException {
        String urlImg = rs.getString("url_imagen");
        if (urlImg == null) urlImg = "";
        String tallas = rs.getString("tallas");
        if (tallas == null || tallas.isEmpty()) tallas = "38,39,40,41,42";
        String estado = rs.getString("estado");
        if (estado == null || estado.isEmpty()) estado = "Disponible";
        return new Producto(
            rs.getInt("id"), rs.getString("codigo"), rs.getString("nombre"),
            rs.getString("categoria"), rs.getInt("stock"), rs.getDouble("precio"),
            tallas, urlImg, estado
        );
    }

    public List<Producto> Obtener_Todos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapRowToProducto(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron obtener los productos.", e);
        }
        return lista;
    }

    public Producto obtenerPorId(int id) {
        String sql = "SELECT * FROM productos WHERE id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProducto(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo obtener el producto con ID " + id + ".", e);
        }
        return null;
    }

    public void insertar(Producto producto) {
        String sql = "INSERT INTO productos(codigo, nombre, categoria, stock, precio, tallas, url_imagen, estado) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.Get_Codigo());
            pstmt.setString(2, producto.Get_Nombre());
            pstmt.setString(3, producto.Get_Categoria());
            pstmt.setInt(4, producto.Get_Stock());
            pstmt.setDouble(5, producto.Get_Precio());
            pstmt.setString(6, producto.Get_Tallas());
            pstmt.setString(7, producto.Get_Url_Imagen());
            pstmt.setString(8, producto.Get_Estado());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo insertar el producto.", e);
        }
    }

    public void actualizar(Producto producto) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, categoria = ?, stock = ?, precio = ?, tallas = ?, url_imagen = ?, estado = ? WHERE id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.Get_Codigo());
            pstmt.setString(2, producto.Get_Nombre());
            pstmt.setString(3, producto.Get_Categoria());
            pstmt.setInt(4, producto.Get_Stock());
            pstmt.setDouble(5, producto.Get_Precio());
            pstmt.setString(6, producto.Get_Tallas());
            pstmt.setString(7, producto.Get_Url_Imagen());
            pstmt.setString(8, producto.Get_Estado());
            pstmt.setInt(9, producto.Get_Id());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el producto con ID " + producto.Get_Id() + ".", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo eliminar el producto con ID " + id + ".", e);
        }
    }

    public void actualizarStock(int id, int cantidadReducir) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
        try (Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidadReducir);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el stock del producto con ID " + id + ".", e);
        }
    }

}
