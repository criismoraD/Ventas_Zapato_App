package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;


import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * JDialog de Pago Premium para el POS (Punto de Venta).
 * Soporta selección de métodos de pago, montos rápidos para efectivo y código de referencia digital.
 */
public class Dialogo_De_Pago extends JDialog {

    private final Venta venta;
    private final double totalAPagar;
    private boolean pagoCompletado = false;

    // Componentes principales
    private CardLayout cardLayout;
    private JPanel centerCards;
    private String metodoSeleccionado = "Efectivo";

    // Campos de Efectivo
    private JTextField txtMontoRecibido;
    private JLabel lblVueltoDisplay;
    private JButton btnConfirmarEfectivo;
    
    // Campos de Tarjeta
    private JTextField txtRefTarjeta;
    private JTextField txtTerminalId;
    private JButton btnConfirmarTarjeta;

    // Campos Digitales (Yape, Plin, Transferencia)
    private JTextField txtRefDigital;
    private JButton btnConfirmarDigital;
    private JLabel lblDigitalInstruction;

    // Botones de Métodos de Pago
    private JButton btnMetodoEfectivo;
    private JButton btnMetodoTarjeta;
    private JButton btnMetodoYape;
    private JButton btnMetodoPlin;
    private JButton btnMetodoTransferencia;

    public Dialogo_De_Pago(Frame parent, Venta venta) {
        super(parent, "Procesar Pago - POS", true);
        this.venta = venta;
        this.totalAPagar = venta.Get_Monto_Total();

        setUndecorated(true); // Estilo personalizado sin bordes nativos
        setSize(700, 600);
        setLocationRelativeTo(parent);
        
        buildUI();
        seleccionarMetodo("Efectivo");
    }

    public boolean Esta_Pago_Completado() {
        return pagoCompletado;
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Gestor_De_Temas.getBgColor());
        container.setBorder(new LineBorder(Gestor_De_Temas.getBorderColor(), 2, true));
        setContentPane(container);

        // ======================= 1. CABECERA PERSONALIZADA =======================
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Gestor_De_Temas.getNavColor());
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 12, 12);
                g2.setColor(Gestor_De_Temas.getBorderColor());
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(0, 65));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("PAGO DE TICKET");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        lblTitle.setForeground(Gestor_De_Temas.getTextColor());
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Mostrar Total a pagar de forma imponente
        JLabel lblTotalHeader = new JLabel(String.format("Total: S/ %.2f", totalAPagar));
        lblTotalHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalHeader.setForeground(Gestor_De_Temas.getAccentColor());
        headerPanel.add(lblTotalHeader, BorderLayout.EAST);

        container.add(headerPanel, BorderLayout.NORTH);

        // ======================= 2. PANEL IZQUIERDO: MÉTODOS DE PAGO =======================
        JPanel methodsPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        methodsPanel.setOpaque(false);
        methodsPanel.setBorder(new EmptyBorder(25, 20, 25, 10));
        methodsPanel.setPreferredSize(new Dimension(240, 0));

        btnMetodoEfectivo = crearBotonMetodo("Efectivo", null, Icono_Elegante.Type.CASH);
        btnMetodoTarjeta = crearBotonMetodo("Tarjeta", "/images/tarjeta-bancaria.png", Icono_Elegante.Type.CREDIT_CARD);
        btnMetodoYape = crearBotonMetodo("Yape", "/images/YAPE.png", Icono_Elegante.Type.CREDIT_CARD);
        btnMetodoPlin = crearBotonMetodo("Plin", "/images/Plin.png", Icono_Elegante.Type.CREDIT_CARD);
        btnMetodoTransferencia = crearBotonMetodo("Transferencia", "/images/Transferencia.png", Icono_Elegante.Type.CREDIT_CARD);

        btnMetodoEfectivo.addActionListener(e -> seleccionarMetodo("Efectivo"));
        btnMetodoTarjeta.addActionListener(e -> seleccionarMetodo("Tarjeta"));
        btnMetodoYape.addActionListener(e -> seleccionarMetodo("Yape"));
        btnMetodoPlin.addActionListener(e -> seleccionarMetodo("Plin"));
        btnMetodoTransferencia.addActionListener(e -> seleccionarMetodo("Transferencia"));

        methodsPanel.add(btnMetodoEfectivo);
        methodsPanel.add(btnMetodoTarjeta);
        methodsPanel.add(btnMetodoYape);
        methodsPanel.add(btnMetodoPlin);
        methodsPanel.add(btnMetodoTransferencia);

        container.add(methodsPanel, BorderLayout.WEST);

        // ======================= 3. PANEL CENTRAL: FORMULARIOS TIPO CARD =======================
        cardLayout = new CardLayout();
        centerCards = new JPanel(cardLayout);
        centerCards.setOpaque(false);
        centerCards.setBorder(new EmptyBorder(25, 10, 25, 20));

        centerCards.add(crearCardEfectivo(), "Efectivo");
        centerCards.add(crearCardTarjeta(), "Tarjeta");
        centerCards.add(crearCardDigital(), "Digital");

        container.add(centerCards, BorderLayout.CENTER);

        // ======================= 4. BARRA DE ACCIÓN INFERIOR =======================
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Gestor_De_Temas.getBorderColor()));

        JButton btnCancelar = new JButton("Cancelar (Esc)");
        btnCancelar.setIcon(new Icono_Elegante(Icono_Elegante.Type.CLOSE, 14));
        btnCancelar.setIconTextGap(8);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());
        actionPanel.add(btnCancelar);

        container.add(actionPanel, BorderLayout.SOUTH);

        // Soporte para cerrar con tecla ESC
        container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        container.getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private JButton crearBotonMetodo(String text, String imageResourcePath, Icono_Elegante.Type fallbackIcon) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", Font.BOLD, 15));
        btn.setForeground(Gestor_De_Temas.getMutedColor());
        btn.setBackground(Gestor_De_Temas.getNavColor());
        
        Icon buttonIcon = null;
        if (imageResourcePath != null) {
            try {
                java.net.URL imgUrl = getClass().getResource(imageResourcePath);
                if (imgUrl != null) {
                    ImageIcon imgIcon = new ImageIcon(imgUrl);
                    Image scaled = imgIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    buttonIcon = new ImageIcon(scaled);
                }
            } catch (Exception e) {
                buttonIcon = null;
            }
        }
        
        if (buttonIcon == null) {
            buttonIcon = new Icono_Elegante(fallbackIcon, 20, Gestor_De_Temas.getMutedColor());
        }
        
        btn.setIcon(buttonIcon);
        btn.setIconTextGap(12);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void destacarBotonMetodo(JButton activeBtn) {
        JButton[] todos = {btnMetodoEfectivo, btnMetodoTarjeta, btnMetodoYape, btnMetodoPlin, btnMetodoTransferencia};
        for (JButton b : todos) {
            b.setBackground(Gestor_De_Temas.getNavColor());
            b.setForeground(Gestor_De_Temas.getMutedColor());
            b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
                new EmptyBorder(12, 15, 12, 15)
            ));
            if (b.getIcon() instanceof Icono_Elegante) {
                ((Icono_Elegante)b.getIcon()).setColor(Gestor_De_Temas.getMutedColor());
            }
        }
        activeBtn.setBackground(Gestor_De_Temas.getAccentColor());
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getAccentColor(), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        if (activeBtn.getIcon() instanceof Icono_Elegante) {
            ((Icono_Elegante)activeBtn.getIcon()).setColor(Color.WHITE);
        }
    }

    private void seleccionarMetodo(String metodo) {
        this.metodoSeleccionado = metodo;
        switch (metodo) {
            case "Efectivo":
                destacarBotonMetodo(btnMetodoEfectivo);
                cardLayout.show(centerCards, "Efectivo");
                setTitle("Cobro asistido - Paso 1 de 2: ingresar efectivo");
                SwingUtilities.invokeLater(() -> txtMontoRecibido.requestFocusInWindow());
                break;
            case "Tarjeta":
                destacarBotonMetodo(btnMetodoTarjeta);
                cardLayout.show(centerCards, "Tarjeta");
                setTitle("Cobro asistido - Paso 1 de 2: verificar tarjeta");
                break;
            case "Yape":
                destacarBotonMetodo(btnMetodoYape);
                lblDigitalInstruction.setText("Paso 1: pague con Yape, verifique la operación e ingrese la referencia:");
                setTitle("Cobro asistido - Paso 1 de 2: verificar Yape");
                cardLayout.show(centerCards, "Digital");
                break;
            case "Plin":
                destacarBotonMetodo(btnMetodoPlin);
                lblDigitalInstruction.setText("Paso 1: pague con Plin, verifique la operación e ingrese la referencia:");
                setTitle("Cobro asistido - Paso 1 de 2: verificar Plin");
                cardLayout.show(centerCards, "Digital");
                break;
            case "Transferencia":
                destacarBotonMetodo(btnMetodoTransferencia);
                lblDigitalInstruction.setText("Paso 1: realice la transferencia, verifique la operación e ingrese la referencia:");
                setTitle("Cobro asistido - Paso 1 de 2: verificar transferencia");
                cardLayout.show(centerCards, "Digital");
                break;
        }
    }

    // ======================= FORMULARIO 1: EFECTIVO =======================
    private JPanel crearCardEfectivo() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setOpaque(false);

        // Header interno
        JLabel lblHeader = new JLabel("PAGO EN EFECTIVO - Ingresa el monto recibido");
        lblHeader.setFont(new Font("Georgia", Font.BOLD, 16));
        lblHeader.setForeground(Gestor_De_Temas.getAccentColor());
        card.add(lblHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;

        // Fila 1: Campo de Monto Recibido
        gbc.gridy = 0;
        JLabel lblRecibido = new JLabel("Monto Recibido (S/):");
        lblRecibido.setFont(new Font("Segoe UI", Font.BOLD, 14));
        body.add(lblRecibido, gbc);

        gbc.gridy = 1;
        txtMontoRecibido = new JTextField();
        txtMontoRecibido.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtMontoRecibido.setPreferredSize(new Dimension(0, 50));
        txtMontoRecibido.setHorizontalAlignment(JTextField.RIGHT);
        txtMontoRecibido.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        txtMontoRecibido.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarVuelto();
            }
        });
        body.add(txtMontoRecibido, gbc);

        // Fila 2: Botones Rápidos
        gbc.gridy = 2;
        JPanel quickCashPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        quickCashPanel.setOpaque(false);
        quickCashPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        double base = Math.max(200.0, Math.ceil(totalAPagar / 50.0) * 50.0);
        double[] denominaciones = {
            totalAPagar,
            base,
            base + 50.0,
            base + 100.0,
            base + 150.0,
            base + 200.0
        };
        String[] labels = {
            "EXACTO",
            String.format("S/ %.0f", denominaciones[1]),
            String.format("S/ %.0f", denominaciones[2]),
            String.format("S/ %.0f", denominaciones[3]),
            String.format("S/ %.0f", denominaciones[4]),
            String.format("S/ %.0f", denominaciones[5])
        };

        for (int i = 0; i < denominaciones.length; i++) {
            final double valor = denominaciones[i];
            JButton btnDenom = new JButton(labels[i]);
            btnDenom.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnDenom.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDenom.addActionListener(e -> {
                txtMontoRecibido.setText(String.format("%.2f", valor).replace(",", "."));
                actualizarVuelto();
            });
            quickCashPanel.add(btnDenom);
        }
        body.add(quickCashPanel, gbc);

        // Fila 3: Vuelto Display
        gbc.gridy = 3;
        lblVueltoDisplay = new JLabel("Vuelto: S/ 0.00", SwingConstants.CENTER);
        lblVueltoDisplay.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblVueltoDisplay.setOpaque(true);
        lblVueltoDisplay.setBackground(Gestor_De_Temas.getNavColor());
        lblVueltoDisplay.setForeground(new Color(220, 53, 69)); // Inicialmente en rojo ("Falta")
        lblVueltoDisplay.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(15, 10, 15, 10)
        ));
        body.add(lblVueltoDisplay, gbc);

        card.add(body, BorderLayout.CENTER);

        // Botón Confirmar
        btnConfirmarEfectivo = new JButton("CONFIRMAR E INTEGRAR VENTA");
        btnConfirmarEfectivo.setIcon(new Icono_Elegante(Icono_Elegante.Type.CREDIT_CARD, 18, Color.WHITE));
        btnConfirmarEfectivo.setIconTextGap(8);
        btnConfirmarEfectivo.setFont(new Font("Georgia", Font.BOLD, 15));
        btnConfirmarEfectivo.setForeground(Color.WHITE);
        btnConfirmarEfectivo.setBackground(new Color(79, 133, 87));
        btnConfirmarEfectivo.setPreferredSize(new Dimension(0, 50));
        btnConfirmarEfectivo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmarEfectivo.setEnabled(false); // Deshabilitado hasta que cubra el monto
        btnConfirmarEfectivo.addActionListener(e -> procesarTransaccion());
        
        card.add(btnConfirmarEfectivo, BorderLayout.SOUTH);

        return card;
    }

    private void actualizarVuelto() {
        try {
            String txt = txtMontoRecibido.getText().trim();
            if (txt.isEmpty()) {
                lblVueltoDisplay.setText("Vuelto: S/ 0.00");
                lblVueltoDisplay.setForeground(Gestor_De_Temas.getTextColor());
                btnConfirmarEfectivo.setEnabled(false);
                return;
            }
            double recibido = Double.parseDouble(txt);
            double dif = recibido - totalAPagar;
            if (dif >= 0) {
                lblVueltoDisplay.setText(String.format("Vuelto: S/ %.2f", dif));
                lblVueltoDisplay.setForeground(new Color(40, 167, 69)); // Verde éxito
                btnConfirmarEfectivo.setEnabled(true);
            } else {
                lblVueltoDisplay.setText(String.format("Falta: S/ %.2f", Math.abs(dif)));
                lblVueltoDisplay.setForeground(new Color(220, 53, 69)); // Rojo error
                btnConfirmarEfectivo.setEnabled(false);
            }
        } catch (NumberFormatException e) {
            lblVueltoDisplay.setText("Monto Inválido");
            lblVueltoDisplay.setForeground(new Color(220, 53, 69));
            btnConfirmarEfectivo.setEnabled(false);
        }
    }

    // ======================= FORMULARIO 2: TARJETA =======================
    private JPanel crearCardTarjeta() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setOpaque(false);

        JLabel lblHeader = new JLabel("PAGO CON TARJETA (CRÉDITO/DÉBITO) - Verifica y confirma");
        lblHeader.setFont(new Font("Georgia", Font.BOLD, 16));
        lblHeader.setForeground(Gestor_De_Temas.getAccentColor());
        card.add(lblHeader, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1.0;

        // Icono o Gráfico representativo
        gbc.gridy = 0;
        JLabel lblIcon = new JLabel("", SwingConstants.CENTER);
        lblIcon.setIcon(new Icono_Elegante(Icono_Elegante.Type.CREDIT_CARD, 60, Gestor_De_Temas.getAccentColor()));
        body.add(lblIcon, gbc);

        gbc.gridy = 1;
        JLabel lblNro = new JLabel("Código de autorización / voucher del POS:");
        lblNro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        body.add(lblNro, gbc);

        gbc.gridy = 2;
        txtRefTarjeta = new JTextField();
        txtRefTarjeta.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtRefTarjeta.setPreferredSize(new Dimension(0, 45));
        txtRefTarjeta.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        body.add(txtRefTarjeta, gbc);

        gbc.gridy = 3;
        JLabel lblTerminal = new JLabel("Identificador del POS (opcional):");
        lblTerminal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        body.add(lblTerminal, gbc);

        gbc.gridy = 4;
        txtTerminalId = new JTextField();
        txtTerminalId.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtTerminalId.setPreferredSize(new Dimension(0, 45));
        txtTerminalId.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        body.add(txtTerminalId, gbc);

        card.add(body, BorderLayout.CENTER);

        btnConfirmarTarjeta = new JButton("CONFIRMAR E INTEGRAR VENTA");
        btnConfirmarTarjeta.setIcon(new Icono_Elegante(Icono_Elegante.Type.CREDIT_CARD, 18, Color.WHITE));
        btnConfirmarTarjeta.setIconTextGap(8);
        btnConfirmarTarjeta.setFont(new Font("Georgia", Font.BOLD, 15));
        btnConfirmarTarjeta.setForeground(Color.WHITE);
        btnConfirmarTarjeta.setBackground(new Color(79, 133, 87));
        btnConfirmarTarjeta.setPreferredSize(new Dimension(0, 50));
        btnConfirmarTarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmarTarjeta.addActionListener(e -> procesarTransaccion());
        card.add(btnConfirmarTarjeta, BorderLayout.SOUTH);

        return card;
    }

    // ======================= FORMULARIO 3: DIGITAL =======================
    private JPanel crearCardDigital() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setOpaque(false);

        lblDigitalInstruction = new JLabel("PAGO DIGITAL (YAPE/PLIN) - Realiza y verifica el pago");
        lblDigitalInstruction.setFont(new Font("Georgia", Font.BOLD, 16));
        lblDigitalInstruction.setForeground(Gestor_De_Temas.getAccentColor());
        card.add(lblDigitalInstruction, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 15));
        body.setOpaque(false);

        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setOpaque(false);
        JLabel qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String qrPath = System.getProperty("senati.qr.digital", "");
        java.io.File qrFile = new java.io.File(qrPath);
        if (qrFile.isFile()) {
            qrLabel.setIcon(new ImageIcon(qrPath));
        } else {
            qrLabel.setText("QR no configurado. Verifique el pago en la aplicación.");
            qrLabel.setForeground(Gestor_De_Temas.getTextColor());
        }
        qrPanel.add(qrLabel, BorderLayout.CENTER);
        qrPanel.setPreferredSize(new Dimension(0, 170));
        body.add(qrPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        JLabel lblRef = new JLabel("Código de Referencia / Operación:");
        lblRef.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(lblRef, BorderLayout.NORTH);

        txtRefDigital = new JTextField();
        txtRefDigital.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtRefDigital.setPreferredSize(new Dimension(0, 45));
        txtRefDigital.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        inputPanel.add(txtRefDigital, BorderLayout.CENTER);
        body.add(inputPanel, BorderLayout.CENTER);

        card.add(body, BorderLayout.CENTER);

        btnConfirmarDigital = new JButton("CONFIRMAR E INTEGRAR VENTA");
        btnConfirmarDigital.setIcon(new Icono_Elegante(Icono_Elegante.Type.CREDIT_CARD, 18, Color.WHITE));
        btnConfirmarDigital.setIconTextGap(8);
        btnConfirmarDigital.setFont(new Font("Georgia", Font.BOLD, 15));
        btnConfirmarDigital.setForeground(Color.WHITE);
        btnConfirmarDigital.setBackground(new Color(79, 133, 87));
        btnConfirmarDigital.setPreferredSize(new Dimension(0, 50));
        btnConfirmarDigital.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmarDigital.addActionListener(e -> procesarTransaccion());
        card.add(btnConfirmarDigital, BorderLayout.SOUTH);

        return card;
    }

    // ======================= LÓGICA DE TRANSACCIÓN =======================
    private void procesarTransaccion() {
        double recibido = totalAPagar;
        double vuelto = 0.0;
        String referencia = "";

        if (metodoSeleccionado.equals("Efectivo")) {
            try {
                recibido = Double.parseDouble(txtMontoRecibido.getText().trim());
                vuelto = recibido - totalAPagar;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "El monto recibido no es válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (metodoSeleccionado.equals("Tarjeta")) {
            referencia = txtRefTarjeta.getText().trim();
            if (referencia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Registre el código de autorización aprobado por el POS.",
                        "Autorización requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else { // Yape, Plin, Transferencia
            referencia = txtRefDigital.getText().trim();
            if (referencia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Registre el código de operación después de verificar el pago.",
                        "Referencia requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            venta.setTerminalId(metodoSeleccionado.equals("Tarjeta")
                    ? txtTerminalId.getText().trim() : "");
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "Paso 2 de 2: confirme únicamente después de verificar que el pago fue aprobado " +
                    "en el POS, Yape, Plin o banca móvil.\n\n" +
                    "Método: " + metodoSeleccionado + "\nMonto: S/ " +
                    String.format("%.2f", totalAPagar) +
                    (referencia.isBlank() ? "" : "\nReferencia: " + referencia),
                    "Confirmar pago verificado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
            boolean exito = Procesador_De_Pagos.Procesar_Pago(venta, metodoSeleccionado, recibido, vuelto, referencia);
            if (exito) {
                pagoCompletado = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Hubo un error inesperado al integrar la venta en la base de datos.", "Error de Transacción", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "La transacción fue revertida (Rollback). Detalle:\n" + ex.getMessage(), "Error de Integridad (ACID)", JOptionPane.ERROR_MESSAGE);
        }
    }
}
