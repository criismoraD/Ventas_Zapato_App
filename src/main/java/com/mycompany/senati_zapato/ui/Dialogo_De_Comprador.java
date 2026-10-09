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
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JLabel lblEstado = new JLabel("Complete los datos del comprador o elija «Venta general» para continuar sin registrarlos.");
    private Cliente cliente;
    private boolean continuar;

    public Dialogo_De_Comprador(Frame parent) {
        super(parent, "Paso 1 de 2 - Datos del comprador", true);
        setSize(590, 510);
        setLocationRelativeTo(parent);
        construirUI();
    }

    public Cliente getCliente() {
        return cliente;
    }

    public boolean continuar() {
        return continuar;
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Gestor_De_Temas.getBgColor());
        raiz.setBorder(new LineBorder(Gestor_De_Temas.getBorderColor(), 1, true));
        setContentPane(raiz);

        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setBackground(Gestor_De_Temas.getNavColor());
        encabezado.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel titulo = new JLabel("PASO 1 DE 2 · DATOS DEL COMPRADOR");
        titulo.setForeground(Gestor_De_Temas.getTextColor());
        titulo.setFont(new Font("Georgia", Font.BOLD, 19));
        encabezado.add(titulo, BorderLayout.WEST);
        JLabel ayuda = new JLabel("Opcional");
        ayuda.setForeground(Gestor_De_Temas.getAccentColor());
        ayuda.setFont(new Font("Segoe UI", Font.BOLD, 13));
        encabezado.add(ayuda, BorderLayout.EAST);
        raiz.add(encabezado, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(10, 24, 0, 24));
        lblEstado.setForeground(Gestor_De_Temas.getMutedColor());
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        agregarCampo(formulario, c, 3, "Correo electrónico", txtCorreo, null);
        agregarCampo(formulario, c, 4, "Dirección (opcional)", txtDireccion, null);
        contenido.add(formulario, BorderLayout.CENTER);

        JButton btnBuscar = crearBoton("Buscar comprador", Gestor_De_Temas.getNavColor());
        btnBuscar.addActionListener(e -> buscar());
        JButton btnGuardar = crearBoton("Guardar y continuar", Gestor_De_Temas.getAccentColor());
        btnGuardar.addActionListener(e -> guardar());
        JButton btnGeneral = crearBoton("Venta general", new Color(105, 105, 105));
        btnGeneral.addActionListener(e -> {
            lblEstado.setText("Venta general confirmada. Continuará con la selección del método de pago.");
            continuar = true;
            dispose();
        });
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        componente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        txtCorreo.setText(encontrado.getCorreo());
        txtDireccion.setText(encontrado.getDireccion());
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
        String correo = txtCorreo.getText().trim();
        if (!correo.isEmpty() && !Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", correo)) {
            mostrarError("Ingrese un correo electrónico válido o déjelo vacío.", txtCorreo);
            return;
        }
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setTipoDocumento((String) cmbTipo.getSelectedItem());
            cliente.setNumeroDocumento(txtDocumento.getText().trim());
        }
        cliente.setNombres(nombres);
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setCorreo(correo);
        cliente.setDireccion(txtDireccion.getText().trim());
        try {
            if (cliente.getId() == 0) {
                cliente = clienteDAO.guardar(cliente);
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
