package com.mycompany.senati_zapato;

import com.mycompany.senati_zapato.datos.Conexion_A_Base_De_Datos;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDb {
    public static void main(String[] args) {
        try {
            System.out.println("Triggering connection (which triggers migration)...");
            Connection conn = Conexion_A_Base_De_Datos.Get_Conexion();
            System.out.println("Connection opened! Checking products and sales in active DB:");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            if (rs.next()) System.out.println("Active Products Count: " + rs.getInt(1));
            rs.close();
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM ventas");
            if (rs.next()) System.out.println("Active Sales Count: " + rs.getInt(1));
            rs.close();
            
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
