package com.hotel.gui;

import com.hotel.dao.ReservacionDAO;
import com.hotel.dao.ClienteDAO;
import com.hotel.dao.HabitacionDAO;
import com.hotel.model.Reservacion;
import com.hotel.model.Cliente;
import com.hotel.model.Habitacion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel para la gestión de reservaciones
 */
public class ReservacionPanel extends JPanel {
    private final ReservacionDAO reservacionDAO;
    private final ClienteDAO clienteDAO;
    private final HabitacionDAO habitacionDAO;
    private JTable reservacionesTable;
    private DefaultTableModel tableModel;
    private JTextField fechaCheckInField, fechaCheckOutField, observacionesField;
    private JComboBox<Cliente> clienteComboBox;
    private JComboBox<Habitacion> habitacionComboBox;
    private JComboBox<Reservacion.EstadoReservacion> estadoComboBox;
    private JComboBox<Reservacion.EstadoReservacion> filtroEstadoComboBox;
    private JLabel costoTotalLabel;
    private Reservacion reservacionSeleccionada;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ReservacionPanel() {
        this.reservacionDAO = new ReservacionDAO();
        this.clienteDAO = new ClienteDAO();
        this.habitacionDAO = new HabitacionDAO();
        initializeComponents();
        cargarDatosComboBoxes();
        cargarReservaciones();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Filtros
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Panel central - Tabla de reservaciones
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Panel derecho - Formulario
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.EAST);
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        panel.add(new JLabel("Estado:"));
        filtroEstadoComboBox = new JComboBox<>();
        filtroEstadoComboBox.addItem(null); // Opción "Todos"
        for (Reservacion.EstadoReservacion estado : Reservacion.EstadoReservacion.values()) {
            filtroEstadoComboBox.addItem(estado);
        }
        filtroEstadoComboBox.addActionListener(e -> filtrarReservaciones());
        panel.add(filtroEstadoComboBox);
        
        JButton mostrarTodasButton = new JButton("Mostrar Todas");
        mostrarTodasButton.addActionListener(e -> {
            filtroEstadoComboBox.setSelectedItem(null);
            cargarReservaciones();
        });
        panel.add(mostrarTodasButton);
        
        JButton verificarDisponibilidadButton = new JButton("Verificar Disponibilidad");
        verificarDisponibilidadButton.addActionListener(e -> verificarDisponibilidad());
        panel.add(verificarDisponibilidadButton);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Reservaciones"));
        
        // Crear modelo de tabla
        String[] columnas = {"ID", "Cliente", "Habitación", "Check-In", "Check-Out", "Estado", "Costo Total"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reservacionesTable = new JTable(tableModel);
        reservacionesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservacionesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarReservacion();
            }
        });
        
        // Configurar anchos de columnas
        reservacionesTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        reservacionesTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Cliente
        reservacionesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Habitación
        reservacionesTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Check-In
        reservacionesTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Check-Out
        reservacionesTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Estado
        reservacionesTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Costo
        
        JScrollPane scrollPane = new JScrollPane(reservacionesTable);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Datos de la Reservación"));
        panel.setPreferredSize(new Dimension(350, 0));
        
        // Campos del formulario
        panel.add(new JLabel("Cliente:"));
        clienteComboBox = new JComboBox<>();
        panel.add(clienteComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Habitación:"));
        habitacionComboBox = new JComboBox<>();
        panel.add(habitacionComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Fecha Check-In (YYYY-MM-DD):"));
        fechaCheckInField = new JTextField(20);
        fechaCheckInField.addActionListener(e -> calcularCosto());
        panel.add(fechaCheckInField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Fecha Check-Out (YYYY-MM-DD):"));
        fechaCheckOutField = new JTextField(20);
        fechaCheckOutField.addActionListener(e -> calcularCosto());
        panel.add(fechaCheckOutField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Estado:"));
        estadoComboBox = new JComboBox<>(Reservacion.EstadoReservacion.values());
        panel.add(estadoComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Observaciones:"));
        observacionesField = new JTextField(20);
        panel.add(observacionesField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel de costo
        JPanel costoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        costoPanel.add(new JLabel("Costo Total: "));
        costoTotalLabel = new JLabel("$0.00");
        costoTotalLabel.setFont(costoTotalLabel.getFont().deriveFont(Font.BOLD, 14f));
        costoTotalLabel.setForeground(new Color(0, 100, 0));
        costoPanel.add(costoTotalLabel);
        
        JButton calcularCostoButton = new JButton("Calcular Costo");
        calcularCostoButton.addActionListener(e -> calcularCosto());
        costoPanel.add(calcularCostoButton);
        
        panel.add(costoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 2, 5, 5));
        
        JButton nuevaButton = new JButton("Nueva");
        nuevaButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(nuevaButton);
        
        JButton guardarButton = new JButton("Registrar");
        guardarButton.addActionListener(e -> registrarReservacion());
        buttonPanel.add(guardarButton);
        
        JButton actualizarButton = new JButton("Actualizar");
        actualizarButton.addActionListener(e -> actualizarReservacion());
        buttonPanel.add(actualizarButton);
        
        JButton cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> cancelarReservacion());
        buttonPanel.add(cancelarButton);
        
        JButton confirmarButton = new JButton("Confirmar");
        confirmarButton.addActionListener(e -> confirmarReservacion());
        buttonPanel.add(confirmarButton);
        
        JButton eliminarButton = new JButton("Eliminar");
        eliminarButton.addActionListener(e -> eliminarReservacion());
        buttonPanel.add(eliminarButton);
        
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private void cargarDatosComboBoxes() {
        try {
            // Cargar clientes
            List<Cliente> clientes = clienteDAO.listarTodos();
            clienteComboBox.removeAllItems();
            for (Cliente cliente : clientes) {
                clienteComboBox.addItem(cliente);
            }
            
            // Cargar habitaciones
            List<Habitacion> habitaciones = habitacionDAO.listarTodas();
            habitacionComboBox.removeAllItems();
            for (Habitacion habitacion : habitaciones) {
                habitacionComboBox.addItem(habitacion);
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }
    
    private void cargarReservaciones() {
        try {
            List<Reservacion> reservaciones = reservacionDAO.listarTodas();
            actualizarTabla(reservaciones);
        } catch (SQLException e) {
            mostrarError("Error al cargar reservaciones: " + e.getMessage());
        }
    }
    
    private void filtrarReservaciones() {
        try {
            Reservacion.EstadoReservacion estadoFiltro = (Reservacion.EstadoReservacion) filtroEstadoComboBox.getSelectedItem();
            
            if (estadoFiltro == null) {
                cargarReservaciones();
            } else {
                List<Reservacion> reservaciones = reservacionDAO.buscarPorEstado(estadoFiltro);
                actualizarTabla(reservaciones);
            }
        } catch (SQLException e) {
            mostrarError("Error al filtrar reservaciones: " + e.getMessage());
        }
    }
    
    private void actualizarTabla(List<Reservacion> reservaciones) {
        tableModel.setRowCount(0);
        for (Reservacion reservacion : reservaciones) {
            String clienteNombre = reservacion.getCliente() != null ? 
                                 reservacion.getCliente().getNombreCompleto() : 
                                 "ID: " + reservacion.getClienteId();
            String habitacionNumero = reservacion.getHabitacion() != null ? 
                                    reservacion.getHabitacion().getNumero() : 
                                    "ID: " + reservacion.getHabitacionId();
            
            Object[] row = {
                reservacion.getId(),
                clienteNombre,
                habitacionNumero,
                reservacion.getFechaCheckIn(),
                reservacion.getFechaCheckOut(),
                reservacion.getEstado().getNombre(),
                reservacion.getCostoTotal() != null ? "$" + reservacion.getCostoTotal() : "$0.00"
            };
            tableModel.addRow(row);
        }
    }
    
    private void seleccionarReservacion() {
        int selectedRow = reservacionesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reservacionId = (Integer) tableModel.getValueAt(selectedRow, 0);
            try {
                reservacionSeleccionada = reservacionDAO.buscarPorId(reservacionId);
                if (reservacionSeleccionada != null) {
                    llenarFormulario(reservacionSeleccionada);
                }
            } catch (SQLException e) {
                mostrarError("Error al cargar reservación: " + e.getMessage());
            }
        }
    }
    
    private void llenarFormulario(Reservacion reservacion) {
        // Buscar cliente en el combo
        for (int i = 0; i < clienteComboBox.getItemCount(); i++) {
            Cliente cliente = clienteComboBox.getItemAt(i);
            if (cliente.getId() == reservacion.getClienteId()) {
                clienteComboBox.setSelectedItem(cliente);
                break;
            }
        }
        
        // Buscar habitación en el combo
        for (int i = 0; i < habitacionComboBox.getItemCount(); i++) {
            Habitacion habitacion = habitacionComboBox.getItemAt(i);
            if (habitacion.getId() == reservacion.getHabitacionId()) {
                habitacionComboBox.setSelectedItem(habitacion);
                break;
            }
        }
        
        fechaCheckInField.setText(reservacion.getFechaCheckIn().toString());
        fechaCheckOutField.setText(reservacion.getFechaCheckOut().toString());
        estadoComboBox.setSelectedItem(reservacion.getEstado());
        observacionesField.setText(reservacion.getObservaciones());
        
        if (reservacion.getCostoTotal() != null) {
            costoTotalLabel.setText("$" + reservacion.getCostoTotal());
        }
    }
    
    private void limpiarFormulario() {
        clienteComboBox.setSelectedIndex(0);
        habitacionComboBox.setSelectedIndex(0);
        fechaCheckInField.setText("");
        fechaCheckOutField.setText("");
        estadoComboBox.setSelectedItem(Reservacion.EstadoReservacion.PENDIENTE);
        observacionesField.setText("");
        costoTotalLabel.setText("$0.00");
        reservacionSeleccionada = null;
        reservacionesTable.clearSelection();
    }
    
    private void calcularCosto() {
        try {
            LocalDate checkIn = LocalDate.parse(fechaCheckInField.getText().trim(), formatter);
            LocalDate checkOut = LocalDate.parse(fechaCheckOutField.getText().trim(), formatter);
            Habitacion habitacion = (Habitacion) habitacionComboBox.getSelectedItem();
            
            if (habitacion != null && checkIn.isBefore(checkOut)) {
                long dias = checkIn.until(checkOut).getDays();
                if (dias <= 0) dias = 1;
                
                BigDecimal costo = habitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(dias));
                costoTotalLabel.setText("$" + costo);
            }
        } catch (DateTimeParseException e) {
            // No hacer nada, esperar a que el usuario complete las fechas
        }
    }
    
    private void verificarDisponibilidad() {
        try {
            LocalDate checkIn = LocalDate.parse(fechaCheckInField.getText().trim(), formatter);
            LocalDate checkOut = LocalDate.parse(fechaCheckOutField.getText().trim(), formatter);
            
            List<Habitacion> disponibles = habitacionDAO.buscarDisponibles(checkIn, checkOut);
            
            StringBuilder mensaje = new StringBuilder("Habitaciones disponibles para " + checkIn + " - " + checkOut + ":\n\n");
            if (disponibles.isEmpty()) {
                mensaje.append("No hay habitaciones disponibles para esas fechas.");
            } else {
                for (Habitacion hab : disponibles) {
                    mensaje.append(String.format("• %s (%s) - $%s/noche\n", 
                                               hab.getNumero(), 
                                               hab.getTipo().getNombre(),
                                               hab.getPrecioPorNoche()));
                }
            }
            
            JOptionPane.showMessageDialog(this, mensaje.toString(), "Disponibilidad", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DateTimeParseException e) {
            mostrarError("Ingrese fechas válidas en formato YYYY-MM-DD");
        } catch (SQLException e) {
            mostrarError("Error al verificar disponibilidad: " + e.getMessage());
        }
    }
    
    private void registrarReservacion() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            Cliente cliente = (Cliente) clienteComboBox.getSelectedItem();
            Habitacion habitacion = (Habitacion) habitacionComboBox.getSelectedItem();
            LocalDate checkIn = LocalDate.parse(fechaCheckInField.getText().trim(), formatter);
            LocalDate checkOut = LocalDate.parse(fechaCheckOutField.getText().trim(), formatter);
            
            Reservacion reservacion = new Reservacion(
                cliente.getId(),
                habitacion.getId(),
                checkIn,
                checkOut,
                observacionesField.getText().trim()
            );
            
            reservacion.setEstado((Reservacion.EstadoReservacion) estadoComboBox.getSelectedItem());
            reservacion.setHabitacion(habitacion);
            reservacion.setCostoTotal(reservacion.calcularCosto());
            
            if (reservacionDAO.registrar(reservacion)) {
                JOptionPane.showMessageDialog(this,
                    "Reservación registrada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarReservaciones();
            } else {
                mostrarError("Error al registrar la reservación");
            }
        } catch (SQLException e) {
            mostrarError("Error al registrar reservación: " + e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarError("Error en formato de fecha. Use YYYY-MM-DD");
        }
    }
    
    private void actualizarReservacion() {
        if (reservacionSeleccionada == null) {
            mostrarError("Seleccione una reservación para actualizar");
            return;
        }
        
        if (!validarFormulario()) {
            return;
        }
        
        try {
            Cliente cliente = (Cliente) clienteComboBox.getSelectedItem();
            Habitacion habitacion = (Habitacion) habitacionComboBox.getSelectedItem();
            LocalDate checkIn = LocalDate.parse(fechaCheckInField.getText().trim(), formatter);
            LocalDate checkOut = LocalDate.parse(fechaCheckOutField.getText().trim(), formatter);
            
            reservacionSeleccionada.setClienteId(cliente.getId());
            reservacionSeleccionada.setHabitacionId(habitacion.getId());
            reservacionSeleccionada.setFechaCheckIn(checkIn);
            reservacionSeleccionada.setFechaCheckOut(checkOut);
            reservacionSeleccionada.setEstado((Reservacion.EstadoReservacion) estadoComboBox.getSelectedItem());
            reservacionSeleccionada.setObservaciones(observacionesField.getText().trim());
            reservacionSeleccionada.setHabitacion(habitacion);
            reservacionSeleccionada.setCostoTotal(reservacionSeleccionada.calcularCosto());
            
            if (reservacionDAO.actualizar(reservacionSeleccionada)) {
                JOptionPane.showMessageDialog(this,
                    "Reservación actualizada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarReservaciones();
            } else {
                mostrarError("Error al actualizar la reservación");
            }
        } catch (SQLException e) {
            mostrarError("Error al actualizar reservación: " + e.getMessage());
        } catch (DateTimeParseException e) {
            mostrarError("Error en formato de fecha. Use YYYY-MM-DD");
        }
    }
    
    private void cancelarReservacion() {
        if (reservacionSeleccionada == null) {
            mostrarError("Seleccione una reservación para cancelar");
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de cancelar esta reservación?",
            "Confirmar Cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                if (reservacionDAO.cancelar(reservacionSeleccionada.getId())) {
                    JOptionPane.showMessageDialog(this,
                        "Reservación cancelada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarReservaciones();
                } else {
                    mostrarError("Error al cancelar la reservación");
                }
            } catch (SQLException e) {
                mostrarError("Error al cancelar reservación: " + e.getMessage());
            }
        }
    }
    
    private void confirmarReservacion() {
        if (reservacionSeleccionada == null) {
            mostrarError("Seleccione una reservación para confirmar");
            return;
        }
        
        try {
            reservacionSeleccionada.setEstado(Reservacion.EstadoReservacion.CONFIRMADA);
            if (reservacionDAO.actualizar(reservacionSeleccionada)) {
                JOptionPane.showMessageDialog(this,
                    "Reservación confirmada exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarReservaciones();
            } else {
                mostrarError("Error al confirmar la reservación");
            }
        } catch (SQLException e) {
            mostrarError("Error al confirmar reservación: " + e.getMessage());
        }
    }
    
    private void eliminarReservacion() {
        if (reservacionSeleccionada == null) {
            mostrarError("Seleccione una reservación para eliminar");
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar esta reservación?\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                if (reservacionDAO.eliminar(reservacionSeleccionada.getId())) {
                    JOptionPane.showMessageDialog(this,
                        "Reservación eliminada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarReservaciones();
                } else {
                    mostrarError("Error al eliminar la reservación");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar reservación: " + e.getMessage());
            }
        }
    }
    
    private boolean validarFormulario() {
        if (clienteComboBox.getSelectedItem() == null) {
            mostrarError("Seleccione un cliente");
            return false;
        }
        
        if (habitacionComboBox.getSelectedItem() == null) {
            mostrarError("Seleccione una habitación");
            return false;
        }
        
        if (fechaCheckInField.getText().trim().isEmpty()) {
            mostrarError("Ingrese la fecha de check-in");
            fechaCheckInField.requestFocus();
            return false;
        }
        
        if (fechaCheckOutField.getText().trim().isEmpty()) {
            mostrarError("Ingrese la fecha de check-out");
            fechaCheckOutField.requestFocus();
            return false;
        }
        
        try {
            LocalDate checkIn = LocalDate.parse(fechaCheckInField.getText().trim(), formatter);
            LocalDate checkOut = LocalDate.parse(fechaCheckOutField.getText().trim(), formatter);
            
            if (!checkIn.isBefore(checkOut)) {
                mostrarError("La fecha de check-out debe ser posterior a la de check-in");
                fechaCheckOutField.requestFocus();
                return false;
            }
            
            if (checkIn.isBefore(LocalDate.now())) {
                mostrarError("La fecha de check-in no puede ser anterior a hoy");
                fechaCheckInField.requestFocus();
                return false;
            }
            
        } catch (DateTimeParseException e) {
            mostrarError("Las fechas deben tener el formato YYYY-MM-DD");
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}