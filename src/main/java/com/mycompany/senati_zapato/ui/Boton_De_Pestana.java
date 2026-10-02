package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

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
        setFont(new Font("Georgia", Font.PLAIN, 20));
        setForeground(getDefaultColor());

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!active) {
                    setForeground(getHoverColor());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!active) {
                    setForeground(getDefaultColor());
                }
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
        // Actualizar el color de texto antes de pintar
        setForeground(active ? getActiveColor() : getDefaultColor());
        super.paintComponent(g);
        if (active) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getIndicatorColor());

            int lineW = getWidth() - 16;
            int lineH = 5;
            int lineX = 8;
            int lineY = getHeight() - lineH;
            g2.fillRoundRect(lineX, lineY, lineW, lineH, 2, 2);
            g2.dispose();
        }
    }
}
