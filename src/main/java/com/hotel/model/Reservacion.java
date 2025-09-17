package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Clase Reservacion - Representa una reservación en el hotel
 */
public class Reservacion {
    public enum EstadoReservacion {
        PENDIENTE("Pendiente"),
        CONFIRMADA("Confirmada"),
        CANCELADA("Cancelada"),
        COMPLETADA("Completada"),
        NO_SHOW("No Show");
        
        private final String nombre;
        
        EstadoReservacion(String nombre) {
            this.nombre = nombre;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        @Override
        public String toString() {
            return nombre;
        }
    }
    
    private int id;
    private int clienteId;
    private int habitacionId;
    private LocalDate fechaCheckIn;
    private LocalDate fechaCheckOut;
    private EstadoReservacion estado;
    private BigDecimal costoTotal;
    private String observaciones;
    private LocalDate fechaReservacion;
    
    // Referencias para mostrar información completa
    private Cliente cliente;
    private Habitacion habitacion;
    
    public Reservacion() {
        this.estado = EstadoReservacion.PENDIENTE;
        this.fechaReservacion = LocalDate.now();
    }
    
    public Reservacion(int clienteId, int habitacionId, LocalDate fechaCheckIn, 
                      LocalDate fechaCheckOut, String observaciones) {
        this.clienteId = clienteId;
        this.habitacionId = habitacionId;
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
        this.observaciones = observaciones;
        this.estado = EstadoReservacion.PENDIENTE;
        this.fechaReservacion = LocalDate.now();
    }
    
    public Reservacion(int id, int clienteId, int habitacionId, LocalDate fechaCheckIn,
                      LocalDate fechaCheckOut, EstadoReservacion estado, BigDecimal costoTotal,
                      String observaciones, LocalDate fechaReservacion) {
        this.id = id;
        this.clienteId = clienteId;
        this.habitacionId = habitacionId;
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
        this.estado = estado;
        this.costoTotal = costoTotal;
        this.observaciones = observaciones;
        this.fechaReservacion = fechaReservacion;
    }
    
    // Métodos de negocio
    public BigDecimal calcularCosto() {
        if (habitacion != null && fechaCheckIn != null && fechaCheckOut != null) {
            long dias = ChronoUnit.DAYS.between(fechaCheckIn, fechaCheckOut);
            if (dias <= 0) {
                dias = 1; // Mínimo una noche
            }
            return habitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(dias));
        }
        return BigDecimal.ZERO;
    }
    
    public long getDuracionEnDias() {
        if (fechaCheckIn != null && fechaCheckOut != null) {
            long dias = ChronoUnit.DAYS.between(fechaCheckIn, fechaCheckOut);
            return dias > 0 ? dias : 1;
        }
        return 0;
    }
    
    public boolean esValida() {
        return fechaCheckIn != null && fechaCheckOut != null && 
               fechaCheckIn.isBefore(fechaCheckOut) && 
               !fechaCheckIn.isBefore(LocalDate.now());
    }
    
    public boolean seSuperponeConFechas(LocalDate inicio, LocalDate fin) {
        if (fechaCheckIn == null || fechaCheckOut == null || inicio == null || fin == null) {
            return false;
        }
        return !(fechaCheckOut.isBefore(inicio) || inicio.equals(fechaCheckOut) || 
                fechaCheckIn.isAfter(fin) || fin.equals(fechaCheckIn));
    }
    
    public void confirmar() {
        this.estado = EstadoReservacion.CONFIRMADA;
    }
    
    public void cancelar() {
        this.estado = EstadoReservacion.CANCELADA;
    }
    
    public void completar() {
        this.estado = EstadoReservacion.COMPLETADA;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }
    
    public int getHabitacionId() {
        return habitacionId;
    }
    
    public void setHabitacionId(int habitacionId) {
        this.habitacionId = habitacionId;
    }
    
    public LocalDate getFechaCheckIn() {
        return fechaCheckIn;
    }
    
    public void setFechaCheckIn(LocalDate fechaCheckIn) {
        this.fechaCheckIn = fechaCheckIn;
    }
    
    public LocalDate getFechaCheckOut() {
        return fechaCheckOut;
    }
    
    public void setFechaCheckOut(LocalDate fechaCheckOut) {
        this.fechaCheckOut = fechaCheckOut;
    }
    
    public EstadoReservacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoReservacion estado) {
        this.estado = estado;
    }
    
    public BigDecimal getCostoTotal() {
        return costoTotal;
    }
    
    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDate getFechaReservacion() {
        return fechaReservacion;
    }
    
    public void setFechaReservacion(LocalDate fechaReservacion) {
        this.fechaReservacion = fechaReservacion;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Habitacion getHabitacion() {
        return habitacion;
    }
    
    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }
    
    @Override
    public String toString() {
        String clienteNombre = cliente != null ? cliente.getNombreCompleto() : "Cliente ID: " + clienteId;
        String habitacionNum = habitacion != null ? habitacion.getNumero() : "Habitación ID: " + habitacionId;
        return String.format("Reservación %d - %s - Hab. %s (%s a %s)", 
                           id, clienteNombre, habitacionNum, fechaCheckIn, fechaCheckOut);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservacion that = (Reservacion) obj;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}