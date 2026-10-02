package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.UIManager;

/**
 * Gestor de Temas para la Zapatería con estilo Warm Leather (Claro/Oscuro).
 */
public class Gestor_De_Temas {
    private static boolean isDarkMode = false;

    // Paleta de Colores - Modo Claro (Warm Leather Light)
    public static final Color LIGHT_BG       = new Color(253, 251, 247); // Crema
    public static final Color LIGHT_PANEL_BG = new Color(253, 251, 247); 
    public static final Color LIGHT_ACCENT   = new Color(138, 82, 57);   // Terracota
    public static final Color LIGHT_TEXT     = new Color(54, 32, 22);    // Chocolate
    public static final Color LIGHT_MUTED    = new Color(156, 132, 118);  // Café Suave
    public static final Color LIGHT_BORDER   = new Color(230, 221, 213);  // Crema Oscuro
    public static final Color LIGHT_NAV      = new Color(250, 246, 240);  // Beige
    public static final Color LIGHT_HOVER    = new Color(242, 234, 225);  // Tostado Suave

    // Paleta de Colores - Modo Oscuro (Warm Leather Dark)
    public static final Color DARK_BG       = new Color(28, 20, 16);    // Cuero Negro-Marrón
    public static final Color DARK_PANEL_BG = new Color(44, 31, 26);    // Chocolate Oscuro
    public static final Color DARK_ACCENT   = new Color(192, 108, 76);   // Terracota Brillante
    public static final Color DARK_TEXT     = new Color(245, 239, 234);  // Crema Lácteo
    public static final Color DARK_MUTED    = new Color(141, 120, 109);  // Café Grisáceo
    public static final Color DARK_BORDER   = new Color(61, 42, 32);     // Borde Café Oscuro
    public static final Color DARK_NAV      = new Color(38, 27, 22);     // Espresso Profundo
    public static final Color DARK_HOVER    = new Color(59, 42, 35);     // Tostado Oscuro

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static Color getBgColor() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }

    public static Color getPanelBgColor() {
        return isDarkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG;
    }

    public static Color getAccentColor() {
        return isDarkMode ? DARK_ACCENT : LIGHT_ACCENT;
    }

    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getMutedColor() {
        return isDarkMode ? DARK_MUTED : LIGHT_MUTED;
    }

    public static Color getBorderColor() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color getNavColor() {
        return isDarkMode ? DARK_NAV : LIGHT_NAV;
    }

    public static Color getHoverColor() {
        return isDarkMode ? DARK_HOVER : LIGHT_HOVER;
    }

    public static Color getSuccessColor() {
        return isDarkMode ? new Color(123, 167, 116) : new Color(103, 137, 96);
    }

    public static Color getWarningColor() {
        return isDarkMode ? new Color(232, 160, 100) : new Color(212, 140, 80);
    }

    public static Color getDangerColor() {
        return isDarkMode ? new Color(206, 95, 88) : new Color(176, 75, 68);
    }

    /**
     * Aplica el tema y sobreescribe los valores predeterminados en el UIManager.
     */
    public static void applyLeatherTheme(boolean dark) {
        isDarkMode = dark;
        if (dark) {
            FlatDarkLaf.setup();
            setupDarkUIOverrides();
        } else {
            FlatLightLaf.setup();
            setupLightUIOverrides();
        }
    }

    private static void setupLightUIOverrides() {
        UIManager.put("Panel.background", LIGHT_BG);
        UIManager.put("Label.foreground", LIGHT_TEXT);
        UIManager.put("Label.background", LIGHT_BG);
        
        UIManager.put("Button.background", LIGHT_NAV);
        UIManager.put("Button.foreground", LIGHT_TEXT);
        UIManager.put("Button.hoverBackground", LIGHT_HOVER);
        UIManager.put("Button.focusedBackground", LIGHT_HOVER);
        UIManager.put("Button.arc", 12);
        
        UIManager.put("TextField.background", LIGHT_PANEL_BG);
        UIManager.put("TextField.foreground", LIGHT_TEXT);
        UIManager.put("TextField.caretColor", LIGHT_TEXT);
        UIManager.put("TextField.arc", 12);
        
        UIManager.put("Table.background", LIGHT_PANEL_BG);
        UIManager.put("Table.foreground", LIGHT_TEXT);
        UIManager.put("Table.gridColor", LIGHT_BORDER);
        UIManager.put("Table.rowHeight", 45);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", LIGHT_HOVER);
        UIManager.put("Table.selectionForeground", LIGHT_TEXT);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        
        UIManager.put("TableHeader.background", LIGHT_NAV);
        UIManager.put("TableHeader.foreground", LIGHT_TEXT);
        
        UIManager.put("ScrollPane.background", LIGHT_BG);
        UIManager.put("Viewport.background", LIGHT_BG);
        UIManager.put("ScrollPane.border", javax.swing.BorderFactory.createLineBorder(LIGHT_BORDER, 1));
        
        UIManager.put("ScrollBar.thumb", LIGHT_MUTED);
        UIManager.put("ScrollBar.thumbArc", 6);
        UIManager.put("ScrollBar.thumbWidth", 8);
        UIManager.put("ScrollBar.track", LIGHT_BG);
        
        UIManager.put("JOptionPane.background", LIGHT_BG);
        UIManager.put("OptionPane.messageForeground", LIGHT_TEXT);
    }

    private static void setupDarkUIOverrides() {
        UIManager.put("Panel.background", DARK_BG);
        UIManager.put("Label.foreground", DARK_TEXT);
        UIManager.put("Label.background", DARK_BG);
        
        UIManager.put("Button.background", DARK_NAV);
        UIManager.put("Button.foreground", DARK_TEXT);
        UIManager.put("Button.hoverBackground", DARK_HOVER);
        UIManager.put("Button.focusedBackground", DARK_HOVER);
        UIManager.put("Button.arc", 12);
        
        UIManager.put("TextField.background", DARK_PANEL_BG);
        UIManager.put("TextField.foreground", DARK_TEXT);
        UIManager.put("TextField.caretColor", DARK_TEXT);
        UIManager.put("TextField.arc", 12);
        
        UIManager.put("Table.background", DARK_PANEL_BG);
        UIManager.put("Table.foreground", DARK_TEXT);
        UIManager.put("Table.gridColor", DARK_BORDER);
        UIManager.put("Table.rowHeight", 45);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", DARK_HOVER);
        UIManager.put("Table.selectionForeground", DARK_TEXT);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        
        UIManager.put("TableHeader.background", DARK_NAV);
        UIManager.put("TableHeader.foreground", DARK_TEXT);
        
        UIManager.put("ScrollPane.background", DARK_BG);
        UIManager.put("Viewport.background", DARK_BG);
        UIManager.put("ScrollPane.border", javax.swing.BorderFactory.createLineBorder(DARK_BORDER, 1));
        
        UIManager.put("ScrollBar.thumb", DARK_MUTED);
        UIManager.put("ScrollBar.thumbArc", 6);
        UIManager.put("ScrollBar.thumbWidth", 8);
        UIManager.put("ScrollBar.track", DARK_BG);
        
        UIManager.put("JOptionPane.background", DARK_BG);
        UIManager.put("OptionPane.messageForeground", DARK_TEXT);
    }
}
