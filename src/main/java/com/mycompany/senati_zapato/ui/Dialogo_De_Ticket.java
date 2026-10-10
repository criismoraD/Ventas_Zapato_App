package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * JDialog que simula un ticket térmico de impresora de forma realista.
 * Proporciona opciones de exportación a archivo .txt y simulación de impresión física.
 */
public class Dialogo_De_Ticket extends JDialog {

    private final Venta venta;
    private String ticketText;

    public Dialogo_De_Ticket(Frame parent, Venta venta) {
        super(parent, "Comprobante de Pago - POS", true);
        this.venta = venta;

        setUndecorated(true);
        setSize(480, 700);
        setLocationRelativeTo(parent);

        generarTextoTicket();
        buildUI();
    }

    private void generarTextoTicket() {
        StringBuilder sb = new StringBuilder();
        String lineSep = "------------------------------------------\n";

        sb.append("            SENATI ZAPATO S.A.C.          \n");
        sb.append("        R.U.C. N° 20123456789             \n");
        sb.append("   Av. Alfredo Mendiola 3520, Lima 15011  \n");
        sb.append("            Tlf: (01) 234-5678            \n");
        sb.append(lineSep);
        sb.append("              BOLETA ELECTRÓNICA          \n");
        sb.append(String.format(" NRO BOLETA: BO01-%08d\n", venta.Get_Id()));
        
        // Formato fecha
        String fecha = venta.getFechaHora();
        if (fecha == null || fecha.isEmpty()) {
            fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
        sb.append(" FECHA HORA: ").append(fecha).append("\n");
        sb.append(" CAJERO    : ").append(venta.Get_Cajero()).append("\n");
        if (venta.getCliente() != null) {
            sb.append(lineSep);
            sb.append(" COMPRADOR:\n");
            sb.append(" ").append(venta.getCliente().getTipoDocumento()).append(": ")
                    .append(venta.getCliente().getNumeroDocumento()).append("\n");
            sb.append(" NOMBRE    : ").append(venta.getCliente().getNombres()).append("\n");
            if (!venta.getCliente().getTelefono().isBlank()) {
                sb.append(" TELÉFONO  : ").append(venta.getCliente().getTelefono()).append("\n");
            }
            if (!venta.getCliente().getCorreo().isBlank()) {
                sb.append(" CORREO    : ").append(venta.getCliente().getCorreo()).append("\n");
            }
        } else {
            sb.append(" CLIENTE   : Venta general\n");
        }
        sb.append(lineSep);
        sb.append(" Cant.  Descripción        P.Unit   Total \n");
        sb.append(lineSep);

        for (Detalle_De_Venta dv : venta.Get_Detalles()) {
            String prodNombre = dv.Get_Nombre_Producto();
            if (prodNombre.length() > 18) {
                prodNombre = prodNombre.substring(0, 15) + "...";
            }
            sb.append(String.format(" %-5d  %-18s  %-7.2f  %-7.2f\n", 
                dv.Get_Cantidad(), 
                prodNombre, 
                dv.Get_Precio_Unitario(), 
                dv.Get_Subtotal()));
        }

        sb.append(lineSep);
        // Cálculos con IGV
        double total = venta.Get_Monto_Total();
        double subtotal = total / 1.18;
        double igv = total - subtotal;

        sb.append(String.format(" SUB-TOTAL              : S/ %10.2f\n", subtotal));
        sb.append(String.format(" I.G.V. (18%%)           : S/ %10.2f\n", igv));
        sb.append(String.format(" TOTAL A PAGAR          : S/ %10.2f\n", total));
        sb.append(lineSep);
        sb.append(" MÉTODO DE PAGO         : ").append(venta.Get_Metodo_Pago().toUpperCase()).append("\n");
        
        if (venta.Get_Metodo_Pago().equalsIgnoreCase("Efectivo")) {
            sb.append(String.format(" MONTO RECIBIDO         : S/ %10.2f\n", venta.getMontoRecibido()));
            sb.append(String.format(" VUELTO ENTREGADO       : S/ %10.2f\n", venta.getVuelto()));
        } else {
            String ref = venta.getReferencia();
            if (ref != null && !ref.trim().isEmpty()) {
                sb.append(" REF. TRANSACCIÓN       : ").append(ref.toUpperCase()).append("\n");
            }
        }
        
        sb.append(lineSep);
        sb.append("      ¡GRACIAS POR ELEGIR SENATI ZAPATO!  \n");
        sb.append("         Calidad, Confort y Estilo        \n");
        sb.append("          Consulte su comprobante         \n");
        sb.append("           en www.senatizapato.pe         \n");
        
        this.ticketText = sb.toString();
    }

    private void buildUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Gestor_De_Temas.getBgColor());
        container.setBorder(new LineBorder(Gestor_De_Temas.getBorderColor(), 2, true));
        setContentPane(container);

        // Cabecera Dialog
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Gestor_De_Temas.getNavColor());
        titlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Gestor_De_Temas.getBorderColor()));
        JLabel lblHeader = new JLabel("TICKET DE VENTA");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 16));
        lblHeader.setForeground(Gestor_De_Temas.getTextColor());
        titlePanel.add(lblHeader);
        container.add(titlePanel, BorderLayout.NORTH);

        // Ticket simulado (Papel térmico)
        JPanel paperPanel = new JPanel(new BorderLayout());
        paperPanel.setBackground(Color.WHITE);
        paperPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 25, 20, 25),
            new LineBorder(new Color(220, 220, 220), 1)
        ));

        JTextArea txtTicket = new JTextArea();
        txtTicket.setText(ticketText);
        txtTicket.setFont(new Font("Courier New", Font.PLAIN, 13));
        txtTicket.setForeground(Color.BLACK);
        txtTicket.setBackground(Color.WHITE);
        txtTicket.setEditable(false);
        txtTicket.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(txtTicket);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        paperPanel.add(scrollPane, BorderLayout.CENTER);
        
        container.add(paperPanel, BorderLayout.CENTER);

        // Panel de acciones inferior
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Gestor_De_Temas.getBorderColor()));

        JButton btnGuardar = new JButton("Guardar TXT");
        btnGuardar.setIcon(new Icono_Elegante(Icono_Elegante.Type.SAVE, 18));
        btnGuardar.setIconTextGap(8);
        btnGuardar.setFont(new Font("Inter", Font.BOLD, 13));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarTicketComoTexto());
        actions.add(btnGuardar);

        JButton btnImprimir = new JButton("Imprimir Ticket");
        btnImprimir.setIcon(new Icono_Elegante(Icono_Elegante.Type.PRINT, 18));
        btnImprimir.setIconTextGap(8);
        btnImprimir.setFont(new Font("Inter", Font.BOLD, 13));
        btnImprimir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnImprimir.addActionListener(e -> simularImpresion());
        actions.add(btnImprimir);

        JButton btnCerrar = new JButton("Cerrar (Esc)");
        btnCerrar.setIcon(new Icono_Elegante(Icono_Elegante.Type.CLOSE, 16));
        btnCerrar.setIconTextGap(8);
        btnCerrar.setFont(new Font("Inter", Font.BOLD, 13));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        actions.add(btnCerrar);

        container.add(actions, BorderLayout.SOUTH);

        // Soporte tecla Esc para cerrar
        container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeTicket");
        container.getActionMap().put("closeTicket", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
    }

    private void guardarTicketComoTexto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Ticket de Compra");
        fileChooser.setSelectedFile(new File(String.format("ticket_BO01-%08d.txt", venta.Get_Id())));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(ticketText);
                JOptionPane.showMessageDialog(this, "Ticket guardado con éxito.", "Archivo Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simularImpresion() {
        // Simular efecto de impresión cargando
        ProgressMonitor pm = new ProgressMonitor(this, "Enviando comando a impresora térmica...", "Preparando cabezal...", 0, 100);
        pm.setProgress(0);
        
        new Thread(() -> {
            try {
                Thread.sleep(800);
                pm.setNote("Imprimiendo código de barras...");
                pm.setProgress(40);
                Thread.sleep(800);
                pm.setNote("Cortando papel...");
                pm.setProgress(80);
                Thread.sleep(500);
                pm.setProgress(100);
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(Dialogo_De_Ticket.this, 
                        "Impresión simulada completada satisfactoriamente.", 
                        "Impresora POS", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                });
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
