package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Proveedor de iconos vectoriales de lujo dibujados por software para Swing.
 * Redimensionables sin pérdida de nitidez y dinámicos según el color de fondo/hover.
 */
public class Icono_Elegante implements Icon {
    public enum Type {
        HOME, CART, BOX, CHART, TRASH, SEARCH, CREDIT_CARD, CLOSE, ADD, SHOE, SUN, MOON, EYE, CASH, SAVE, PRINT, MINIMIZE
    }

    private final Type type;
    private final int width;
    private final int height;
    private Color customColor;

    public Icono_Elegante(Type type, int size) {
        this(type, size, null);
    }

    public Icono_Elegante(Type type, int size, Color customColor) {
        this.type = type;
        this.width = size;
        this.height = size;
        this.customColor = customColor;
    }

    public void setColor(Color customColor) {
        this.customColor = customColor;
    }

    @Override
    public int getIconWidth() { return width; }

    @Override
    public int getIconHeight() { return height; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);

        Color drawColor = customColor;
        if (drawColor == null) {
            drawColor = c != null ? c.getForeground() : Gestor_De_Temas.getAccentColor();
        }
        g2.setColor(drawColor);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int w = width;
        int h = height;

        switch (type) {
            case HOME:
                // Perfil de casa moderna
                g2.drawRect(4, h / 2 - 2, w - 8, h / 2 - 2);
                Path2D roof = new Path2D.Float();
                roof.moveTo(1, h / 2 - 2);
                roof.lineTo(w / 2, 2);
                roof.lineTo(w - 1, h / 2 - 2);
                g2.draw(roof);
                // Puerta centradita
                g2.drawRect(w / 2 - 3, h - 7, 6, 6);
                break;

            case CART:
                // Carrito de compras elegante
                g2.drawRect(5, 4, w - 10, h / 2 - 2);
                // Ruedas
                g2.setStroke(new BasicStroke(1.0f));
                g2.fillOval(w / 4 + 2, h / 2 + 5, 4, 4);
                g2.fillOval(w * 3 / 4 - 5, h / 2 + 5, 4, 4);
                // Mango y soporte
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(1, 3, 4, 3);
                g2.drawLine(3, 3, 4, h / 2 + 2);
                g2.drawLine(4, h / 2 + 2, w - 4, h / 2 + 2);
                break;

            case BOX:
                // Caja tridimensional (Gestor)
                Path2D boxPath = new Path2D.Float();
                boxPath.moveTo(w / 2, 2);
                boxPath.lineTo(w - 3, h / 3);
                boxPath.lineTo(w - 3, h * 3 / 4);
                boxPath.lineTo(w / 2, h - 2);
                boxPath.lineTo(3, h * 3 / 4);
                boxPath.lineTo(3, h / 3);
                boxPath.closePath();
                g2.draw(boxPath);
                
                // Líneas internas para volumen 3D
                g2.drawLine(w / 2, 2, w / 2, h - 2);
                g2.drawLine(3, h / 3, w / 2, h / 2);
                g2.drawLine(w - 3, h / 3, w / 2, h / 2);
                break;

            case CHART:
                // Reportes (Gráfico de barras)
                g2.drawLine(3, h - 3, w - 3, h - 3); // Eje X
                g2.setStroke(new BasicStroke(1.0f));
                // Columna 1
                g2.fillRect(5, h - 9, 3, 5);
                // Columna 2
                g2.fillRect(10, h - 16, 3, 12);
                // Columna 3
                g2.fillRect(15, h - 12, 3, 8);
                break;

            case TRASH:
                // Tacho de basura
                g2.drawRect(5, 6, w - 10, h - 9);
                g2.drawLine(2, 6, w - 2, 6); // Tapa
                g2.drawRect(w / 2 - 3, 2, 6, 4); // Agarradera superior
                // Líneas decorativas verticales
                g2.drawLine(w / 3 + 1, 9, w / 3 + 1, h - 6);
                g2.drawLine(w * 2 / 3 - 1, 9, w * 2 / 3 - 1, h - 6);
                break;

            case SEARCH:
                // Lupa de búsqueda
                int d = w * 2 / 3 - 1;
                g2.drawOval(2, 2, d, d);
                g2.drawLine(d - 2, d - 2, w - 2, h - 2);
                break;

            case CREDIT_CARD:
                // Tarjeta de pago
                g2.drawRoundRect(2, 4, w - 4, h - 8, 4, 4);
                g2.fillRect(2, 8, w - 4, 3); // Banda magnética
                g2.drawRect(5, 14, 4, 3);    // Chip
                break;

            case CLOSE:
                // X de cerrado
                g2.drawLine(4, 4, w - 4, h - 4);
                g2.drawLine(w - 4, 4, 4, h - 4);
                break;

            case ADD:
                // Signo más
                g2.drawLine(w / 2, 3, w / 2, h - 3);
                g2.drawLine(3, h / 2, w - 3, h / 2);
                break;

            case SHOE:
                // Silueta elegante de calzado
                Path2D shoe = new Path2D.Float();
                shoe.moveTo(2, h - 6); 
                shoe.lineTo(4, h - 13);
                shoe.quadTo(8, 4, 13, 6);
                shoe.lineTo(w - 5, h - 12);
                shoe.quadTo(w - 1, h - 7, w - 3, h - 5);
                shoe.lineTo(6, h - 5);
                shoe.closePath();
                g2.draw(shoe);
                g2.drawLine(6, h - 5, 5, h - 11); // Tacón estilizado
                break;

            case SUN:
                // Sol para modo Claro
                int sunR = w / 2;
                g2.drawOval(w / 4 + 1, h / 4 + 1, sunR - 2, sunR - 2);
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = (int) (w / 2 + (w / 4) * Math.cos(angle));
                    int y1 = (int) (h / 2 + (h / 4) * Math.sin(angle));
                    int x2 = (int) (w / 2 + (w / 2 - 1) * Math.cos(angle));
                    int y2 = (int) (h / 2 + (h / 2 - 1) * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
                break;

            case MOON:
                // Luna creciente con estrella para modo oscuro
                Path2D moon = new Path2D.Float();
                moon.moveTo(w / 3, h / 5);
                moon.quadTo(w / 5, h / 2, w / 3, h * 4 / 5);
                moon.quadTo(w * 3 / 4, h * 3 / 4, w * 4 / 5, h / 3);
                moon.quadTo(w * 3 / 5, h / 2, w / 3, h / 5);
                moon.closePath();
                g2.fill(moon);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(w - 5, 4, w - 5, 8);
                g2.drawLine(w - 7, 6, w - 3, 6);
                break;

            case EYE:
                // Ojo para ver comprobantes
                Path2D eye = new Path2D.Float();
                eye.moveTo(2, h / 2);
                eye.quadTo(w / 2, -2, w - 2, h / 2);
                eye.quadTo(w / 2, h + 2, 2, h / 2);
                eye.closePath();
                g2.draw(eye);
                g2.drawOval(w / 2 - 3, h / 2 - 3, 6, 6);
                g2.fillOval(w / 2 - 1, h / 2 - 1, 2, 2);
                break;

            case CASH:
                // Billete elegante con bordes redondeados y relieve interno
                g2.drawRoundRect(2, 4, w - 4, h - 8, 4, 4);
                g2.drawOval(w / 2 - 4, h / 2 - 4, 8, 8); // Círculo central
                g2.drawLine(4, 7, 4, h - 8);
                g2.drawLine(w - 5, 7, w - 5, h - 8);
                break;

            case SAVE:
                // Disquete premium optimizado
                Path2D savePath = new Path2D.Float();
                savePath.moveTo(3, h - 3);
                savePath.lineTo(w - 3, h - 3);
                savePath.lineTo(w - 3, 6);
                savePath.lineTo(w - 6, 3);
                savePath.lineTo(3, 3);
                savePath.closePath();
                g2.draw(savePath);
                // Etiqueta inferior
                g2.drawRect(6, h - 10, w - 12, 7);
                // Placa deslizante superior
                g2.drawRect(w / 2 - 4, 3, 8, 5);
                break;

            case PRINT:
                // Impresora premium escalable
                int bodyY = h / 3;
                int bodyH = h / 3 + 2;
                g2.drawRoundRect(3, bodyY, w - 6, bodyH, 3, 3); // Cuerpo central
                g2.drawRect(6, 2, w - 12, bodyY - 2); // Rodillo superior (entrada de papel)
                // Ranura y ticket saliendo por debajo
                g2.drawLine(6, bodyY + bodyH - 3, w - 6, bodyY + bodyH - 3); // Ranura
                g2.drawRect(w / 4, bodyY + bodyH - 3, w / 2, h - (bodyY + bodyH) + 1); // Ticket saliendo
                break;

            case MINIMIZE:
                // Línea de minimizar
                g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(5, h / 2 + 4, w - 5, h / 2 + 4);
                break;
        }

        g2.dispose();
    }
}
