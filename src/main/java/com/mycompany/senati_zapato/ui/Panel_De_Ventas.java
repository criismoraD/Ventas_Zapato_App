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

        JLabel lblTicketHeader = new JLabel("Lista de Compras", SwingConstants.CENTER);
        lblTicketHeader.setFont(new Font("Georgia", Font.BOLD, 22));
        lblTicketHeader.setForeground(Gestor_De_Temas.getTextColor());
        lblTicketHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        leftPanel.add(lblTicketHeader, BorderLayout.NORTH);

        String[] columns = { "ID", "Cant", "Producto", "Total", "X" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable cartTable = new JTable(tableModel);
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
                aplicarFiltros();
            }
        });
        
        headerRightPanel.add(txtSearch, BorderLayout.NORTH);

        // Tabs de Filtros (Categorías de Zapatos)
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabsPanel.setOpaque(false);
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
        SwingUtilities.invokeLater(this::procesarVenta);
        return "Abriendo panel de pago...";
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
        String searchText = txtSearch.getText().toLowerCase();
        List<Producto> filtrados = new ArrayList<>();
        
        for (Producto p : todosLosProductos) {
            boolean matchesSearch = p.Get_Nombre().toLowerCase().contains(searchText) || 
                                    (p.Get_Codigo() != null && p.Get_Codigo().toLowerCase().contains(searchText));
            boolean matchesCat = currentCategory.equals("Todos") || 
                                 (p.Get_Categoria() != null && p.Get_Categoria().equalsIgnoreCase(currentCategory));
                                 
            if (matchesSearch && matchesCat) {
                filtrados.add(p);
            }
        }
        cargarProductos(filtrados);
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
