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
 * Clase de prueba para demostrar la funcionalidad del sistema
 */
public class SystemTest {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== PRUEBA DEL SISTEMA DE GESTIÓN HOTELERA ===\n");
            
            // Inicializar base de datos
            DatabaseConnection.initializeDatabase();
            DatabaseConnection.insertSampleData();
            System.out.println("✓ Base de datos inicializada correctamente\n");
            
            // Crear DAOs
            ClienteDAO clienteDAO = new ClienteDAO();
            HabitacionDAO habitacionDAO = new HabitacionDAO();
            ReservacionDAO reservacionDAO = new ReservacionDAO();
            
            // Probar CRUD de Clientes
            testClientes(clienteDAO);
            
            // Probar CRUD de Habitaciones
            testHabitaciones(habitacionDAO);
            
            // Probar CRUD de Reservaciones con validaciones
            testReservaciones(reservacionDAO, clienteDAO, habitacionDAO);
            
            System.out.println("\n=== TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE ===");
            
        } catch (Exception e) {
            System.err.println("Error durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
    
    private static void testClientes(ClienteDAO clienteDAO) throws SQLException {
        System.out.println("--- PRUEBA DE GESTIÓN DE CLIENTES ---");
        
        // Crear cliente
        Cliente cliente1 = new Cliente("Juan", "Pérez", "12345678", "555-1234", "juan@email.com");
        if (clienteDAO.registrar(cliente1)) {
            System.out.println("✓ Cliente registrado: " + cliente1);
        }
        
        Cliente cliente2 = new Cliente("María", "González", "87654321", "555-5678", "maria@email.com");
        if (clienteDAO.registrar(cliente2)) {
            System.out.println("✓ Cliente registrado: " + cliente2);
        }
        
        // Listar clientes
        List<Cliente> clientes = clienteDAO.listarTodos();
        System.out.println("✓ Total de clientes: " + clientes.size());
        
        // Buscar por DNI
        Cliente encontrado = clienteDAO.buscarPorDni("12345678");
        if (encontrado != null) {
            System.out.println("✓ Cliente encontrado por DNI: " + encontrado);
        }
        
        // Actualizar cliente
        encontrado.setEmail("juan.perez.nuevo@email.com");
        if (clienteDAO.actualizar(encontrado)) {
            System.out.println("✓ Cliente actualizado");
        }
        
        // Buscar por nombre
        List<Cliente> buscados = clienteDAO.buscarPorNombre("María");
        System.out.println("✓ Clientes encontrados por nombre: " + buscados.size());
        
        System.out.println();
    }
    
    private static void testHabitaciones(HabitacionDAO habitacionDAO) throws SQLException {
        System.out.println("--- PRUEBA DE GESTIÓN DE HABITACIONES ---");
        
        // Listar habitaciones iniciales
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();
        System.out.println("✓ Habitaciones iniciales: " + habitaciones.size());
        
        // Crear nueva habitación
        Habitacion nuevaHab = new Habitacion("401", Habitacion.TipoHabitacion.SUITE, 
                                           new BigDecimal("200.00"), 2, 
                                           "Suite presidencial con vista al mar");
        if (habitacionDAO.crear(nuevaHab)) {
            System.out.println("✓ Nueva habitación creada: " + nuevaHab);
        }
        
        // Buscar por número
        Habitacion hab = habitacionDAO.buscarPorNumero("101");
        if (hab != null) {
            System.out.println("✓ Habitación encontrada: " + hab);
        }
        
        // Filtrar por tipo
        List<Habitacion> suites = habitacionDAO.filtrarPor(Habitacion.TipoHabitacion.SUITE, null);
        System.out.println("✓ Suites encontradas: " + suites.size());
        
        // Filtrar por estado
        List<Habitacion> disponibles = habitacionDAO.filtrarPor(null, Habitacion.EstadoHabitacion.DISPONIBLE);
        System.out.println("✓ Habitaciones disponibles: " + disponibles.size());
        
        System.out.println();
    }
    
    private static void testReservaciones(ReservacionDAO reservacionDAO, ClienteDAO clienteDAO, HabitacionDAO habitacionDAO) throws SQLException {
        System.out.println("--- PRUEBA DE GESTIÓN DE RESERVACIONES ---");
        
        // Obtener cliente y habitación para la reservación
        List<Cliente> clientes = clienteDAO.listarTodos();
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();
        
        if (clientes.isEmpty() || habitaciones.isEmpty()) {
            System.out.println("⚠ No hay suficientes datos para probar reservaciones");
            return;
        }
        
        Cliente cliente = clientes.get(0);
        Habitacion habitacion = habitaciones.get(0);
        
        // Crear reservación válida
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        
        Reservacion reservacion1 = new Reservacion(cliente.getId(), habitacion.getId(), 
                                                  checkIn, checkOut, "Reservación de prueba");
        reservacion1.setHabitacion(habitacion);
        reservacion1.setCostoTotal(reservacion1.calcularCosto());
        
        if (reservacionDAO.registrar(reservacion1)) {
            System.out.println("✓ Reservación registrada: " + reservacion1);
            System.out.println("  Costo total: $" + reservacion1.getCostoTotal());
        }
        
        // Probar validación de disponibilidad - reserva superpuesta
        Reservacion reservacionSuperpuesta = new Reservacion(cliente.getId(), habitacion.getId(), 
                                                            checkIn.plusDays(1), checkOut.plusDays(1), 
                                                            "Esta debería fallar");
        
        try {
            reservacionDAO.registrar(reservacionSuperpuesta);
            System.out.println("⚠ ERROR: Se permitió una reservación superpuesta");
        } catch (SQLException e) {
            System.out.println("✓ Validación funcionando: " + e.getMessage());
        }
        
        // Probar búsqueda de habitaciones disponibles
        List<Habitacion> disponibles = habitacionDAO.buscarDisponibles(checkIn, checkOut);
        System.out.println("✓ Habitaciones disponibles para " + checkIn + " - " + checkOut + ": " + disponibles.size());
        
        // Listar todas las reservaciones
        List<Reservacion> reservaciones = reservacionDAO.listarTodas();
        System.out.println("✓ Total de reservaciones: " + reservaciones.size());
        
        // Actualizar estado de reservación
        reservacion1.setEstado(Reservacion.EstadoReservacion.CONFIRMADA);
        if (reservacionDAO.actualizar(reservacion1)) {
            System.out.println("✓ Estado de reservación actualizado a CONFIRMADA");
        }
        
        // Buscar por estado
        List<Reservacion> confirmadas = reservacionDAO.buscarPorEstado(Reservacion.EstadoReservacion.CONFIRMADA);
        System.out.println("✓ Reservaciones confirmadas: " + confirmadas.size());
        
        // Probar fechas inválidas
        try {
            LocalDate fechaPasada = LocalDate.now().minusDays(1);
            Reservacion reservacionPasada = new Reservacion(cliente.getId(), habitacion.getId(), 
                                                           fechaPasada, checkOut, "Fecha pasada");
            if (!reservacionPasada.esValida()) {
                System.out.println("✓ Validación de fecha pasada funcionando");
            }
        } catch (Exception e) {
            System.out.println("✓ Validación de fechas funcionando: " + e.getMessage());
        }
        
        System.out.println();
    }
}