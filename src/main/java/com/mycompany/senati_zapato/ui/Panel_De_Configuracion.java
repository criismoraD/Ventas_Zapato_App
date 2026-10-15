package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.utilidades.Configuracion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Modulo Configuracion: API key + modelos. Persiste en .env y aplica en caliente. */
public class Panel_De_Configuracion extends JPanel {

    protected final JPasswordField txtApiKey = new JPasswordField(28);
    protected final JLabel lblEstadoKey = new JLabel();
    protected final JPasswordField txtApiKeyRespaldo = new JPasswordField(28);
    protected final JLabel lblEstadoKeyRespaldo = new JLabel();
    protected final JComboBox<String> cmbModeloChat;
    protected final JComboBox<String> cmbModeloVoz;
    protected final JComboBox<String> cmbThinking;
    protected final JCheckBox chkBusquedaWeb = new JCheckBox("Activar busqueda web (Google Search)");
    protected final JTextArea txtRuta = new JTextArea(2, 30);
    protected final JLabel lblVozInfo = new JLabel();

    public Panel_De_Configuracion() {
        cmbModeloChat = new JComboBox<>(new String[]{
            Configuracion.MODELO_CHAT_PRINCIPAL,
            Configuracion.MODELO_CHAT_SECUNDARIO_1,
            Configuracion.MODELO_CHAT_SECUNDARIO_2
        });
        cmbModeloVoz = new JComboBox<>(new String[]{
            Configuracion.MODELO_VOZ_PRINCIPAL, Configuracion.MODELO_VOZ_FALLBACK
        });
        cmbThinking = new JComboBox<>(new String[]{"MINIMAL", "LOW", "MEDIUM", "HIGH"});
        buildUI();
        Cargar_Valores();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Configuracion del Asistente IA");
        titulo.setFont(new Font("Inter", Font.BOLD, 26));
        titulo.setForeground(Gestor_De_Temas.getTextColor());
        JLabel subt = new JLabel("Configura tus API Keys de Gemini (Principal y Respaldo) y modelos. Se guardan en .env.");
        subt.setFont(new Font("Inter", Font.PLAIN, 14));
        subt.setForeground(Gestor_De_Temas.getMutedColor());
        JPanel head = new JPanel(new BorderLayout(4, 4));
        head.setOpaque(false);
        head.add(titulo, BorderLayout.NORTH);
        head.add(subt, BorderLayout.CENTER);
        add(head, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int fila = 0;

        g.gridx = 0; g.gridy = fila; g.weightx = 0;
        form.add(new JLabel("GEMINI_API_KEY (Principal / Free):"), g);
        g.gridx = 1; g.weightx = 1;
        txtApiKey.setFont(new Font("Consolas", Font.PLAIN, 13));
        form.add(txtApiKey, g);
        fila++;

        g.gridx = 0; g.gridy = fila; g.weightx = 0;
        form.add(new JLabel("Estado Principal:"), g);
        g.gridx = 1; g.weightx = 1;
        lblEstadoKey.setFont(new Font("Inter", Font.BOLD, 13));
        form.add(lblEstadoKey, g);
        fila++;

        g.gridx = 0; g.gridy = fila; g.weightx = 0;
        form.add(new JLabel("GEMINI_API_KEY_PAID (Respaldo / Pago):"), g);
        g.gridx = 1; g.weightx = 1;
        txtApiKeyRespaldo.setFont(new Font("Consolas", Font.PLAIN, 13));
        form.add(txtApiKeyRespaldo, g);
        fila++;

        g.gridx = 0; g.gridy = fila; g.weightx = 0;
        form.add(new JLabel("Estado Respaldo:"), g);
        g.gridx = 1; g.weightx = 1;
        lblEstadoKeyRespaldo.setFont(new Font("Inter", Font.BOLD, 13));
        form.add(lblEstadoKeyRespaldo, g);
        fila++;

        g.gridx = 0; g.gridy = fila;
        form.add(new JLabel("Modelo chat principal:"), g);
        g.gridx = 1;
        form.add(cmbModeloChat, g);
        fila++;

        g.gridx = 0; g.gridy = fila;
        form.add(new JLabel("Modelo de voz Live:"), g);
        g.gridx = 1;
        form.add(cmbModeloVoz, g);
        fila++;

        g.gridx = 1; g.gridy = fila;
        lblVozInfo.setFont(new Font("Inter", Font.ITALIC, 12));
        lblVozInfo.setForeground(Gestor_De_Temas.getMutedColor());
        lblVozInfo.setText("Principal: Gemini 3.8 Live. Fallback automatico: 3.1 si tu cuenta no lo tiene.");
        form.add(lblVozInfo, g);
        fila++;

        g.gridx = 0; g.gridy = fila;
        form.add(new JLabel("Thinking level:"), g);
        g.gridx = 1;
        form.add(cmbThinking, g);
        fila++;

        g.gridx = 1; g.gridy = fila;
        form.add(chkBusquedaWeb, g);
        fila++;

        g.gridx = 0; g.gridy = fila;
        form.add(new JLabel("Archivo .env:"), g);
        g.gridx = 1;
        txtRuta.setEditable(false);
        txtRuta.setLineWrap(true);
        txtRuta.setWrapStyleWord(true);
        txtRuta.setFont(new Font("Consolas", Font.PLAIN, 11));
        form.add(new JScrollPane(txtRuta), g);
        fila++;

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        botones.setOpaque(false);
        JButton btnProbar = new JButton("Probar conexion");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnRecargar = new JButton("Recargar");
        for (JButton b : new JButton[]{btnProbar, btnGuardar, btnRecargar}) {
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setFont(new Font("Inter", Font.BOLD, 14));
            b.setPreferredSize(new Dimension(170, 42));
        }
        btnGuardar.setBackground(new Color(79, 133, 87));
        btnGuardar.setForeground(Color.WHITE);
        botones.add(btnRecargar);
        botones.add(btnProbar);
        botones.add(btnGuardar);
        add(botones, BorderLayout.SOUTH);

        btnRecargar.addActionListener(e -> Cargar_Valores());
        btnGuardar.addActionListener(e -> Guardar());
        btnProbar.addActionListener(e -> Probar_Conexion());
    }

    public void Cargar_Valores() {
        String key = "";
        try {
            key = Configuracion.Obtener("GEMINI_API_KEY", "");
        } catch (Exception ex) {
            key = "";
        }
        txtApiKey.setText(key != null ? key : "");
        txtApiKey.putClientProperty("keyActual", key);
        boolean hay = Configuracion.esKeyValida(key);
        lblEstadoKey.setText(hay ? "Configurada: " + Configuracion.Enmascarar(key) : "Sin configurar");
        lblEstadoKey.setForeground(hay ? new Color(79, 133, 87) : new Color(178, 74, 74));

        String keyRespaldo = "";
        try {
            keyRespaldo = Configuracion.Obtener_Gemini_Api_Key_Respaldo();
        } catch (Exception ex) {
            keyRespaldo = "";
        }
        txtApiKeyRespaldo.setText(keyRespaldo != null ? keyRespaldo : "");
        txtApiKeyRespaldo.putClientProperty("keyActual", keyRespaldo);
        boolean hayRespaldo = Configuracion.esKeyValida(keyRespaldo);
        lblEstadoKeyRespaldo.setText(hayRespaldo ? "Configurada (Paid): " + Configuracion.Enmascarar(keyRespaldo) : "Sin configurar (opcional)");
        lblEstadoKeyRespaldo.setForeground(hayRespaldo ? new Color(79, 133, 87) : Gestor_De_Temas.getMutedColor());

        cmbModeloChat.setSelectedItem(Configuracion.Obtener_Modelo_Chat());
        cmbModeloVoz.setSelectedItem(Configuracion.Obtener_Modelo_Voz());
        cmbThinking.setSelectedItem(Configuracion.Obtener_Thinking_Level());
        chkBusquedaWeb.setSelected(Configuracion.Usar_Busqueda_Web());
        txtRuta.setText(Configuracion.Ruta_Env()
            + "\nKey gratis en: https://aistudio.google.com/apikey");
    }

    public void Guardar() {
        String nueva = new String(txtApiKey.getPassword()).trim();
        String actual = (String) txtApiKey.getClientProperty("keyActual");
        String nuevaRespaldo = new String(txtApiKeyRespaldo.getPassword()).trim();
        String actualRespaldo = (String) txtApiKeyRespaldo.getClientProperty("keyActual");

        java.util.Map<String, String> cambios = new java.util.HashMap<>();
        if (!nueva.isEmpty()) {
            if (nueva.length() < 15) {
                javax.swing.JOptionPane.showMessageDialog(this, "Key principal no valida (minimo 15 caracteres).",
                    "API Key", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            cambios.put("GEMINI_API_KEY", nueva);
        } else if (actual == null || actual.isBlank()) {
            if (nuevaRespaldo.isEmpty() && (actualRespaldo == null || actualRespaldo.isBlank())) {
                javax.swing.JOptionPane.showMessageDialog(this, "Ingresa al menos una API Key de Gemini.",
                    "API Key", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (!nuevaRespaldo.isEmpty()) {
            if (nuevaRespaldo.length() < 15) {
                javax.swing.JOptionPane.showMessageDialog(this, "Key de respaldo no valida (minimo 15 caracteres).",
                    "API Key", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            cambios.put("GEMINI_API_KEY_PAID", nuevaRespaldo);
        }

        cambios.put("GEMINI_MODEL", (String) cmbModeloChat.getSelectedItem());
        cambios.put("GEMINI_LIVE_MODEL", (String) cmbModeloVoz.getSelectedItem());
        cambios.put("GEMINI_THINKING", (String) cmbThinking.getSelectedItem());
        cambios.put("GEMINI_SEARCH", chkBusquedaWeb.isSelected() ? "true" : "false");
        cambios.put("GEMINI_MODELS", String.join(",", Configuracion.MODELOS_CHAT));
        try {
            Configuracion.Guardar_En_Env(cambios);
            txtApiKey.putClientProperty("keyActual", Configuracion.Obtener("GEMINI_API_KEY", ""));
            txtApiKeyRespaldo.putClientProperty("keyActual", Configuracion.Obtener_Gemini_Api_Key_Respaldo());
            Cargar_Valores();
            javax.swing.JOptionPane.showMessageDialog(this, "Guardado en:\n" + Configuracion.Ruta_Env(),
                "Configuracion", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo guardar:\n" + ex.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void Probar_Conexion() {
        String k0 = new String(txtApiKey.getPassword()).trim();
        if (k0.isEmpty()) {
            k0 = (String) txtApiKey.getClientProperty("keyActual");
        }
        if (k0 == null || k0.isBlank()) {
            k0 = new String(txtApiKeyRespaldo.getPassword()).trim();
            if (k0.isEmpty()) {
                k0 = (String) txtApiKeyRespaldo.getClientProperty("keyActual");
            }
        }
        if (k0 == null || k0.isBlank()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pega primero tu API Key.",
                "Probar", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String k = k0;
        final String m = (String) cmbModeloChat.getSelectedItem();
        new Thread(() -> {
            String resultado;
            com.google.genai.Client c = null;
            try {
                c = com.google.genai.Client.builder().apiKey(k).build();
                com.google.genai.types.GenerateContentConfig cfg =
                    com.google.genai.types.GenerateContentConfig.builder().build();
                java.util.List<com.google.genai.types.Content> msgs = java.util.List.of(
                    com.google.genai.types.Content.builder().role("user")
                        .parts(java.util.List.of(com.google.genai.types.Part.fromText("Di solo: OK")))
                        .build());
                StringBuilder sb = new StringBuilder();
                try (com.google.genai.ResponseStream<com.google.genai.types.GenerateContentResponse> rs =
                        c.models.generateContentStream(m, msgs, cfg)) {
                    for (com.google.genai.types.GenerateContentResponse r : rs) {
                        if (!r.candidates().isEmpty()
                                && r.candidates().get().get(0).content().isPresent()
                                && r.candidates().get().get(0).content().get().parts().isPresent()) {
                            for (com.google.genai.types.Part p :
                                    r.candidates().get().get(0).content().get().parts().get()) {
                                if (p.text().isPresent()) {
                                    sb.append(p.text().get());
                                }
                            }
                        }
                    }
                }
                resultado = "OK con " + m + ": " + (sb.length() > 0 ? sb.toString() : "(conecto sin texto)");
            } catch (Exception ex) {
                resultado = "Fallo: " + ex.getMessage();
            } finally {
                if (c != null) {
                    try { c.close(); } catch (Exception ignored) { }
                }
            }
            final String r2 = resultado;
            javax.swing.SwingUtilities.invokeLater(() ->
                javax.swing.JOptionPane.showMessageDialog(this, r2, "Probar", 1));
        }).start();
    }
}
