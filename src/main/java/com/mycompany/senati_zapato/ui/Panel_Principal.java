package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Panel_Principal extends JFrame {

    private JPanel chatbotWrapper;
    private final float[] animState = {0f, 0.5f};

    private static Color CLOSE_BG   = new Color(138, 82, 57);
    private static Color CLOSE_HOVER = new Color(110, 63, 44);

    protected JPanel topNavBar;
    protected JPanel tabsPanel;
    protected Boton_De_Pestana btnInicio;
    protected Boton_De_Pestana btnVentas;
    protected Boton_De_Pestana btnGestor;
    protected Boton_De_Pestana btnReportes;
    protected Boton_De_Pestana btnConfig;
    protected JPanel rightPanel;
    protected JButton btnMinimizeWindow;
    protected JButton btnCloseX;
    protected JButton btnFloatChat;
    protected JPanel contentPanel;
    private Image bgImage;
    protected JButton btnToggleTheme;

    public Panel_Principal() {
        CLOSE_BG = Gestor_De_Temas.getAccentColor();
        CLOSE_HOVER = Gestor_De_Temas.getHoverColor();
        try {
            bgImage = new ImageIcon(getClass().getResource("/images/bg_leather.png")).getImage();
        } catch (Exception e) {
            bgImage = null;
        }
        buildUI();
    }

    public JButton Get_Boton_Flotante_De_Chat() {
        return btnFloatChat;
    }

    public void Establecer_Modo_Oscuro(boolean nextDark) {
        SwingUtilities.invokeLater(() -> {
            Gestor_De_Temas.applyLeatherTheme(nextDark);
            CLOSE_BG = Gestor_De_Temas.getAccentColor();
            CLOSE_HOVER = Gestor_De_Temas.getHoverColor();
            if (btnToggleTheme != null) {
                updateThemeToggleButton(btnToggleTheme);
            }
            
            // Re-apply component tree UI changes
            SwingUtilities.updateComponentTreeUI(Panel_Principal.this);
            
            // Revalidate and repaint everything
            Panel_Principal.this.revalidate();
            Panel_Principal.this.repaint();
        });
    }

    public void Cambiar_Pestana(String cardName) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, cardName);
        if ("Inicio".equals(cardName)) {
            updateTabSelection(btnInicio);
        } else if ("Ventas".equals(cardName)) {
            updateTabSelection(btnVentas);
        } else if ("Gestor".equals(cardName)) {
            updateTabSelection(btnGestor);
        } else if ("Reportes".equals(cardName)) {
            updateTabSelection(btnReportes);
        } else if ("Config".equals(cardName)) {
            updateTabSelection(btnConfig);
        }
        
        // Sincronización reactiva de datos al alternar pestañas
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (cardName.equals("Ventas") && target instanceof Panel_De_Ventas) {
                ((Panel_De_Ventas) target).Refrescar_Catalogo();
            } else if (cardName.equals("Gestor") && target instanceof Panel_De_Inventario) {
                ((Panel_De_Inventario) target).Refrescar_Tabla();
            } else if (cardName.equals("Reportes") && target instanceof Panel_De_Reportes) {
                ((Panel_De_Reportes) target).Refrescar_Reportes();
            }
        }
    }

    public String Agregar_Producto_Al_Carrito(String busqueda, int cantidad) {
        Cambiar_Pestana("Ventas");
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (target instanceof Panel_De_Ventas) {
                return ((Panel_De_Ventas) target).Agregar_Producto_Al_Carrito(busqueda, cantidad);
            }
        }
        return "No se pudo acceder al módulo de Ventas.";
    }

    public String Vaciar_Carrito() {
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (target instanceof Panel_De_Ventas) {
                return ((Panel_De_Ventas) target).Vaciar_Carrito();
            }
        }
        return "No se pudo acceder al módulo de Ventas.";
    }

    public String Abrir_Panel_De_Pago() {
        Cambiar_Pestana("Ventas");
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (target instanceof Panel_De_Ventas) {
                return ((Panel_De_Ventas) target).Abrir_Panel_De_Pago();
            }
        }
        return "No se pudo acceder al módulo de Ventas.";
    }

    /** Confirma «Venta general» en el formulario del comprador (lo usa el asistente). */
    public String Seleccionar_Venta_General() {
        Cambiar_Pestana("Ventas");
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (target instanceof Panel_De_Ventas) {
                return ((Panel_De_Ventas) target).Seleccionar_Venta_General();
            }
        }
        return "No se pudo acceder al módulo de Ventas.";
    }

    /** Cambia a Ventas y deja el catálogo filtrado según la consulta del asistente. */
    public String Mostrar_En_Ventas(String consulta) {
        Cambiar_Pestana("Ventas");
        for (Component comp : contentPanel.getComponents()) {
            Component target = comp;
            if (comp instanceof javax.swing.JScrollPane) {
                target = ((javax.swing.JScrollPane) comp).getViewport().getView();
            }
            if (target instanceof Panel_De_Ventas) {
                return ((Panel_De_Ventas) target).Aplicar_Filtro_Desde_Chatbot(consulta);
            }
        }
        return "No se pudo acceder al módulo de Ventas.";
    }

    private void buildUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("SELLO MASCULINO — Sistema de Zapatería");
        try {
            setIconImage(new ImageIcon(getClass().getResource("/images/icono.jpg")).getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setUndecorated(true);
        setAlwaysOnTop(true);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        setPreferredSize(new Dimension(screenWidth, screenHeight));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Gestor_De_Temas.getBgColor());
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(root);

        // ── Barra superior con bordes redondeados y sombra ──
        topNavBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int barHeight = getHeight() - 10; // Dejar 10px al fondo para la sombra
                
                g2.setColor(Gestor_De_Temas.getNavColor());
                g2.fillRoundRect(0, 0, getWidth(), barHeight + 16, 24, 24);
                
                // Costura sutil
                g2.setColor(Gestor_De_Temas.getBorderColor());
                g2.fillRect(0, barHeight - 2, getWidth(), 2);
                
                // Sombra proyectada hacia abajo
                for (int i = 0; i < 10; i++) {
                    int alpha = (int)(30 * (1f - (float)i / 10));
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRect(0, barHeight + i, getWidth(), 1);
                }
                g2.dispose();
            }
        };
        topNavBar.setOpaque(false);
        topNavBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topNavBar.setPreferredSize(new Dimension(screenWidth, 80));

        // ── Tabs centrados ──
        tabsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20));
        tabsPanel.setOpaque(false);

        btnInicio   = createTab("Inicio", "Inicio", Icono_Elegante.Type.HOME);
        btnVentas   = createTab("Ventas", "Ventas", Icono_Elegante.Type.CART);
        btnGestor   = createTab("Inventario", "Gestor", Icono_Elegante.Type.BOX);
        btnReportes = createTab("Reportes", "Reportes", Icono_Elegante.Type.CHART);
        btnConfig = createTab("Config", "Config", Icono_Elegante.Type.SAVE);

        tabsPanel.add(btnInicio);
        tabsPanel.add(btnVentas);
        tabsPanel.add(btnGestor);
        tabsPanel.add(btnReportes);
        tabsPanel.add(btnConfig);

        topNavBar.add(tabsPanel, BorderLayout.CENTER);

        // ── Botón X rojo y Botón de Modo Oscuro ──
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        rightPanel.setOpaque(false);

        btnToggleTheme = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = Gestor_De_Temas.isDarkMode() ? new Color(253, 251, 247) : new Color(82, 50, 37);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, Gestor_De_Temas.isDarkMode() ? 70 : 35));
                g2.drawOval(3, 3, getWidth() - 7, getHeight() - 7);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnToggleTheme.setPreferredSize(new Dimension(40, 40));
        btnToggleTheme.setContentAreaFilled(false);
        btnToggleTheme.setBorderPainted(false);
        btnToggleTheme.setFocusPainted(false);
        btnToggleTheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        updateThemeToggleButton(btnToggleTheme);

        btnToggleTheme.addActionListener(e -> {
            boolean nextDark = !Gestor_De_Temas.isDarkMode();
            Gestor_De_Temas.applyLeatherTheme(nextDark);
            CLOSE_BG = Gestor_De_Temas.getAccentColor();
            CLOSE_HOVER = Gestor_De_Temas.getHoverColor();
            updateThemeToggleButton(btnToggleTheme);
            
            // Re-apply component tree UI changes
            SwingUtilities.updateComponentTreeUI(Panel_Principal.this);
            
            // Revalidate and repaint everything
            Panel_Principal.this.revalidate();
            Panel_Principal.this.repaint();
        });

        btnMinimizeWindow = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? Gestor_De_Temas.getHoverColor() : Gestor_De_Temas.getAccentColor());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnMinimizeWindow.setIcon(new Icono_Elegante(Icono_Elegante.Type.MINIMIZE, 20, Color.WHITE));
        btnMinimizeWindow.setToolTipText("Minimizar ventana");
        btnMinimizeWindow.setPreferredSize(new Dimension(40, 40));
        btnMinimizeWindow.setContentAreaFilled(false);
        btnMinimizeWindow.setBorderPainted(false);
        btnMinimizeWindow.setFocusPainted(false);
        btnMinimizeWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMinimizeWindow.addActionListener(e -> setState(Frame.ICONIFIED));

        btnCloseX = new JButton("X");
        btnCloseX.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCloseX.setForeground(Color.WHITE);
        btnCloseX.setBackground(CLOSE_BG);
        btnCloseX.setOpaque(true);
        btnCloseX.setBorderPainted(false);
        btnCloseX.setFocusPainted(false);
        btnCloseX.setMargin(new Insets(0, 0, 0, 0));
        btnCloseX.setPreferredSize(new Dimension(40, 40));
        btnCloseX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCloseX.addActionListener(e -> System.exit(0));
        btnCloseX.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnCloseX.setBackground(CLOSE_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btnCloseX.setBackground(CLOSE_BG); }
        });

        rightPanel.add(btnToggleTheme);
        rightPanel.add(btnMinimizeWindow);
        rightPanel.add(btnCloseX);
        topNavBar.add(rightPanel, BorderLayout.EAST);

        root.add(topNavBar, BorderLayout.NORTH);

        // ── Panel de contenido (CardLayout) ──
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        contentPanel.add(crearScrollWrapper(new Panel_De_Inicio(this)), "Inicio");
        contentPanel.add(new Panel_De_Ventas(),    "Ventas");
        contentPanel.add(new Panel_De_Inventario(),    "Gestor");
        contentPanel.add(crearScrollWrapper(new Panel_De_Reportes()),  "Reportes");
        contentPanel.add(crearScrollWrapper(new Panel_De_Configuracion()), "Config");

        root.add(contentPanel, BorderLayout.CENTER);

        // ── Capa de superposición para chatbot flotante ──
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(screenWidth, screenHeight));
        setContentPane(layeredPane);
        
        root.setBounds(0, 0, screenWidth, screenHeight);
        layeredPane.add(root, JLayeredPane.DEFAULT_LAYER);

        animState[0] = 0f;
        animState[1] = 0.5f;

        chatbotWrapper = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animState[0]));
                // Scale from bottom right
                g2.translate(getWidth() * (1 - animState[1]), getHeight() * (1 - animState[1]));
                g2.scale(animState[1], animState[1]);
                super.paint(g2);
                g2.dispose();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra suave (Drop Shadow difuminado)
                int shadowLevels = 10;
                for (int i = 0; i < shadowLevels; i++) {
                    int alpha = (int) (30 * (1.0f - (float) i / shadowLevels));
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillRoundRect(8 - i, 12 - i, getWidth() - 16 + i * 2, getHeight() - 16 + i * 2, 32, 32);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chatbotWrapper.setOpaque(false);
        chatbotWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 16));
        chatbotWrapper.setLayout(null);
        
        Panel_De_Chatbot chatbot = new Panel_De_Chatbot(this);
        chatbot.setBorder(BorderFactory.createEmptyBorder());
        chatbotWrapper.add(chatbot);

        chatbotWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                chatbot.setBounds(0, 0, chatbotWrapper.getWidth(), chatbotWrapper.getHeight());
            }
        });

        chatbotWrapper.setBounds(screenWidth - 450 - 16, screenHeight - 620, 416, 516);
        chatbotWrapper.setVisible(false);
        layeredPane.add(chatbotWrapper, JLayeredPane.POPUP_LAYER);

        btnFloatChat = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra suave difuminada circular
                for (int i = 0; i < 6; i++) {
                    int alpha = (int)(30 * (1f - (float)i / 6));
                    g2.setColor(new Color(0, 0, 0, alpha));
                    g2.fillOval(i, i + 3, getWidth() - i * 2, getHeight() - i * 2);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        try {
            Image iconImg = new ImageIcon(getClass().getResource("/images/chatbot_icon.png")).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            btnFloatChat.setIcon(new ImageIcon(iconImg));
        } catch(Exception e) {
            btnFloatChat.setText("Chat");
        }
        btnFloatChat.setBounds(screenWidth - 120, screenHeight - 120, 96, 96);
        btnFloatChat.setContentAreaFilled(false);
        btnFloatChat.setBorderPainted(false);
        btnFloatChat.setFocusPainted(false);
        btnFloatChat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        MouseAdapter chatMouseAdapter = new MouseAdapter() {
            private int initialX;
            private int initialY;
            private int originalY;
            private boolean wasDragged = false;
            private int offsetX, offsetY; // Offset between icon and wrapper

            @Override public void mouseEntered(MouseEvent e) {
                originalY = btnFloatChat.getY();
                btnFloatChat.setLocation(btnFloatChat.getX(), originalY - 5);
            }
            @Override public void mouseExited(MouseEvent e) {
                btnFloatChat.setLocation(btnFloatChat.getX(), originalY);
            }
            @Override public void mousePressed(MouseEvent e) {
                initialX = e.getX();
                initialY = e.getY();
                wasDragged = false;
                // Calculate offset: wrapper position relative to icon
                offsetX = chatbotWrapper.getX() - btnFloatChat.getX();
                offsetY = chatbotWrapper.getY() - btnFloatChat.getY();
            }
            @Override public void mouseDragged(MouseEvent e) {
                wasDragged = true;
                int newX = btnFloatChat.getX() + e.getX() - initialX;
                int newY = btnFloatChat.getY() + e.getY() - initialY;
                
                newX = Math.max(0, Math.min(newX, Panel_Principal.this.getWidth() - btnFloatChat.getWidth()));
                newY = Math.max(0, Math.min(newY, Panel_Principal.this.getHeight() - btnFloatChat.getHeight()));
                
                btnFloatChat.setLocation(newX, newY);
                originalY = newY;
                
                // Sync wrapper with icon movement
                if (chatbotWrapper.isVisible()) {
                    chatbotWrapper.setLocation(newX + offsetX, newY + offsetY);
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (!wasDragged) {
                    if (!chatbotWrapper.isVisible() || animState[0] == 0f) {
                        int cx = btnFloatChat.getX() - chatbotWrapper.getWidth() + btnFloatChat.getWidth();
                        int cy = btnFloatChat.getY() - chatbotWrapper.getHeight() - 10;
                        if (cx < 0) cx = btnFloatChat.getX();
                        if (cy < 0) cy = btnFloatChat.getY() + btnFloatChat.getHeight() + 10;
                        chatbotWrapper.setLocation(cx, cy);

                        chatbotWrapper.setVisible(true);
                        animState[0] = 0f;
                        animState[1] = 0.5f;
                        Timer t = new Timer(15, null);
                        t.addActionListener(ev -> {
                            animState[0] = Math.min(1f, animState[0] + 0.1f);
                            animState[1] = Math.min(1f, animState[1] + 0.05f);
                            chatbotWrapper.repaint();
                            if (animState[0] >= 1f && animState[1] >= 1f) {
                                ((Timer)ev.getSource()).stop();
                            }
                        });
                        t.start();
                    } else {
                        Timer t = new Timer(15, null);
                        t.addActionListener(ev -> {
                            animState[0] = Math.max(0f, animState[0] - 0.1f);
                            animState[1] = Math.max(0.5f, animState[1] - 0.05f);
                            chatbotWrapper.repaint();
                            if (animState[0] <= 0f) {
                                chatbotWrapper.setVisible(false);
                                ((Timer)ev.getSource()).stop();
                            }
                        });
                        t.start();
                    }
                }
            }
        };
        btnFloatChat.addMouseListener(chatMouseAdapter);
        btnFloatChat.addMouseMotionListener(chatMouseAdapter);
        
        layeredPane.add(btnFloatChat, JLayeredPane.PALETTE_LAYER);

        // Establecer Inicio como activo inicialmente
        updateTabSelection(btnInicio);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();
                btnFloatChat.setBounds(w - 120, h - 120, 96, 96);
                if (chatbotWrapper.isVisible()) {
                    int cx = btnFloatChat.getX() - chatbotWrapper.getWidth() + btnFloatChat.getWidth();
                    int cy = btnFloatChat.getY() - chatbotWrapper.getHeight() - 10;
                    chatbotWrapper.setLocation(cx, cy);
                }
            }
        });

        pack();
        setSize(screenWidth, screenHeight);
        setLocationRelativeTo(null);
    }

    public void Minimizar_Chatbot() {
        if (!chatbotWrapper.isVisible() || animState[0] <= 0f) return;
        Timer t = new Timer(15, null);
        t.addActionListener(ev -> {
            animState[0] = Math.max(0f, animState[0] - 0.1f);
            animState[1] = Math.max(0.5f, animState[1] - 0.05f);
            chatbotWrapper.repaint();
            if (animState[0] <= 0f) {
                chatbotWrapper.setVisible(false);
                ((Timer) ev.getSource()).stop();
            }
        });
        t.start();
    }

    private Boton_De_Pestana createTab(String text, String cardName, Icono_Elegante.Type iconType) {
        Boton_De_Pestana btn = new Boton_De_Pestana(text);
        btn.setIcon(new Icono_Elegante(iconType, 24));
        btn.setIconTextGap(10);
        btn.addActionListener(e -> {
            Cambiar_Pestana(cardName);
            updateTabSelection(btn);
        });
        return btn;
    }

    private void updateTabSelection(Boton_De_Pestana activeBtn) {
        btnInicio.setActive(btnInicio == activeBtn);
        btnVentas.setActive(btnVentas == activeBtn);
        btnGestor.setActive(btnGestor == activeBtn);
        btnReportes.setActive(btnReportes == activeBtn);
        btnConfig.setActive(btnConfig == activeBtn);
    }

    private void updateThemeToggleButton(JButton btn) {
        if (Gestor_De_Temas.isDarkMode()) {
            btn.setIcon(new Icono_Elegante(Icono_Elegante.Type.SUN, 22, new Color(138, 82, 57)));
            btn.setToolTipText("Cambiar a Modo Claro");
        } else {
            btn.setIcon(new Icono_Elegante(Icono_Elegante.Type.MOON, 22, new Color(253, 231, 177)));
            btn.setToolTipText("Cambiar a Modo Oscuro");
        }
    }

    private javax.swing.JScrollPane crearScrollWrapper(javax.swing.JPanel panel) {
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    public byte[] Capturar_Pantalla_Actual() {
        try {
            java.awt.image.BufferedImage image = new java.awt.Robot().createScreenCapture(this.getBounds());
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
