package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Panel_De_Inventario extends javax.swing.JPanel {

    private void addHoverEffect(JButton btn, Color normalColor, Color hoverColor) {
        btn.putClientProperty("currentColor", normalColor);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.putClientProperty("currentColor", hoverColor);
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.putClientProperty("currentColor", normalColor);
                btn.repaint();
            }
        });
    }

    private JTable table;
    private DefaultTableModel tableModel;
    private Dao_De_Producto productoDAO;
    private JButton btnSelectAll;

    public Panel_De_Inventario() {
        productoDAO = new Dao_De_Producto();
        initComponents();
        initCustomUI();
        cargarDatos();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout(0, 20));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        toolbar.setLayout(new java.awt.BorderLayout());
        add(toolbar, java.awt.BorderLayout.NORTH);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void initCustomUI() {
        setOpaque(false);
        toolbar.setOpaque(false);

        JPanel leftActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton btnNew = new JButton(" Nuevo Producto") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : Gestor_De_Temas.getAccentColor();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                
                Color bg = Gestor_De_Temas.getBgColor();
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 100));
                float[] btnDash = {3.0f, 3.0f};
                g2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, btnDash, 0.0f));
                g2.drawRoundRect(4, 4, w - 8, h - 8, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnNew.setContentAreaFilled(false);
        btnNew.setBorderPainted(false);
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Inter", Font.BOLD, 16));
        btnNew.setFocusPainted(false);
        btnNew.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNew.setIcon(new Icono_Elegante(Icono_Elegante.Type.ADD, 20, Color.WHITE));

        JButton btnEdit = new JButton(" Editar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : new Color(240, 173, 78);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Inter", Font.BOLD, 16));
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEdit.setIcon(new Icono_Elegante(Icono_Elegante.Type.BOX, 20, Color.WHITE));

        JButton btnDelete = new JButton(" Eliminar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : new Color(220, 53, 69);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Inter", Font.BOLD, 16));
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDelete.setIcon(new Icono_Elegante(Icono_Elegante.Type.TRASH, 20, Color.WHITE));

        addHoverEffect(btnNew, Gestor_De_Temas.getAccentColor(), Gestor_De_Temas.getAccentColor().brighter());
        addHoverEffect(btnEdit, new Color(240, 173, 78), new Color(250, 185, 95));
        addHoverEffect(btnDelete, new Color(220, 53, 69), new Color(235, 75, 90));

        btnSelectAll = new JButton(" Seleccionar Todo") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                Color c = getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : Gestor_De_Temas.getAccentColor();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSelectAll.setContentAreaFilled(false);
        btnSelectAll.setBorderPainted(false);
        btnSelectAll.setForeground(Color.WHITE);
        btnSelectAll.setFont(new Font("Inter", Font.BOLD, 16));
        btnSelectAll.setFocusPainted(false);
        btnSelectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSelectAll.setIcon(new Icono_Elegante(Icono_Elegante.Type.ADD, 20, Color.WHITE));
        btnSelectAll.setVisible(false);
        addHoverEffect(btnSelectAll, Gestor_De_Temas.getAccentColor(), Gestor_De_Temas.getAccentColor().brighter());

        btnSelectAll.addActionListener(e -> {
            boolean todosSeleccionados = true;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Boolean val = (Boolean) tableModel.getValueAt(i, 0);
                if (val == null || !val) {
                    todosSeleccionados = false;
                    break;
                }
            }
            
            boolean target = !todosSeleccionados;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(target, i, 0);
            }
            actualizarEstadoBotonSeleccion();
        });

        btnNew.addActionListener(e -> mostrarFormulario(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para editar");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 1);
            Producto p = productoDAO.obtenerPorId(id);
            if (p != null) {
                mostrarFormulario(p);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo cargar el producto desde la base de datos.");
            }
        });
        
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 1);
            int conf = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                productoDAO.eliminar(id);
                cargarDatos();
            }
        });

        leftActions.add(btnNew);
        leftActions.add(btnEdit);
        leftActions.add(btnDelete);
        leftActions.add(btnSelectAll);
        toolbar.add(leftActions, java.awt.BorderLayout.WEST);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(375, 45));
        txtSearch.setFont(new Font("Inter", Font.PLAIN, 16));
        txtSearch.putClientProperty("JTextField.placeholderText", "Buscar producto por nombre o código...");
        txtSearch.putClientProperty("JTextField.leadingIcon", new Icono_Elegante(Icono_Elegante.Type.SEARCH, 18, Gestor_De_Temas.getMutedColor()));
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        txtSearch.putClientProperty("JComponent.roundRect", true);
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            
            private void filtrar() {
                String query = txtSearch.getText().toLowerCase().trim();
                tableModel.setRowCount(0);
                List<Producto> productos = productoDAO.Obtener_Todos();
                for (Producto p : productos) {
                    boolean matches = p.Get_Nombre().toLowerCase().contains(query) ||
                                      (p.Get_Codigo() != null && p.Get_Codigo().toLowerCase().contains(query)) ||
                                      (p.Get_Categoria() != null && p.Get_Categoria().toLowerCase().contains(query));
                    if (matches) {
                        tableModel.addRow(new Object[]{
                            Boolean.FALSE,
                            p.Get_Id(),
                            p.Get_Codigo(),
                            p.Get_Nombre(),
                            p.Get_Categoria(),
                            p.Get_Stock(),
                            String.format("S/ %.2f", p.Get_Precio())
                        });
                    }
                }
                actualizarEstadoBotonSeleccion();
            }
        });
        
        toolbar.add(txtSearch, java.awt.BorderLayout.EAST);

        String[] columns = {"", "ID DB", "Código", "Nombre", "Categoria", "Stock", "Precio"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE || e.getType() == javax.swing.event.TableModelEvent.INSERT || e.getType() == javax.swing.event.TableModelEvent.DELETE) {
                actualizarEstadoBotonSeleccion();
            }
        });

        table = new JTable(tableModel);
        table.setRowHeight(55);
        table.setBackground(Gestor_De_Temas.getPanelBgColor());
        table.setForeground(Gestor_De_Temas.getTextColor());
        table.setFont(new Font("Inter", Font.PLAIN, 15));
        table.setGridColor(new Color(230, 225, 215));
        table.getTableHeader().setBackground(Gestor_De_Temas.getAccentColor());
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 15));
        table.getTableHeader().setPreferredSize(new Dimension(100, 45));

        // 1. Evitar seleccionar varios productos (Selección Única) y 2. Comportamiento de "Interruptor" (Toggle)
        javax.swing.DefaultListSelectionModel selectionModel = new javax.swing.DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (index0 == index1) {
                    if (isSelectedIndex(index0)) {
                        removeSelectionInterval(index0, index0);
                        return;
                    }
                }
                super.setSelectionInterval(index0, index1);
            }
        };
        selectionModel.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionModel(selectionModel);
        
        // 3. Deseleccionar al hacer clic en área vacía
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) == -1) {
                    table.clearSelection();
                }
            }
        });

        javax.swing.table.DefaultTableCellRenderer cellRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                
                Color bg;
                if (!isSelected) {
                    bg = row % 2 == 0 ? Gestor_De_Temas.getPanelBgColor() : new Color(248, 245, 240);
                } else {
                    bg = new Color(215, 225, 215);
                }
                c.setBackground(bg);
                
                double luma = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
                if (luma < 128) {
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(new Color(30, 30, 30));
                }
                
                return c;
            }
        };
        // Omitir columna 0 (checkboxes)
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        
        // Dimensionamiento de columnas
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setMinWidth(40);

        scrollPane.setViewportView(table);
        scrollPane.getViewport().setBackground(Gestor_De_Temas.getPanelBgColor());
        scrollPane.setBorder(BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1));
    }

    public void Refrescar_Tabla() {
        cargarDatos();
    }

    private void cargarDatos() {
        tableModel.setRowCount(0);
        List<Producto> productos = productoDAO.Obtener_Todos();
        for (Producto p : productos) {
            tableModel.addRow(new Object[]{
                Boolean.FALSE,
                p.Get_Id(),
                p.Get_Codigo(),
                p.Get_Nombre(),
                p.Get_Categoria(),
                p.Get_Stock(),
                String.format("S/ %.2f", p.Get_Precio())
            });
        }
        actualizarEstadoBotonSeleccion();
    }

    private void actualizarEstadoBotonSeleccion() {
        if (btnSelectAll == null) return;
        boolean algoSeleccionado = false;
        boolean todosSeleccionados = true;
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            todosSeleccionados = false;
        } else {
            for (int i = 0; i < rowCount; i++) {
                Boolean val = (Boolean) tableModel.getValueAt(i, 0);
                if (val != null && val) {
                    algoSeleccionado = true;
                } else {
                    todosSeleccionados = false;
                }
            }
        }
        
        btnSelectAll.setVisible(algoSeleccionado);
        if (todosSeleccionados) {
            btnSelectAll.setText(" Deseleccionar Todo");
            btnSelectAll.setIcon(new Icono_Elegante(Icono_Elegante.Type.CLOSE, 20, Color.WHITE));
        } else {
            btnSelectAll.setText(" Seleccionar Todo");
            btnSelectAll.setIcon(new Icono_Elegante(Icono_Elegante.Type.ADD, 20, Color.WHITE));
        }
        toolbar.revalidate();
        toolbar.repaint();
    }

    private void mostrarFormulario(Producto producto) {
        javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Window) javax.swing.SwingUtilities.getWindowAncestor(this));
        dialog.setTitle(producto == null ? "Nuevo Producto" : "Editar Producto");
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(Gestor_De_Temas.getPanelBgColor());
        
        JTextField txtCodigo = new JTextField(producto != null ? producto.Get_Codigo() : "");
        JTextField txtNombre = new JTextField(producto != null ? producto.Get_Nombre() : "");
        
        javax.swing.JComboBox<String> cbCategoria = new javax.swing.JComboBox<>(new String[]{
            "Seleccionar...", "Mocasines", "Botas", "Zapatillas", "Sandalias", "Deportivos", "Urbanos", "Elegantes"
        });
        cbCategoria.setBackground(Color.WHITE);
        if (producto != null && producto.Get_Categoria() != null) {
            boolean found = false;
            for (int i=0; i<cbCategoria.getItemCount(); i++) {
                if (cbCategoria.getItemAt(i).equalsIgnoreCase(producto.Get_Categoria())) {
                    cbCategoria.setSelectedIndex(i);
                    found = true; break;
                }
            }
            if (!found) {
                cbCategoria.addItem(producto.Get_Categoria());
                cbCategoria.setSelectedItem(producto.Get_Categoria());
            }
        }
        
        JTextField txtStock = new JTextField(producto != null ? String.valueOf(producto.Get_Stock()) : "0");
        JTextField txtPrecio = new JTextField(producto != null ? String.valueOf(producto.Get_Precio()) : "0.0");
        
        JTextField txtUrlImagen = new JTextField(producto != null && producto.Get_Url_Imagen() != null ? producto.Get_Url_Imagen() : "");
        txtUrlImagen.setEditable(false);
        JButton btnSeleccionarImagen = new JButton("...");
        btnSeleccionarImagen.setFont(new Font("Inter", Font.BOLD, 14));
        btnSeleccionarImagen.setBackground(Gestor_De_Temas.getAccentColor());
        btnSeleccionarImagen.setForeground(Color.WHITE);
        btnSeleccionarImagen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSeleccionarImagen.setPreferredSize(new Dimension(45, 38));
        btnSeleccionarImagen.setFocusPainted(false);
        
        JLabel lblPreview = new JLabel("Sin imagen");
        lblPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreview.setBorder(BorderFactory.createEmptyBorder());
        lblPreview.setOpaque(false);
        
        java.util.function.Consumer<String> updatePreview = path -> {
            lblPreview.setIcon(null);
            lblPreview.setText("");
            if (path != null && !path.trim().isEmpty()) {
                try {
                    java.io.File file = new java.io.File(path);
                    javax.swing.ImageIcon icon = null;
                    
                    if (file.exists()) {
                        icon = new javax.swing.ImageIcon(file.getAbsolutePath());
                    } else {
                        // Check portable imagenes folder
                        String portableDir = System.getProperty("user.dir") + java.io.File.separator + "Datos_SenatiZapato" + java.io.File.separator + "imagenes";
                        java.io.File portableFile = new java.io.File(portableDir, file.getName());
                        if (portableFile.exists()) {
                            icon = new javax.swing.ImageIcon(portableFile.getAbsolutePath());
                        } else {
                            String fileName = file.getName();
                            java.net.URL url = getClass().getResource("/images/" + fileName);
                            if (url != null) {
                                icon = new javax.swing.ImageIcon(url);
                            }
                        }
                    }
                    
                    if (icon != null && icon.getIconWidth() > 0) {
                        // Obtener dimensiones reales de la imagen
                        int imgW = icon.getIconWidth();
                        int imgH = icon.getIconHeight();

                        // Limitar a un máximo de 280px de ancho o 220px de alto
                        // manteniendo la proporción original
                        int maxW = 280;
                        int maxH = 220;
                        double ratio = Math.min((double) maxW / imgW, (double) maxH / imgH);
                        int newW = (int) (imgW * ratio);
                        int newH = (int) (imgH * ratio);

                        java.awt.Image scaled = icon.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
                        javax.swing.ImageIcon scaledIcon = new javax.swing.ImageIcon(scaled);

                        // Ajustar el tamaño del label exactamente al de la imagen escalada
                        lblPreview.setPreferredSize(new Dimension(newW, newH));
                        lblPreview.setIcon(scaledIcon);
                        lblPreview.setText("");

                        // Forzar redibujado del diálogo para que se ajuste
                        dialog.pack();
                    } else {
                        lblPreview.setPreferredSize(new Dimension(200, 40));
                        lblPreview.setText("Imagen no encontrada");
                    }
                } catch (Exception e) {
                    lblPreview.setPreferredSize(new Dimension(200, 40));
                    lblPreview.setText("Error al cargar");
                }
            } else {
                lblPreview.setPreferredSize(new Dimension(200, 40));
                lblPreview.setText("Sin imagen");
            }
        };
        updatePreview.accept(txtUrlImagen.getText());

        btnSeleccionarImagen.addActionListener(ev -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Seleccionar Imagen del Producto");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (JPG, PNG, WEBP, GIF, BMP)", "jpg", "jpeg", "png", "webp", "gif", "bmp"));
            if (fileChooser.showOpenDialog(dialog) == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                // Copiar imagen a carpeta portable
                String imgDir = System.getProperty("user.dir") + java.io.File.separator + "Datos_SenatiZapato" + java.io.File.separator + "imagenes";
                java.io.File imgDirFile = new java.io.File(imgDir);
                if (!imgDirFile.exists()) imgDirFile.mkdirs();
                String fileName = selectedFile.getName();
                java.io.File destFile = new java.io.File(imgDirFile, fileName);
                // Evitar duplicados: si ya existe con mismo nombre, añadir timestamp
                if (destFile.exists() && !destFile.getAbsolutePath().equals(selectedFile.getAbsolutePath())) {
                    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                    String ext = fileName.substring(fileName.lastIndexOf('.'));
                    fileName = baseName + "_" + System.currentTimeMillis() + ext;
                    destFile = new java.io.File(imgDirFile, fileName);
                }
                try {
                    if (!destFile.getAbsolutePath().equals(selectedFile.getAbsolutePath())) {
                        java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
                // Guardar solo el nombre del archivo (portable)
                txtUrlImagen.setText(fileName);
                updatePreview.accept(destFile.getAbsolutePath());
            }
        });
        JPanel panelImagen = new JPanel(new java.awt.BorderLayout(5, 0));
        panelImagen.setOpaque(false);
        panelImagen.add(txtUrlImagen, java.awt.BorderLayout.CENTER);
        panelImagen.add(btnSeleccionarImagen, java.awt.BorderLayout.EAST);

        JLabel lblError = new JLabel(" ");
        lblError.setForeground(new Color(220, 53, 69));
        lblError.setFont(new Font("Inter", Font.BOLD, 13));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        ((javax.swing.text.AbstractDocument) txtNombre.getDocument()).setDocumentFilter(new LetterFilter(lblError));
        ((javax.swing.text.AbstractDocument) txtStock.getDocument()).setDocumentFilter(new IntegerFilter(lblError));
        ((javax.swing.text.AbstractDocument) txtPrecio.getDocument()).setDocumentFilter(new DoubleFilter(lblError));
        ((javax.swing.text.AbstractDocument) txtCodigo.getDocument()).setDocumentFilter(new AlphanumericFilter(lblError));

        java.awt.event.KeyAdapter keyNav = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getSource() instanceof javax.swing.JComboBox) {
                    javax.swing.JComboBox<?> combo = (javax.swing.JComboBox<?>) e.getSource();
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                        if (combo.isPopupVisible()) {
                            combo.setPopupVisible(false);
                        } else {
                            combo.setPopupVisible(true);
                        }
                        e.consume();
                        return;
                    }
                    if (combo.isPopupVisible()) {
                        return; // Permitir navegar por las categorías si está desplegado
                    }
                }

                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    if (e.getSource() == txtPrecio) return; // No bajar más allá del último campo
                    ((java.awt.Component) e.getSource()).transferFocus();
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP) {
                    if (e.getSource() == txtCodigo) return; // No subir más allá del primer campo
                    ((java.awt.Component) e.getSource()).transferFocusBackward();
                    e.consume();
                }
            }
        };

        javax.swing.JComponent[] fieldContainers = {txtCodigo, txtNombre, cbCategoria, txtStock, txtPrecio, panelImagen, lblPreview};
        for (javax.swing.JComponent tf : new javax.swing.JComponent[]{txtCodigo, txtNombre, cbCategoria, txtStock, txtPrecio, txtUrlImagen}) {
            tf.setFont(new Font("Inter", Font.PLAIN, 15));
            tf.setPreferredSize(new Dimension(280, 38));
            tf.addKeyListener(keyNav);
            if (tf instanceof JTextField) {
                tf.putClientProperty("JComponent.roundRect", true);
                tf.putClientProperty("JTextField.padding", new java.awt.Insets(5, 10, 5, 10));
            }
        }

        JPanel formPanel = new JPanel(new java.awt.GridBagLayout());
        formPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 30, 10, 30));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(8, 10, 8, 10);

        JLabel[] labels = {
            new JLabel("Código:"), new JLabel("Nombre:"), new JLabel("Categoría:"),
            new JLabel("Stock:"), new JLabel("Precio (S/):"), new JLabel("Imagen:"), new JLabel("Producto:")
        };
        
        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(new Font("Inter", Font.BOLD, 15));
            labels[i].setForeground(Gestor_De_Temas.getTextColor());
            
            gbc.gridx = 0; gbc.gridy = i;
            gbc.anchor = java.awt.GridBagConstraints.EAST;
            gbc.weightx = 0.0;
            formPanel.add(labels[i], gbc);
            
            gbc.gridx = 1; gbc.gridy = i;
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            formPanel.add(fieldContainers[i], gbc);
        }

        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        btnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 20));

        JButton btnGuardar = new JButton(" Guardar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isEnabled() ? (getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : Gestor_De_Temas.getAccentColor()) : new Color(180, 180, 180);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnGuardar.setContentAreaFilled(false); btnGuardar.setBorderPainted(false); btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Inter", Font.BOLD, 15)); btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(110, 38));
        btnGuardar.setEnabled(false);
        addHoverEffect(btnGuardar, Gestor_De_Temas.getAccentColor(), Gestor_De_Temas.getAccentColor().brighter());

        JButton btnCancelar = new JButton(" Cancelar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getClientProperty("currentColor") != null ? (Color) getClientProperty("currentColor") : new Color(220, 53, 69);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnCancelar.setContentAreaFilled(false); btnCancelar.setBorderPainted(false); btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Inter", Font.BOLD, 15)); btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        addHoverEffect(btnCancelar, new Color(220, 53, 69), new Color(235, 75, 90));

        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        JPanel mainPanel = new JPanel(new java.awt.BorderLayout());
        mainPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        mainPanel.add(formPanel, java.awt.BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new java.awt.BorderLayout());
        bottomPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        bottomPanel.add(lblError, java.awt.BorderLayout.NORTH);
        bottomPanel.add(btnPanel, java.awt.BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dialog.dispose());

        // Validación: habilitar Guardar solo con campos requeridos llenos
        Runnable validarCampos = () -> {
            boolean valid = !txtCodigo.getText().trim().isEmpty()
                    && !txtNombre.getText().trim().isEmpty()
                    && cbCategoria.getSelectedIndex() > 0
                    && !txtStock.getText().trim().isEmpty()
                    && !txtPrecio.getText().trim().isEmpty();
            btnGuardar.setEnabled(valid);
        };

        DocumentListener validarListener = new DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validarCampos.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validarCampos.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validarCampos.run(); }
        };
        txtCodigo.getDocument().addDocumentListener(validarListener);
        txtNombre.getDocument().addDocumentListener(validarListener);
        txtStock.getDocument().addDocumentListener(validarListener);
        txtPrecio.getDocument().addDocumentListener(validarListener);
        cbCategoria.addActionListener(e -> validarCampos.run());

        // Si es editar, verificar estado inicial
        if (producto != null) {
            validarCampos.run();
        }

        btnGuardar.addActionListener(e -> {
            boolean isCategoriaValid = cbCategoria.getSelectedIndex() > 0;
            boolean isCodigoValid = !txtCodigo.getText().trim().isEmpty();
            boolean isNombreValid = !txtNombre.getText().trim().isEmpty();
            boolean isStockValid = !txtStock.getText().trim().isEmpty();
            boolean isPrecioValid = !txtPrecio.getText().trim().isEmpty();
            
            if (!(isCategoriaValid && isCodigoValid && isNombreValid && isStockValid && isPrecioValid)) {
                JOptionPane.showMessageDialog(dialog, "Debes completar todos los campos para poder guardar.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Producto p = new Producto(
                    producto != null ? producto.Get_Id() : 0,
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    cbCategoria.getSelectedItem().toString(),
                    Integer.parseInt(txtStock.getText().trim().isEmpty() ? "0" : txtStock.getText().trim()),
                    Double.parseDouble(txtPrecio.getText().trim().isEmpty() ? "0" : txtPrecio.getText().trim()),
                    producto != null && producto.Get_Tallas() != null ? producto.Get_Tallas() : "38,39,40,41,42",
                    txtUrlImagen.getText().trim()
                );
                
                if (producto == null) productoDAO.insertar(p);
                else productoDAO.actualizar(p);
                
                cargarDatos();
                dialog.dispose();
            } catch (Exception ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg != null && errorMsg.contains("UNIQUE constraint failed")) {
                    errorMsg = "El código ya existe";
                }
                lblError.setText("Error: " + errorMsg);
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });

        dialog.getRootPane().setDefaultButton(btnGuardar);
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                // Mover el cursor al final del texto para quitar la selección azul
                txtCodigo.setCaretPosition(txtCodigo.getText().length());
            }
        });
        
        dialog.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPanel toolbar;
    // End of variables declaration//GEN-END:variables

    class LetterFilter extends DocumentFilter {
        private JLabel errorLabel;
        public LetterFilter(JLabel errorLabel) { this.errorLabel = errorLabel; }
        private void showError() {
            java.awt.Toolkit.getDefaultToolkit().beep();
            errorLabel.setText("Error: Solo se aceptan letras y espacios.");
        }
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (string.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) { errorLabel.setText(" "); super.insertString(fb, offset, string, attr); }
            else showError();
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (text.isEmpty() || text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) { errorLabel.setText(" "); super.replace(fb, offset, length, text, attrs); }
            else showError();
        }
    }

    class AlphanumericFilter extends DocumentFilter {
        private JLabel errorLabel;
        public AlphanumericFilter(JLabel errorLabel) { this.errorLabel = errorLabel; }
        private void showError() {
            java.awt.Toolkit.getDefaultToolkit().beep();
            errorLabel.setText("Error: Solo se aceptan letras y números.");
        }
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (string.matches("[a-zA-Z0-9]+")) { errorLabel.setText(" "); super.insertString(fb, offset, string, attr); }
            else showError();
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (text.isEmpty() || text.matches("[a-zA-Z0-9]+")) { errorLabel.setText(" "); super.replace(fb, offset, length, text, attrs); }
            else showError();
        }
    }

    class IntegerFilter extends DocumentFilter {
        private JLabel errorLabel;
        public IntegerFilter(JLabel errorLabel) { this.errorLabel = errorLabel; }
        private void showError() {
            java.awt.Toolkit.getDefaultToolkit().beep();
            errorLabel.setText("Error: Solo números enteros.");
        }
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (string.matches("[0-9]+")) { errorLabel.setText(" "); super.insertString(fb, offset, string, attr); }
            else showError();
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (text.isEmpty() || text.matches("[0-9]+")) { errorLabel.setText(" "); super.replace(fb, offset, length, text, attrs); }
            else showError();
        }
    }

    class DoubleFilter extends DocumentFilter {
        private JLabel errorLabel;
        public DoubleFilter(JLabel errorLabel) { this.errorLabel = errorLabel; }
        private void showError() {
            java.awt.Toolkit.getDefaultToolkit().beep();
            errorLabel.setText("Error: Formato de precio inválido (ej. 50.50).");
        }
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
            if (newText.matches("\\d*\\.?\\d*")) { errorLabel.setText(" "); super.insertString(fb, offset, string, attr); }
            else showError();
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
            if (newText.matches("\\d*\\.?\\d*")) { errorLabel.setText(" "); super.replace(fb, offset, length, text, attrs); }
            else showError();
        }
    }
}
