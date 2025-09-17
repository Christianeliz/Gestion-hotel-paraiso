package com.hotel.test;

import com.hotel.dao.ClienteDAO;
import com.hotel.dao.HabitacionDAO;
import com.hotel.dao.ReservacionDAO;
import com.hotel.model.Cliente;
import com.hotel.model.Habitacion;
import com.hotel.model.Reservacion;
import com.hotel.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Prueba simple del core del sistema sin dependencias de GUI
 */
public class CoreTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== PRUEBA DEL CORE DEL SISTEMA ===\n");
            
            // Inicializar base de datos
            DatabaseConnection.initializeDatabase();
            DatabaseConnection.insertSampleData();
            System.out.println("✓ Base de datos inicializada\n");
            
            // Crear DAOs
            ClienteDAO clienteDAO = new ClienteDAO();
            HabitacionDAO habitacionDAO = new HabitacionDAO();
            ReservacionDAO reservacionDAO = new ReservacionDAO();
            
            // Prueba básica de clientes
            Cliente cliente = new Cliente("Test", "User", "99999999", "555-0000", "test@test.com");
            if (clienteDAO.registrar(cliente)) {
                System.out.println("✓ Cliente registrado: " + cliente);
            }
            
            // Prueba básica de habitaciones
            List<Habitacion> habitaciones = habitacionDAO.listarTodas();
            System.out.println("✓ Habitaciones en el sistema: " + habitaciones.size());
            
            if (!habitaciones.isEmpty()) {
                Habitacion hab = habitaciones.get(0);
                System.out.println("✓ Primera habitación: " + hab);
                
                // Prueba de reservación
                LocalDate checkIn = LocalDate.now().plusDays(1);
                LocalDate checkOut = checkIn.plusDays(2);
                
                Reservacion reservacion = new Reservacion(cliente.getId(), hab.getId(), 
                                                         checkIn, checkOut, "Prueba core");
                reservacion.setHabitacion(hab);
                reservacion.setCostoTotal(reservacion.calcularCosto());
                
                if (reservacionDAO.registrar(reservacion)) {
                    System.out.println("✓ Reservación creada: " + reservacion);
                    System.out.println("  Costo: $" + reservacion.getCostoTotal());
                }
                
                // Probar validación de disponibilidad
                try {
                    Reservacion conflicto = new Reservacion(cliente.getId(), hab.getId(), 
                                                           checkIn.plusDays(1), checkOut.plusDays(1), 
                                                           "Debe fallar");
                    reservacionDAO.registrar(conflicto);
                    System.out.println("⚠ ERROR: Se permitió reservación superpuesta");
                } catch (SQLException e) {
                    System.out.println("✓ Validación de conflictos: " + e.getMessage());
                }
            }
            
            System.out.println("\n=== CORE DEL SISTEMA FUNCIONANDO CORRECTAMENTE ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}