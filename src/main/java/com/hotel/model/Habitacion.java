package com.hotel.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Clase Habitacion - Representa una habitación del hotel
 */
public class Habitacion {
    public enum TipoHabitacion {
        SIMPLE("Simple"),
        DOBLE("Doble"),
        SUITE("Suite"),
        FAMILIAR("Familiar");
        
        private final String nombre;
        
        TipoHabitacion(String nombre) {
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
    
    public enum EstadoHabitacion {
        DISPONIBLE("Disponible"),
        OCUPADA("Ocupada"),
        MANTENIMIENTO("En Mantenimiento"),
        LIMPIEZA("En Limpieza");
        
        private final String nombre;
        
        EstadoHabitacion(String nombre) {
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
    private String numero;
    private TipoHabitacion tipo;
    private EstadoHabitacion estado;
    private BigDecimal precioPorNoche;
    private int capacidad;
    private String descripcion;
    
    public Habitacion() {
        this.estado = EstadoHabitacion.DISPONIBLE;
    }
    
    public Habitacion(String numero, TipoHabitacion tipo, BigDecimal precioPorNoche, 
                     int capacidad, String descripcion) {
        this.numero = numero;
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
        this.capacidad = capacidad;
        this.descripcion = descripcion;
        this.estado = EstadoHabitacion.DISPONIBLE;
    }
    
    public Habitacion(int id, String numero, TipoHabitacion tipo, EstadoHabitacion estado,
                     BigDecimal precioPorNoche, int capacidad, String descripcion) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.estado = estado;
        this.precioPorNoche = precioPorNoche;
        this.capacidad = capacidad;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public TipoHabitacion getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
    }
    
    public EstadoHabitacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoHabitacion estado) {
        this.estado = estado;
    }
    
    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }
    
    public void setPrecioPorNoche(BigDecimal precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }
    
    public int getCapacidad() {
        return capacidad;
    }
    
    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public boolean estaDisponible() {
        return estado == EstadoHabitacion.DISPONIBLE;
    }
    
    @Override
    public String toString() {
        return String.format("Habitación %s (%s) - %s", numero, tipo.getNombre(), estado.getNombre());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Habitacion that = (Habitacion) obj;
        return Objects.equals(numero, that.numero);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}