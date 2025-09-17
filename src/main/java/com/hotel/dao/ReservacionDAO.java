package com.hotel.dao;

import com.hotel.model.Reservacion;
import com.hotel.model.Cliente;
import com.hotel.model.Habitacion;
import com.hotel.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Reservacion - Operaciones CRUD para la entidad Reservacion
 */
public class ReservacionDAO {
    private final ClienteDAO clienteDAO;
    private final HabitacionDAO habitacionDAO;
    
    public ReservacionDAO() {
        this.clienteDAO = new ClienteDAO();
        this.habitacionDAO = new HabitacionDAO();
    }
    
    public boolean registrar(Reservacion reservacion) throws SQLException {
        // Validar disponibilidad antes de registrar
        if (!esHabitacionDisponible(reservacion.getHabitacionId(), 
                                   reservacion.getFechaCheckIn(), 
                                   reservacion.getFechaCheckOut(), 
                                   0)) {
            throw new SQLException("La habitación no está disponible para las fechas seleccionadas");
        }
        
        String sql = """
            INSERT INTO reservaciones (cliente_id, habitacion_id, fecha_check_in, fecha_check_out, 
                                     estado, costo_total, observaciones, fecha_reservacion) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, reservacion.getClienteId());
            pstmt.setInt(2, reservacion.getHabitacionId());
            pstmt.setString(3, reservacion.getFechaCheckIn().toString());
            pstmt.setString(4, reservacion.getFechaCheckOut().toString());
            pstmt.setString(5, reservacion.getEstado().name());
            pstmt.setBigDecimal(6, reservacion.getCostoTotal());
            pstmt.setString(7, reservacion.getObservaciones());
            pstmt.setString(8, reservacion.getFechaReservacion().toString());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        reservacion.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    public Reservacion buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM reservaciones WHERE id = ?";
        
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
    
    public List<Reservacion> listarTodas() throws SQLException {
        String sql = "SELECT * FROM reservaciones ORDER BY fecha_check_in DESC";
        List<Reservacion> reservaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reservaciones.add(mapearResultSet(rs));
            }
        }
        
        return reservaciones;
    }
    
    public List<Reservacion> buscarPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT * FROM reservaciones WHERE cliente_id = ? ORDER BY fecha_check_in DESC";
        List<Reservacion> reservaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reservaciones.add(mapearResultSet(rs));
            }
        }
        
        return reservaciones;
    }
    
    public List<Reservacion> buscarPorHabitacion(int habitacionId) throws SQLException {
        String sql = "SELECT * FROM reservaciones WHERE habitacion_id = ? ORDER BY fecha_check_in DESC";
        List<Reservacion> reservaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, habitacionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reservaciones.add(mapearResultSet(rs));
            }
        }
        
        return reservaciones;
    }
    
    public List<Reservacion> buscarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = """
            SELECT * FROM reservaciones 
            WHERE NOT (fecha_check_out <= ? OR fecha_check_in >= ?)
            ORDER BY fecha_check_in
        """;
        List<Reservacion> reservaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fechaInicio.toString());
            pstmt.setString(2, fechaFin.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reservaciones.add(mapearResultSet(rs));
            }
        }
        
        return reservaciones;
    }
    
    public List<Reservacion> buscarPorEstado(Reservacion.EstadoReservacion estado) throws SQLException {
        String sql = "SELECT * FROM reservaciones WHERE estado = ? ORDER BY fecha_check_in";
        List<Reservacion> reservaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reservaciones.add(mapearResultSet(rs));
            }
        }
        
        return reservaciones;
    }
    
    public boolean actualizar(Reservacion reservacion) throws SQLException {
        // Validar disponibilidad si se cambiaron las fechas o habitación
        if (!esHabitacionDisponible(reservacion.getHabitacionId(), 
                                   reservacion.getFechaCheckIn(), 
                                   reservacion.getFechaCheckOut(), 
                                   reservacion.getId())) {
            throw new SQLException("La habitación no está disponible para las fechas seleccionadas");
        }
        
        String sql = """
            UPDATE reservaciones SET cliente_id = ?, habitacion_id = ?, fecha_check_in = ?, 
                                   fecha_check_out = ?, estado = ?, costo_total = ?, observaciones = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservacion.getClienteId());
            pstmt.setInt(2, reservacion.getHabitacionId());
            pstmt.setString(3, reservacion.getFechaCheckIn().toString());
            pstmt.setString(4, reservacion.getFechaCheckOut().toString());
            pstmt.setString(5, reservacion.getEstado().name());
            pstmt.setBigDecimal(6, reservacion.getCostoTotal());
            pstmt.setString(7, reservacion.getObservaciones());
            pstmt.setInt(8, reservacion.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean cancelar(int id) throws SQLException {
        String sql = "UPDATE reservaciones SET estado = 'CANCELADA' WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM reservaciones WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean esHabitacionDisponible(int habitacionId, LocalDate fechaInicio, 
                                        LocalDate fechaFin, int reservacionExcluir) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservaciones 
            WHERE habitacion_id = ? 
            AND estado IN ('PENDIENTE', 'CONFIRMADA')
            AND NOT (fecha_check_out <= ? OR fecha_check_in >= ?)
            AND id != ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, habitacionId);
            pstmt.setString(2, fechaInicio.toString());
            pstmt.setString(3, fechaFin.toString());
            pstmt.setInt(4, reservacionExcluir);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        
        return false;
    }
    
    private Reservacion mapearResultSet(ResultSet rs) throws SQLException {
        Reservacion reservacion = new Reservacion(
            rs.getInt("id"),
            rs.getInt("cliente_id"),
            rs.getInt("habitacion_id"),
            LocalDate.parse(rs.getString("fecha_check_in")),
            LocalDate.parse(rs.getString("fecha_check_out")),
            Reservacion.EstadoReservacion.valueOf(rs.getString("estado")),
            rs.getBigDecimal("costo_total"),
            rs.getString("observaciones"),
            LocalDate.parse(rs.getString("fecha_reservacion"))
        );
        
        // Cargar cliente y habitación relacionados
        try {
            Cliente cliente = clienteDAO.buscarPorId(reservacion.getClienteId());
            Habitacion habitacion = habitacionDAO.buscarPorId(reservacion.getHabitacionId());
            
            reservacion.setCliente(cliente);
            reservacion.setHabitacion(habitacion);
        } catch (SQLException e) {
            // Log error pero no fallar completamente
            System.err.println("Error cargando datos relacionados para reservación " + reservacion.getId() + ": " + e.getMessage());
        }
        
        return reservacion;
    }
}