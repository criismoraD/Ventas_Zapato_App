package com.mycompany.senati_zapato.datos;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion_A_Base_De_Datos {
    private static final String APP_DIR = System.getProperty("user.dir") + java.io.File.separator + "Datos_SenatiZapato";
    private static final String OLD_APP_DIR = System.getProperty("user.home") + java.io.File.separator + "SenatiZapato";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "senati_zapato.db";
    private static boolean databaseInitialized;

    private Conexion_A_Base_De_Datos() {}

    public static synchronized Connection Get_Conexion() throws SQLException {
        if (!databaseInitialized) {
            java.io.File userDir = new java.io.File(System.getProperty("user.dir"));
            java.io.File appDirFile = new java.io.File(APP_DIR);
            if (!appDirFile.exists()) {
                if (!appDirFile.mkdirs() && !appDirFile.exists()) {
                    throw new SQLException("No se pudo crear el directorio de datos: " + APP_DIR);
                }
            }
            java.io.File dbFile = new java.io.File(APP_DIR, "senati_zapato.db");
            java.io.File rootDb = new java.io.File(userDir, "senati_zapato.db");

            // Si se ejecuta desde "target", detectar la base de datos real del proyecto en la carpeta superior
            if (userDir.getName().equalsIgnoreCase("target") && userDir.getParentFile() != null) {
                java.io.File parentDatosDb = new java.io.File(userDir.getParentFile(), "Datos_SenatiZapato" + java.io.File.separator + "senati_zapato.db");
                java.io.File parentRootDb = new java.io.File(userDir.getParentFile(), "senati_zapato.db");
                java.io.File parentRealDb = parentDatosDb.exists() ? parentDatosDb : (parentRootDb.exists() ? parentRootDb : null);

                if (parentRealDb != null) {
                    int countTarget = contarProductosSilencioso(dbFile);
                    int countParent = contarProductosSilencioso(parentRealDb);
                    // Si no existe en target, o target tiene <= 9 semillas y el proyecto principal tiene más
                    if (!dbFile.exists() || (countTarget <= 9 && countParent > 9)) {
                        copiarBaseDeDatos(parentRealDb, dbFile, "el proyecto principal");
                    }
                }
            }

            // Solo migrar una base alternativa cuando la base portable aún no existe.
            if (!dbFile.exists() && rootDb.exists()) {
                copiarBaseDeDatos(rootDb, dbFile, "la raíz del proyecto");
            }

            // Si aún no existe la DB portable, intentar desde user.home.
            if (!dbFile.exists()) {
                java.io.File oldDb = new java.io.File(OLD_APP_DIR, "senati_zapato.db");
                if (oldDb.exists()) {
                    copiarBaseDeDatos(oldDb, dbFile, "user.home");
                }
            }

            // Asegurar que senati_zapato.db también esté visible directamente en la carpeta de ejecución (junto al .exe)
            if (dbFile.exists() && !rootDb.exists()) {
                try {
                    java.nio.file.Files.copy(dbFile.toPath(), rootDb.toPath());
                } catch (Exception ignored) {}
            }

            try {
                Class.forName("org.sqlite.JDBC");
                try (Connection initialConnection = DriverManager.getConnection(DB_URL)) {
                    Inicializar_Base_De_Datos(initialConnection);
                }
                databaseInitialized = true;
            }
            catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite JDBC no encontrado.", e);
            }
        }
        Connection newConnection = DriverManager.getConnection(DB_URL);
        try (Statement stmt = newConnection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA busy_timeout = 5000");
        }
        return newConnection;
    }

    private static int contarProductosSilencioso(java.io.File file) {
        if (file == null || !file.exists()) return 0;
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT count(*) FROM productos")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static void copiarBaseDeDatos(java.io.File origen, java.io.File destino, String descripcion) throws SQLException {
        try {
            java.nio.file.Files.copy(origen.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Base de datos migrada desde " + descripcion + ".");
        } catch (java.io.IOException ex) {
            throw new SQLException("No se pudo migrar la base de datos desde " + descripcion + ".", ex);
        }
    }

    private static void Inicializar_Base_De_Datos(Connection connection) throws SQLException {
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT UNIQUE NOT NULL," +
                "nombre TEXT NOT NULL," +
                "categoria TEXT NOT NULL," +
                "stock INTEGER NOT NULL," +
                "precio REAL NOT NULL," +
                "tallas TEXT DEFAULT ''," +
                "url_imagen TEXT DEFAULT ''," +
                "estado TEXT DEFAULT 'Disponible')";

        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "cajero TEXT NOT NULL," +
                "monto_total REAL NOT NULL," +
                "estado TEXT NOT NULL," +
                "metodo_pago TEXT DEFAULT 'Efectivo'," +
                "monto_recibido REAL DEFAULT 0.0," +
                "vuelto REAL DEFAULT 0.0," +
                "referencia TEXT DEFAULT ''," +
                "estado_pago TEXT DEFAULT 'PENDIENTE_VERIFICACION'," +
                "terminal_id TEXT DEFAULT ''," +
                "cliente_id INTEGER NULL," +
                "FOREIGN KEY (cliente_id) REFERENCES clientes(id))";

        String sqlClientes = "CREATE TABLE IF NOT EXISTS clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tipo_documento TEXT NOT NULL," +
                "numero_documento TEXT NOT NULL UNIQUE," +
                "nombres TEXT NOT NULL," +
                "telefono TEXT DEFAULT ''," +
                "correo TEXT DEFAULT ''," +
                "direccion TEXT DEFAULT ''," +
                "fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP)";

        String sqlDetalles = "CREATE TABLE IF NOT EXISTS detalles_venta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "venta_id INTEGER NOT NULL," +
                "producto_id INTEGER NOT NULL," +
                "cantidad INTEGER NOT NULL," +
                "precio_unitario REAL NOT NULL," +
                "subtotal REAL NOT NULL," +
                "FOREIGN KEY (venta_id) REFERENCES ventas(id)," +
                "FOREIGN KEY (producto_id) REFERENCES productos(id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlProductos);
            stmt.execute(sqlClientes);
            stmt.execute(sqlVentas);
            stmt.execute(sqlDetalles);

            agregarColumnaSiNoExiste(stmt, "productos", "tallas TEXT DEFAULT ''");
            agregarColumnaSiNoExiste(stmt, "productos", "url_imagen TEXT DEFAULT ''");
            agregarColumnaSiNoExiste(stmt, "productos", "estado TEXT DEFAULT 'Disponible'");

            // Asignar imágenes a productos existentes que no tienen una
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=1' WHERE codigo = 'P001' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=2' WHERE codigo = 'P002' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=3' WHERE codigo = 'P003' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=4' WHERE codigo = 'P004' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=5' WHERE codigo = 'P005' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=6' WHERE codigo = 'P006' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=7' WHERE codigo = 'P007' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=8' WHERE codigo = 'P008' AND (url_imagen IS NULL OR url_imagen = '')");
            stmt.execute("UPDATE productos SET url_imagen = 'https://picsum.photos/400?random=9' WHERE codigo = 'P009' AND (url_imagen IS NULL OR url_imagen = '')");

            agregarColumnaSiNoExiste(stmt, "ventas", "metodo_pago TEXT DEFAULT 'Efectivo'");
            agregarColumnaSiNoExiste(stmt, "ventas", "monto_recibido REAL DEFAULT 0.0");
            agregarColumnaSiNoExiste(stmt, "ventas", "vuelto REAL DEFAULT 0.0");
            agregarColumnaSiNoExiste(stmt, "ventas", "referencia TEXT DEFAULT ''");
            agregarColumnaSiNoExiste(stmt, "ventas", "estado_pago TEXT DEFAULT 'PENDIENTE_VERIFICACION'");
            agregarColumnaSiNoExiste(stmt, "ventas", "terminal_id TEXT DEFAULT ''");
            agregarColumnaSiNoExiste(stmt, "ventas", "cliente_id INTEGER NULL");
            agregarColumnaSiNoExiste(stmt, "detalles_venta", "talla TEXT DEFAULT ''");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_ventas_metodo_referencia " +
                    "ON ventas(metodo_pago, referencia) WHERE trim(referencia) <> ''");

            seedData(stmt);
            migrarTallasAFormatoConStock(stmt);
        }
    }

    private static void agregarColumnaSiNoExiste(Statement stmt, String tabla, String definicion) throws SQLException {
        try {
            stmt.execute("ALTER TABLE " + tabla + " ADD COLUMN " + definicion);
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) {
                throw e;
            }
        }
    }

    private static void migrarTallasAFormatoConStock(Statement stmt) {
        try (ResultSet rs = stmt.executeQuery("SELECT id, stock, tallas FROM productos WHERE tallas IS NOT NULL AND trim(tallas) <> '' AND tallas NOT LIKE '%:%'")) {
            java.util.List<Object[]> filas = new java.util.ArrayList<>();
            while (rs.next()) {
                filas.add(new Object[]{rs.getInt("id"), rs.getInt("stock"), rs.getString("tallas")});
            }
            for (Object[] fila : filas) {
                int id = (int) fila[0];
                int stock = (int) fila[1];
                String tallasStr = (String) fila[2];
                String[] tallasArr = tallasStr.split("[,;]+");
                java.util.List<String> validas = new java.util.ArrayList<>();
                for (String t : tallasArr) {
                    if (!t.trim().isEmpty()) validas.add(t.trim());
                }
                if (!validas.isEmpty()) {
                    int cantBase = stock / validas.size();
                    int resto = stock % validas.size();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < validas.size(); i++) {
                        if (i > 0) sb.append(", ");
                        int cant = cantBase + (i == 0 ? resto : 0);
                        sb.append(validas.get(i)).append(":").append(Math.max(0, cant));
                    }
                    try (PreparedStatement up = stmt.getConnection().prepareStatement("UPDATE productos SET tallas = ? WHERE id = ?")) {
                        up.setString(1, sb.toString());
                        up.setInt(2, id);
                        up.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DB Migracion Tallas] Info: " + e.getMessage());
        }
    }

    private static void seedData(Statement stmt) throws SQLException {
        // Verificar si hay datos
        var rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM productos");
        if (rs.next() && rs.getInt("count") == 0) {
            String[] semillas = {
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P001', 'Zapato Oxford', 'Elegante', 58, 120.00, '38:5, 39:10, 40:15, 42:8, 44:20')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P002', 'Zapatillas Run X', 'Deportivo', 42, 150.50, '39:8, 40:12, 41:10, 42:7, 44:5')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P003', 'Botas Seguridad', 'Industrial', 35, 180.00, '40:10, 41:10, 42:10, 44:5')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P004', 'Mocasines', 'Casual', 34, 90.00, '38:6, 39:8, 40:10, 42:6, 44:4')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P005', 'Tenis Urbanos', 'Urbano', 50, 110.00, '39:10, 40:15, 41:15, 42:10')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P006', 'Sandalias Verano', 'Temporada', 60, 45.00, '38:15, 39:15, 40:15, 41:15')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P007', 'Botines Cuero', 'Elegante', 38, 160.00, '39:8, 40:10, 41:10, 42:10')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P008', 'Zapatos Vestir', 'Elegante', 40, 135.00, '39:10, 40:10, 41:10, 42:10')",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio, tallas) VALUES ('P009', 'Deportivos', 'Deportivo', 40, 95.00, '40:10, 41:10, 42:10, 44:10')"
            };
            for (String sql : semillas) {
                stmt.execute(sql);
            }
        }
    }

}
