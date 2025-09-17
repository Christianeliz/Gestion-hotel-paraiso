package com.hotel.main;

import com.hotel.gui.MainWindow;

import javax.swing.*;

/**
 * Clase principal para ejecutar el sistema de gestión hotelera
 */
public class MainApp {
    
    public static void main(String[] args) {
        // Configurar Look & Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error al establecer Look & Feel: " + e.getMessage());
        }
        
        // Ejecutar en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación: " + e.getMessage(),
                    "Error de Inicio",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}