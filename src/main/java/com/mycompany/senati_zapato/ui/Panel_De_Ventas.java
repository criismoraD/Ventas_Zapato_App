package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.text.Normalizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Panel_De_Ventas extends JPanel {

    private Dao_De_Producto productoDAO;
    private DefaultTableModel tableModel;
    private JLabel lblTotalDisplay;
    private JLabel lblSubtotal;
    private JLabel lblIgv;
    private JPanel leftPanel; // Carrito
    private JPanel rightPanel; // Catálogo
    private JPanel gridPanel;
    private JTextField txtSearch;
    private String currentCategory = "Todos";
    private List<Producto> todosLosProductos;
    private JButton btnCancelar;
    private JButton btnCobrar;
    private List<Producto> productosMostrados = new ArrayList<>();
    private javax.swing.JScrollPane scrollGrid;
    private JTable cartTable;

    // ── Filtros aplicados por el asistente (color, precio, talla) ──
    private String filtroColor = null;
    private Double filtroPrecioMax = null;
    private Double filtroPrecioMin = null;
    // "menos de 150" es estricto (excluye 150); "hasta 150" lo incluye.
    private boolean precioMaxEstricto = false;
    private String filtroTalla = null;
    private boolean hayFiltroAsistente = false;
    private JPanel lblFiltroAsistente;   // Chip informativo con botón para limpiar
    private JLabel chipLabel;
    private JPanel tabsPanelRef;        // Referencia a las pestañas de categoría
    private String filtroTextoChip = "";
    private String consultaOriginal = "";
    private boolean totalColumnVisible = true;

    private static final java.util.Map<String, javax.swing.ImageIcon> IMAGE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ExecutorService IMAGE_LOAD_EXECUTOR = java.util.concurrent.Executors.newFixedThreadPool(4);
    private static javax.swing.ImageIcon defaultShoeIcon = null;

    private static synchronized javax.swing.ImageIcon getDefaultShoeIcon(java.net.URL defaultUrl) {
        if (defaultShoeIcon == null && defaultUrl != null) {
            try {
                java.awt.Image defaultImage = javax.imageio.ImageIO.read(defaultUrl);
                java.awt.Image scaled = defaultImage.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                defaultShoeIcon = new javax.swing.ImageIcon(scaled);
            } catch (Exception ex) {
                System.err.println("Error loading default shoe icon: " + ex.getMessage());
            }
        }
        return defaultShoeIcon;
    }
    
    public Panel_De_Ventas() {
        productoDAO = new Dao_De_Producto();
        todosLosProductos = productoDAO.Obtener_Todos();
        initComponents();
        initCustomUI();
        cargarProductos(todosLosProductos);
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(440, 0)); // ~30% width
        add(leftPanel, BorderLayout.WEST);

        rightPanel = new JPanel(new BorderLayout(0, 15));
        add(rightPanel, BorderLayout.CENTER);
    }

    private void initCustomUI() {
        // ======================= PANEL IZQUIERDO: CARRITO =======================
        leftPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        leftPanel.setBorder(BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1));

        // Cabecera del carrito con boton para mostrar/ocultar la columna Total
        JPanel headerCarrito = new JPanel(new BorderLayout());
        headerCarrito.setBackground(Gestor_De_Temas.getPanelBgColor());
        headerCarrito.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel lblTicketHeader = new JLabel("Lista de Compras", SwingConstants.CENTER);
        lblTicketHeader.setFont(new Font("Georgia", Font.BOLD, 22));
        lblTicketHeader.setForeground(Gestor_De_Temas.getTextColor());
        headerCarrito.add(lblTicketHeader, BorderLayout.CENTER);

        // Boton conmutador: oculta el Total para dar ese espacio a la columna Producto
        final JButton btnToggleTotal = new JButton();
        btnToggleTotal.setFocusPainted(false);
        btnToggleTotal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggleTotal.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        btnToggleTotal.setContentAreaFilled(false);
        btnToggleTotal.setOpaque(false);
        btnToggleTotal.setToolTipText("Ocultar la columna Total");
        btnToggleTotal.setIcon(new Icono_Elegante(Icono_Elegante.Type.EYE, 20, Gestor_De_Temas.getMutedColor()));
        btnToggleTotal.addActionListener(e -> {
            Alternar_Columna_Total(btnToggleTotal);
        });
        headerCarrito.add(btnToggleTotal, BorderLayout.EAST);

        leftPanel.add(headerCarrito, BorderLayout.NORTH);

        String[] columns = { "ID", "Cant", "Producto", "Total", "X" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Columna ID oculta, "Total" conmutable desde el boton de la cabecera
        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(48);
        cartTable.setBackground(Gestor_De_Temas.getPanelBgColor());
        cartTable.setForeground(Gestor_De_Temas.getTextColor());
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cartTable.setShowGrid(false);
        cartTable.setIntercellSpacing(new Dimension(0, 0));
        cartTable.getTableHeader().setBackground(Gestor_De_Temas.getNavColor());
        cartTable.getTableHeader().setForeground(Gestor_De_Temas.getTextColor());
        cartTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));
        cartTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Gestor_De_Temas.getBorderColor()));
        
        // Hide ID column
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(0).setWidth(0);
        
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50); // Cant
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Prod
        cartTable.getColumnModel().getColumn(3).setMinWidth(90); // Total
        cartTable.getColumnModel().getColumn(3).setMaxWidth(110);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(50); // Delete (X)

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer deleteRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText("");
                label.setIcon(new Icono_Elegante(Icono_Elegante.Type.TRASH, 18, new Color(220, 53, 69)));
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return label;
            }
        };
        cartTable.getColumnModel().getColumn(4).setCellRenderer(deleteRenderer);

        cartTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (col == 4 && row >= 0) { // Clic en 🗑️
                    tableModel.removeRow(row);
                    updateTotals();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.getViewport().setBackground(Gestor_De_Temas.getPanelBgColor());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Checkout panel (bottom left)
        JPanel checkoutPanel = new JPanel();
        checkoutPanel.setLayout(new BoxLayout(checkoutPanel, BoxLayout.Y_AXIS));
        checkoutPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        checkoutPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Gestor_De_Temas.getBorderColor()),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        lblSubtotal = new JLabel("Subtotal: S/ 0.00");
        lblSubtotal.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtotal.setForeground(Gestor_De_Temas.getMutedColor());
        lblSubtotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblIgv = new JLabel("IGV (18%): S/ 0.00");
        lblIgv.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblIgv.setForeground(Gestor_De_Temas.getMutedColor());
        lblIgv.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTotalDisplay = new JLabel("S/ 0.00");
        lblTotalDisplay.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblTotalDisplay.setForeground(Gestor_De_Temas.getAccentColor());
        lblTotalDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        checkoutPanel.add(lblSubtotal);
        checkoutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        checkoutPanel.add(lblIgv);
        checkoutPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel lblTotalLabel = new JLabel("TOTAL A PAGAR");
        lblTotalLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        lblTotalLabel.setForeground(Gestor_De_Temas.getTextColor());
        lblTotalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutPanel.add(lblTotalLabel);
        checkoutPanel.add(lblTotalDisplay);
        checkoutPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones: CANCELAR y COBRAR
        JPanel buttonsPanel = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));

        btnCancelar = new JButton(" CANCELAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = isEnabled() ? new Color(220, 53, 69) : new Color(180, 180, 180);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(new Color(255, 255, 255, 50));
                float[] btnDash = {3.0f, 3.0f};
                g2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, btnDash, 0.0f));
                g2.drawRoundRect(4, 4, w - 8, h - 8, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCancelar.setIcon(new Icono_Elegante(Icono_Elegante.Type.TRASH, 20, Color.WHITE));
        btnCancelar.setFont(new Font("Georgia", Font.BOLD, 16));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setPreferredSize(new Dimension(140, 55));
        btnCancelar.setEnabled(false);
        btnCancelar.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) {
                int resp = JOptionPane.showConfirmDialog(this, "¿Desea vaciar el carrito?", "Cancelar Venta", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    tableModel.setRowCount(0);
                    updateTotals();
                }
            }
        });

        btnCobrar = new JButton(" COBRAR (F12)") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = isEnabled() ? new Color(79, 133, 87) : new Color(180, 180, 180);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(new Color(255, 255, 255, 50));
                float[] btnDash = {3.0f, 3.0f};
                g2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, btnDash, 0.0f));
                g2.drawRoundRect(4, 4, w - 8, h - 8, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnCobrar.setIcon(new Icono_Elegante(Icono_Elegante.Type.CREDIT_CARD, 20, Color.WHITE));
        btnCobrar.setFont(new Font("Georgia", Font.BOLD, 16));
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setContentAreaFilled(false);
        btnCobrar.setBorderPainted(false);
        btnCobrar.setFocusPainted(false);
        btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCobrar.setPreferredSize(new Dimension(180, 55));
        btnCobrar.setEnabled(false);
        btnCobrar.addActionListener(e -> procesarVenta());

        buttonsPanel.add(btnCancelar);
        buttonsPanel.add(btnCobrar);

        checkoutPanel.add(buttonsPanel);
        leftPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // ======================= PANEL DERECHO: CATÁLOGO =======================
        rightPanel.setOpaque(false);
        
        JPanel headerRightPanel = new JPanel(new BorderLayout(0, 10));
        headerRightPanel.setOpaque(false);
        
        // Buscador
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtSearch.setPreferredSize(new Dimension(0, 45));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtSearch.setBackground(Gestor_De_Temas.getPanelBgColor());
        txtSearch.setForeground(Gestor_De_Temas.getTextColor());
        txtSearch.setCaretColor(Gestor_De_Temas.getTextColor());
        
        // Placeholder simulation
        JLabel lblSearchIcon = new JLabel("  Buscar por código de barras (SKU) o nombre...");
        lblSearchIcon.setIcon(new Icono_Elegante(Icono_Elegante.Type.SEARCH, 18, Gestor_De_Temas.getMutedColor()));
        lblSearchIcon.setForeground(Gestor_De_Temas.getMutedColor());
        lblSearchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtSearch.setLayout(new BorderLayout());
        txtSearch.add(lblSearchIcon, BorderLayout.WEST);
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterList(); }
            public void removeUpdate(DocumentEvent e) { filterList(); }
            public void changedUpdate(DocumentEvent e) { filterList(); }
            private void filterList() {
                lblSearchIcon.setVisible(txtSearch.getText().isEmpty());
                // La busqueda manual se COMBINA con los filtros del asistente: buscar
                // "Oxford" con el filtro talla 44 debe intersectar ambos, no vaciar uno.
                aplicarFiltros();
                Actualizar_Chip_Filtro();
            }
        });
        
        headerRightPanel.add(txtSearch, BorderLayout.NORTH);

        // ── Chip de filtro aplicado por el asistente ──
        // Indica por qué se está filtrando el catálogo y permite quitarlo con la X.
        lblFiltroAsistente = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lblFiltroAsistente.setVisible(false);
        lblFiltroAsistente.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getAccentColor(), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 6)
        ));
        chipLabel = new JLabel("");
        chipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chipLabel.setForeground(Gestor_De_Temas.getTextColor());
        lblFiltroAsistente.add(chipLabel);

        JButton btnQuitarFiltro = new JButton();
        btnQuitarFiltro.setIcon(new Icono_Elegante(Icono_Elegante.Type.CLOSE, 14, new Color(220, 53, 69)));
        btnQuitarFiltro.setFocusPainted(false);
        btnQuitarFiltro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuitarFiltro.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        btnQuitarFiltro.setContentAreaFilled(false);
        btnQuitarFiltro.setToolTipText("Quitar el filtro");
        btnQuitarFiltro.addActionListener(e -> Limpiar_Filtro_Asistente());
        lblFiltroAsistente.add(btnQuitarFiltro);

        headerRightPanel.add(lblFiltroAsistente, BorderLayout.SOUTH);

        // Tabs de Filtros (Categorías de Zapatos)
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabsPanel.setOpaque(false);
        tabsPanelRef = tabsPanel;
        String[] categorias = {"Todos", "Zapatos de vestir", "Zapatos casuales", "Mocasines", "Botas"};
        
        for (String cat : categorias) {
            JButton tabBtn = new JButton(cat);
            tabBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            tabBtn.setForeground(Gestor_De_Temas.getMutedColor());
            tabBtn.setBackground(Gestor_De_Temas.getBgColor());
            tabBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            tabBtn.setFocusPainted(false);
            tabBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (cat.equals("Todos")) {
                tabBtn.setForeground(Gestor_De_Temas.getTextColor());
                tabBtn.setBackground(Gestor_De_Temas.getAccentColor());
            }
            
            tabBtn.addActionListener(e -> {
                // Reset colors
                for (Component c : tabsPanel.getComponents()) {
                    if (c instanceof JButton) {
                        ((JButton)c).setForeground(Gestor_De_Temas.getMutedColor());
                        ((JButton)c).setBackground(Gestor_De_Temas.getBgColor());
                    }
                }
                tabBtn.setForeground(Gestor_De_Temas.getTextColor());
                tabBtn.setBackground(Gestor_De_Temas.getAccentColor());
                
                currentCategory = cat;
                aplicarFiltros();
            });
            tabsPanel.add(tabBtn);
        }
        
        headerRightPanel.add(tabsPanel, BorderLayout.CENTER);
        rightPanel.add(headerRightPanel, BorderLayout.NORTH);

        // Grid de Productos
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); 
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(800, 2000)); 
        
        JPanel wrapPanel = new JPanel(new BorderLayout());
        wrapPanel.setOpaque(false);
        wrapPanel.add(gridPanel, BorderLayout.NORTH);

        scrollGrid = new JScrollPane(wrapPanel);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        scrollGrid.setBorder(BorderFactory.createEmptyBorder());
        scrollGrid.getVerticalScrollBar().setUnitIncrement(20);
        scrollGrid.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                recalcularAlturaGrid();
            }
        });
        rightPanel.add(scrollGrid, BorderLayout.CENTER);

        // Key Binding para F12
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "cobrarVenta");
        this.getActionMap().put("cobrarVenta", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarVenta();
            }
        });
    }
    
    /**
     * Muestra u oculta la columna "Total" del carrito. Al ocultarla, el ancho que ocupaba
     * pasa a la columna "Producto", que es la que realmente interesa leer (el total ya
     * se muestra abajo como TOTAL A PAGAR).
     */
    private void Alternar_Columna_Total(JButton boton) {
        totalColumnVisible = !totalColumnVisible;
        if (cartTable == null) return;

        javax.swing.table.TableColumn colTotal = cartTable.getColumnModel().getColumn(3);
        javax.swing.table.TableColumn colProducto = cartTable.getColumnModel().getColumn(2);

        if (totalColumnVisible) {
            colTotal.setMinWidth(90);
            colTotal.setMaxWidth(110);
            colTotal.setPreferredWidth(100);
            colProducto.setPreferredWidth(200);
            boton.setToolTipText("Ocultar la columna Total");
            boton.setIcon(new Icono_Elegante(Icono_Elegante.Type.EYE, 20, Gestor_De_Temas.getMutedColor()));
        } else {
            colTotal.setMinWidth(0);
            colTotal.setMaxWidth(0);
            colTotal.setPreferredWidth(0);
            colTotal.setWidth(0);
            colProducto.setPreferredWidth(300);
            boton.setToolTipText("Mostrar la columna Total");
            boton.setIcon(new Icono_Elegante(Icono_Elegante.Type.EYE, 20, Gestor_De_Temas.getAccentColor()));
        }
        cartTable.revalidate();
        cartTable.repaint();
    }

    private void updateTotals() {
        double sub = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sub += (double) tableModel.getValueAt(i, 3);
        }
        double igv = sub * 0.18;
        double tot = sub + igv;
        lblSubtotal.setText(String.format("Subtotal: S/ %.2f", sub));
        lblIgv.setText(String.format("IGV (18%%): S/ %.2f", igv));
        lblTotalDisplay.setText(String.format("S/ %.2f", tot));
        btnCancelar.setEnabled(tableModel.getRowCount() > 0);
        btnCobrar.setEnabled(tableModel.getRowCount() > 0);
    }
    
    public void Refrescar_Catalogo() {
        todosLosProductos = productoDAO.Obtener_Todos();
        aplicarFiltros();
    }

    // =========================================================================
    // FILTRADO POR CONSULTA DEL ASISTENTE
    // Interpreta texto libre ("zapatos negros menos de 150 soles") y deja el
    // catálogo filtrado. Devuelve un resumen para que el modelo responda con datos.
    // =========================================================================

    /**
     * Alias de color -> forma canonica (masculino singular).
     * Se necesita el mapa porque el usuario escribe "negras" o "rojos" y los
     * productos se llaman "Mocasín Clásico Negro". Sin canonizar, "negras" no
     * encontraba nada y el filtro devolvia cero resultados.
     */
    private static final java.util.Map<String, String> COLORES_CANONICOS = new java.util.LinkedHashMap<>();
    static {
        registrarColor("negro",   "negro", "negra", "negros", "negras");
        registrarColor("blanco",  "blanco", "blanca", "blancos", "blancas");
        registrarColor("marrón",  "marrón", "marrones", "marron");
        registrarColor("café",    "café", "cafes", "cafe");
        registrarColor("beige",   "beige", "beiges");
        registrarColor("azul",    "azul", "azules");
        registrarColor("rojo",    "rojo", "roja", "rojos", "rojas");
        registrarColor("verde",   "verde", "verdes");
        registrarColor("gris",    "gris", "grises");
        registrarColor("plateado", "plateado", "plateados", "plateada", "plateadas");
        registrarColor("dorado",  "dorado", "dorados", "dorada", "doradas");
        registrarColor("vino",    "vino", "vinos");
        registrarColor("burdeos", "burdeos");
        registrarColor("turquesa", "turquesa", "turquesas");
        registrarColor("crema",   "crema", "cremas");
        registrarColor("naranja", "naranja", "naranjas");
        registrarColor("amarillo", "amarillo", "amarillos", "amarilla", "amarillas");
        registrarColor("lila",    "lila", "lilas");
        registrarColor("morado",  "morado", "morados", "morada", "moradas");
    }

    private static void registrarColor(String canonico, String... alias) {
        for (String a : alias) COLORES_CANONICOS.put(a, canonico);
    }

    /**
     * Aplica el filtro deducido de la consulta del usuario y devuelve un resumen
     * de los productos encontrados (nombre + precio) para que el asistente responda.
     */
    public String Aplicar_Filtro_Desde_Chatbot(String consulta) {
        if (consulta == null || consulta.trim().isEmpty()) {
            return "Error: consulta vacia.";
        }
        todosLosProductos = productoDAO.Obtener_Todos();
        consultaOriginal = consulta;

        Interpretar_Consulta(consulta);
        Aplicar_Categoria_Detectada(consulta);

        // Limpia la busqueda manual para que no se reste el filtro del asistente.
        txtSearch.setText("");
        aplicarFiltros();
        Actualizar_Chip_Filtro();

        return Resumen_Productos_Filtrados();
    }

    /**
     * Detecta color, rango de precios y talla dentro de la frase del usuario.
     *
     * IMPORTANTE: los filtros se ACUMULAN, no se reemplazan. Si el usuario pide
     * "zapatos talla 44" y luego "en negros", debe quedar talla 44 + negro. Por eso
     * aqui NO se resetea nada al principio: solo se sobrescribe la dimension que
     * la frase nueva menciona de forma explicita.
     */
    private void Interpretar_Consulta(String consulta) {
        String q = consulta.toLowerCase();
        // Quita acentos para comparar "marron" contra "marrón".
        String qPlano = Normalizer.normalize(q, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        // Peticiones de reinicio explicitas: "ver todo", "quita el filtro", "sin filtro".
        if (qPlano.matches(".*\\b(ver\\s+todo|muestra(me)?\\s+todo|todo\\s+el\\s+catalogo|quita(r)?\\s+(el\\s+)?filtro|sin\\s+filtro|limpia(r)?\\s+filtro|sin\\s+restricciones?)\\b.*")) {
            hayFiltroAsistente = false;
            filtroColor = null;
            filtroPrecioMax = null;
            filtroPrecioMin = null;
            filtroTalla = null;
            precioMaxEstricto = false;
            return;
        }

        // Color: se canoniza ("negras" -> "negro") para que encuentre "Clásico Negro".
        for (java.util.Map.Entry<String, String> e : COLORES_CANONICOS.entrySet()) {
            String aliasPlano = Normalizer.normalize(e.getKey(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            if (qPlano.contains(aliasPlano)) {
                filtroColor = e.getValue();
                hayFiltroAsistente = true;
                break;
            }
        }

        // "menos de 150", "hasta 150", "bajo 150", "maximo 150"
        java.util.regex.Matcher mMax = java.util.regex.Pattern
            .compile("(menos\\s+de|hasta|maximo|max|bajo|menor\\s+a)\\s*(s\\/)?\\s*(\\d+(?:\\.\\d+)?)")
            .matcher(qPlano);
        if (mMax.find()) {
            filtroPrecioMax = Double.parseDouble(mMax.group(3));
            // "menos de 150" excluye el 150; "hasta 150" y "maximo 150" lo incluyen.
            precioMaxEstricto = qPlano.substring(mMax.start(), mMax.end()).startsWith("menos")
                    || qPlano.substring(mMax.start(), mMax.end()).startsWith("menor");
            hayFiltroAsistente = true;
        }
        // "mas de 150", "desde 150", "minimo 150", "sobre 150"
        java.util.regex.Matcher mMin = java.util.regex.Pattern
            .compile("(mas\\s+de|desde|minimo|sobre)\\s*(s\\/)?\\s*(\\d+(?:\\.\\d+)?)")
            .matcher(qPlano);
        if (mMin.find()) {
            filtroPrecioMin = Double.parseDouble(mMin.group(3));
            hayFiltroAsistente = true;
        }

        // "talla 42"
        java.util.regex.Matcher mt = java.util.regex.Pattern
            .compile("talla\\s*(\\d{2})").matcher(qPlano);
        if (mt.find()) {
            filtroTalla = mt.group(1);
            hayFiltroAsistente = true;
        }
    }

    /**
     * Si la consulta menciona una categoria, deja esa pestana activa.
     * Si NO menciona ninguna, conserva la que ya habia ("zapatos talla 44" y luego
     * "en negros" debe seguir en la misma categoria, no volver a "Todos").
     */
    private void Aplicar_Categoria_Detectada(String consulta) {
        String q = Normalizer.normalize(consulta.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        if (q.contains("bota")) {
            currentCategory = "Botas";
        } else if (q.contains("mocasin")) {
            currentCategory = "Mocasines";
        } else if (q.contains("casual")) {
            currentCategory = "Zapatos casuales";
        } else if (q.contains("vestir") || q.contains("formal") || q.contains("elegante")) {
            currentCategory = "Zapatos de vestir";
        } else if (q.matches(".*\\b(ver\\s+todo|muestra(me)?\\s+todo|todo\\s+el\\s+catalogo)\\b.*")) {
            currentCategory = "Todos";
        }
        // Sin mencion de categoria: se mantiene la actual a proposito (contexto).
        Sincronizar_Pestana_Visual();
    }

    /** Pinta la pestana activa luego de un cambio programático de categoría. */
    private void Sincronizar_Pestana_Visual() {
        if (tabsPanelRef == null) return;
        for (Component c : tabsPanelRef.getComponents()) {
            if (c instanceof JButton) {
                boolean activo = ((JButton) c).getText().equals(currentCategory);
                ((JButton) c).setForeground(activo ? Gestor_De_Temas.getTextColor() : Gestor_De_Temas.getMutedColor());
                ((JButton) c).setBackground(activo ? Gestor_De_Temas.getAccentColor() : Gestor_De_Temas.getBgColor());
            }
        }
    }

    /** Muestra u oculta el chip que indica el filtro activo del asistente. */
    private void Actualizar_Chip_Filtro() {
        if (lblFiltroAsistente == null) return;
        if (!hayFiltroAsistente) {
            lblFiltroAsistente.setVisible(false);
            return;
        }
        filtroTextoChip = Descripcion_Filtro_Activo();
        chipLabel.setText(filtroTextoChip.isEmpty() ? "Filtro: todos" : "Filtro: " + filtroTextoChip);
        lblFiltroAsistente.setVisible(true);
    }

    private String capitalizar(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Limpia los filtros del asistente (boton X del chip). */
    private void Limpiar_Filtro_Asistente() {
        filtroColor = null;
        filtroPrecioMax = null;
        filtroPrecioMin = null;
        filtroTalla = null;
        hayFiltroAsistente = false;
        filtroTextoChip = "";
        consultaOriginal = "";
        precioMaxEstricto = false;
        currentCategory = "Todos";
        Sincronizar_Pestana_Visual();
        if (lblFiltroAsistente != null) lblFiltroAsistente.setVisible(false);
        aplicarFiltros();
    }

    /** Texto que el modelo recibe como resultado de la herramienta. */
    private String Resumen_Productos_Filtrados() {
        // Se le devuelve al modelo el filtro COMPLETO (acumulado), para que su respuesta
        // mencione talla y color a la vez y no repita solo lo de la ultima frase.
        StringBuilder cab = new StringBuilder("Catalogo de Ventas filtrado");
        String desc = Descripcion_Filtro_Activo();
        if (!desc.isEmpty()) cab.append(" (").append(desc).append(")");
        cab.append(": ");

        List<Producto> encontrados = productosMostrados;
        if (encontrados.isEmpty()) {
            return cab.append("0 productos. No hay resultados con ese filtro.").toString();
        }
        // "el mas barato" / "el mas caro": se ordena y se prioriza el extremo.
        // Se lee de la consulta original: el chip solo describe color/precio/talla.
        String q = consultaOriginal != null ? consultaOriginal.toLowerCase() : "";
        List<Producto> copia = new ArrayList<>(encontrados);
        if (q.contains("barat")) {
            copia.sort((a, b) -> Double.compare(a.Get_Precio(), b.Get_Precio()));
        } else if (q.contains("caro")) {
            copia.sort((a, b) -> Double.compare(b.Get_Precio(), a.Get_Precio()));
        }

        int max = Math.min(copia.size(), 6);
        StringBuilder sb = new StringBuilder(cab);
        sb.append(copia.size()).append(" producto(s). ");
        for (int i = 0; i < max; i++) {
            Producto p = copia.get(i);
            sb.append(p.Get_Nombre()).append(" S/ ").append(String.format("%.2f", p.Get_Precio()));
            // La talla se incluye porque el filtro puede venir de un turno anterior.
            if (filtroTalla != null && p.Get_Tallas() != null && !p.Get_Tallas().trim().isEmpty()) {
                sb.append(" (tallas ").append(p.Get_Tallas()).append(")");
            }
            if (i < max - 1) sb.append(", ");
        }
        if (copia.size() > max) sb.append(", y ").append(copia.size() - max).append(" mas.");
        return sb.toString();
    }

    /** Descripcion legible de TODOS los filtros activos (acumulados). */
    private String Descripcion_Filtro_Activo() {
        List<String> partes = new ArrayList<>();
        if (filtroColor != null) partes.add(capitalizar(filtroColor));
        if (filtroPrecioMax != null) {
            partes.add((precioMaxEstricto ? "menos de S/ " : "hasta S/ ")
                    + String.format("%.0f", filtroPrecioMax));
        }
        if (filtroPrecioMin != null) partes.add("desde S/ " + String.format("%.0f", filtroPrecioMin));
        if (filtroTalla != null) partes.add("talla " + filtroTalla);
        if (currentCategory != null && !currentCategory.equals("Todos")) partes.add(currentCategory);
        return String.join(" + ", partes);
    }

    public String Agregar_Producto_Al_Carrito(String busqueda, int cantidad) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return "Indica el nombre o código del producto.";
        }
        cantidad = Math.max(1, cantidad);
        todosLosProductos = productoDAO.Obtener_Todos();
        Producto producto = buscarProductoFlexible(busqueda);
        if (producto == null) {
            return "No encontré un producto parecido a: " + busqueda;
        }
        if (producto.Get_Stock() <= 0 || "Agotado".equalsIgnoreCase(producto.Get_Estado())) {
            return "El producto " + producto.Get_Nombre() + " está agotado.";
        }
        int agregados = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!Agregar_Producto_Al_Carrito(producto, false)) break;
            agregados++;
        }
        if (agregados == 0) {
            return "No hay stock suficiente para agregar " + producto.Get_Nombre() + ".";
        }
        return "Agregado al carrito: " + agregados + " x " + producto.Get_Nombre() + ".";
    }

    public String Vaciar_Carrito() {
        if (tableModel.getRowCount() == 0) {
            return "El carrito ya está vacío.";
        }
        tableModel.setRowCount(0);
        updateTotals();
        return "Carrito vaciado correctamente.";
    }

    public String Abrir_Panel_De_Pago() {
        if (tableModel.getRowCount() == 0) {
            return "El carrito está vacío. Agrega productos antes de cobrar.";
        }
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (double) tableModel.getValueAt(i, 3);
        }
        total *= 1.18;
        SwingUtilities.invokeLater(this::procesarVenta);
        return String.format(
                "Se procede a realizar el cobro por un total de S/ %.2f. "
                + "En el panel de pago, selecciona el método, completa los datos solicitados "
                + "y pulsa «CONFIRMAR E INTEGRAR VENTA» después de verificar el pago. "
                + "La venta solo quedará registrada cuando la confirmes.", total);
    }

    private Producto buscarProductoFlexible(String busqueda) {
        String q = normalizar(busqueda);
        Producto mejor = null;
        int mejorScore = 0;
        for (Producto p : todosLosProductos) {
            String nombre = normalizar(p.Get_Nombre());
            String codigo = normalizar(p.Get_Codigo());
            String categoria = normalizar(p.Get_Categoria());
            if (codigo.equals(q) || nombre.equals(q)) return p;
            int score = 0;
            if (nombre.contains(q) || q.contains(nombre)) score += 100;
            if (codigo.contains(q)) score += 80;
            if (categoria.contains(q)) score += 20;
            for (String token : q.split("\\s+")) {
                if (token.length() < 3) continue;
                if (nombre.contains(token)) score += 10;
                if (categoria.contains(token)) score += 3;
            }
            if (score > mejorScore) {
                mejorScore = score;
                mejor = p;
            }
        }
        return mejorScore >= 20 ? mejor : null;
    }

    private String normalizar(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\b(el|la|los|las|un|una|al|del|por|favor|carrito|agrega|agregar|anade|añade|pon|mete)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean Agregar_Producto_Al_Carrito(Producto prod, boolean mostrarAviso) {
        boolean found = false;
        int actualEnCarrito = 0;
        int rowIdx = -1;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((int)tableModel.getValueAt(i, 0) == prod.Get_Id()) {
                actualEnCarrito = (int) tableModel.getValueAt(i, 1);
                rowIdx = i;
                found = true;
                break;
            }
        }

        if (actualEnCarrito >= prod.Get_Stock()) {
            if (mostrarAviso) {
                JOptionPane.showMessageDialog(Panel_De_Ventas.this, "No hay suficiente stock disponible para este producto.", "Sin Stock", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }

        if (!found) {
            tableModel.addRow(new Object[] { prod.Get_Id(), 1, prod.Get_Nombre(), prod.Get_Precio(), "" });
        } else {
            int qty = actualEnCarrito + 1;
            tableModel.setValueAt(qty, rowIdx, 1);
            tableModel.setValueAt(qty * prod.Get_Precio(), rowIdx, 3);
        }
        updateTotals();
        return true;
    }

    private void aplicarFiltros() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        List<Producto> filtrados = new ArrayList<>();
        
        for (Producto p : todosLosProductos) {
            boolean matchesSearch = searchText.isEmpty() || 
                                    p.Get_Nombre().toLowerCase().contains(searchText) || 
                                    (p.Get_Codigo() != null && p.Get_Codigo().toLowerCase().contains(searchText));
            boolean matchesCat = currentCategory.equals("Todos") || 
                                 (p.Get_Categoria() != null && p.Get_Categoria().equalsIgnoreCase(currentCategory));
            
            if (matchesSearch && matchesCat && Cumple_Filtro_Asistente(p)) {
                filtrados.add(p);
            }
        }
        // "el mas barato" / "el mas caro": se ordena tambien en pantalla, no solo en el texto.
        String q = consultaOriginal != null ? consultaOriginal.toLowerCase() : "";
        if (q.contains("barat")) {
            filtrados.sort((a, b) -> Double.compare(a.Get_Precio(), b.Get_Precio()));
        } else if (q.contains("caro") || q.contains("precio")) {
            filtrados.sort((a, b) -> Double.compare(b.Get_Precio(), a.Get_Precio()));
        }
        cargarProductos(filtrados);
    }

    /** Aplica color, precio y talla que el asistente haya deducido de la pregunta. */
    private boolean Cumple_Filtro_Asistente(Producto p) {
        if (!hayFiltroAsistente) return true;

        if (filtroColor != null) {
            String nombre = normalizar(p.Get_Nombre());
            if (!nombre.contains(normalizar(filtroColor))) return false;
        }
        if (filtroPrecioMax != null) {
            if (precioMaxEstricto) {
                if (p.Get_Precio() >= filtroPrecioMax) return false;
            } else if (p.Get_Precio() > filtroPrecioMax) {
                return false;
            }
        }
        if (filtroPrecioMin != null && p.Get_Precio() < filtroPrecioMin) return false;
        if (filtroTalla != null) {
            // Comparacion EXACTA por token: si no, "44" apareceria dentro de "40,41"
            // o "38,39,40,41,42" y el filtro dejaria pasar productos que no la tienen.
            String[] disponibles = (p.Get_Tallas() != null ? p.Get_Tallas() : "").split("[,\\s]+");
            boolean tieneTalla = false;
            for (String t : disponibles) {
                if (t.trim().equals(filtroTalla)) { tieneTalla = true; break; }
            }
            if (!tieneTalla) return false;
        }
        return true;
    }

    private void cargarProductos(List<Producto> productos) {
        this.productosMostrados = productos;
        gridPanel.removeAll();
        
        for (Producto prod : productos) {
            gridPanel.add(new ProductCard(prod, () -> {
                Agregar_Producto_Al_Carrito(prod, true);
            }));
        }
        
        SwingUtilities.invokeLater(() -> {
            recalcularAlturaGrid();
        });
    }

    private void recalcularAlturaGrid() {
        if (productosMostrados == null || gridPanel == null || scrollGrid == null) return;
        int containerWidth = scrollGrid.getViewport().getWidth();
        if (containerWidth <= 0) {
            containerWidth = scrollGrid.getWidth();
        }
        if (containerWidth <= 0) {
            containerWidth = 800; // Ancho inicial de respaldo
        }
        
        int cardWidth = 280;
        int gap = 15;
        
        // Calcular cuántas columnas caben físicamente en el ancho actual
        int columns = Math.max(1, (containerWidth - gap) / (cardWidth + gap));
        int rows = (int) Math.ceil((double) productosMostrados.size() / columns);
        
        int cardHeight = 230;
        int calculatedHeight = Math.max(150, rows * (cardHeight + gap) + gap);
        
        Dimension currentDim = gridPanel.getPreferredSize();
        if (currentDim.width != containerWidth || currentDim.height != calculatedHeight) {
            gridPanel.setPreferredSize(new Dimension(containerWidth, calculatedHeight));
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    @Override
    public void doLayout() {
        if (leftPanel != null) {
            if (getWidth() < 1200) {
                leftPanel.setPreferredSize(new Dimension(320, 0));
            } else {
                leftPanel.setPreferredSize(new Dimension(440, 0));
            }
        }
        super.doLayout();
        recalcularAlturaGrid();
    }

    private void procesarVenta() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (double) tableModel.getValueAt(i, 3);
        }
        total = total * 1.18; // Incluyendo IGV

        Venta venta = new Venta();
        venta.Set_Cajero("Admin"); // Usuario por defecto
        venta.Set_Monto_Total(total);
        venta.Set_Estado("Pendiente");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int idProd = (int) tableModel.getValueAt(i, 0);
            int cant = (int) tableModel.getValueAt(i, 1);
            String nombre = (String) tableModel.getValueAt(i, 2);
            double subTot = (double) tableModel.getValueAt(i, 3);
            double pUnit = subTot / cant;

            venta.Get_Detalles().add(new Detalle_De_Venta(idProd, nombre, cant, pUnit, subTot));
        }

        // Obtener el Frame superior para centrar diálogos modales
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (ancestor instanceof Frame) ? (Frame) ancestor : null;

        // Abrir panel modal de Pago
        Dialogo_De_Pago pagoDialog = new Dialogo_De_Pago(parentFrame, venta);
        pagoDialog.setVisible(true);

        if (pagoDialog.Esta_Pago_Completado()) {
            // Mostrar comprobante tipo ticket térmico
            Dialogo_De_Ticket ticketDialog = new Dialogo_De_Ticket(parentFrame, venta);
            ticketDialog.setVisible(true);

            // Vaciar carrito
            tableModel.setRowCount(0);
            updateTotals();
            
            // Refrescar stock y estados del catálogo
            todosLosProductos = productoDAO.Obtener_Todos();
            aplicarFiltros();
        }
    }

    private class ProductCard extends JPanel {
        private boolean hovered = false;
        private final Producto prod;

        public ProductCard(Producto prod, Runnable onClick) {
            this.prod = prod;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            setPreferredSize(new Dimension(280, 230)); // Tamaño fijo por tarjeta

            boolean isAgotado = prod.Get_Stock() <= 0 || "Agotado".equalsIgnoreCase(prod.Get_Estado());

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);
            
            JLabel iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(100, 100));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Set default icon first using cached default icon
            java.net.URL defaultUrl = getClass().getResource("/images/default_shoe.png");
            javax.swing.ImageIcon cachedDefault = getDefaultShoeIcon(defaultUrl);
            if (cachedDefault != null) {
                iconLabel.setText("");
                iconLabel.setIcon(cachedDefault);
            } else {
                iconLabel.setIcon(new Icono_Elegante(Icono_Elegante.Type.SHOE, 72, isAgotado ? Color.GRAY : Gestor_De_Temas.getAccentColor()));
            }
            
            // Load image async with caching and thread pooling
            if (prod.Get_Url_Imagen() != null && !prod.Get_Url_Imagen().trim().isEmpty()) {
                String imgPath = prod.Get_Url_Imagen().trim();
                
                // 1. Check cache first
                if (IMAGE_CACHE.containsKey(imgPath)) {
                    iconLabel.setText("");
                    iconLabel.setIcon(IMAGE_CACHE.get(imgPath));
                } else {
                    // 2. Load async using thread pool
                    IMAGE_LOAD_EXECUTOR.submit(() -> {
                        try {
                            String fName = imgPath;
                            if (fName.contains("/")) fName = fName.substring(fName.lastIndexOf("/") + 1);
                            if (fName.contains("\\")) fName = fName.substring(fName.lastIndexOf("\\") + 1);

                            java.awt.Image image = null;

                            // 1. Intentar cargar desde el Classpath (Ideal si está compilado)
                            java.net.URL url = getClass().getResource("/images/" + fName);
                            if (url != null) {
                                try { image = javax.imageio.ImageIO.read(url); } catch (Exception ignored) {}
                            }

                            // 2. Si falla, intentar cargar desde la carpeta portable (Datos_SenatiZapato/imagenes/)
                            if (image == null) {
                                String basePath = System.getProperty("user.dir");
                                if (basePath.endsWith("target") || basePath.endsWith("target" + java.io.File.separator)) {
                                    basePath = new java.io.File(basePath).getParent();
                                }
                                java.io.File portableFile = new java.io.File(basePath, "Datos_SenatiZapato" + java.io.File.separator + "imagenes" + java.io.File.separator + fName);
                                if (portableFile.exists()) {
                                    try { image = javax.imageio.ImageIO.read(portableFile); } catch (Exception ignored) {}
                                }
                            }

                            // 3. Si falla, intentar cargar desde el código fuente directamente (Ideal para NetBeans sin compilar)
                            if (image == null) {
                                String basePath = System.getProperty("user.dir");
                                if (basePath.endsWith("target") || basePath.endsWith("target" + java.io.File.separator)) {
                                    basePath = new java.io.File(basePath).getParent();
                                }
                                java.io.File devFile = new java.io.File(basePath, "src/main/resources/images/" + fName);
                                if (devFile.exists()) {
                                    try { image = javax.imageio.ImageIO.read(devFile); } catch (Exception ignored) {}
                                }
                            }

                            // 4. Si falla, intentar cargar desde la ruta absoluta o relativa cruda
                            if (image == null) {
                                java.io.File rawFile = new java.io.File(imgPath);
                                if (rawFile.exists()) {
                                    try { image = javax.imageio.ImageIO.read(rawFile); } catch (Exception ignored) {}
                                }
                            }
                            
                            // Si se pudo cargar la imagen, actualizar la UI
                            if (image != null) {
                                java.awt.Image scaled = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(scaled);
                                
                                // Guardar en caché
                                IMAGE_CACHE.put(imgPath, icon);
                                
                                // Actualizar el componente gráfico
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    iconLabel.setText("");
                                    iconLabel.setIcon(icon);
                                    iconLabel.revalidate();
                                    iconLabel.repaint();
                                });
                            } else {
                                System.err.println(">> ALERTA: No se encontró la imagen en ninguna ruta: " + fName);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error loading image for " + prod.Get_Nombre() + " (" + imgPath + "): " + ex.getMessage());
                        }
                    });
                }
            }
            
            JLabel lblPrice = new JLabel(String.format("S/ %.2f", prod.Get_Precio()));
            lblPrice.setFont(new Font("Georgia", Font.BOLD, 17));
            lblPrice.setForeground(isAgotado ? Color.GRAY : Gestor_De_Temas.getAccentColor());
            
            headerPanel.add(iconLabel, BorderLayout.CENTER);
            headerPanel.add(lblPrice, BorderLayout.EAST);
            add(headerPanel);
            add(Box.createRigidArea(new Dimension(0, 10)));

            JLabel lblName = new JLabel("<html><div style='text-align: center; width: 200px;'>" + prod.Get_Nombre() + "</div></html>");
            lblName.setFont(new Font("Georgia", Font.BOLD, 16));
            lblName.setForeground(isAgotado ? Gestor_De_Temas.getMutedColor() : Gestor_De_Temas.getTextColor());
            lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblName);
            add(Box.createRigidArea(new Dimension(0, 5)));

            // Tallas text
            String tallasText = (prod.Get_Tallas() != null && !prod.Get_Tallas().isEmpty()) ? prod.Get_Tallas() : "Única";
            JLabel lblTallas = new JLabel("Tallas: " + tallasText, SwingConstants.CENTER);
            lblTallas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTallas.setForeground(Gestor_De_Temas.getMutedColor());
            lblTallas.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblTallas);
            add(Box.createVerticalGlue());

            // Badge / Icono de agregar
            if (isAgotado) {
                JLabel lblAgotadoBadge = new JLabel("AGOTADO", SwingConstants.CENTER);
                lblAgotadoBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblAgotadoBadge.setForeground(new Color(220, 53, 69)); // Rojo
                lblAgotadoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(lblAgotadoBadge);
            } else {
                JLabel lblAdd = new JLabel("", SwingConstants.CENTER);
                lblAdd.setIcon(new Icono_Elegante(Icono_Elegante.Type.ADD, 22, new Color(79, 133, 87)));
                lblAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(lblAdd);
            }

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isAgotado) {
                        hovered = true;
                        repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isAgotado) {
                        onClick.run();
                    } else {
                        JOptionPane.showMessageDialog(Panel_De_Ventas.this, "El producto '" + prod.Get_Nombre() + "' se encuentra agotado temporalmente.", "Sin Stock", JOptionPane.WARNING_MESSAGE);
                    }
                }
            };

            addMouseListener(ma);
            for (Component c : getComponents()) c.addMouseListener(ma);
            headerPanel.addMouseListener(ma);
            iconLabel.addMouseListener(ma);
            lblPrice.addMouseListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean isAgotado = prod.Get_Stock() <= 0 || "Agotado".equalsIgnoreCase(prod.Get_Estado());

            if (isAgotado) {
                g2.setColor(Gestor_De_Temas.isDarkMode() ? new Color(38, 28, 24) : new Color(242, 237, 233));
            } else if (hovered) {
                g2.setColor(Gestor_De_Temas.getHoverColor());
            } else {
                g2.setColor(Gestor_De_Temas.getPanelBgColor());
            }
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            
            if (isAgotado) {
                g2.setColor(new Color(220, 53, 69, 100)); // Borde rojo tenue
            } else {
                g2.setColor(Gestor_De_Temas.getBorderColor());
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            Color acc = isAgotado ? Color.GRAY : Gestor_De_Temas.getAccentColor();
            g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), isAgotado ? 50 : 100));
            float[] dash = {4.0f, 4.0f};
            g2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 0, dash, 0));
            g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 12, 12);

            if (isAgotado) {
                g2.setColor(new Color(0, 0, 0, 20)); // Capa sutil translúcida
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
