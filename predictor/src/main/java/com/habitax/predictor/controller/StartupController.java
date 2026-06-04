package com.habitax.predictor.controller;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;

public class StartupController {

    public static void mostrar() {
        // Solo mostrar si no hay variables de entorno configuradas
        String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (datasourceUrl != null && !datasourceUrl.isBlank()) {
            return; // Ya hay variables → arrancar directo sin popup
        }

        // Necesario para que Swing funcione correctamente en Mac
        System.setProperty("apple.awt.UIElement", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // CountDownLatch thread-safe para bloquear hasta que el usuario elija
        CountDownLatch latch = new CountDownLatch(1);

        // Crear y mostrar la ventana en el EDT (hilo correcto para Swing)
        // Sin esto los eventos de los botones no se procesan correctamente
        SwingUtilities.invokeLater(() -> {

            // Color de fondo del sistema (respeta tema claro/oscuro)
            Color bgSistema = UIManager.getColor("Panel.background");

            JFrame frame = new JFrame("Habitax — Configuracion de arranque");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(520, 480);
            frame.setLocationRelativeTo(null); // centrar en pantalla
            frame.setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            panel.setBackground(bgSistema);

            // ── Titulo ────────────────────────────────────────────────────────────
            JLabel titulo = new JLabel("HABITAX");
            titulo.setFont(new Font("Arial", Font.BOLD, 28));
            titulo.setForeground(new Color(255, 107, 16));
            titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subtitulo = new JLabel("Selecciona como arrancar la aplicacion");
            subtitulo.setFont(new Font("Arial", Font.PLAIN, 13));
            subtitulo.setForeground(Color.GRAY);
            subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(titulo);
            panel.add(Box.createVerticalStrut(6));
            panel.add(subtitulo);
            panel.add(Box.createVerticalStrut(24));

            // Separador
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            panel.add(sep);
            panel.add(Box.createVerticalStrut(20));

            // ── OPCION 1: PRODUCCION ──────────────────────────────────────────────
            JLabel lblProd = new JLabel("Conectar a produccion (AWS RDS)");
            lblProd.setFont(new Font("Arial", Font.BOLD, 13));
            lblProd.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Fila con campo de ruta y boton de explorar
            JPanel filaProd = new JPanel(new BorderLayout(8, 0));
            filaProd.setBackground(bgSistema);
            filaProd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

            JTextField campoRuta = new JTextField();
            // Ruta por defecto segun sistema operativo
            String rutaDefault = System.getProperty("os.name").toLowerCase().contains("win")
                    ? System.getProperty("user.home") + "\\habitax-env.bat"
                    : System.getProperty("user.home") + "/habitax-env.sh";
            campoRuta.setText(rutaDefault);
            campoRuta.setFont(new Font("Courier New", Font.PLAIN, 11));

            // Boton "..." para abrir el explorador de ficheros del sistema
            JButton btnExaminar = new JButton("...");
            btnExaminar.setPreferredSize(new Dimension(36, 28));
            btnExaminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnExaminar.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
                chooser.setDialogTitle("Selecciona tu fichero de variables de entorno");
                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    campoRuta.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });

            filaProd.add(campoRuta, BorderLayout.CENTER);
            filaProd.add(btnExaminar, BorderLayout.EAST);

            // Boton principal de produccion (borde naranja, texto naranja)
            JButton btnProduccion = new JButton("Cargar variables y conectar a produccion");
            btnProduccion.setBackground(null);
            btnProduccion.setForeground(new Color(255, 107, 16));
            btnProduccion.setFont(new Font("Arial", Font.BOLD, 13));
            btnProduccion.setBorder(BorderFactory.createLineBorder(new Color(255, 107, 16), 2));
            btnProduccion.setFocusPainted(false);
            btnProduccion.setOpaque(false);
            btnProduccion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnProduccion.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnProduccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btnProduccion.addActionListener(e -> {
                try {
                    cargarFicheroEnv(campoRuta.getText());
                    latch.countDown(); // libera el bloqueo al hilo principal
                    frame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo cargar el fichero:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            panel.add(lblProd);
            panel.add(Box.createVerticalStrut(6));
            panel.add(filaProd);
            panel.add(Box.createVerticalStrut(8));
            panel.add(btnProduccion);
            panel.add(Box.createVerticalStrut(16));

            // ── Separador con "o" ─────────────────────────────────────────────────
            JPanel filaOr = new JPanel(new BorderLayout(8, 0));
            filaOr.setBackground(bgSistema);
            filaOr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            JSeparator sepIzq = new JSeparator();
            JSeparator sepDer = new JSeparator();
            JLabel lblO = new JLabel("  o  ");
            lblO.setForeground(Color.GRAY);
            filaOr.add(sepIzq, BorderLayout.WEST);
            filaOr.add(lblO, BorderLayout.CENTER);
            filaOr.add(sepDer, BorderLayout.EAST);
            panel.add(filaOr);
            panel.add(Box.createVerticalStrut(16));

            // ── OPCION 2: DESARROLLO ──────────────────────────────────────────────
            JLabel lblDev = new JLabel("Modo desarrollo (H2 en memoria)");
            lblDev.setFont(new Font("Arial", Font.BOLD, 13));
            lblDev.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblDevInfo = new JLabel("Los datos no se guardan al cerrar. Sin acceso a RDS.");
            lblDevInfo.setFont(new Font("Arial", Font.PLAIN, 11));
            lblDevInfo.setForeground(Color.GRAY);
            lblDevInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Campo opcional para introducir la API Key de RapidAPI
            JPanel filaKey = new JPanel(new BorderLayout(8, 0));
            filaKey.setBackground(bgSistema);
            filaKey.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JTextField campoApiKey = new JTextField();
            campoApiKey.setToolTipText("Opcional: pega tu RapidAPI Key para busquedas reales de Idealista");
            campoApiKey.setFont(new Font("Courier New", Font.PLAIN, 11));
            JLabel lblApiKey = new JLabel("API Key: ");
            lblApiKey.setFont(new Font("Arial", Font.PLAIN, 11));
            filaKey.add(lblApiKey, BorderLayout.WEST);
            filaKey.add(campoApiKey, BorderLayout.CENTER);

            // Boton de desarrollo (borde gris, texto gris)
            JButton btnDesarrollo = new JButton("Continuar en modo desarrollo");
            btnDesarrollo.setBackground(null);
            btnDesarrollo.setForeground(new Color(80, 80, 80));
            btnDesarrollo.setFont(new Font("Arial", Font.BOLD, 13));
            btnDesarrollo.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2));
            btnDesarrollo.setFocusPainted(false);
            btnDesarrollo.setOpaque(false);
            btnDesarrollo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDesarrollo.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnDesarrollo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btnDesarrollo.addActionListener(e -> {
                // Si el usuario introdujo una API Key la cargamos como propiedad del sistema
                String apiKey = campoApiKey.getText().trim();
                if (!apiKey.isEmpty()) {
                    System.setProperty("RAPIDAPI_KEY", apiKey);
                }
                // Marcar modo desarrollo para que Spring arranque con H2
                System.setProperty("MODO_DESARROLLO", "true");
                latch.countDown(); // libera el bloqueo al hilo principal
                frame.dispose();
            });

            panel.add(lblDev);
            panel.add(Box.createVerticalStrut(4));
            panel.add(lblDevInfo);
            panel.add(Box.createVerticalStrut(6));
            panel.add(filaKey);
            panel.add(Box.createVerticalStrut(8));
            panel.add(btnDesarrollo);

            // ── Montar ventana con scroll por si el contenido no cabe ─────────────
            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            frame.add(scroll);
            frame.setVisible(true);

        }); // fin SwingUtilities.invokeLater

        // El hilo principal espera aqui FUERA del invokeLater
        // hasta que el usuario elija una opcion
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    // Carga las variables de entorno desde un fichero .sh o .bat
    // Formato soportado: export VARIABLE=valor  o  VARIABLE=valor
    private static void cargarFicheroEnv(String ruta) throws Exception {
        File fichero = new File(ruta.replace("~", System.getProperty("user.home")));
        if (!fichero.exists()) {
            throw new Exception("Fichero no encontrado: " + ruta);
        }
        for (String linea : Files.readAllLines(fichero.toPath())) {
            linea = linea.trim();
            // Ignorar comentarios y lineas vacias
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            // Soportar formato con o sin "export"
            linea = linea.replace("export ", "");
            int idx = linea.indexOf('=');
            if (idx > 0) {
                String clave = linea.substring(0, idx).trim();
                String valor = linea.substring(idx + 1).trim()
                        .replaceAll("^\"|\"$", "") // quita comillas dobles
                        .replaceAll("^'|'$", "");  // quita comillas simples
                System.setProperty(clave, valor);
            }
        }
    }
}