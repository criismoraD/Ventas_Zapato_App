package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.datos.Dao_De_Cliente;
import com.mycompany.senati_zapato.modelos.Cliente;
import java.awt.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Dialogo_De_Comprador extends JDialog {
    private final Dao_De_Cliente clienteDAO = new Dao_De_Cliente();
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"DNI", "RUC"});
    private final JTextField txtDocumento = new JTextField();
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = null;
    private final JTextField txtDireccion = null;
    private final JLabel lblEstado = new JLabel("Complete los datos del comprador o elija «Venta general» para continuar sin registrarlos.");
    private Cliente cliente;
    private boolean continuar;

    public Dialogo_De_Comprador(Frame parent) {
        super(parent, "Datos del comprador", true);
        setSize(590, 510);
        setLocationRelativeTo(parent);
        configurarCampoDocumento();
        construirUI();
        prellenarDesdeBot();
    }

    /**
     * Si el bot guardó un DNI/RUC, pre-llena el formulario y lanza la consulta en background.
     */
    private void prellenarDesdeBot() {
        String doc = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Obtener_DNI_Pendiente();
        if (doc == null || doc.isBlank()) {
            // También revisa si ya hay datos del bot (fallback)
            doc = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Obtener_Ultimo_DNI();
            String nombres = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Obtener_Ultimos_Nombres();
            if (doc != null && nombres != null) {
                if (doc.length() == 11) cmbTipo.setSelectedItem("RUC");
                else cmbTipo.setSelectedItem("DNI");
                txtDocumento.setText(doc);
                txtNombres.setText(nombres);
                lblEstado.setText("Datos obtenidos automáticamente del bot (RENIEC/SUNAT). Verifique y continúe.");
                lblEstado.setForeground(new Color(79, 133, 87));
                com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Limpiar();
            }
            return;
        }

        // Abrir el formulario YA con el documento, y consultar en segundo plano
        if (doc.length() == 11) {
            cmbTipo.setSelectedItem("RUC");
        } else {
            cmbTipo.setSelectedItem("DNI");
        }
        txtDocumento.setText(doc);
        lblEstado.setText("Consultando RENIEC/SUNAT en segundo plano...");
        lblEstado.setForeground(new Color(100, 100, 100));

        final String docFinal = doc;
        new Thread(() -> {
            String nombres;
            if (docFinal.length() == 11) {
                nombres = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Consultar_Razon_Social(docFinal);
            } else {
                nombres = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Consultar_Nombres(docFinal);
            }
            final String resultado = nombres;
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (resultado != null && !resultado.isBlank()) {
                    txtNombres.setText(resultado);
                    lblEstado.setText("Datos obtenidos de RENIEC/SUNAT. Verifique y continúe.");
                    lblEstado.setForeground(new Color(79, 133, 87));
                } else {
                    lblEstado.setText("No se pudo consultar. Ingrese los nombres manualmente.");
                    lblEstado.setForeground(new Color(190, 130, 45));
                }
            });
        }, "consulta-dni-background").start();
    }

    private void configurarCampoDocumento() {
        ((javax.swing.text.AbstractDocument) txtDocumento.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (string == null) return;
                string = string.replaceAll("\\D", "");
                int max = "RUC".equals(cmbTipo.getSelectedItem()) ? 11 : 8;
                int restante = max - fb.getDocument().getLength();
                if (restante > 0 && !string.isEmpty()) {
                    super.insertString(fb, offset, string.substring(0, Math.min(string.length(), restante)), attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (text != null) {
                    text = text.replaceAll("\\D", "");
                }
                int max = "RUC".equals(cmbTipo.getSelectedItem()) ? 11 : 8;
                int actual = fb.getDocument().getLength();
                int disponible = max - (actual - length);
                if (disponible > 0 && text != null && !text.isEmpty()) {
                    super.replace(fb, offset, length, text.substring(0, Math.min(text.length(), disponible)), attrs);
                } else {
                    super.replace(fb, offset, length, "", attrs);
                }
            }
        });
        cmbTipo.addActionListener(e -> {
            int max = "RUC".equals(cmbTipo.getSelectedItem()) ? 11 : 8;
            String actual = txtDocumento.getText();
            if (actual.length() > max) {
                txtDocumento.setText(actual.substring(0, max));
            }
            txtDocumento.setToolTipText("Máximo " + max + " dígitos");
        });
        txtDocumento.setToolTipText("Máximo 8 dígitos");

        // Auto-consulta RENIEC al completar 8 dígitos (solo DNI)
        txtDocumento.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { consultarSiCompleto(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {}
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
    }

    private void consultarSiCompleto() {
        String tipo = (String) cmbTipo.getSelectedItem();
        String doc = txtDocumento.getText().trim();

        if ("DNI".equals(tipo) && doc.length() == 8) {
            if (cliente != null && doc.equals(cliente.getNumeroDocumento())) return;
            lblEstado.setText("Consultando RENIEC...");
            lblEstado.setForeground(new Color(100, 100, 100));
            new Thread(() -> {
                String nombres = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Consultar_Nombres(doc);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (nombres != null && !nombres.isBlank()) {
                        if (txtNombres.getText().trim().isEmpty()) {
                            txtNombres.setText(nombres);
                        }
                        lblEstado.setText("Datos obtenidos de RENIEC. Verifique y continúe.");
                        lblEstado.setForeground(new Color(79, 133, 87));
                    } else {
                        lblEstado.setText("No se pudo consultar RENIEC. Ingrese los nombres manualmente.");
                        lblEstado.setForeground(new Color(190, 130, 45));
                    }
                });
            }, "consulta-dni").start();
        } else if ("RUC".equals(tipo) && doc.length() == 11) {
            if (cliente != null && doc.equals(cliente.getNumeroDocumento())) return;
            lblEstado.setText("Consultando SUNAT...");
            lblEstado.setForeground(new Color(100, 100, 100));
            new Thread(() -> {
                String razon = com.mycompany.senati_zapato.servicios.Servicio_De_DNI.Consultar_Razon_Social(doc);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (razon != null && !razon.isBlank()) {
                        if (txtNombres.getText().trim().isEmpty()) {
                            txtNombres.setText(razon);
                        }
                        lblEstado.setText("Datos obtenidos de SUNAT. Verifique y continúe.");
                        lblEstado.setForeground(new Color(79, 133, 87));
                    } else {
                        lblEstado.setText("No se pudo consultar SUNAT. Ingrese la razón social manualmente.");
                        lblEstado.setForeground(new Color(190, 130, 45));
                    }
                });
            }, "consulta-ruc").start();
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public boolean continuar() {
        return continuar;
    }

    /**
     * Confirma «Venta general» con la misma acción que el botón del formulario:
     * cierra este diálogo y el flujo continúa con la selección del método de pago.
     * Lo usa el asistente cuando el usuario pide omitir los datos del comprador.
     */
    public void aplicarVentaGeneral() {
        if (!isDisplayable()) {
            return;
        }
        lblEstado.setText("Venta general confirmada. Continuará con la selección del método de pago.");
        continuar = true;
        dispose();
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Gestor_De_Temas.getBgColor());
        raiz.setBorder(new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true));
        setContentPane(raiz);

        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setBackground(Gestor_De_Temas.getNavColor());
        encabezado.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel titulo = new JLabel("DATOS DEL COMPRADOR");
        titulo.setForeground(Gestor_De_Temas.getTextColor());
        titulo.setFont(new Font("Inter", Font.BOLD, 19));
        encabezado.add(titulo, BorderLayout.WEST);
        JLabel ayuda = new JLabel("Opcional");
        ayuda.setForeground(Gestor_De_Temas.getAccentColor());
        ayuda.setFont(new Font("Inter", Font.BOLD, 13));
        encabezado.add(ayuda, BorderLayout.EAST);
        raiz.add(encabezado, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(10, 24, 0, 24));
        lblEstado.setForeground(Gestor_De_Temas.getMutedColor());
        lblEstado.setFont(new Font("Inter", Font.PLAIN, 13));
        contenido.add(lblEstado, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        agregarCampo(formulario, c, 0, "Documento", cmbTipo, txtDocumento);
        agregarCampo(formulario, c, 1, "Nombres / razón social", txtNombres, null);
        agregarCampo(formulario, c, 2, "Teléfono", txtTelefono, null);
        contenido.add(formulario, BorderLayout.CENTER);

        JButton btnBuscar = crearBoton("Buscar comprador", Gestor_De_Temas.getNavColor());
        btnBuscar.addActionListener(e -> buscar());
        JButton btnGuardar = crearBoton("Guardar y continuar", Gestor_De_Temas.getAccentColor());
        btnGuardar.addActionListener(e -> guardar());
        JButton btnGeneral = crearBoton("Venta general", new Color(105, 105, 105));
        btnGeneral.addActionListener(e -> aplicarVentaGeneral());
        JButton btnCancelar = crearBoton("Cancelar cobro", new Color(130, 70, 70));
        btnCancelar.addActionListener(e -> dispose());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        acciones.setOpaque(false);
        acciones.add(btnBuscar);
        acciones.add(btnGeneral);
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);
        contenido.add(acciones, BorderLayout.SOUTH);
        raiz.add(contenido, BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnGuardar);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints base, int fila, String etiqueta,
            JComponent principal, JComponent secundario) {
        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridy = fila;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        JLabel label = new JLabel(etiqueta);
        label.setForeground(Gestor_De_Temas.getTextColor());
        label.setFont(new Font("Inter", Font.BOLD, 13));
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = secundario == null ? 2 : 1;
        prepararCampo(principal);
        panel.add(principal, c);
        if (secundario != null) {
            c.gridx = 2;
            c.gridwidth = 1;
            c.weightx = 2;
            prepararCampo(secundario);
            panel.add(secundario, c);
        }
    }

    private void prepararCampo(JComponent componente) {
        componente.setFont(new Font("Inter", Font.PLAIN, 14));
        componente.setPreferredSize(new Dimension(0, 34));
        if (componente instanceof JTextField) {
            componente.setBackground(Gestor_De_Temas.getPanelBgColor());
            componente.setForeground(Gestor_De_Temas.getTextColor());
            componente.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(Gestor_De_Temas.getBorderColor()), new EmptyBorder(4, 8, 4, 8)));
        }
    }

    private JButton crearBoton(String texto, Color fondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Inter", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(9, 12, 9, 12));
        return boton;
    }

    private void buscar() {
        String documento = txtDocumento.getText().trim();
        if (!validarDocumento(documento)) {
            return;
        }
        Cliente encontrado = clienteDAO.buscarPorDocumento((String) cmbTipo.getSelectedItem(), documento);
        if (encontrado == null) {
            lblEstado.setText("No existe. Complete los datos y presione «Guardar y continuar».");
            lblEstado.setForeground(new Color(190, 130, 45));
            txtNombres.requestFocusInWindow();
            return;
        }
        cliente = encontrado;
        txtNombres.setText(encontrado.getNombres());
        txtTelefono.setText(encontrado.getTelefono());
        lblEstado.setText("Comprador encontrado. Verifique los datos y continúe.");
        lblEstado.setForeground(new Color(79, 133, 87));
    }

    private void guardar() {
        String documentoActual = txtDocumento.getText().trim();
        String tipoActual = (String) cmbTipo.getSelectedItem();
        if (!validarDocumento(documentoActual)) {
            return;
        }
        if (cliente != null && (!tipoActual.equals(cliente.getTipoDocumento())
                || !documentoActual.equals(cliente.getNumeroDocumento()))) {
            cliente = null;
        }
        String nombres = txtNombres.getText().trim();
        if (nombres.isEmpty()) {
            mostrarError("Ingrese los nombres o la razón social del comprador.", txtNombres);
            return;
        }
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setTipoDocumento((String) cmbTipo.getSelectedItem());
            cliente.setNumeroDocumento(txtDocumento.getText().trim());
        }
        cliente.setNombres(nombres);
        cliente.setTelefono(txtTelefono.getText().trim());
        try {
            if (cliente.getId() == 0) {
                Cliente previo = clienteDAO.buscarPorDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento());
                if (previo != null) {
                    cliente = previo;
                    JOptionPane.showMessageDialog(this,
                            "Ese documento ya está registrado a nombre de:\n\n"
                                    + previo.getNombres() + "\n"
                                    + previo.getTipoDocumento() + " " + previo.getNumeroDocumento()
                                    + "\n\nSe usarán estos datos registrados para continuar.",
                            "Comprador ya registrado",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    cliente = clienteDAO.guardar(cliente);
                }
            } else {
                cliente = clienteDAO.actualizar(cliente);
            }
            continuar = true;
            dispose();
        } catch (RuntimeException ex) {
            mostrarError(ex.getMessage(), txtDocumento);
        }
    }

    private boolean validarDocumento(String documento) {
        int esperado = "RUC".equals(cmbTipo.getSelectedItem()) ? 11 : 8;
        if (!documento.matches("\\d{" + esperado + "}")) {
            mostrarError("El " + cmbTipo.getSelectedItem() + " debe tener " + esperado + " dígitos.", txtDocumento);
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje, JComponent campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Dato requerido", JOptionPane.WARNING_MESSAGE);
        campo.requestFocusInWindow();
    }
}
