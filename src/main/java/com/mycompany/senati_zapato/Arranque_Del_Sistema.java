/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.senati_zapato;

import com.mycompany.senati_zapato.ui.*;

import javax.swing.SwingUtilities;

/**
 *
 * @author criis
 */
public class Arranque_Del_Sistema {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gestor_De_Temas.applyLeatherTheme(false); // Iniciar en Modo Claro Warm Leather
            Panel_Principal frame = new Panel_Principal();
            frame.setVisible(true);
        });
    }
}
