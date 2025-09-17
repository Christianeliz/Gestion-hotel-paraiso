package com.hotel.dao;

import com.hotel.model.Habitacion;
import com.hotel.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Habitacion - Operaciones CRUD para la entidad Habitacion
 */
public class HabitacionDAO {
    
    public boolean crear(Habitacion habitacion) throws SQLException {
        String sql = "INSERT INTO habitaciones (numero, tipo, estado, precio_por_noche, capacidad, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, habitacion.getNumero());
            pstmt.setString(2, habitacion.getTipo().name());
            pstmt.setString(3, habitacion.getEstado().name());
            pstmt.setBigDecimal(4, habitacion.getPrecioPorNoche());
            pstmt.setInt(5, habitacion.getCapacidad());
            pstmt.setString(6, habitacion.getDescripcion());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                // Obtener el último ID insertado
                String lastIdSql = "SELECT last_insert_rowid()";
                try (PreparedStatement lastIdStmt = conn.prepareStatement(lastIdSql);
                     ResultSet rs = lastIdStmt.executeQuery()) {
                    if (rs.next()) {
                        habitacion.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    public Habitacion buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM habitaciones WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;
        }
    }
    
    public Habitacion buscarPorNumero(String numero) throws SQLException {
        String sql = "SELECT * FROM habitaciones WHERE numero = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numero);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapearResultSet(rs);
            }
            return null;
        }
    }
    
    public List<Habitacion> listarTodas() throws SQLException {
        String sql = "SELECT * FROM habitaciones ORDER BY numero";
        List<Habitacion> habitaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                habitaciones.add(mapearResultSet(rs));
            }
        }
        
        return habitaciones;
    }
    
    public List<Habitacion> buscarDisponibles(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = """
            SELECT h.* FROM habitaciones h
            WHERE h.estado = 'DISPONIBLE'
            AND h.id NOT IN (
                SELECT r.habitacion_id FROM reservaciones r
                WHERE r.estado IN ('PENDIENTE', 'CONFIRMADA')
                AND NOT (r.fecha_check_out <= ? OR r.fecha_check_in >= ?)
            )
            ORDER BY h.numero
        """;
        
        List<Habitacion> habitaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                habitaciones.add(mapearResultSet(rs));
            }
        }
        
        return habitaciones;
    }
    
    public List<Habitacion> filtrarPor(Habitacion.TipoHabitacion tipo, Habitacion.EstadoHabitacion estado) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM habitaciones WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        
        if (tipo != null) {
            sql.append(" AND tipo = ?");
            parametros.add(tipo.name());
        }
        
        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }
        
        sql.append(" ORDER BY numero");
        
        List<Habitacion> habitaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                habitaciones.add(mapearResultSet(rs));
            }
        }
        
        return habitaciones;
    }
    
    public boolean actualizar(Habitacion habitacion) throws SQLException {
        String sql = "UPDATE habitaciones SET numero = ?, tipo = ?, estado = ?, precio_por_noche = ?, capacidad = ?, descripcion = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, habitacion.getNumero());
            pstmt.setString(2, habitacion.getTipo().name());
            pstmt.setString(3, habitacion.getEstado().name());
            pstmt.setBigDecimal(4, habitacion.getPrecioPorNoche());
            pstmt.setInt(5, habitacion.getCapacidad());
            pstmt.setString(6, habitacion.getDescripcion());
            pstmt.setInt(7, habitacion.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean eliminar(int id) throws SQLException {
        // Verificar si la habitación tiene reservaciones activas
        String checkSql = "SELECT COUNT(*) FROM reservaciones WHERE habitacion_id = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("No se puede eliminar la habitación. Tiene reservaciones activas.");
            }
        }
        
        String sql = "DELETE FROM habitaciones WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    private Habitacion mapearResultSet(ResultSet rs) throws SQLException {
        return new Habitacion(
            rs.getInt("id"),
            rs.getString("numero"),
            Habitacion.TipoHabitacion.valueOf(rs.getString("tipo")),
            Habitacion.EstadoHabitacion.valueOf(rs.getString("estado")),
            rs.getBigDecimal("precio_por_noche"),
            rs.getInt("capacidad"),
            rs.getString("descripcion")
        );
    }
}