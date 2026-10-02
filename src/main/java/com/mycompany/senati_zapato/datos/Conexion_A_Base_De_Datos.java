package com.mycompany.senati_zapato.datos;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion_A_Base_De_Datos {
    private static final String APP_DIR = System.getProperty("user.dir") + java.io.File.separator + "Datos_SenatiZapato";
    private static final String OLD_APP_DIR = System.getProperty("user.home") + java.io.File.separator + "SenatiZapato";
    private static final String DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "senati_zapato.db";
    private static Connection connection;

    private Conexion_A_Base_De_Datos() {}

    public static synchronized Connection Get_Conexion() throws SQLException {
        if (connection == null || connection.isClosed()) {
            java.io.File appDirFile = new java.io.File(APP_DIR);
            if (!appDirFile.exists()) {
                appDirFile.mkdirs();
            }
            java.io.File dbFile = new java.io.File(APP_DIR, "senati_zapato.db");
            
            // 1. Intentar migrar desde la raíz del proyecto
            java.io.File rootDb = new java.io.File(System.getProperty("user.dir"), "senati_zapato.db");
            if (rootDb.exists()) {
                boolean shouldMigrate = false;
                if (!dbFile.exists()) {
                    shouldMigrate = true;
                } else {
                    // Si ya existe la DB portable, pero la de la raíz tiene más productos o ventas, migrarla para no perder datos
                    try {
                        int rootProducts = obtenerCountTablas("jdbc:sqlite:" + rootDb.getAbsolutePath(), "productos");
                        int portableProducts = obtenerCountTablas("jdbc:sqlite:" + dbFile.getAbsolutePath(), "productos");
                        int rootVentas = obtenerCountTablas("jdbc:sqlite:" + rootDb.getAbsolutePath(), "ventas");
                        int portableVentas = obtenerCountTablas("jdbc:sqlite:" + dbFile.getAbsolutePath(), "ventas");
                        
                        if (rootProducts > portableProducts || rootVentas > portableVentas) {
                            shouldMigrate = true;
                        }
                    } catch (Exception e) {
                        // Fallback a comparar tamaño
                        try {
                            if (rootDb.length() > dbFile.length()) {
                                shouldMigrate = true;
                            }
                        } catch (Exception ignored) {}
                    }
                }

                if (shouldMigrate) {
                    try {
                        if (dbFile.exists()) {
                            // Crear un backup por seguridad de la base de datos portable existente
                            java.io.File backup = new java.io.File(APP_DIR, "senati_zapato_backup_" + System.currentTimeMillis() + ".db");
                            java.nio.file.Files.copy(dbFile.toPath(), backup.toPath());
                        }
                        java.nio.file.Files.copy(rootDb.toPath(), dbFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Base de datos de la raíz del proyecto migrada a la versión portable.");
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // 2. Si aún no existe la DB portable, intentar desde user.home
            if (!dbFile.exists()) {
                java.io.File oldDb = new java.io.File(OLD_APP_DIR, "senati_zapato.db");
                if (oldDb.exists()) {
                    try {
                        java.nio.file.Files.copy(oldDb.toPath(), dbFile.toPath());
                        System.out.println("Base de datos migrada desde user.home a la versión portable.");
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver SQLite JDBC no encontrado: " + e.getMessage());
            }
            connection = DriverManager.getConnection(DB_URL);
            Inicializar_Base_De_Datos();
        }
        return connection;
    }

    private static void Inicializar_Base_De_Datos() {
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
                "referencia TEXT DEFAULT '')";

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
            stmt.execute(sqlVentas);
            stmt.execute(sqlDetalles);

            // Migrar productos existentes si es necesario
            try { stmt.execute("ALTER TABLE productos ADD COLUMN tallas TEXT DEFAULT ''"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE productos ADD COLUMN url_imagen TEXT DEFAULT ''"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE productos ADD COLUMN estado TEXT DEFAULT 'Disponible'"); } catch (SQLException e) {}

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

            // Migrar ventas existentes si es necesario
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN metodo_pago TEXT DEFAULT 'Efectivo'"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN monto_recibido REAL DEFAULT 0.0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN vuelto REAL DEFAULT 0.0"); } catch (SQLException e) {}
            try { stmt.execute("ALTER TABLE ventas ADD COLUMN referencia TEXT DEFAULT ''"); } catch (SQLException e) {}

            seedData(stmt);
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }

    private static void seedData(Statement stmt) throws SQLException {
        // Verificar si hay datos
        var rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM productos");
        if (rs.next() && rs.getInt("count") == 0) {
            String[] semillas = {
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P001', 'Zapato Oxford', 'Elegante', 45, 120.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P002', 'Zapatillas Run X', 'Deportivo', 12, 150.50)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P003', 'Botas Seguridad', 'Industrial', 8, 180.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P004', 'Mocasines', 'Casual', 34, 90.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P005', 'Tenis Urbanos', 'Urbano', 50, 110.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P006', 'Sandalias Verano', 'Temporada', 120, 45.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P007', 'Botines Cuero', 'Elegante', 30, 160.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P008', 'Zapatos Vestir', 'Elegante', 25, 135.00)",
                "INSERT INTO productos (codigo, nombre, categoria, stock, precio) VALUES ('P009', 'Deportivos', 'Deportivo', 40, 95.00)"
            };
            for (String sql : semillas) {
                stmt.execute(sql);
            }
        }
    }

    private static int obtenerCountTablas(String jdbcUrl, String tabla) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
