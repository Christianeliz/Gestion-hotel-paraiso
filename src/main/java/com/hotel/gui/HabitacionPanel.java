package com.hotel.gui;

import com.hotel.dao.HabitacionDAO;
import com.hotel.model.Habitacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel para la gestión de habitaciones
 */
public class HabitacionPanel extends JPanel {
    private final HabitacionDAO habitacionDAO;
    private JTable habitacionesTable;
    private DefaultTableModel tableModel;
    private JTextField numeroField, precioPorNocheField, capacidadField, descripcionField;
    private JComboBox<Habitacion.TipoHabitacion> tipoComboBox;
    private JComboBox<Habitacion.EstadoHabitacion> estadoComboBox;
    private JComboBox<Habitacion.TipoHabitacion> filtroTipoComboBox;
    private JComboBox<Habitacion.EstadoHabitacion> filtroEstadoComboBox;
    private Habitacion habitacionSeleccionada;
    
    public HabitacionPanel() {
        this.habitacionDAO = new HabitacionDAO();
        initializeComponents();
        cargarHabitaciones();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Filtros
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Panel central - Tabla de habitaciones
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Panel derecho - Formulario
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.EAST);
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        panel.add(new JLabel("Tipo:"));
        filtroTipoComboBox = new JComboBox<>();
        filtroTipoComboBox.addItem(null); // Opción "Todos"
        for (Habitacion.TipoHabitacion tipo : Habitacion.TipoHabitacion.values()) {
            filtroTipoComboBox.addItem(tipo);
        }
        filtroTipoComboBox.addActionListener(e -> filtrarHabitaciones());
        panel.add(filtroTipoComboBox);
        
        panel.add(new JLabel("Estado:"));
        filtroEstadoComboBox = new JComboBox<>();
        filtroEstadoComboBox.addItem(null); // Opción "Todos"
        for (Habitacion.EstadoHabitacion estado : Habitacion.EstadoHabitacion.values()) {
            filtroEstadoComboBox.addItem(estado);
        }
        filtroEstadoComboBox.addActionListener(e -> filtrarHabitaciones());
        panel.add(filtroEstadoComboBox);
        
        JButton mostrarTodosButton = new JButton("Mostrar Todas");
        mostrarTodosButton.addActionListener(e -> {
            filtroTipoComboBox.setSelectedItem(null);
            filtroEstadoComboBox.setSelectedItem(null);
            cargarHabitaciones();
        });
        panel.add(mostrarTodosButton);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Habitaciones"));
        
        // Crear modelo de tabla
        String[] columnas = {"ID", "Número", "Tipo", "Estado", "Precio/Noche", "Capacidad", "Descripción"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        habitacionesTable = new JTable(tableModel);
        habitacionesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitacionesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarHabitacion();
            }
        });
        
        // Configurar anchos de columnas
        habitacionesTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        habitacionesTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Número
        habitacionesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Tipo
        habitacionesTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Estado
        habitacionesTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Precio
        habitacionesTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Capacidad
        habitacionesTable.getColumnModel().getColumn(6).setPreferredWidth(200); // Descripción
        
        JScrollPane scrollPane = new JScrollPane(habitacionesTable);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la Habitación"));
        panel.setPreferredSize(new Dimension(320, 0));
        
        // Campos del formulario
        panel.add(new JLabel("Número:"));
        numeroField = new JTextField(20);
        panel.add(numeroField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Tipo:"));
        tipoComboBox = new JComboBox<>(Habitacion.TipoHabitacion.values());
        panel.add(tipoComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Estado:"));
        estadoComboBox = new JComboBox<>(Habitacion.EstadoHabitacion.values());
        panel.add(estadoComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Precio por Noche:"));
        precioPorNocheField = new JTextField(20);
        panel.add(precioPorNocheField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Capacidad:"));
        capacidadField = new JTextField(20);
        panel.add(capacidadField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Descripción:"));
        descripcionField = new JTextField(20);
        panel.add(descripcionField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 5, 5));
        
        JButton nuevoButton = new JButton("Nueva");
        nuevoButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(nuevoButton);
        
        JButton guardarButton = new JButton("Guardar");
        guardarButton.addActionListener(e -> guardarHabitacion());
        buttonPanel.add(guardarButton);
        
        JButton actualizarButton = new JButton("Actualizar");
        actualizarButton.addActionListener(e -> actualizarHabitacion());
        buttonPanel.add(actualizarButton);
        
        JButton eliminarButton = new JButton("Eliminar");
        eliminarButton.addActionListener(e -> eliminarHabitacion());
        buttonPanel.add(eliminarButton);
        
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private void cargarHabitaciones() {
        try {
            List<Habitacion> habitaciones = habitacionDAO.listarTodas();
            actualizarTabla(habitaciones);
        } catch (SQLException e) {
            mostrarError("Error al cargar habitaciones: " + e.getMessage());
        }
    }
    
    private void filtrarHabitaciones() {
        try {
            Habitacion.TipoHabitacion tipoFiltro = (Habitacion.TipoHabitacion) filtroTipoComboBox.getSelectedItem();
            Habitacion.EstadoHabitacion estadoFiltro = (Habitacion.EstadoHabitacion) filtroEstadoComboBox.getSelectedItem();
            
            List<Habitacion> habitaciones = habitacionDAO.filtrarPor(tipoFiltro, estadoFiltro);
            actualizarTabla(habitaciones);
        } catch (SQLException e) {
            mostrarError("Error al filtrar habitaciones: " + e.getMessage());
        }
    }
    
    private void actualizarTabla(List<Habitacion> habitaciones) {
        tableModel.setRowCount(0);
        for (Habitacion habitacion : habitaciones) {
            Object[] row = {
                habitacion.getId(),
                habitacion.getNumero(),
                habitacion.getTipo().getNombre(),
                habitacion.getEstado().getNombre(),
                "$" + habitacion.getPrecioPorNoche(),
                habitacion.getCapacidad(),
                habitacion.getDescripcion()
            };
            tableModel.addRow(row);
        }
    }
    
    private void seleccionarHabitacion() {
        int selectedRow = habitacionesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int habitacionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            try {
                habitacionSeleccionada = habitacionDAO.buscarPorId(habitacionId);
                if (habitacionSeleccionada != null) {
                    llenarFormulario(habitacionSeleccionada);
                }
            } catch (SQLException e) {
                mostrarError("Error al cargar habitación: " + e.getMessage());
            }
        }
    }
    
    private void llenarFormulario(Habitacion habitacion) {
        numeroField.setText(habitacion.getNumero());
        tipoComboBox.setSelectedItem(habitacion.getTipo());
        estadoComboBox.setSelectedItem(habitacion.getEstado());
        precioPorNocheField.setText(habitacion.getPrecioPorNoche().toString());
        capacidadField.setText(String.valueOf(habitacion.getCapacidad()));
        descripcionField.setText(habitacion.getDescripcion());
    }
    
    private void limpiarFormulario() {
        numeroField.setText("");
        tipoComboBox.setSelectedIndex(0);
        estadoComboBox.setSelectedItem(Habitacion.EstadoHabitacion.DISPONIBLE);
        precioPorNocheField.setText("");
        capacidadField.setText("");
        descripcionField.setText("");
        habitacionSeleccionada = null;
        habitacionesTable.clearSelection();
    }
    
    private void guardarHabitacion() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            Habitacion habitacion = new Habitacion(
                numeroField.getText().trim(),
                (Habitacion.TipoHabitacion) tipoComboBox.getSelectedItem(),
                new BigDecimal(precioPorNocheField.getText().trim()),
                Integer.parseInt(capacidadField.getText().trim()),
                descripcionField.getText().trim()
            );
            habitacion.setEstado((Habitacion.EstadoHabitacion) estadoComboBox.getSelectedItem());
            
            if (habitacionDAO.crear(habitacion)) {
                JOptionPane.showMessageDialog(this,
                    "Habitación creada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarHabitaciones();
            } else {
                mostrarError("Error al crear la habitación");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarError("El número de habitación ya existe");
            } else {
                mostrarError("Error al crear habitación: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            mostrarError("Error en formato numérico: " + e.getMessage());
        }
    }
    
    private void actualizarHabitacion() {
        if (habitacionSeleccionada == null) {
            mostrarError("Seleccione una habitación para actualizar");
            return;
        }
        
        if (!validarFormulario()) {
            return;
        }
        
        try {
            habitacionSeleccionada.setNumero(numeroField.getText().trim());
            habitacionSeleccionada.setTipo((Habitacion.TipoHabitacion) tipoComboBox.getSelectedItem());
            habitacionSeleccionada.setEstado((Habitacion.EstadoHabitacion) estadoComboBox.getSelectedItem());
            habitacionSeleccionada.setPrecioPorNoche(new BigDecimal(precioPorNocheField.getText().trim()));
            habitacionSeleccionada.setCapacidad(Integer.parseInt(capacidadField.getText().trim()));
            habitacionSeleccionada.setDescripcion(descripcionField.getText().trim());
            
            if (habitacionDAO.actualizar(habitacionSeleccionada)) {
                JOptionPane.showMessageDialog(this,
                    "Habitación actualizada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarHabitaciones();
            } else {
                mostrarError("Error al actualizar la habitación");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarError("El número de habitación ya existe para otra habitación");
            } else {
                mostrarError("Error al actualizar habitación: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            mostrarError("Error en formato numérico: " + e.getMessage());
        }
    }
    
    private void eliminarHabitacion() {
        if (habitacionSeleccionada == null) {
            mostrarError("Seleccione una habitación para eliminar");
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar la habitación " + habitacionSeleccionada.getNumero() + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                if (habitacionDAO.eliminar(habitacionSeleccionada.getId())) {
                    JOptionPane.showMessageDialog(this,
                        "Habitación eliminada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarHabitaciones();
                } else {
                    mostrarError("Error al eliminar la habitación");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar habitación: " + e.getMessage());
            }
        }
    }
    
    private boolean validarFormulario() {
        if (numeroField.getText().trim().isEmpty()) {
            mostrarError("El número de habitación es requerido");
            numeroField.requestFocus();
            return false;
        }
        
        if (precioPorNocheField.getText().trim().isEmpty()) {
            mostrarError("El precio por noche es requerido");
            precioPorNocheField.requestFocus();
            return false;
        }
        
        try {
            BigDecimal precio = new BigDecimal(precioPorNocheField.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarError("El precio debe ser mayor a cero");
                precioPorNocheField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("El precio debe ser un número válido");
            precioPorNocheField.requestFocus();
            return false;
        }
        
        if (capacidadField.getText().trim().isEmpty()) {
            mostrarError("La capacidad es requerida");
            capacidadField.requestFocus();
            return false;
        }
        
        try {
            int capacidad = Integer.parseInt(capacidadField.getText().trim());
            if (capacidad <= 0) {
                mostrarError("La capacidad debe ser mayor a cero");
                capacidadField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("La capacidad debe ser un número entero válido");
            capacidadField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}