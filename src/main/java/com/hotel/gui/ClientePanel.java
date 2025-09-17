package com.hotel.gui;

import com.hotel.dao.ClienteDAO;
import com.hotel.model.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel para la gestión de clientes
 */
public class ClientePanel extends JPanel {
    private final ClienteDAO clienteDAO;
    private JTable clientesTable;
    private DefaultTableModel tableModel;
    private JTextField nombreField, apellidoField, dniField, telefonoField, emailField;
    private JTextField buscarField;
    private Cliente clienteSeleccionado;
    
    public ClientePanel() {
        this.clienteDAO = new ClienteDAO();
        initializeComponents();
        cargarClientes();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Búsqueda
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // Panel central - Tabla de clientes
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Panel derecho - Formulario
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.EAST);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Búsqueda"));
        
        panel.add(new JLabel("Buscar cliente:"));
        buscarField = new JTextField(20);
        panel.add(buscarField);
        
        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarClientes());
        panel.add(buscarButton);
        
        JButton mostrarTodosButton = new JButton("Mostrar Todos");
        mostrarTodosButton.addActionListener(e -> cargarClientes());
        panel.add(mostrarTodosButton);
        
        // Búsqueda en tiempo real
        buscarField.addActionListener(e -> buscarClientes());
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Lista de Clientes"));
        
        // Crear modelo de tabla
        String[] columnas = {"ID", "Nombre", "Apellido", "DNI", "Teléfono", "Email"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        clientesTable = new JTable(tableModel);
        clientesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCliente();
            }
        });
        
        // Configurar anchos de columnas
        clientesTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        clientesTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Nombre
        clientesTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Apellido
        clientesTable.getColumnModel().getColumn(3).setPreferredWidth(100); // DNI
        clientesTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Teléfono
        clientesTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Email
        
        JScrollPane scrollPane = new JScrollPane(clientesTable);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        panel.setPreferredSize(new Dimension(300, 0));
        
        // Campos del formulario
        panel.add(new JLabel("Nombre:"));
        nombreField = new JTextField(20);
        panel.add(nombreField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Apellido:"));
        apellidoField = new JTextField(20);
        panel.add(apellidoField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("DNI:"));
        dniField = new JTextField(20);
        panel.add(dniField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Teléfono:"));
        telefonoField = new JTextField(20);
        panel.add(telefonoField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton nuevoButton = new JButton("Nuevo");
        nuevoButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(nuevoButton);
        
        JButton guardarButton = new JButton("Guardar");
        guardarButton.addActionListener(e -> guardarCliente());
        buttonPanel.add(guardarButton);
        
        JButton actualizarButton = new JButton("Actualizar");
        actualizarButton.addActionListener(e -> actualizarCliente());
        buttonPanel.add(actualizarButton);
        
        JButton eliminarButton = new JButton("Eliminar");
        eliminarButton.addActionListener(e -> eliminarCliente());
        buttonPanel.add(eliminarButton);
        
        panel.add(buttonPanel);
        
        return panel;
    }
    
    private void cargarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();
            actualizarTabla(clientes);
        } catch (SQLException e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
        }
    }
    
    private void buscarClientes() {
        String busqueda = buscarField.getText().trim();
        if (busqueda.isEmpty()) {
            cargarClientes();
            return;
        }
        
        try {
            List<Cliente> clientes = clienteDAO.buscarPorNombre(busqueda);
            actualizarTabla(clientes);
        } catch (SQLException e) {
            mostrarError("Error al buscar clientes: " + e.getMessage());
        }
    }
    
    private void actualizarTabla(List<Cliente> clientes) {
        tableModel.setRowCount(0);
        for (Cliente cliente : clientes) {
            Object[] row = {
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getEmail()
            };
            tableModel.addRow(row);
        }
    }
    
    private void seleccionarCliente() {
        int selectedRow = clientesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int clienteId = (Integer) tableModel.getValueAt(selectedRow, 0);
            try {
                clienteSeleccionado = clienteDAO.buscarPorId(clienteId);
                if (clienteSeleccionado != null) {
                    llenarFormulario(clienteSeleccionado);
                }
            } catch (SQLException e) {
                mostrarError("Error al cargar cliente: " + e.getMessage());
            }
        }
    }
    
    private void llenarFormulario(Cliente cliente) {
        nombreField.setText(cliente.getNombre());
        apellidoField.setText(cliente.getApellido());
        dniField.setText(cliente.getDni());
        telefonoField.setText(cliente.getTelefono());
        emailField.setText(cliente.getEmail());
    }
    
    private void limpiarFormulario() {
        nombreField.setText("");
        apellidoField.setText("");
        dniField.setText("");
        telefonoField.setText("");
        emailField.setText("");
        clienteSeleccionado = null;
        clientesTable.clearSelection();
    }
    
    private void guardarCliente() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            Cliente cliente = new Cliente(
                nombreField.getText().trim(),
                apellidoField.getText().trim(),
                dniField.getText().trim(),
                telefonoField.getText().trim(),
                emailField.getText().trim()
            );
            
            if (clienteDAO.registrar(cliente)) {
                JOptionPane.showMessageDialog(this,
                    "Cliente registrado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarClientes();
            } else {
                mostrarError("Error al registrar el cliente");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarError("El DNI ya está registrado en el sistema");
            } else {
                mostrarError("Error al registrar cliente: " + e.getMessage());
            }
        }
    }
    
    private void actualizarCliente() {
        if (clienteSeleccionado == null) {
            mostrarError("Seleccione un cliente para actualizar");
            return;
        }
        
        if (!validarFormulario()) {
            return;
        }
        
        try {
            clienteSeleccionado.setNombre(nombreField.getText().trim());
            clienteSeleccionado.setApellido(apellidoField.getText().trim());
            clienteSeleccionado.setDni(dniField.getText().trim());
            clienteSeleccionado.setTelefono(telefonoField.getText().trim());
            clienteSeleccionado.setEmail(emailField.getText().trim());
            
            if (clienteDAO.actualizar(clienteSeleccionado)) {
                JOptionPane.showMessageDialog(this,
                    "Cliente actualizado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                cargarClientes();
            } else {
                mostrarError("Error al actualizar el cliente");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                mostrarError("El DNI ya está registrado para otro cliente");
            } else {
                mostrarError("Error al actualizar cliente: " + e.getMessage());
            }
        }
    }
    
    private void eliminarCliente() {
        if (clienteSeleccionado == null) {
            mostrarError("Seleccione un cliente para eliminar");
            return;
        }
        
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar al cliente " + clienteSeleccionado.getNombreCompleto() + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                if (clienteDAO.eliminar(clienteSeleccionado.getId())) {
                    JOptionPane.showMessageDialog(this,
                        "Cliente eliminado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarClientes();
                } else {
                    mostrarError("Error al eliminar el cliente");
                }
            } catch (SQLException e) {
                mostrarError("Error al eliminar cliente: " + e.getMessage());
            }
        }
    }
    
    private boolean validarFormulario() {
        if (nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es requerido");
            nombreField.requestFocus();
            return false;
        }
        
        if (apellidoField.getText().trim().isEmpty()) {
            mostrarError("El apellido es requerido");
            apellidoField.requestFocus();
            return false;
        }
        
        if (dniField.getText().trim().isEmpty()) {
            mostrarError("El DNI es requerido");
            dniField.requestFocus();
            return false;
        }
        
        // Validar formato de email si se proporciona
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            mostrarError("El formato del email no es válido");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}