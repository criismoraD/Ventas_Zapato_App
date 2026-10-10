package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class Panel_De_Inicio extends javax.swing.JPanel {

    private final Panel_Principal frame;

    public Panel_De_Inicio() {
        this.frame = null;
        initComponents();
        initCustomUI();
    }

    public Panel_De_Inicio(Panel_Principal frame) {
        this.frame = frame;
        initComponents();
        initCustomUI();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardsContainer = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        cardsContainer.setLayout(new java.awt.GridBagLayout());
        add(cardsContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void initCustomUI() {
        setOpaque(false);
        cardsContainer.setOpaque(false);

        // ======================= PANEL KPI (Resumen Rápido) =======================
        com.mycompany.senati_zapato.datos.Dao_De_Venta ventaDAO = new com.mycompany.senati_zapato.datos.Dao_De_Venta();
        com.mycompany.senati_zapato.datos.Dao_De_Producto productoDAO = new com.mycompany.senati_zapato.datos.Dao_De_Producto();

        double[] kpis = ventaDAO.obtenerKpisDelDia();
        int totalProductos = productoDAO.Obtener_Todos().size();
        int stockBajo = (int) productoDAO.Obtener_Todos().stream().filter(p -> p.Get_Stock() <= 5 && p.Get_Stock() > 0).count();
        int agotados = (int) productoDAO.Obtener_Todos().stream().filter(p -> p.Get_Stock() == 0).count();

        JPanel kpiPanel = new JPanel();
        kpiPanel.setLayout(new BoxLayout(kpiPanel, BoxLayout.X_AXIS));
        kpiPanel.setOpaque(false);
        kpiPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        kpiPanel.add(crearKpiMini("Ingresos Hoy", String.format("S/ %.2f", kpis[0]), new Color(79, 133, 87), Icono_Elegante.Type.CART));
        kpiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        kpiPanel.add(crearKpiMini("En Stock", String.valueOf(totalProductos), Gestor_De_Temas.getAccentColor(), Icono_Elegante.Type.BOX));
        kpiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        kpiPanel.add(crearKpiMini("Stock Bajo", String.valueOf(stockBajo), new Color(240, 173, 78), Icono_Elegante.Type.CHART));
        kpiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        kpiPanel.add(crearKpiMini("Agotados", String.valueOf(agotados), new Color(220, 53, 69), Icono_Elegante.Type.TRASH));

        // ======================= MENSAJE DE BIENVENIDA =======================
        JPanel welcomePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Gestor_De_Temas.getPanelBgColor());
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.setColor(Gestor_De_Temas.getBorderColor());
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                // Línea acento superior
                g2.setColor(Gestor_De_Temas.getAccentColor());
                g2.fillRoundRect(0, 0, w, 5, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        welcomePanel.setOpaque(false);
        welcomePanel.setLayout(new java.awt.BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        welcomePanel.setPreferredSize(new Dimension(0, 100));

        JLabel lblWelcome = new JLabel("Bienvenido a Sello Masculino");
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 28));
        lblWelcome.setForeground(Gestor_De_Temas.getTextColor());

        JLabel lblSub = new JLabel("Gestiona ventas, inventario y reportes de tu zapatería de caballero.");
        lblSub.setFont(new Font("Inter", Font.PLAIN, 16));
        lblSub.setForeground(Gestor_De_Temas.getMutedColor());

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(lblWelcome);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblSub);
        welcomePanel.add(textPanel, java.awt.BorderLayout.WEST);

        add(welcomePanel, java.awt.BorderLayout.NORTH);
        add(kpiPanel, java.awt.BorderLayout.SOUTH);

        // ======================= TARJETAS DE ACCIÓN =======================
        ActionCard cardVentas = new ActionCard("Modulo de Ventas", "Crear boletas, procesar pagos y consultar transacciones del dia.", "/images/img_ventas.png", Icono_Elegante.Type.CART);
        cardVentas.addActionListener(e -> {
            if (frame != null) {
                frame.Cambiar_Pestana("Ventas");
            }
        });

        ActionCard cardGestor = new ActionCard("Administrar Inventario", "Administrar calzados, catalogo, marcas, tallas y stock en inventario.", "/images/img_gestor.png", Icono_Elegante.Type.BOX);
        cardGestor.addActionListener(e -> {
            if (frame != null) {
                frame.Cambiar_Pestana("Gestor");
            }
        });

        ActionCard cardReportes = new ActionCard("Historial y Reportes", "Estadisticas detalladas, balance economico e informes de ventas.", "/images/img_reportes.png", Icono_Elegante.Type.CHART);
        cardReportes.addActionListener(e -> {
            if (frame != null) {
                frame.Cambiar_Pestana("Reportes");
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 0;

        gbc.gridx = 0;
        cardsContainer.add(cardVentas, gbc);

        gbc.gridx = 1;
        cardsContainer.add(cardGestor, gbc);

        gbc.gridx = 2;
        cardsContainer.add(cardReportes, gbc);
    }

    private JPanel crearKpiMini(String titulo, String valor, Color accent, Icono_Elegante.Type iconType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Gestor_De_Temas.getPanelBgColor());
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(Gestor_De_Temas.getBorderColor());
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, h, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new java.awt.BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        card.setPreferredSize(new Dimension(200, 80));

        JLabel iconLabel = new JLabel(new Icono_Elegante(iconType, 28, accent));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        card.add(iconLabel, java.awt.BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Inter", Font.PLAIN, 13));
        lblTitulo.setForeground(Gestor_De_Temas.getMutedColor());

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Inter", Font.BOLD, 22));
        lblValor.setForeground(Gestor_De_Temas.getTextColor());

        textPanel.add(lblTitulo);
        textPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        textPanel.add(lblValor);
        card.add(textPanel, java.awt.BorderLayout.CENTER);

        return card;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cardsContainer;
    // End of variables declaration//GEN-END:variables
}

class ActionCard extends JButton {

    private final String title;
    private final String subtitle;
    private final Icono_Elegante.Type badgeIconType;
    private Image cardImage;
    private boolean hovered = false;
    private float hoverProgress = 0f;
    private Timer hoverTimer;

    private Color getBgColor() {
        return Gestor_De_Temas.getPanelBgColor();
    }

    private Color getTextColor() {
        return Gestor_De_Temas.getTextColor();
    }

    private Color getSubtextColor() {
        return Gestor_De_Temas.getMutedColor();
    }

    private Color getBorderColor() {
        return Gestor_De_Temas.getBorderColor();
    }

    private Color getAccentColor() {
        return Gestor_De_Temas.getAccentColor();
    }

    public ActionCard(String title, String subtitle, String imagePath, Icono_Elegante.Type badgeIconType) {
        this.title = title;
        this.subtitle = subtitle;
        this.badgeIconType = badgeIconType;

        try {
            this.cardImage = new javax.swing.ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            this.cardImage = null;
        }

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(340, 480));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                animateHover(true);
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered = false;
                animateHover(false);
                repaint();
            }
        });
    }

    private void animateHover(boolean entering) {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        hoverTimer = new Timer(16, e -> {
            float target = entering ? 1f : 0f;
            float distance = target - hoverProgress;
            hoverProgress += distance * 0.28f;
            if (Math.abs(distance) < 0.01f) {
                hoverProgress = target;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // ── Hover Animation Scale ──
        double scale = 1.0 + (0.035 * hoverProgress);
        int scaledW = (int) (w * scale);
        int scaledH = (int) (h * scale);
        int offsetX = (w - scaledW) / 2;
        int offsetY = (h - scaledH) / 2 - (int) (5 * hoverProgress);

        g2.translate(offsetX, offsetY);
        w = scaledW;
        h = scaledH;

        // ── Sombra suave difuminada ──
        for (int i = 0; i < 10; i++) {
            int alpha = (int) ((25 + 22 * hoverProgress) * (1f - (float) i / 10));
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRoundRect(2 + i, 4 + i + (int) (3 * (1f - hoverProgress)),
                    w - 4 - i * 2, h - 6 - i * 2, 22, 22);
        }

        // ── Fondo de tarjeta crema ──
        g2.setColor(getBgColor());
        g2.fillRoundRect(2, 2, w - 4, h - 6, 20, 20);

        // ── Borde exterior fino ──
        g2.setColor(getBorderColor());
        g2.setStroke(new java.awt.BasicStroke(1.0f));
        g2.drawRoundRect(2, 2, w - 5, h - 7, 20, 20);

        // ── Costura interna discontinua (efecto costura cuero) ──
        Color acc = getAccentColor();
        g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 180));
        float[] dash = {6.0f, 4.0f};
        g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawRoundRect(8, 8, w - 17, h - 19, 16, 16);

        // ── Dibujar Imagen Superior con Recorte de Esquinas Redondeadas ──
        int imgH = 260;
        java.awt.geom.Area clipArea = new java.awt.geom.Area(new java.awt.geom.RoundRectangle2D.Float(2, 2, w - 4, h - 6, 20, 20));
        java.awt.geom.Area topRect = new java.awt.geom.Area(new java.awt.Rectangle(0, 0, w, imgH));
        clipArea.intersect(topRect);

        g2.setClip(clipArea);
        if (cardImage != null) {
            // Escalar imagen proporcionalmente y centrar
            int imgW = cardImage.getWidth(null);
            int originalImgH = cardImage.getHeight(null);
            double imgScale = Math.max((double) (w - 4) / imgW, (double) (imgH - 2) / originalImgH);
            int drawW = (int) (imgW * imgScale);
            int drawH = (int) (originalImgH * imgScale);
            int drawX = 2 + ((w - 4) - drawW) / 2;
            int drawY = 2 + ((imgH - 2) - drawH) / 2;
            g2.drawImage(cardImage, drawX, drawY, drawW, drawH, null);
        } else {
            g2.setColor(getBorderColor());
            g2.fillRect(2, 2, w - 4, imgH - 2);
        }
        g2.setClip(null);

        // Línea de separación de la imagen
        g2.setColor(getBorderColor());
        g2.setStroke(new java.awt.BasicStroke(1.0f));
        g2.drawLine(2, imgH, w - 3, imgH);

        // ── Insignia circular superpuesta (Badge) ──
        int bSize = 56;
        int bx = (w - bSize) / 2;
        int by = imgH - (bSize / 2) - (int) (4 * hoverProgress);

        // Sombra de insignia
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(bx, by + 2, bSize, bSize);

        // Fondo terracota de insignia
        g2.setColor(hoverProgress > 0.5f
                ? Gestor_De_Temas.getHoverColor() : Gestor_De_Temas.getAccentColor());
        g2.fillOval(bx, by, bSize, bSize);

        // Borde de insignia
        g2.setColor(getBgColor());
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawOval(bx, by, bSize, bSize);

        // Icono vectorial centrado
        Icono_Elegante badgeIcon = new Icono_Elegante(badgeIconType, 24, new Color(253, 251, 247));
        int emojiX = bx + (bSize - badgeIcon.getIconWidth()) / 2;
        int emojiY = by + (bSize - badgeIcon.getIconHeight()) / 2;
        badgeIcon.paintIcon(this, g2, emojiX, emojiY);

        // ── Título (Georgia) ──
        g2.setColor(getTextColor());
        g2.setFont(new Font("Inter", Font.BOLD, 24));
        java.awt.FontMetrics fmTitle = g2.getFontMetrics();
        int titleX = (w - fmTitle.stringWidth(title)) / 2;
        int titleY = imgH + 50;
        g2.drawString(title, titleX, titleY);

        // ── Separador elegante — ⬥ — ──
        g2.setColor(getSubtextColor());
        g2.setFont(new Font("Inter", Font.PLAIN, 12));
        java.awt.FontMetrics fmSep = g2.getFontMetrics();
        String sepStr = "---  *  ---";
        int sepX = (w - fmSep.stringWidth(sepStr)) / 2;
        int sepY = titleY + 20;
        g2.drawString(sepStr, sepX, sepY);

        // ── Subtítulo / Descripción ──
        g2.setColor(getSubtextColor());
        g2.setFont(new Font("Inter", Font.PLAIN, 15));
        java.awt.FontMetrics fmSub = g2.getFontMetrics();

        String[] words = subtitle.split(" ");
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        int maxWidth = w - 60;
        for (String word : words) {
            String test = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fmSub.stringWidth(test) < maxWidth) {
                currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        int subY = sepY + 25;
        for (String line : lines) {
            int subX = (w - fmSub.stringWidth(line)) / 2;
            g2.drawString(line, subX, subY);
            subY += 20;
        }

        g2.dispose();
    }
}
