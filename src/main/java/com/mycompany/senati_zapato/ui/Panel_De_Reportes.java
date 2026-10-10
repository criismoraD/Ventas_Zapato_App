package com.mycompany.senati_zapato.ui;

import com.mycompany.senati_zapato.ui.*;
import com.mycompany.senati_zapato.modelos.*;
import com.mycompany.senati_zapato.datos.*;
import com.mycompany.senati_zapato.servicios.*;
import com.mycompany.senati_zapato.utilidades.*;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class Panel_De_Reportes extends javax.swing.JPanel {

    private Dao_De_Venta ventaDAO;
    private Dao_De_Producto productoDAO;
    
    private DefaultTableModel modelTransacciones;

    private JPanel kpiPanel;
    private JPanel chartPanel;
    private JPanel bottomPanel;

    private final Color[] chartColors = {
        new Color(79, 133, 87),   
        new Color(168, 62, 45),   
        new Color(240, 173, 78),  
        new Color(54, 162, 235),  
        new Color(153, 102, 255), 
        new Color(255, 159, 64)   
    };

    public Panel_De_Reportes() {
        ventaDAO = new Dao_De_Venta();
        productoDAO = new Dao_De_Producto();
        initComponents();
        initCustomUI();
    }

    private void initComponents() {
        kpiPanel = new javax.swing.JPanel();
        chartPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // 1. KPIs Fila Superior (weighty = 0.0)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 0.0;
        kpiPanel.setLayout(new java.awt.GridLayout(1, 3, 20, 0));
        kpiPanel.setPreferredSize(new Dimension(0, 100));
        add(kpiPanel, gbc);

        // 2. Gráficos Fila Central (weighty = 1.0 — ocupa el espacio restante)
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        chartPanel.setLayout(new java.awt.BorderLayout());
        add(chartPanel, gbc);

        // 3. Tabla Fila Inferior (weighty = 0.0 — se ajusta al contenido)
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        bottomPanel.setLayout(new java.awt.BorderLayout(0, 10));
        add(bottomPanel, gbc);
    }

    private void initCustomUI() {
        setOpaque(false);
        kpiPanel.setOpaque(false);
        bottomPanel.setOpaque(false);

        configurarModelosDeTabla();
        
        cargarKpis();
        cargarGraficos();
        cargarTablas();
    }
    
    private void configurarModelosDeTabla() {
        String[] colsTx = {"Fecha/Hora", "Cajero", "Método", "Monto", "Estado pago", "Ver"};
        modelTransacciones = new DefaultTableModel(colsTx, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void cargarKpis() {
        kpiPanel.removeAll();
        double[] kpis = ventaDAO.obtenerKpisDelDia();
        String strIngresos = String.format("S/ %.2f", kpis[0]);
        String strZapatos = String.format("%.0f", kpis[1]);
        String strVentas = String.format("%.0f", kpis[2]);

        kpiPanel.add(new ProgressRingKPI("Ingresos del Día", kpis[0], 1500.0, strIngresos, Gestor_De_Temas.getSuccessColor(), Icono_Elegante.Type.CART));
        kpiPanel.add(new ProgressRingKPI("Zapatos Vendidos", kpis[1], 20.0, strZapatos, Gestor_De_Temas.getAccentColor(), Icono_Elegante.Type.BOX));
        kpiPanel.add(new ProgressRingKPI("Transacciones", kpis[2], 15.0, strVentas, Gestor_De_Temas.getDangerColor(), Icono_Elegante.Type.CREDIT_CARD));
        
        kpiPanel.revalidate();
        kpiPanel.repaint();
    }

    private void cargarGraficos() {
        chartPanel.removeAll();
        chartPanel.setBackground(Gestor_De_Temas.getPanelBgColor());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel graficosContainer = new JPanel(new java.awt.GridLayout(1, 3, 20, 0));
        graficosContainer.setOpaque(false);
        
        // Cuadro 1: Barras
        JPanel barrasWrapper = new JPanel(new java.awt.BorderLayout());
        barrasWrapper.setBackground(Gestor_De_Temas.getPanelBgColor());
        barrasWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JLabel lblChartTitle = new JLabel("Ventas Últimos 7 Días", SwingConstants.CENTER);
        lblChartTitle.setFont(new Font("Inter", Font.BOLD, 15));
        lblChartTitle.setForeground(Gestor_De_Temas.getTextColor());
        barrasWrapper.add(lblChartTitle, java.awt.BorderLayout.NORTH);
        barrasWrapper.add(crearGraficoBarras(), java.awt.BorderLayout.CENTER);
        graficosContainer.add(barrasWrapper);

        // Cuadro 2: Tabla Top Productos
        List<Object[]> topProductos = ventaDAO.Obtener_Top_Productos(5);
        graficosContainer.add(crearTopProductosPanel(topProductos));

        // Cuadro 3: Dona
        List<Producto> todosProductos = productoDAO.Obtener_Todos();
        graficosContainer.add(crearDonutChartPanel(todosProductos));

        chartPanel.add(graficosContainer, java.awt.BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private JPanel crearGraficoBarras() {
        JPanel barsPanel = new JPanel(new java.awt.GridLayout(1, 7, 5, 0));
        barsPanel.setOpaque(false);
        barsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        List<Object[]> datosBD = ventaDAO.obtenerVentasUltimos7Dias();
        Map<String, Double> ventasMap = new HashMap<>();
        for (Object[] fila : datosBD) {
            ventasMap.put((String) fila[0], (Double) fila[1]);
        }

        LocalDate today = LocalDate.now();
        List<LocalDate> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) last7Days.add(today.minusDays(i));

        double maxVenta = 100;
        double[] amounts = new double[7];
        String[] daysStr = new String[7];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("E dd");

        for (int i = 0; i < 7; i++) {
            LocalDate d = last7Days.get(i);
            daysStr[i] = d.format(dtf);
            amounts[i] = ventasMap.getOrDefault(d.toString(), 0.0);
            if (amounts[i] > maxVenta) maxVenta = amounts[i];
        }

        final double finalMax = maxVenta;

        // progreso de animación: 0.0 → 1.0
        final double[] progreso = {0.0};

        for (int i = 0; i < 7; i++) {
            final double amount = amounts[i];
            final String dayLabel = daysStr[i].substring(0, 1).toUpperCase();

            JPanel dayCol = new JPanel(new java.awt.BorderLayout());
            dayCol.setOpaque(false);

            JPanel bar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    double animatedAmount = amount * progreso[0];
                    int maxBarHeight = getHeight() - 20;
                    int barHeight = (int) ((animatedAmount / finalMax) * maxBarHeight);
                    if (barHeight < 5 && animatedAmount > 0) barHeight = 5;
                    int yPos = getHeight() - barHeight;

                    // Gradiente en la barra
                    if (barHeight > 0) {
                        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                            0, yPos, Gestor_De_Temas.getAccentColor().brighter(),
                            0, getHeight(), Gestor_De_Temas.getAccentColor().darker()
                        );
                        g2.setPaint(gp);
                        g2.fillRoundRect(0, yPos, getWidth(), barHeight, 8, 8);
                    }

                    // Valor encima de la barra (solo cuando la animación terminó)
                    if (progreso[0] >= 1.0 && amount > 0) {
                        g2.setColor(Gestor_De_Temas.getAccentColor().darker());
                        g2.setFont(new Font("Inter", Font.BOLD, 10));
                        String valStr = String.format("%.0f", amount);
                        int strW = g2.getFontMetrics().stringWidth(valStr);
                        g2.drawString(valStr, (getWidth() - strW) / 2, yPos - 3);
                    }
                    g2.dispose();
                }
            };
            bar.setOpaque(false);

            JLabel lblDay = new JLabel(dayLabel, SwingConstants.CENTER);
            lblDay.setForeground(Gestor_De_Temas.getMutedColor());
            lblDay.setFont(new Font("Inter", Font.BOLD, 11));
            lblDay.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            dayCol.add(bar, java.awt.BorderLayout.CENTER);
            dayCol.add(lblDay, java.awt.BorderLayout.SOUTH);
            barsPanel.add(dayCol);
        }

        // Timer de animación: 60 fps durante ~600ms
        final int[] frame = {0};
        final int totalFrames = 36; // 36 frames × 16ms ≈ 600ms
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            frame[0]++;
            // Easing: ease-out (desacelera al final)
            double t = (double) frame[0] / totalFrames;
            progreso[0] = 1.0 - Math.pow(1.0 - t, 3); // ease-out cúbico
            if (frame[0] >= totalFrames) {
                progreso[0] = 1.0;
                timer.stop();
            }
            barsPanel.repaint();
        });
        timer.start();

        return barsPanel;
    }

    private JPanel crearPieChartPanel(List<Object[]> datos) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Gestor_De_Temas.getPanelBgColor());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Top 5 Productos", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 15));
        title.setForeground(Gestor_De_Temas.getTextColor());
        wrapper.add(title, BorderLayout.NORTH);

        double total = 0;
        for (Object[] row : datos) {
            total += ((Number) row[3]).doubleValue(); 
        }
        final double finalTotal = total;

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                if (datos.isEmpty() || finalTotal == 0) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillOval(x, y, size, size);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Inter", Font.BOLD, 14));
                    g2.drawString("Sin datos", getWidth() / 2 - 30, getHeight() / 2);
                    g2.dispose();
                    return;
                }

                double currentAngle = 90; 
                for (int i = 0; i < datos.size(); i++) {
                    double val = ((Number) datos.get(i)[3]).doubleValue();
                    double angle = (val / finalTotal) * 360.0;
                    
                    g2.setColor(chartColors[i % chartColors.length]);
                    g2.fill(new Arc2D.Double(x, y, size, size, currentAngle, -angle, Arc2D.PIE));
                    currentAngle -= angle;
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(100, 100));

        JPanel legendPanel = new JPanel();
        legendPanel.setOpaque(false);
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        if (!datos.isEmpty()) {
            for (int i = 0; i < datos.size(); i++) {
                Object[] row = datos.get(i);
                String name = (String) row[1];
                if (name.length() > 15) name = name.substring(0, 15) + "..."; // Acortar para no desbordar
                int qty = ((Number) row[3]).intValue();
                double pct = (qty / total) * 100;

                JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                itemRow.setOpaque(false);
                
                JPanel colorBox = new JPanel();
                colorBox.setBackground(chartColors[i % chartColors.length]);
                colorBox.setPreferredSize(new Dimension(10, 10));
                
                JLabel lblName = new JLabel(String.format("%s - %d (%.0f%%)", name, qty, pct));
                lblName.setFont(new Font("Inter", Font.PLAIN, 11));
                lblName.setForeground(Gestor_De_Temas.getTextColor());

                itemRow.add(colorBox);
                itemRow.add(lblName);
                legendPanel.add(itemRow);
                legendPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            }
        }

        wrapper.add(chart, BorderLayout.CENTER);
        wrapper.add(legendPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel crearTopProductosPanel(List<Object[]> datos) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(Gestor_De_Temas.getPanelBgColor());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Top 5 Más Vendidos", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 15));
        title.setForeground(Gestor_De_Temas.getTextColor());
        wrapper.add(title, BorderLayout.NORTH);

        if (datos.isEmpty()) {
            JLabel lblVacio = new JLabel("Sin ventas registradas", SwingConstants.CENTER);
            lblVacio.setForeground(Gestor_De_Temas.getMutedColor());
            lblVacio.setFont(new Font("Inter", Font.PLAIN, 13));
            wrapper.add(lblVacio, BorderLayout.CENTER);
            return wrapper;
        }

        // Calcular total para porcentajes
        int totalVendido = 0;
        for (Object[] row : datos) totalVendido += ((Number) row[3]).intValue();

        JPanel listaPanel = new JPanel();
        listaPanel.setOpaque(false);
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < datos.size(); i++) {
            Object[] row = datos.get(i);
            String nombre    = (String) row[1];
            String categoria = (String) row[2];
            int    cantidad  = ((Number) row[3]).intValue();
            double pct       = totalVendido > 0 ? (cantidad * 100.0 / totalVendido) : 0;

            // Fila contenedora
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setOpaque(false);
            fila.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

            // Número de posición
            JLabel lblPos = new JLabel("#" + (i + 1));
            lblPos.setFont(new Font("Inter", Font.BOLD, 16));
            lblPos.setForeground(i == 0 ? new Color(200, 150, 30) :   // oro
                                 i == 1 ? new Color(140, 140, 140) :   // plata
                                 i == 2 ? new Color(160, 100, 50)  :   // bronce
                                          Gestor_De_Temas.getMutedColor());
            lblPos.setPreferredSize(new Dimension(30, 20));

            // Info del producto
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

            String nombreCorto = nombre.length() > 18 ? nombre.substring(0, 18) + "…" : nombre;
            JLabel lblNombre = new JLabel(nombreCorto);
            lblNombre.setFont(new Font("Inter", Font.BOLD, 13));
            lblNombre.setForeground(Gestor_De_Temas.getTextColor());

            JLabel lblCat = new JLabel(categoria);
            lblCat.setFont(new Font("Inter", Font.PLAIN, 11));
            lblCat.setForeground(Gestor_De_Temas.getMutedColor());

            infoPanel.add(lblNombre);
            infoPanel.add(lblCat);

            // Cantidad + barra de progreso
            JPanel rightPanel = new JPanel();
            rightPanel.setOpaque(false);
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            JLabel lblCantidad = new JLabel(cantidad + " uds", SwingConstants.RIGHT);
            lblCantidad.setFont(new Font("Inter", Font.BOLD, 13));
            lblCantidad.setForeground(Gestor_De_Temas.getAccentColor());
            lblCantidad.setAlignmentX(1.0f);

            // Mini barra de progreso animada
            final double finalPct = pct;
            final Color barColor = chartColors[i % chartColors.length];
            final double[] progresoBar = {0.0};
            JPanel barraProgreso = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Gestor_De_Temas.isDarkMode() ? new Color(60,50,45) : new Color(230,225,218));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    int w = (int)(getWidth() * finalPct / 100.0 * progresoBar[0]);
                    if (w > 0) {
                        g2.setColor(barColor);
                        g2.fillRoundRect(0, 0, w, getHeight(), 6, 6);
                    }
                    g2.dispose();
                }
            };
            barraProgreso.setOpaque(false);
            barraProgreso.setPreferredSize(new Dimension(80, 6));
            barraProgreso.setMaximumSize(new Dimension(Short.MAX_VALUE, 6));

            // Timer animación barra individual con delay escalonado por posición
            final int delayInicial = i * 80; // cada barra empieza 80ms después
            Timer timerBar = new Timer(16, null);
            final int[] frameBar = {0};
            final int totalFramesBar = 30;
            timerBar.addActionListener(e -> {
                frameBar[0]++;
                double t = (double) frameBar[0] / totalFramesBar;
                progresoBar[0] = 1.0 - Math.pow(1.0 - t, 3);
                if (frameBar[0] >= totalFramesBar) {
                    progresoBar[0] = 1.0;
                    timerBar.stop();
                }
                barraProgreso.repaint();
            });
            // Iniciar con delay escalonado
            Timer delayTimer = new Timer(delayInicial, e -> timerBar.start());
            delayTimer.setRepeats(false);
            delayTimer.start();

            rightPanel.add(lblCantidad);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            rightPanel.add(barraProgreso);

            fila.add(lblPos,     BorderLayout.WEST);
            fila.add(infoPanel,  BorderLayout.CENTER);
            fila.add(rightPanel, BorderLayout.EAST);

            listaPanel.add(fila);

            // Separador entre filas (excepto la última)
            if (i < datos.size() - 1) {
                JPanel sep = new JPanel();
                sep.setOpaque(true);
                sep.setBackground(Gestor_De_Temas.getBorderColor());
                sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
                sep.setPreferredSize(new Dimension(0, 1));
                listaPanel.add(sep);
            }
        }

        wrapper.add(listaPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private String buildTooltip(String title, List<String> list) {
        if (list.isEmpty()) return title + ": Ninguno";
        StringBuilder sb = new StringBuilder("<html><b>" + title + "</b><br>");
        int max = 10;
        for (int i = 0; i < Math.min(list.size(), max); i++) {
            sb.append("- ").append(list.get(i)).append("<br>");
        }
        if (list.size() > max) {
            sb.append("<i>...y ").append(list.size() - max).append(" más</i>");
        }
        sb.append("</html>");
        return sb.toString();
    }

    private JPanel crearDonutChartPanel(List<Producto> productos) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Gestor_De_Temas.getPanelBgColor());
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Gestor_De_Temas.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("Distribución de Almacén", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 15));
        title.setForeground(Gestor_De_Temas.getTextColor());
        wrapper.add(title, BorderLayout.NORTH);

        List<String> agotadosList = new ArrayList<>();
        List<String> bajoStockList = new ArrayList<>();
        List<String> disponiblesList = new ArrayList<>();

        for (Producto p : productos) {
            if (p.Get_Stock() == 0) agotadosList.add(p.Get_Nombre());
            else if (p.Get_Stock() <= 5) bajoStockList.add(p.Get_Nombre());
            else disponiblesList.add(p.Get_Nombre());
        }

        final int disponibles = disponiblesList.size();
        final int bajoStock = bajoStockList.size();
        final int agotados = agotadosList.size();
        final int total = disponibles + bajoStock + agotados;

        // progreso de animación 0.0 → 1.0
        final double[] progresoDonut = {0.0};

        JPanel chart = new JPanel() {
            private Arc2D arcAgotado = new Arc2D.Double();
            private Arc2D arcBajo    = new Arc2D.Double();
            private Arc2D arcDisp    = new Arc2D.Double();
            private java.awt.geom.Ellipse2D innerHole = new java.awt.geom.Ellipse2D.Double();

            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                if (innerHole.contains(e.getPoint())) return null;
                if (arcAgotado.contains(e.getPoint())) return buildTooltip("Agotados", agotadosList);
                if (arcBajo.contains(e.getPoint()))    return buildTooltip("Bajo Stock", bajoStockList);
                if (arcDisp.contains(e.getPoint()))    return buildTooltip("Disponibles", disponiblesList);
                return super.getToolTipText(e);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                if (total == 0) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawOval(x, y, size, size);
                    g2.dispose();
                    return;
                }

                // Ángulos animados
                double angDisp = ((double) disponibles / total) * 360.0 * progresoDonut[0];
                double angBajo = ((double) bajoStock   / total) * 360.0 * progresoDonut[0];
                double angAgot = ((double) agotados    / total) * 360.0 * progresoDonut[0];

                double start = 90;

                g2.setColor(new Color(220, 53, 69));
                arcAgotado.setArc(x, y, size, size, start, -angAgot, Arc2D.PIE);
                g2.fill(arcAgotado);
                start -= angAgot;

                g2.setColor(new Color(255, 153, 0));
                arcBajo.setArc(x, y, size, size, start, -angBajo, Arc2D.PIE);
                g2.fill(arcBajo);
                start -= angBajo;

                g2.setColor(new Color(40, 167, 69));
                arcDisp.setArc(x, y, size, size, start, -angDisp, Arc2D.PIE);
                g2.fill(arcDisp);

                // Hueco interior
                int innerSize = (int)(size * 0.55);
                int ix = x + (size - innerSize) / 2;
                int iy = y + (size - innerSize) / 2;
                g2.setColor(Gestor_De_Temas.getPanelBgColor());
                innerHole.setFrame(ix, iy, innerSize, innerSize);
                g2.fill(innerHole);

                // Total en el centro (solo al terminar)
                if (progresoDonut[0] >= 1.0) {
                    g2.setColor(Gestor_De_Temas.getTextColor());
                    g2.setFont(new Font("Inter", Font.BOLD, 18));
                    String totStr = String.valueOf(total);
                    int sw = g2.getFontMetrics().stringWidth(totStr);
                    g2.drawString(totStr, getWidth() / 2 - sw / 2, getHeight() / 2 + 5);
                }

                g2.dispose();
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(100, 100));
        chart.setToolTipText("");

        // Timer animación dona
        final int[] frameDonut = {0};
        final int totalFramesDonut = 40;
        Timer timerDonut = new Timer(16, null);
        timerDonut.addActionListener(e -> {
            frameDonut[0]++;
            double t = (double) frameDonut[0] / totalFramesDonut;
            progresoDonut[0] = 1.0 - Math.pow(1.0 - t, 3); // ease-out cúbico
            if (frameDonut[0] >= totalFramesDonut) {
                progresoDonut[0] = 1.0;
                timerDonut.stop();
            }
            chart.repaint();
        });
        timerDonut.start();

        JPanel legendPanel = new JPanel();
        legendPanel.setOpaque(false);
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        Object[][] items = {
            {"Disponibles", disponibles, new Color(40, 167, 69)},
            {"Bajo Stock", bajoStock, new Color(255, 153, 0)},
            {"Agotados", agotados, new Color(220, 53, 69)}
        };

        for (Object[] item : items) {
            JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            itemRow.setOpaque(false);
            
            JPanel colorBox = new JPanel();
            colorBox.setBackground((Color) item[2]);
            colorBox.setPreferredSize(new Dimension(10, 10));
            
            int cant = (Integer) item[1];
            double pct = total > 0 ? ((double) cant / total) * 100 : 0;
            JLabel lblName = new JLabel(String.format("%s: %d (%.0f%%)", item[0], cant, pct));
            lblName.setFont(new Font("Inter", Font.PLAIN, 11));
            lblName.setForeground(Gestor_De_Temas.getTextColor());

            itemRow.add(colorBox);
            itemRow.add(lblName);
            legendPanel.add(itemRow);
            legendPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        }

        wrapper.add(chart, BorderLayout.CENTER);
        wrapper.add(legendPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private void cargarTablas() {
        bottomPanel.removeAll();

        modelTransacciones.setRowCount(0);
        List<Venta> recientes = ventaDAO.Obtener_Ventas_Recientes(4);
        for (Venta v : recientes) {
            modelTransacciones.addRow(new Object[]{v.getFechaHora(), v.Get_Cajero(), v.Get_Metodo_Pago(),
                String.format("S/ %.2f", v.Get_Monto_Total()), v.getEstadoPago(), v});
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 16));
        
        JTable tableTx = crearTablaEstilizada(modelTransacciones, "Transacciones");

        tableTx.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private Icono_Elegante eyeIcon = new Icono_Elegante(Icono_Elegante.Type.EYE, 22, Gestor_De_Temas.getAccentColor());
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                label.setIcon(eyeIcon);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                return label;
            }
        });
        tableTx.getColumnModel().getColumn(5).setMaxWidth(60);

        tableTx.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tableTx.columnAtPoint(e.getPoint());
                int row = tableTx.rowAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    Venta v = (Venta) modelTransacciones.getValueAt(row, 5);
                    com.mycompany.senati_zapato.utilidades.Generador_De_Pdf.generarComprobante(v);
                }
            }
        });

        // Calcular altura exacta: encabezado + 4 filas
        int headerHeight = tableTx.getTableHeader().getPreferredSize().height;
        int rowsHeight   = tableTx.getRowHeight() * 4;
        int totalHeight  = headerHeight + rowsHeight + 2; // +2 borde

        tableTx.setPreferredScrollableViewportSize(new Dimension(0, totalHeight));

        // Sin scroll — la tabla se ajusta exactamente a las 4 filas
        JScrollPane scrollTx = new JScrollPane(tableTx);
        scrollTx.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollTx.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTx.setBorder(BorderFactory.createEmptyBorder());
        scrollTx.setPreferredSize(new Dimension(0, totalHeight + 30)); // +30 tab del JTabbedPane

        tabbedPane.addTab("Últimas 4 Transacciones", scrollTx);
        tabbedPane.setPreferredSize(new Dimension(0, totalHeight + 55)); // +55 tab + padding

        bottomPanel.add(tabbedPane, java.awt.BorderLayout.CENTER);
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private JTable crearTablaEstilizada(DefaultTableModel model, String tipo) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setBackground(Gestor_De_Temas.getPanelBgColor());
        table.setForeground(Gestor_De_Temas.getTextColor());
        table.setFont(new Font("Inter", Font.PLAIN, 15));
        table.setGridColor(new Color(230, 225, 215));
        table.getTableHeader().setBackground(Gestor_De_Temas.getAccentColor());
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 15));
        table.getTableHeader().setPreferredSize(new Dimension(100, 35));

        javax.swing.table.DefaultTableCellRenderer cellRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Gestor_De_Temas.getPanelBgColor() : new Color(248, 245, 240));
                } else {
                    c.setBackground(new Color(235, 240, 235));
                }
                
                c.setForeground(Color.BLACK);
                setFont(new Font("Inter", Font.PLAIN, 15));

                if (tipo.equals("Transacciones") && column == 3) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("Completado")) c.setForeground(new Color(40, 140, 60));
                    else if (status.equalsIgnoreCase("Cancelado")) c.setForeground(new Color(200, 50, 50));
                    else c.setForeground(Color.GRAY);
                    setFont(new Font("Inter", Font.BOLD, 15));
                } 
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        return table;
    }

    public void Refrescar_Reportes() {
        cargarKpis();
        cargarGraficos();
        cargarTablas();
    }

}

class ProgressRingKPI extends javax.swing.JPanel {
    private final String title;
    private final double currentValue;
    private final double maxValue;
    private final String displayValue;
    private final Color ringColor;
    private final Icono_Elegante.Type iconType;

    public ProgressRingKPI(String title, double currentValue, double maxValue, String displayValue, Color ringColor, Icono_Elegante.Type iconType) {
        this.title = title;
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.displayValue = displayValue;
        this.ringColor = ringColor;
        this.iconType = iconType;
        setPreferredSize(new Dimension(240, 100));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Fondo de tarjeta premium con esquinas redondeadas
        g2.setColor(Gestor_De_Temas.getPanelBgColor());
        g2.fillRoundRect(0, 0, w, h, 16, 16);
        g2.setColor(Gestor_De_Temas.getBorderColor());
        g2.setStroke(new java.awt.BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        // 2. Costura interna discontinua sutil
        float[] dash = {4.0f, 4.0f};
        g2.setStroke(new java.awt.BasicStroke(1.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        Color acc = Gestor_De_Temas.getAccentColor();
        g2.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 60));
        g2.drawRoundRect(4, 4, w - 9, h - 9, 12, 12);

        // 3. Dibujar el anillo de progreso a la izquierda
        int ringSize = 64;
        int rx = 20;
        int ry = (h - ringSize) / 2;

        // Anillo de fondo (tenue)
        g2.setColor(Gestor_De_Temas.isDarkMode() ? new Color(60, 50, 45) : new Color(235, 230, 225));
        g2.setStroke(new java.awt.BasicStroke(5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawOval(rx, ry, ringSize, ringSize);

        // Anillo de progreso activo
        double percentage = Math.min(1.0, currentValue / maxValue);
        int angle = (int) (percentage * 360);
        g2.setColor(ringColor);
        g2.setStroke(new java.awt.BasicStroke(6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.draw(new java.awt.geom.Arc2D.Double(rx, ry, ringSize, ringSize, 90, -angle, java.awt.geom.Arc2D.OPEN));

        // Dibujar porcentaje numérico en el centro del anillo
        g2.setFont(new Font("Inter", Font.BOLD, 12));
        g2.setColor(Gestor_De_Temas.getTextColor());
        String pctStr = String.format("%.0f%%", percentage * 100);
        int swPct = g2.getFontMetrics().stringWidth(pctStr);
        g2.drawString(pctStr, rx + (ringSize - swPct) / 2, ry + (ringSize / 2) + 4);

        // 4. Textos descriptivos al lado derecho
        int tx = rx + ringSize + 15;
        
        // Título del KPI
        g2.setFont(new Font("Inter", Font.BOLD, 13));
        g2.setColor(Gestor_De_Temas.getMutedColor());
        g2.drawString(title, tx, 32);

        // Valor Numérico Destacado (Georgia)
        g2.setFont(new Font("Inter", Font.BOLD, 22));
        g2.setColor(Gestor_De_Temas.getTextColor());
        g2.drawString(displayValue, tx, 56);

        // Subtexto de la meta
        g2.setFont(new Font("Inter", Font.PLAIN, 11));
        g2.setColor(Gestor_De_Temas.getMutedColor());
        String targetStr = String.format("Meta: " + (title.contains("Ingresos") ? "S/ %.2f" : "%.0f u."), maxValue);
        g2.drawString(targetStr, tx, 75);

        // 5. Icono sutil en la esquina superior derecha
        Icono_Elegante icon = new Icono_Elegante(iconType, 24, new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), 80));
        icon.paintIcon(this, g2, w - 36, 12);

        g2.dispose();
    }
}
