package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

public class Panel_De_Chatbot extends JPanel {

    private static class ChatMsg {
        boolean isAssistant;
        String text;
        String time;
        boolean thinking;
        ChatMsg(boolean asst, String txt, String tm) { isAssistant=asst; text=txt; time=tm; }
    }
    private List<ChatMsg> messages = new ArrayList<>();
    private JTextField txtInput;
    private JPanel chatBody;
    private JScrollPane chatScroll;

    private Image avatarImg;
    private Panel_Principal frameRef;
    private boolean arrowHovered = false;
    private boolean sendHovered = false;
    private boolean clipHovered = false;
    private boolean voiceHovered = false;
    private static final int EDGE = 8;
    private Servicio_De_Gemini geminiService;
    private byte[] pendingImage = null;
    private String pendingImageName = null;
    private String pendingImageMimeType = "image/jpeg";
    private Timer thinkingTimer;
    private int thinkingFrame = 0;
    private boolean awaitingResponse = false;
    private Servicio_De_Voz_En_Vivo liveVoiceService;
    private boolean liveVoiceActive = false;
    private String assistantStatus = "Listo";
    private Color assistantStatusColor = new Color(225, 245, 225);

    public Panel_De_Chatbot() {
        this.frameRef = null;
        initUI();
    }

    public Panel_De_Chatbot(Panel_Principal frame) {
        this.frameRef = frame;
        initUI();
    }

    private void initUI() {
        // Inicializar Gemini LLM
        geminiService = new Servicio_De_Gemini();
        geminiService.setOnNavigate(modulo -> {
            if (frameRef != null) {
                SwingUtilities.invokeLater(() -> frameRef.Cambiar_Pestana(modulo));
            }
        });
        geminiService.setOnAddCart((producto, cantidad) -> {
            if (frameRef != null) {
                final String[] res = new String[1];
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        res[0] = frameRef.Agregar_Producto_Al_Carrito(producto, cantidad);
                    } else {
                        SwingUtilities.invokeAndWait(() -> res[0] = frameRef.Agregar_Producto_Al_Carrito(producto, cantidad));
                    }
                } catch (Exception e) {
                    res[0] = frameRef.Agregar_Producto_Al_Carrito(producto, cantidad);
                }
                return res[0];
            }
            return "Error: referencia a la ventana principal no configurada.";
        });
        geminiService.setOnCheckout(() -> {
            if (frameRef != null) {
                final String[] res = new String[1];
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        res[0] = frameRef.Abrir_Panel_De_Pago();
                    } else {
                        SwingUtilities.invokeAndWait(() -> res[0] = frameRef.Abrir_Panel_De_Pago());
                    }
                } catch (Exception e) {
                    res[0] = frameRef.Abrir_Panel_De_Pago();
                }
                return res[0];
            }
            return "Error: referencia a la ventana principal no configurada.";
        });
        geminiService.setOnCancel(() -> {
            if (frameRef != null) {
                final String[] res = new String[1];
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        res[0] = frameRef.Vaciar_Carrito();
                    } else {
                        SwingUtilities.invokeAndWait(() -> res[0] = frameRef.Vaciar_Carrito());
                    }
                } catch (Exception e) {
                    res[0] = frameRef.Vaciar_Carrito();
                }
                return res[0];
            }
            return "Error: referencia a la ventana principal no configurada.";
        });
        geminiService.setOnVentaGeneral(() -> {
            if (frameRef != null) {
                final String[] res = new String[1];
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        res[0] = frameRef.Seleccionar_Venta_General();
                    } else {
                        SwingUtilities.invokeAndWait(() -> res[0] = frameRef.Seleccionar_Venta_General());
                    }
                } catch (Exception e) {
                    res[0] = frameRef.Seleccionar_Venta_General();
                }
                return res[0];
            }
            return "Error: referencia a la ventana principal no configurada.";
        });
        geminiService.setOnShowInSales(consulta -> {
            if (frameRef != null) {
                final String[] res = new String[1];
                try {
                    if (SwingUtilities.isEventDispatchThread()) {
                        res[0] = frameRef.Mostrar_En_Ventas(consulta);
                    } else {
                        SwingUtilities.invokeAndWait(() -> res[0] = frameRef.Mostrar_En_Ventas(consulta));
                    }
                } catch (Exception e) {
                    res[0] = frameRef.Mostrar_En_Ventas(consulta);
                }
                return res[0];
            }
            return "Error: referencia a la ventana principal no configurada.";
        });
        geminiService.setOnSetDarkMode(activar -> {
            if (frameRef != null) {
                frameRef.Establecer_Modo_Oscuro(activar);
            }
        });

        try {
            avatarImg = new ImageIcon(getClass().getResource("/images/avatar_assistant.png")).getImage();
        } catch (Exception e) {
            avatarImg = null;
        }

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 220));
        setOpaque(false);

        // ── Cabecera Terracota del Chat ──
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(Gestor_De_Temas.getAccentColor());
                g2.fillRoundRect(0, 0, w, h + 10, 24, 24);

                // Costura superior blanca elegante
                g2.setColor(new Color(253, 251, 247, 100));
                float[] dash = {4.0f, 4.0f};
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawRoundRect(6, 6, w - 12, h + 10, 16, 16);

                // Avatar circular del asistente (sin fondo blanco)
                int avSize = 58;
                int avX = 12;
                int avY = (h - avSize) / 2;

                if (avatarImg != null) {
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(avX, avY, avSize, avSize));
                    g2.drawImage(avatarImg, avX, avY, avSize, avSize, null);
                    g2.setClip(null);
                } else {
                    g2.setColor(new Color(253, 251, 247));
                    g2.fillOval(avX, avY, avSize, avSize);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                    g2.drawString("\uD83E\uDDD1", avX + 10, avY + 36);
                }

                // Título centrado verticalmente
                g2.setColor(new Color(253, 251, 247));
                g2.setFont(new Font("Georgia", Font.BOLD, 20));
                FontMetrics fmTitle = g2.getFontMetrics();
                int titleY = avY + (avSize + fmTitle.getAscent()) / 2 - 2;
                g2.drawString("Asistente Virtual", avX + avSize + 15, titleY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(assistantStatusColor);
                g2.drawString(assistantStatus, avX + avSize + 16, titleY + 19);

                // Flecha hacia abajo para cerrar
                int cx = w - 30;
                int cy = avY + 24;
                g2.setColor(arrowHovered ? new Color(255, 255, 255, 230) : Color.WHITE);
                g2.setStroke(new BasicStroke(arrowHovered ? 4f : 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(new int[]{cx - 8, cx, cx + 8}, new int[]{cy - 4, cy + 4, cy - 4}, 3);

                g2.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 70));
        
        java.awt.event.MouseAdapter dragListener = new java.awt.event.MouseAdapter() {
            int pX, pY;
            int iconOffX, iconOffY;
            int resizeMask = 0;
            int startW, startH, startWX, startWY;

            private int getEdgeMask(java.awt.event.MouseEvent e) {
                int mask = 0;
                if (e.getX() < EDGE) mask |= 1;
                if (e.getX() > getWidth() - EDGE) mask |= 2;
                if (e.getY() < EDGE) mask |= 4;
                if (e.getY() > getHeight() - EDGE) mask |= 8;
                return mask;
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                resizeMask = 0;
                if (e.getSource() == headerPanel) {
                    int cx = headerPanel.getWidth() - 30;
                    if(e.getX() >= cx - 20 && e.getX() <= cx + 20) {
                        pX = 0; pY = 0; return;
                    }
                }
                // Check if near edge for resize
                int mask = getEdgeMask(e);
                Container wrapper = Panel_De_Chatbot.this.getParent();
                if (mask != 0 && wrapper != null) {
                    resizeMask = mask;
                    pX = e.getXOnScreen();
                    pY = e.getYOnScreen();
                    startW = wrapper.getWidth();
                    startH = wrapper.getHeight();
                    startWX = wrapper.getX();
                    startWY = wrapper.getY();
                    return;
                }
                // Otherwise drag
                resizeMask = 0;
                pX = e.getXOnScreen();
                pY = e.getYOnScreen();
                if (frameRef != null) {
                    javax.swing.JButton icon = frameRef.Get_Boton_Flotante_De_Chat();
                    if (wrapper != null && icon != null) {
                        iconOffX = icon.getX() - wrapper.getX();
                        iconOffY = icon.getY() - wrapper.getY();
                    }
                }
            }
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (pX == 0 && pY == 0) return;
                Container wrapper = Panel_De_Chatbot.this.getParent();
                if (wrapper == null) return;

                // Resize mode
                if (resizeMask != 0) {
                    int dx = e.getXOnScreen() - pX;
                    int dy = e.getYOnScreen() - pY;
                    int newX = startWX, newY = startWY;
                    int newW = startW, newH = startH;

                    if ((resizeMask & 2) != 0) newW = Math.max(320, startW + dx);
                    if ((resizeMask & 8) != 0) newH = Math.max(350, startH + dy);
                    if ((resizeMask & 1) != 0) {
                        newW = Math.max(320, startW - dx);
                        newX = startWX + startW - newW;
                    }
                    if ((resizeMask & 4) != 0) {
                        newH = Math.max(350, startH - dy);
                        newY = startWY + startH - newH;
                    }
                    wrapper.setBounds(newX, newY, newW, newH);
                    Panel_De_Chatbot.this.setBounds(0, 0, newW, newH);
                    wrapper.revalidate();
                    wrapper.repaint();
                    return;
                }

                // Drag mode
                int dX = e.getXOnScreen() - pX;
                int dY = e.getYOnScreen() - pY;
                wrapper.setLocation(wrapper.getX() + dX, wrapper.getY() + dY);
                pX = e.getXOnScreen();
                pY = e.getYOnScreen();
                if (frameRef != null) {
                    javax.swing.JButton icon = frameRef.Get_Boton_Flotante_De_Chat();
                    if (icon != null) {
                        icon.setLocation(wrapper.getX() + iconOffX, wrapper.getY() + iconOffY);
                    }
                }
            }
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int mask = getEdgeMask(e);
                int ct = Cursor.DEFAULT_CURSOR;
                switch (mask) {
                    case 1: ct = Cursor.W_RESIZE_CURSOR; break;
                    case 2: ct = Cursor.E_RESIZE_CURSOR; break;
                    case 4: ct = Cursor.N_RESIZE_CURSOR; break;
                    case 8: ct = Cursor.S_RESIZE_CURSOR; break;
                    case 5: ct = Cursor.NW_RESIZE_CURSOR; break;
                    case 6: ct = Cursor.NE_RESIZE_CURSOR; break;
                    case 9: ct = Cursor.SW_RESIZE_CURSOR; break;
                    case 10: ct = Cursor.SE_RESIZE_CURSOR; break;
                }
                Panel_De_Chatbot.this.setCursor(Cursor.getPredefinedCursor(ct));
            }
        };
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
        headerPanel.addMouseListener(dragListener);
        headerPanel.addMouseMotionListener(dragListener);

        // Propagate resize cursor to child components
        java.awt.event.MouseMotionAdapter edgeCursorAdapter = new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                java.awt.Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), Panel_De_Chatbot.this);
                int mask = 0;
                if (p.x < EDGE) mask |= 1;
                if (p.x > getWidth() - EDGE) mask |= 2;
                if (p.y < EDGE) mask |= 4;
                if (p.y > getHeight() - EDGE) mask |= 8;
                int ct = Cursor.DEFAULT_CURSOR;
                switch (mask) {
                    case 1: ct = Cursor.W_RESIZE_CURSOR; break;
                    case 2: ct = Cursor.E_RESIZE_CURSOR; break;
                    case 4: ct = Cursor.N_RESIZE_CURSOR; break;
                    case 8: ct = Cursor.S_RESIZE_CURSOR; break;
                    case 5: ct = Cursor.NW_RESIZE_CURSOR; break;
                    case 6: ct = Cursor.NE_RESIZE_CURSOR; break;
                    case 9: ct = Cursor.SW_RESIZE_CURSOR; break;
                    case 10: ct = Cursor.SE_RESIZE_CURSOR; break;
                }
                e.getComponent().setCursor(Cursor.getPredefinedCursor(ct));
            }
        };
        headerPanel.addMouseMotionListener(edgeCursorAdapter);
        
        headerPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int cx = headerPanel.getWidth() - 30;
                int cy = 70 / 2;
                boolean overArrow = e.getX() >= cx - 20 && e.getX() <= cx + 20
                        && e.getY() >= cy - 20 && e.getY() <= cy + 20;
                if (overArrow != arrowHovered) {
                    arrowHovered = overArrow;
                    headerPanel.setCursor(Cursor.getPredefinedCursor(overArrow ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    headerPanel.repaint();
                }
            }
        });
        
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int cx = headerPanel.getWidth() - 30;
                if(e.getX() >= cx - 20 && e.getX() <= cx + 20) {
                    if (frameRef != null) {
                        frameRef.Minimizar_Chatbot();
                    }
                }
            }
        });
        add(headerPanel, BorderLayout.NORTH);

        chatBody = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
                 int w = getWidth();
                 int h = getHeight();
 
                 // Fondo general del chat (marfil suave)
                 g2.setColor(Gestor_De_Temas.getBgColor());
                 g2.fillRect(0, 0, w, h);
 
                 // Borde inferior fino
                 g2.setColor(Gestor_De_Temas.getBorderColor());
                 g2.drawLine(0, h - 1, w, h - 1);
 
                 // Costuras laterales
                 Color acc = Gestor_De_Temas.getAccentColor();
                 g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 40));
                 float[] dash = {5.0f, 5.0f};
                 g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                 g2.drawLine(10, 5, 10, h - 5);
                 g2.drawLine(w - 10, 5, w - 10, h - 5);

                // ── MÚLTIPLES GLOBOS DE MENSAJES DINÁMICOS ──
                int currentY = 20;
                int maxBubbleW = w - 80; // Máximo ancho del globo

                for (ChatMsg msg : messages) {
                    g2.setFont(new Font("Georgia", Font.PLAIN, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    int lineHeight = fm.getHeight() + 4;
                    int padding = 15;

                    List<String> lines = wrapMessage(msg, fm, maxBubbleW - padding * 2);

                    // Calculate bubble size
                    int textMaxW = 0;
                    for (String line : lines) {
                        textMaxW = Math.max(textMaxW, fm.stringWidth(line));
                    }
                    int minBubbleW = msg.thinking ? 84 : 80;
                    int bW = Math.min(Math.max(textMaxW + padding * 2, minBubbleW), maxBubbleW);
                    int bH = lines.size() * lineHeight + padding * 2;

                    if (msg.isAssistant) {
                        int bX = 20;
                        g2.setColor(Gestor_De_Temas.getPanelBgColor());
                        g2.fillRoundRect(bX, currentY, bW, bH, 14, 14);
                        g2.setColor(Gestor_De_Temas.getBorderColor());
                        g2.drawRoundRect(bX, currentY, bW, bH, 14, 14);

                        g2.setColor(Gestor_De_Temas.getTextColor());
                        if (msg.thinking && msg.text.isEmpty()) {
                            paintThinkingDots(g2, bX + padding, currentY + bH / 2);
                        } else {
                            int textY = currentY + padding + fm.getAscent();
                            for (String line : lines) {
                                g2.drawString(line, bX + padding, textY);
                                textY += lineHeight;
                            }
                        }
                    } else {
                        int bX = w - bW - 20;
                        g2.setColor(Gestor_De_Temas.getHoverColor());
                        g2.fillRoundRect(bX, currentY, bW, bH, 14, 14);
                        g2.setColor(Gestor_De_Temas.getBorderColor());
                        g2.drawRoundRect(bX, currentY, bW, bH, 14, 14);

                        g2.setColor(Gestor_De_Temas.getTextColor());
                        int textY = currentY + padding + fm.getAscent();
                        for (String line : lines) {
                            g2.drawString(line, bX + padding, textY);
                            textY += lineHeight;
                        }
                    }
                    currentY += bH + 15;
                }

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                int currentY = 20;
                Font f = new Font("Georgia", Font.PLAIN, 16);
                int padding = 15;
                int w = getWidth() > 0 ? getWidth() : 350;
                int maxBubbleW = w - 80;
                BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int lineHeight = fm.getHeight() + 4;

                for (ChatMsg msg : messages) {
                    int lines = wrapMessage(msg, fm, maxBubbleW - padding * 2).size();
                    int bH = lines * lineHeight + padding * 2;
                    currentY += bH + 15;
                }
                g2.dispose();
                return new Dimension(0, Math.max(300, currentY + 20));
            }
        };
        chatScroll = new JScrollPane(chatBody);
        chatScroll.setOpaque(false);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        chatScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(chatScroll, BorderLayout.CENTER);
        
        // Agregar soporte de arrastre al cuerpo del chat
        chatBody.addMouseListener(dragListener);
        chatBody.addMouseMotionListener(dragListener);
        chatBody.addMouseMotionListener(edgeCursorAdapter);
        chatScroll.addMouseListener(dragListener);
        chatScroll.addMouseMotionListener(dragListener);
        
        messages.add(new ChatMsg(true, "¡Hola! Soy tu asistente.", ""));

        JPanel inputPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(Gestor_De_Temas.getBgColor());
                g2.fillRoundRect(0, -16, w, h + 16, 24, 24);

                // Pintar la caja de entrada tipo píldora blanca pegada a la izquierda
                int px = 15;
                int py = 10;
                int pw = w - 30;
                int ph = h - 20;

                g2.setColor(Gestor_De_Temas.getPanelBgColor());
                g2.fillRoundRect(px, py, pw, ph, ph, ph);
                g2.setColor(Gestor_De_Temas.getBorderColor());
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(px, py, pw, ph, ph, ph);

                // Costura interna de la píldora
                g2.setColor(new Color(138, 82, 57, 50));
                float[] dash = {3.0f, 3.0f};
                g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawRoundRect(px + 3, py + 3, pw - 6, ph - 6, ph - 6, ph - 6);

                // Botón Clip (adjuntar imagen)
                int clipSize = 26;
                int clipX = px + 10;
                int clipY = py + (ph - clipSize) / 2;
                g2.setColor(clipHovered ? Gestor_De_Temas.getAccentColor() : Gestor_De_Temas.getMutedColor());
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(clipX + 4, clipY + 2, 12, 18, -45, 270);
                g2.drawLine(clipX + 10, clipY + 11, clipX + 10, clipY + 22);
                if (pendingImage != null) {
                    g2.setColor(new Color(79, 133, 87));
                    g2.fillOval(clipX + 16, clipY, 10, 10);
                }

                // Botón Voz Live
                int voiceSize = 26;
                int voiceX = px + 42;
                int voiceY = py + (ph - voiceSize) / 2;
                g2.setColor(liveVoiceActive ? new Color(79, 133, 87) :
                        (voiceHovered ? Gestor_De_Temas.getAccentColor() : Gestor_De_Temas.getMutedColor()));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(voiceX + 8, voiceY + 3, 10, 15, 8, 8);
                g2.drawLine(voiceX + 13, voiceY + 18, voiceX + 13, voiceY + 23);
                g2.drawLine(voiceX + 8, voiceY + 23, voiceX + 18, voiceY + 23);
                g2.drawArc(voiceX + 4, voiceY + 9, 18, 12, 190, 160);
                if (liveVoiceActive) {
                    g2.fillOval(voiceX + 19, voiceY + 2, 7, 7);
                }

                // Botón Enviar circular café
                int btnSize = 30;
                int btnX = px + pw - btnSize - 6;
                int btnY = py + (ph - btnSize) / 2;

                g2.setColor(sendHovered ? Gestor_De_Temas.getAccentColor().brighter() : Gestor_De_Temas.getAccentColor());
                g2.fillOval(btnX, btnY, btnSize, btnSize);

                // Flecha de enviar dibujada con polígono
                g2.setColor(Color.WHITE);
                int[] xPoints = {btnX + 10, btnX + 10, btnX + 20};
                int[] yPoints = {btnY + 8, btnY + 22, btnY + 15};
                g2.fillPolygon(xPoints, yPoints, 3);
                g2.dispose();
            }
        };
        inputPanel.setPreferredSize(new Dimension(0, 56));
        inputPanel.setLayout(null);
        
        txtInput = new JTextField();
        txtInput.setOpaque(false);
        txtInput.setBorder(null);
        txtInput.setFont(new Font("Georgia", Font.PLAIN, 16));
        txtInput.setForeground(Gestor_De_Temas.getTextColor());
        
        inputPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int px = 15;
                int py = 10;
                int pw = inputPanel.getWidth() - 30;
                int ph = inputPanel.getHeight() - 20;
                txtInput.setBounds(px + 74, py, pw - 114, ph);
            }
        });
        
        // Clip button click handler
        inputPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int pw = inputPanel.getWidth() - 30;
                // Clip button area
                if (e.getX() >= 15 && e.getX() <= 55) {
                    javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
                    fc.setDialogTitle("Adjuntar imagen del producto");
                    fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png"));
                    if (fc.showOpenDialog(Panel_De_Chatbot.this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        try {
                            java.io.File file = fc.getSelectedFile();
                            pendingImage = java.nio.file.Files.readAllBytes(file.toPath());
                            pendingImageName = file.getName();
                            pendingImageMimeType = detectImageMimeType(pendingImageName);
                            inputPanel.repaint();
                        } catch (Exception ex) {
                            pendingImage = null;
                            pendingImageName = null;
                            pendingImageMimeType = "image/jpeg";
                        }
                    }
                }
                // Voice Live button area
                if (e.getX() >= 55 && e.getX() <= 90) {
                    toggleLiveVoice();
                    inputPanel.repaint();
                    return;
                }
                // Send button area
                int btnX = 15 + pw - 30 - 6;
                if(e.getX() >= btnX && e.getX() <= btnX + 30) {
                    sendMessage();
                }
            }
        });
        inputPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int pw = inputPanel.getWidth() - 30;
                boolean overClip = e.getX() >= 15 && e.getX() <= 55;
                boolean overVoice = e.getX() >= 55 && e.getX() <= 90;
                int btnX = 15 + pw - 30 - 6;
                boolean overSend = e.getX() >= btnX && e.getX() <= btnX + 30;
                if (overClip != clipHovered || overVoice != voiceHovered || overSend != sendHovered) {
                    clipHovered = overClip;
                    voiceHovered = overVoice;
                    sendHovered = overSend;
                    inputPanel.setCursor(Cursor.getPredefinedCursor((overClip || overVoice || overSend) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    inputPanel.repaint();
                }
            }
        });
        
        txtInput.addActionListener(e -> sendMessage());
        
        inputPanel.add(txtInput);
        inputPanel.addMouseMotionListener(edgeCursorAdapter);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void toggleLiveVoice() {
        if (liveVoiceActive) {
            if (liveVoiceService != null) {
                liveVoiceService.stop();
            }
            liveVoiceActive = false;
            setAssistantStatus("Listo", new Color(225, 245, 225));
            addAssistantMessage("Modo voz detenido.");
            return;
        }

        if (!com.mycompany.senati_zapato.utilidades.Configuracion.Hay_Gemini_Api_Key()) {
            setAssistantStatus("Error de configuración", new Color(255, 220, 220));
            addAssistantMessage("No se puede iniciar la voz: falta GEMINI_API_KEY. "
                    + "Configúrala en el módulo Configuración y vuelve a intentarlo.");
            return;
        }

        liveVoiceService = new Servicio_De_Voz_En_Vivo(
                frameRef,
                status -> SwingUtilities.invokeLater(() -> {
                    if (status != null && status.startsWith("No se pudo iniciar voz:")) {
                        liveVoiceActive = false;
                        setAssistantStatus("Error de conexión", new Color(255, 220, 220));
                    } else if (status != null && status.contains("detenido")) {
                        setAssistantStatus("Listo", new Color(225, 245, 225));
                    } else if (status != null && status.contains("activo")) {
                        setAssistantStatus("Escuchando...", new Color(225, 245, 225));
                    } else {
                        setAssistantStatus("Conectando...", new Color(255, 240, 200));
                    }
                    addAssistantMessage(status);
                    repaint();
                }),
                text -> SwingUtilities.invokeLater(() -> addUserMessage("[voz] " + text)),
                text -> SwingUtilities.invokeLater(() -> addAssistantMessage(cleanAssistantText(text)))
        );
        liveVoiceActive = true;
        setAssistantStatus("Conectando...", new Color(255, 240, 200));
        addAssistantMessage("Modo voz activo. Puedes hablarme.");
        liveVoiceService.start();
    }

    private void setAssistantStatus(String status, Color color) {
        assistantStatus = status;
        assistantStatusColor = color;
        repaint();
    }

    private void addUserMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;
        messages.add(new ChatMsg(false, text.trim(), ""));
        chatBody.revalidate();
        chatBody.repaint();
        scrollToBottom();
    }

    private void addAssistantMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;
        messages.add(new ChatMsg(true, text.trim(), ""));
        chatBody.revalidate();
        chatBody.repaint();
        scrollToBottom();
    }

    private List<String> wrapMessage(ChatMsg msg, FontMetrics fm, int maxTextWidth) {
        List<String> lines = new ArrayList<>();
        if (msg.thinking && msg.text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String text = msg.text == null ? "" : msg.text;
        maxTextWidth = Math.max(40, maxTextWidth);
        for (String paragraph : text.split("\\R", -1)) {
            String[] words = paragraph.trim().isEmpty() ? new String[]{""} : paragraph.trim().split("\\s+");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String test = currentLine.length() == 0 ? word : currentLine + " " + word;
                if (fm.stringWidth(test) > maxTextWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    if (currentLine.length() > 0) currentLine.append(" ");
                    currentLine.append(word);
                }
            }
            lines.add(currentLine.toString());
        }
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private void paintThinkingDots(Graphics2D g2, int x, int centerY) {
        Color acc = Gestor_De_Temas.getAccentColor();
        for (int i = 0; i < 3; i++) {
            int phase = (thinkingFrame + i * 4) % 12;
            int offsetY = phase < 6 ? -phase / 2 : -(12 - phase) / 2;
            int alpha = 110 + (phase < 6 ? phase : 12 - phase) * 18;
            g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), Math.min(230, alpha)));
            g2.fillOval(x + i * 16, centerY - 4 + offsetY, 8, 8);
        }
    }
    
    private void scrollToBottom() {
        if (chatScroll != null) {
            SwingUtilities.invokeLater(() -> {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = chatScroll.getVerticalScrollBar();
                    int target = Math.max(0, vertical.getMaximum() - vertical.getVisibleAmount());
                    vertical.setValue(target);
                });
            });
        }
    }

    private boolean isNearBottom() {
        if (chatScroll == null) return true;
        JScrollBar vertical = chatScroll.getVerticalScrollBar();
        int distance = vertical.getMaximum() - vertical.getVisibleAmount() - vertical.getValue();
        return distance < 40;
    }

    private void keepBottomIfNeeded(boolean shouldStick) {
        if (shouldStick) {
            scrollToBottom();
        }
    }

    private void sendMessage() {
        if (awaitingResponse) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String txt = txtInput.getText().trim();
        boolean contextualScreenshot = shouldSendScreenContext(txt);
        String targetModule = detectNavigationTarget(txt);
        String cartProduct = extractCartProduct(txt);
        byte[] screenContext = null;
        String screenContextMimeType = "image/png";
        if (contextualScreenshot && pendingImage == null) {
            screenContext = captureCurrentScreen();
        }

        if(txt.isEmpty() && pendingImage == null && screenContext == null) return;

        String displayMsg = txt.isEmpty() ? "[Imagen adjunta]" : txt;
        if (pendingImage != null && !txt.isEmpty()) {
            displayMsg = txt + " 📎 " + pendingImageName;
        } else if (pendingImage != null) {
            displayMsg = "📎 " + pendingImageName;
        }

        messages.add(new ChatMsg(false, displayMsg, ""));
        txtInput.setText("");

        chatBody.revalidate();
        chatBody.repaint();
        scrollToBottom();

        if (cartProduct != null && frameRef != null) {
            String result = frameRef.Agregar_Producto_Al_Carrito(cartProduct, extractQuantity(txt));
            // Si el producto fue encontrado y agregado exitosamente (el mensaje de error nativo no se gatilló)
            if (result != null && !result.startsWith("No encontré un producto parecido a:")) {
                messages.add(new ChatMsg(true, result, ""));
                chatBody.revalidate();
                chatBody.repaint();
                scrollToBottom();
                pendingImage = null;
                pendingImageName = null;
                pendingImageMimeType = "image/jpeg";
                return;
            }
            // Si no se encontró mediante regex, dejamos que continúe el flujo hacia el LLM (Gemini) para que analice y busque
        }

        // Peticion explicita de categoria de zapatos: filtra el catalogo en Ventas de una vez,
        // sin esperar al modelo y sin que la deteccion de navegacion lo mande a otro modulo
        // ("muestrame zapatos mocasines" antes caia en el modulo Gestor por contener "zapato").
        String categoriaSolicitada = detectarCategoriaDeZapatos(txt);
        if (categoriaSolicitada != null && frameRef != null) {
            String resultado = frameRef.Mostrar_En_Ventas(categoriaSolicitada);
            messages.add(new ChatMsg(true, resultado, ""));
            chatBody.revalidate();
            chatBody.repaint();
            scrollToBottom();
            pendingImage = null;
            pendingImageName = null;
            pendingImageMimeType = "image/jpeg";
            return;
        }

        if (targetModule != null && frameRef != null) {
            SwingUtilities.invokeLater(() -> frameRef.Cambiar_Pestana(targetModule));
            messages.add(new ChatMsg(true, "Te he llevado al módulo de " + moduleDisplayName(targetModule) + ".", ""));
            chatBody.revalidate();
            chatBody.repaint();
            scrollToBottom();
            pendingImage = null;
            pendingImageName = null;
            pendingImageMimeType = "image/jpeg";
            return;
        }

        // Placeholder mientras responde
        ChatMsg placeholder = new ChatMsg(true, "", "");
        placeholder.thinking = true;
        messages.add(placeholder);
        int placeholderIdx = messages.size() - 1;
        chatBody.revalidate();
        chatBody.repaint();
        scrollToBottom();
        startThinkingAnimation();
        setAssistantStatus("Procesando...", new Color(255, 240, 200));
        awaitingResponse = true;

        String userMsg = txt.isEmpty() ? "Describe esta imagen de un producto de zapatería" : txt;
        byte[] imgData = pendingImage != null ? pendingImage : screenContext;
        String imageMimeType = pendingImage != null ? pendingImageMimeType : screenContextMimeType;
        if (screenContext != null && pendingImage == null) {
            userMsg = txt + "\n\nLa imagen adjunta es una captura temporal de la pantalla actual del sistema. Úsala solo como contexto visual para responder.";
        }
        pendingImage = null;
        pendingImageName = null;
        pendingImageMimeType = "image/jpeg";

        // Watchdog de INACTIVIDAD, no de duracion total. Las consultas de inventario/color
        // activan Function Calling, que hace 2+ viajes secuenciales a la API (elegir
        // herramienta -> ejecutar DAO -> redactar respuesta) con un intervalo silencioso
        // entre medio. Un limite total de 15s abortaba consultas validas y mostraba un
        // falso "servidor caido". Aqui solo se corta si el modelo lleva 60s sin emitir nada.
        final boolean[] abortar = {false};
        final int[] ultimaActividad = {(int) (System.currentTimeMillis() / 1000L)};
        final int TIEMPO_MAX_SIN_ACTIVIDAD = 60; // segundos

        // Se usa un array porque la lambda del Timer se referencia a si misma durante su
        // propia inicializacion (comun patron en Swing para timers autoreferentes).
        final Timer[] timerRef = new Timer[1];
        timerRef[0] = new Timer(1000, e -> {
            if (!awaitingResponse) {
                timerRef[0].stop();
                return;
            }
            int inactivo = (int) (System.currentTimeMillis() / 1000L) - ultimaActividad[0];
            if (inactivo >= TIEMPO_MAX_SIN_ACTIVIDAD) {
                timerRef[0].stop();
                abortar[0] = true;
                SwingUtilities.invokeLater(() -> {
                    ChatMsg msg = messages.get(placeholderIdx);
                    msg.thinking = false;
                    msg.text = "La IA tard\u00f3 demasiado en responder y cancel\u00e9 la consulta. "
                        + "Puede que est\u00e9 pensando o que el servidor est\u00e9 lento. Int\u00e9ntalo de nuevo.";
                    stopThinkingAnimationIfIdle();
                    awaitingResponse = false;
                    setAssistantStatus("Error de respuesta", new Color(255, 220, 220));
                    chatBody.revalidate();
                    chatBody.repaint();
                    scrollToBottom();
                });
            }
        });
        timerRef[0].setRepeats(true);
        timerRef[0].start();

        geminiService.Enviar_Mensaje_Asincrono(userMsg, imgData, imageMimeType,
            chunk -> {
                // Cada fragmento reinicia el reloj: mide silencio, no duracion.
                ultimaActividad[0] = (int) (System.currentTimeMillis() / 1000L);
                if (abortar[0]) return; // la respuesta llego tras cancelar: se descarta
                ChatMsg msg = messages.get(placeholderIdx);
                msg.thinking = false;
                msg.text = cleanAssistantText(msg.text + chunk);
                SwingUtilities.invokeLater(() -> {
                    boolean shouldStick = isNearBottom();
                    chatBody.revalidate();
                    chatBody.repaint();
                    keepBottomIfNeeded(shouldStick);
                });
            },
            () -> {
                if (timerRef[0].isRunning()) {
                    timerRef[0].stop();
                }
                if (abortar[0]) return; // ya se mostro el aviso de cancelacion
                SwingUtilities.invokeLater(() -> {
                    ChatMsg msg = messages.get(placeholderIdx);
                    msg.thinking = false;
                    msg.text = cleanAssistantText(msg.text);
                    stopThinkingAnimationIfIdle();
                    awaitingResponse = false;
                    setAssistantStatus(liveVoiceActive ? "Escuchando..." : "Listo",
                            new Color(225, 245, 225));
                    boolean shouldStick = isNearBottom();
                    chatBody.revalidate();
                    chatBody.repaint();
                    keepBottomIfNeeded(shouldStick);
                });
            }
        );
    }

    private String detectImageMimeType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "image/jpeg";
    }

    private String normalizeUserText(String text) {
        if (text == null) return "";
        return java.text.Normalizer.normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /**
     * Detecta si el usuario pide ver una categoria de zapatos (mocasines, botas,
     * casuales, vestir o todo el catalogo). Devuelve el termino que debe aplicar
     * el filtro de Ventas, o null si no es una peticion de categoria.
     */
    private String detectarCategoriaDeZapatos(String txt) {
        if (txt == null || txt.trim().isEmpty()) return null;
        String t = normalizeUserText(txt);
        // Tolerancia a typos con letras repetidas: "cassuales" -> "casuales", "mocassines" -> "mocasines".
        String plano = t.replaceAll("(.)\\1+", "$1");
        // Órdenes de carrito o de compra: las deja pasar al flujo normal (Gemini / agregar_carrito).
        if (plano.matches(".*\\b(agrega|agregar|agrego|anade|anadir|anado|pon|poner|mete|meter|quita|quitar|elimina|eliminar|compra|comprar)\\b.*")) {
            return null;
        }
        boolean mencionaCalzado = plano.matches(".*\\b(zapato|zapatos|zapatilla|zapatillas|calzado|catalogo|categoria)\\b.*");
        if (plano.contains("mocasin")) return "mocasines";
        if (plano.matches(".*\\bbot(as|os)\\b.*")) return "botas";
        if (plano.contains("casual")) return "zapatos casuales";
        if (plano.contains("vestir") || plano.contains("formal") || plano.contains("elegante")) return "zapatos de vestir";
        if (mencionaCalzado && plano.matches(".*\\btodos?\\b.*")) return "ver todo el catalogo";
        return null;
    }

    private String detectNavigationTarget(String text) {
        String t = normalizeUserText(text);
        boolean asksNavigation = t.matches(".*\\b(donde|modulo|ir|lleva|llevar|redirige|redirigir|gestionar|abrir|abre|entra|entrar|vamos|vamonos|voy|anda|dirige|dirigeme|hacer|registrar|ahora a|ve a|cambia a|pasa a|muestrame)\\b.*")
                || t.startsWith("a ") || t.startsWith("al ");
        if (!asksNavigation) return null;

        if (t.matches(".*\\b(inventario|gestor|producto|productos|stock|zapato)\\b.*")) {
            return "Gestor";
        }
        if (t.matches(".*\\b(venta|vender|cobrar|facturar|boleta|comprobante)\\b.*")) {
            return "Ventas";
        }
        if (t.matches(".*\\b(reporte|reportes|historial|estadistica|estadisticas|kpi)\\b.*")) {
            return "Reportes";
        }
        if (t.matches(".*\\b(inicio|principal|home)\\b.*")) {
            return "Inicio";
        }
        return null;
    }

    private boolean esReferenciaAnaforica(String productText) {
        if (productText == null) return false;
        String t = normalizeUserText(productText).trim();
        if (t.isEmpty()) return true;
        
        // Si el texto resultante consiste puramente de confirmaciones o pronombres
        String[] words = t.split("\\s+");
        for (String w : words) {
            if (w.equals("si") || w.equals("ok") || w.equals("anadelo") || w.equals("agregalo")
                    || w.equals("anadela") || w.equals("agregala") || w.equals("lo") || w.equals("la")
                    || w.equals("ese") || w.equals("este") || w.equals("esta") || w.equals("eso")
                    || w.equals("anade") || w.equals("agrega") || w.equals("por") || w.equals("favor")
                    || w.equals("el") || w.equals("un") || w.equals("una")) {
                continue;
            }
            return false;
        }
        return true;
    }

    private String obtenerUltimoProductoDiscutido() {
        try {
            List<Producto> productos = new Dao_De_Producto().Obtener_Todos();
            // Recorrer los mensajes de chat en sentido inverso (del más reciente al más antiguo)
            for (int i = messages.size() - 1; i >= 0; i--) {
                String text = messages.get(i).text;
                if (text == null || text.trim().isEmpty()) continue;
                
                String normalizedText = normalizeUserText(text);
                
                // Buscar si el texto del mensaje menciona algún producto del catálogo
                Producto mejorMatch = null;
                int maxLen = 0;
                for (Producto p : productos) {
                    String normName = normalizeUserText(p.Get_Nombre());
                    if (normName.length() > 5 && normalizedText.contains(normName)) {
                        if (normName.length() > maxLen) {
                            maxLen = normName.length();
                            mejorMatch = p;
                        }
                    }
                }
                if (mejorMatch != null) {
                    return mejorMatch.Get_Nombre();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractCartProduct(String text) {
        String t = normalizeUserText(text);
        
        boolean addIntent = t.matches(".*\\b(agrega|agregar|anade|añade|pon|mete|coloca|incluye|agregalo|anadelo|agregala|anadela)\\b.*");
                
        boolean cartIntent = t.matches(".*\\b(carrito|lista de compras|venta|lo|la|ese|este|anadelo|agregalo|anadela|agregala|si|ok|listo)\\b.*");
                
        if (!addIntent && !cartIntent) return null;
        
        // Si tiene intención de agregar pero no especifica "carrito", ver si es una referencia implícita ("agrega el mocasín", "añádelo")
        if (addIntent && !cartIntent) {
            boolean mentionsProduct = false;
            try {
                List<Producto> productos = new Dao_De_Producto().Obtener_Todos();
                for (Producto p : productos) {
                    if (t.contains(normalizeUserText(p.Get_Nombre()))) {
                        mentionsProduct = true;
                        break;
                    }
                }
            } catch (Exception ignored) {}
            
            if (!mentionsProduct && t.replaceAll("\\b(agrega|agregar|anade|añade|pon|mete|coloca|incluye)\\b", "").trim().length() > 10) {
                return null;
            }
        }

        String product = t.replaceAll("\\b(agrega|agregar|anade|añade|pon|mete|coloca|incluye|agregalo|anadelo|agregala|anadela)\\b", " ")
                .replaceAll("\\b(al|a la|a el|el|la|los|las|un|una|carrito|lista de compras|venta|por favor|si|ok|de)\\b", " ")
                .replaceAll("\\b\\d+\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
                
        if (product.isEmpty() || esReferenciaAnaforica(product)) {
            String ultimo = obtenerUltimoProductoDiscutido();
            if (ultimo != null) {
                return ultimo;
            }
        }
        
        return product.isEmpty() ? null : product;
    }

    private int extractQuantity(String text) {
        String t = normalizeUserText(text);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d+)\\b").matcher(t);
        if (matcher.find()) {
            try { return Math.max(1, Integer.parseInt(matcher.group(1))); } catch (NumberFormatException e) { return 1; }
        }
        if (t.matches(".*\\bdos\\b.*")) return 2;
        if (t.matches(".*\\btres\\b.*")) return 3;
        if (t.matches(".*\\bcuatro\\b.*")) return 4;
        if (t.matches(".*\\bcinco\\b.*")) return 5;
        return 1;
    }

    private String moduleDisplayName(String cardName) {
        if ("Gestor".equals(cardName)) return "Inventario";
        return cardName;
    }

    private String cleanAssistantText(String text) {
        if (text == null) return "";
        String cleaned = text.replace("**", "")
                .replace("__", "")
                .replace("`", "")
                .replaceAll("(?m)^\\s*[-*]\\s+", "")
                .replaceAll("\\s+\n", "\n")
                .replaceAll("\n{3,}", "\n\n");
        return cleaned.trim();
    }

    private void startThinkingAnimation() {
        if (thinkingTimer != null && thinkingTimer.isRunning()) return;
        thinkingTimer = new Timer(120, e -> {
            thinkingFrame++;
            chatBody.repaint();
        });
        thinkingTimer.start();
    }

    private void stopThinkingAnimationIfIdle() {
        for (ChatMsg msg : messages) {
            if (msg.thinking) return;
        }
        if (thinkingTimer != null) {
            thinkingTimer.stop();
        }
    }

    private boolean shouldSendScreenContext(String text) {
        String t = normalizeUserText(text);
        return t.contains("que es esto")
                || t.contains("q es esto")
                || t.contains("donde estoy")
                || t.contains("en donde estoy")
                || t.contains("que pantalla")
                || t.contains("que estoy viendo")
                || t.contains("que ves")
                || t.contains("que hay aqui")
                || t.contains("esta pantalla")
                || t.contains("en la pantalla")
                || t.contains("la pantalla")
                || t.contains("en pantalla");
    }

    private byte[] captureCurrentScreen() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) return null;
        try {
            Rectangle bounds = window.getBounds();
            BufferedImage image = new Robot().createScreenCapture(bounds);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // Dibujar marco simulando costura de cuero sobre todo el panel
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color acc = Gestor_De_Temas.getAccentColor();
        g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 120));
        float[] dash = {6.0f, 4.0f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
        g2.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);
    }
}
