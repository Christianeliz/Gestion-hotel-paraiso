package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase DatabaseConnection - Maneja la conexión con la base de datos SQLite
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:hotel_paraiso.db";
    private static Connection connection = null;
    
    private DatabaseConnection() {
        // Constructor privado para patrón Singleton
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                
                // Habilitar claves foráneas
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.close();
                
                System.out.println("Conexión a SQLite establecida: " + DB_URL);
            } catch (ClassNotFoundException e) {
                System.err.println("Driver SQLite no encontrado: " + e.getMessage());
                throw new SQLException("Driver SQLite no disponible", e);
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión a SQLite cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    public static void initializeDatabase() throws SQLException {
        Connection conn = getConnection();
        
        // Crear tabla clientes
        String createClientesTable = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                dni TEXT UNIQUE NOT NULL,
                telefono TEXT,
                email TEXT
            )
        """;
        
        // Crear tabla habitaciones
        String createHabitacionesTable = """
            CREATE TABLE IF NOT EXISTS habitaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT UNIQUE NOT NULL,
                tipo TEXT NOT NULL,
                estado TEXT NOT NULL DEFAULT 'DISPONIBLE',
                precio_por_noche DECIMAL(10,2) NOT NULL,
                capacidad INTEGER NOT NULL,
                descripcion TEXT
            )
        """;
        
        // Crear tabla reservaciones
        String createReservacionesTable = """
            CREATE TABLE IF NOT EXISTS reservaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                habitacion_id INTEGER NOT NULL,
                fecha_check_in DATE NOT NULL,
                fecha_check_out DATE NOT NULL,
                estado TEXT NOT NULL DEFAULT 'PENDIENTE',
                costo_total DECIMAL(10,2),
                observaciones TEXT,
                fecha_reservacion DATE NOT NULL DEFAULT CURRENT_DATE,
                FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
                FOREIGN KEY (habitacion_id) REFERENCES habitaciones (id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createClientesTable);
            stmt.execute(createHabitacionesTable);
            stmt.execute(createReservacionesTable);
            
            System.out.println("Base de datos inicializada correctamente");
        }
    }
    
    public static void insertSampleData() throws SQLException {
        Connection conn = getConnection();
        
        // Verificar si ya hay datos
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM habitaciones");
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Ya hay datos, no insertar muestras
            }
        }
        
        // Insertar habitaciones de muestra
        String insertHabitaciones = """
            INSERT INTO habitaciones (numero, tipo, estado, precio_por_noche, capacidad, descripcion) VALUES
            ('101', 'SIMPLE', 'DISPONIBLE', 50.00, 1, 'Habitación simple con cama individual'),
            ('102', 'SIMPLE', 'DISPONIBLE', 50.00, 1, 'Habitación simple con cama individual'),
            ('201', 'DOBLE', 'DISPONIBLE', 80.00, 2, 'Habitación doble con cama matrimonial'),
            ('202', 'DOBLE', 'DISPONIBLE', 80.00, 2, 'Habitación doble con dos camas individuales'),
            ('301', 'SUITE', 'DISPONIBLE', 150.00, 3, 'Suite con sala de estar y jacuzzi'),
            ('302', 'FAMILIAR', 'DISPONIBLE', 120.00, 4, 'Habitación familiar con literas')
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(insertHabitaciones);
            System.out.println("Datos de muestra insertados");
        }
    }
}