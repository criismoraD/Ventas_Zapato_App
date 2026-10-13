package com.mycompany.senati_zapato.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Botón de pestaña personalizado con indicador de selección.
 */
public class Boton_De_Pestana extends JButton {

    private boolean active = false;
    private boolean hovered = false;

    private Color getActiveColor() {
        return Gestor_De_Temas.getTextColor();
    }

    private Color getHoverColor() {
        return Gestor_De_Temas.getAccentColor();
    }

    private Color getDefaultColor() {
        return Gestor_De_Temas.getMutedColor();
    }

    private Color getIndicatorColor() {
        return Gestor_De_Temas.getAccentColor();
    }

    public Boton_De_Pestana(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(new Font("Inter", Font.BOLD, 17));
        setForeground(getDefaultColor());

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                if (!active) {
                    setForeground(getHoverColor());
                }
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered = false;
                if (!active) {
                    setForeground(getDefaultColor());
                }
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? getActiveColor() : getDefaultColor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        setForeground(active ? getActiveColor() : (hovered ? getHoverColor() : getDefaultColor()));

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo sutil al hacer hover o estar activo
        if (active || hovered) {
            g2.setColor(new Color(
                getIndicatorColor().getRed(),
                getIndicatorColor().getGreen(),
                getIndicatorColor().getBlue(),
                active ? 40 : 25
            ));
            g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 14, 14);
        }

        g2.dispose();
        super.paintComponent(g);

        // Indicador inferior cuando está activo
        if (active) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setColor(getIndicatorColor());

            int lineW = getWidth() - 32;
            int lineH = 3;
            int lineX = 16;
            int lineY = getHeight() - lineH - 2;
            g3.fillRoundRect(lineX, lineY, lineW, lineH, 2, 2);
            g3.dispose();
        }
    }
}
