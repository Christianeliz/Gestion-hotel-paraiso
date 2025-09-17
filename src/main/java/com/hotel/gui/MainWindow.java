package com.hotel.gui;

import com.hotel.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * Ventana principal del sistema de gestión hotelera
 */
public class MainWindow extends JFrame {
    
    public MainWindow() {
        initializeComponents();
        setupEventHandlers();
        initializeDatabase();
    }
    
    private void initializeComponents() {
        setTitle("Hotel Paraíso - Sistema de Gestión");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Establecer Look & Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al establecer Look & Feel: " + e.getMessage());
        }
        
        // Crear barra de menús
        createMenuBar();
        
        // Crear panel principal con pestañas
        createMainPanel();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menú Archivo
        JMenu archivoMenu = new JMenu("Archivo");
        
        JMenuItem salirItem = new JMenuItem("Salir");
        salirItem.addActionListener(e -> confirmExit());
        archivoMenu.add(salirItem);
        
        // Menú Gestión
        JMenu gestionMenu = new JMenu("Gestión");
        
        JMenuItem clientesItem = new JMenuItem("Gestionar Clientes");
        JMenuItem habitacionesItem = new JMenuItem("Gestionar Habitaciones");
        JMenuItem reservacionesItem = new JMenuItem("Gestionar Reservaciones");
        
        gestionMenu.add(clientesItem);
        gestionMenu.add(habitacionesItem);
        gestionMenu.add(reservacionesItem);
        
        // Menú Reportes
        JMenu reportesMenu = new JMenu("Reportes");
        
        JMenuItem ocupacionItem = new JMenuItem("Reporte de Ocupación");
        JMenuItem clientesReportItem = new JMenuItem("Reporte de Clientes");
        
        reportesMenu.add(ocupacionItem);
        reportesMenu.add(clientesReportItem);
        
        // Menú Ayuda
        JMenu ayudaMenu = new JMenu("Ayuda");
        
        JMenuItem acercaItem = new JMenuItem("Acerca de");
        acercaItem.addActionListener(e -> showAboutDialog());
        ayudaMenu.add(acercaItem);
        
        menuBar.add(archivoMenu);
        menuBar.add(gestionMenu);
        menuBar.add(reportesMenu);
        menuBar.add(ayudaMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Panel de bienvenida
        JPanel bienvenidaPanel = createWelcomePanel();
        tabbedPane.addTab("Inicio", new ImageIcon(), bienvenidaPanel, "Panel de inicio");
        
        // Panel de clientes
        ClientePanel clientePanel = new ClientePanel();
        tabbedPane.addTab("Clientes", new ImageIcon(), clientePanel, "Gestión de clientes");
        
        // Panel de habitaciones  
        HabitacionPanel habitacionPanel = new HabitacionPanel();
        tabbedPane.addTab("Habitaciones", new ImageIcon(), habitacionPanel, "Gestión de habitaciones");
        
        // Panel de reservaciones
        ReservacionPanel reservacionPanel = new ReservacionPanel();
        tabbedPane.addTab("Reservaciones", new ImageIcon(), reservacionPanel, "Gestión de reservaciones");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Barra de estado
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Sistema iniciado correctamente");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Panel central con información
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Título
        JLabel titleLabel = new JLabel("Hotel Paraíso");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(0, 100, 150));
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Sistema de Gestión Hotelera");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.GRAY);
        
        // Descripción
        JTextArea descriptionArea = new JTextArea(
            "Bienvenido al sistema de gestión del Hotel Paraíso.\n\n" +
            "Funcionalidades disponibles:\n" +
            "• Gestión de clientes (registro, búsqueda, actualización)\n" +
            "• Administración de habitaciones\n" +
            "• Control de reservaciones con validación de disponibilidad\n" +
            "• Reportes básicos de ocupación\n\n" +
            "Utilice las pestañas superiores para navegar entre las diferentes secciones."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(descriptionArea);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }
    
    private void initializeDatabase() {
        try {
            DatabaseConnection.initializeDatabase();
            DatabaseConnection.insertSampleData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al inicializar la base de datos: " + e.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea salir del sistema?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection();
            System.exit(0);
        }
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Hotel Paraíso - Sistema de Gestión v1.0.0\n\n" +
            "Desarrollado en Java con SQLite\n" +
            "Interfaz gráfica con Swing\n\n" +
            "© 2024 Hotel Paraíso",
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE);
    }
}