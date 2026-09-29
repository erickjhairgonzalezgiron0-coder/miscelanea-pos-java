import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VentanaAdministrador extends JFrame {

    private VentaControlador controlador;

    // Componentes del Formulario de Alta
    private JTextField txtAltaCodigo, txtAltaNombre, txtAltaPrecio, txtAltaStock;
    private JComboBox<String> comboAltaCategoria;

    // Componentes del Formulario de Reabastecimiento
    private JTextField txtStockCodigo, txtStockCantidad;

    // Tabla de Productos en BD
    private JTable tablaInventario;
    private DefaultTableModel modeloInventario;

    // Componentes del Panel de Reportes (KPIs)
    private JLabel lblValTotalProductos, lblValCategorias, lblValArticulosStock, lblValValorInventario;
    private JTable tablaBajoStock;
    private DefaultTableModel modeloBajoStock;

    private static final Color BG_DARK = new Color(24, 26, 32);
    private static final Color PANEL_BG = new Color(33, 37, 45);
    private static final Color INPUT_BG = new Color(44, 49, 60);
    private static final Color TEXT_COLOR = new Color(230, 235, 245);
    private static final Color TEXT_MUTED = new Color(140, 147, 160);
    private static final Color ACCENT_PRIMARY = new Color(99, 102, 241);
    private static final Color ACCENT_SUCCESS = new Color(16, 185, 129);
    private static final Color ACCENT_WARNING = new Color(245, 158, 11);

    public VentanaAdministrador(VentaControlador controlador) {
        this.controlador = controlador;

        setTitle("PANEL DE CONTROL — ENCARGADO / ADMINISTRACIÓN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 880);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pestanas.setBackground(PANEL_BG);
        pestanas.setForeground(TEXT_COLOR);

        // Pestaña 1: Gestión de Productos e Inventario
        JPanel panelGestion = crearPanelGestionProductos();
        pestanas.addTab("📦 Gestión de Productos", panelGestion);

        // Pestaña 2: Cortes y Reportes
        JPanel panelReportes = crearPanelReportes();
        pestanas.addTab("📊 Cortes & Reportes", panelReportes);

        add(pestanas);

        // Cargar datos iniciales desde la BD
        cargarTablaInventario();
        cargarCategoriasEnCombo();
        cargarReportesEfectivos();
    }

    private JPanel crearPanelGestionProductos() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBackground(BG_DARK);
        panelPrincipal.setBorder(new EmptyBorder(16, 16, 16, 16));

        JTabbedPane pestanasSub = new JTabbedPane();
        pestanasSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pestanasSub.setBackground(PANEL_BG);
        pestanasSub.setForeground(TEXT_COLOR);

        // Sub-Pestaña A: Nuevo Producto
        JPanel panelNuevo = new RoundedPanel(16, PANEL_BG);
        panelNuevo.setLayout(new GridBagLayout());
        panelNuevo.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtAltaCodigo = crearTextField();
        txtAltaNombre = crearTextField();
        txtAltaPrecio = crearTextField();
        comboAltaCategoria = crearComboBox();
        txtAltaStock = crearTextField();

        gbc.gridx = 0; gbc.gridy = 0; panelNuevo.add(crearLabelForm("Código de Barras:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelNuevo.add(txtAltaCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panelNuevo.add(crearLabelForm("Nombre / Descripción:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelNuevo.add(txtAltaNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panelNuevo.add(crearLabelForm("Precio ($):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelNuevo.add(txtAltaPrecio, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; panelNuevo.add(crearLabelForm("Categoría:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelNuevo.add(comboAltaCategoria, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; panelNuevo.add(crearLabelForm("Stock Inicial:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panelNuevo.add(txtAltaStock, gbc);

        JButton btnGuardarNuevo = crearBotonEstilizado("+ Guardar Nuevo Producto", ACCENT_SUCCESS, Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(12, 6, 6, 6);
        panelNuevo.add(btnGuardarNuevo, gbc);

        pestanasSub.addTab("🟢 Registrar Nuevo Producto", panelNuevo);

        // Sub-Pestaña B: Aumentar Stock Existente
        JPanel panelStock = new RoundedPanel(16, PANEL_BG);
        panelStock.setLayout(new GridBagLayout());
        panelStock.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(8, 8, 8, 8);
        gbcS.fill = GridBagConstraints.HORIZONTAL;

        txtStockCodigo = crearTextField();
        txtStockCantidad = crearTextField();

        gbcS.gridx = 0; gbcS.gridy = 0; panelStock.add(crearLabelForm("Código del Producto:"), gbcS);
        gbcS.gridx = 1; gbcS.weightx = 1.0; panelStock.add(txtStockCodigo, gbcS);

        gbcS.gridx = 0; gbcS.gridy = 1; gbcS.weightx = 0; panelStock.add(crearLabelForm("Cantidad a Añadir:"), gbcS);
        gbcS.gridx = 1; gbcS.weightx = 1.0; panelStock.add(txtStockCantidad, gbcS);

        JButton btnAumentarStock = crearBotonEstilizado("▲ Aumentar Stock", ACCENT_WARNING, Color.BLACK);
        gbcS.gridx = 0; gbcS.gridy = 2; gbcS.gridwidth = 2; gbcS.insets = new Insets(14, 8, 8, 8);
        panelStock.add(btnAumentarStock, gbcS);

        pestanasSub.addTab("🔵 Aumentar Stock Existente", panelStock);

        // Tabla de Consulta en Vivo de BD
        RoundedPanel panelTabla = new RoundedPanel(16, PANEL_BG);
        panelTabla.setLayout(new BorderLayout(10, 10));
        panelTabla.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Código", "Producto", "Precio ($)", "Categoría", "Stock"};
        modeloInventario = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaInventario = new JTable(modeloInventario);
        estilizarTabla(tablaInventario);

        JScrollPane scroll = new JScrollPane(tablaInventario);
        scroll.getViewport().setBackground(PANEL_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(500, 240));

        JButton btnRefrescar = crearBotonEstilizado("🔄 Actualizar Tabla", ACCENT_PRIMARY, Color.WHITE);

        panelTabla.add(scroll, BorderLayout.CENTER);
        panelTabla.add(btnRefrescar, BorderLayout.SOUTH);

        panelPrincipal.add(pestanasSub, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        // Eventos
        btnGuardarNuevo.addActionListener(e -> registrarProductoBD());
        btnAumentarStock.addActionListener(e -> aumentarStockBD());
        btnRefrescar.addActionListener(e -> {
            cargarTablaInventario();
            cargarCategoriasEnCombo();
            cargarReportesEfectivos();
        });

        return panelPrincipal;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitulo = new JLabel("RESUMEN GENERAL & REPORTES DE INVENTARIO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(TEXT_COLOR);

        // Panel de Tarjetas KPIs
        JPanel panelTarjetas = new JPanel(new GridLayout(2, 2, 12, 12));
        panelTarjetas.setOpaque(false);

        lblValTotalProductos = new JLabel("0", SwingConstants.CENTER);
        lblValCategorias = new JLabel("0", SwingConstants.CENTER);
        lblValArticulosStock = new JLabel("0", SwingConstants.CENTER);
        lblValValorInventario = new JLabel("$0.00", SwingConstants.CENTER);

        panelTarjetas.add(crearTarjetaCustom("Total Productos", lblValTotalProductos, ACCENT_PRIMARY));
        panelTarjetas.add(crearTarjetaCustom("Categorías Registradas", lblValCategorias, ACCENT_PRIMARY));
        panelTarjetas.add(crearTarjetaCustom("Unidades en Stock", lblValArticulosStock, ACCENT_SUCCESS));
        panelTarjetas.add(crearTarjetaCustom("Valor Total Inventario", lblValValorInventario, ACCENT_WARNING));

        // Panel para el Desglose de Productos
        RoundedPanel panelAvisoStock = new RoundedPanel(16, PANEL_BG);
        panelAvisoStock.setLayout(new BorderLayout(8, 8));
        panelAvisoStock.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblTituloAviso = new JLabel("📋 Estado de Stock por Producto (Ordenado por Existencias)", SwingConstants.LEFT);
        lblTituloAviso.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTituloAviso.setForeground(TEXT_COLOR);

        String[] colsAviso = {"Código", "Producto", "Categoría", "Stock Actual"};
        modeloBajoStock = new DefaultTableModel(colsAviso, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaBajoStock = new JTable(modeloBajoStock);
        estilizarTabla(tablaBajoStock);

        JScrollPane scrollAviso = new JScrollPane(tablaBajoStock);
        scrollAviso.getViewport().setBackground(PANEL_BG);
        scrollAviso.setBorder(BorderFactory.createEmptyBorder());
        scrollAviso.setPreferredSize(new Dimension(500, 180));

        panelAvisoStock.add(lblTituloAviso, BorderLayout.NORTH);
        panelAvisoStock.add(scrollAviso, BorderLayout.CENTER);

        JButton btnActualizarReportes = crearBotonEstilizado("📋 Generar Corte y Mostrar Resumen", ACCENT_SUCCESS, Color.WHITE);
        btnActualizarReportes.addActionListener(e -> generarReporteFlotante());

        JPanel panelCentroReporte = new JPanel(new BorderLayout(12, 12));
        panelCentroReporte.setOpaque(false);
        panelCentroReporte.add(panelTarjetas, BorderLayout.NORTH);
        panelCentroReporte.add(panelAvisoStock, BorderLayout.CENTER);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelCentroReporte, BorderLayout.CENTER);
        panel.add(btnActualizarReportes, BorderLayout.SOUTH);

        return panel;
    }

    private void generarReporteFlotante() {
        cargarReportesEfectivos();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        String fechaHora = sdf.format(new Date());

        StringBuilder reporte = new StringBuilder();
        reporte.append("=== CORTE DE INVENTARIO Y REPORTE ===\n");
        reporte.append("Fecha/Hora: ").append(fechaHora).append("\n");
        reporte.append("--------------------------------------------------\n");
        reporte.append("• Total de Productos Registrados: ").append(lblValTotalProductos.getText()).append("\n");
        reporte.append("• Categorías Únicas: ").append(lblValCategorias.getText()).append("\n");
        reporte.append("• Unidades Totales en Almacén: ").append(lblValArticulosStock.getText()).append(" piezas\n");
        reporte.append("• Valor Total Monetario: ").append(lblValValorInventario.getText()).append("\n");
        reporte.append("--------------------------------------------------\n");
        reporte.append("ALERTAS DE REABASTECIMIENTO (< 5 UNIDADES):\n");

        boolean hayBajoStock = false;
        for (int i = 0; i < modeloBajoStock.getRowCount(); i++) {
            int stock = (int) modeloBajoStock.getValueAt(i, 3);
            if (stock < 5) {
                String prod = (String) modeloBajoStock.getValueAt(i, 1);
                reporte.append("  - ").append(prod).append(" (Quedan sólo ").append(stock).append(" pzas)\n");
                hayBajoStock = true;
            }
        }

        if (!hayBajoStock) {
            reporte.append("  (Todos los productos cuentan con suficiente stock)\n");
        }

        reporte.append("--------------------------------------------------\n");
        reporte.append("Estado: Datos consultados exitosamente desde MySQL.");

        JOptionPane.showMessageDialog(this, reporte.toString(), "Corte de Inventario Generado", JOptionPane.INFORMATION_MESSAGE);
    }

    private RoundedPanel crearTarjetaCustom(String titulo, JLabel lblValor, Color colorValor) {
        RoundedPanel card = new RoundedPanel(16, PANEL_BG);
        card.setLayout(new GridLayout(2, 1));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblT = new JLabel(titulo, SwingConstants.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblT.setForeground(TEXT_MUTED);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValor.setForeground(colorValor);

        card.add(lblT);
        card.add(lblValor);
        return card;
    }

    private void cargarReportesEfectivos() {
        String sqlMetricas = "SELECT COUNT(*) as total_prod, " +
                             "COUNT(DISTINCT categoria) as total_cat, " +
                             "SUM(stock) as total_units, " +
                             "SUM(precio * stock) as total_val " +
                             "FROM productos";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlMetricas);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int totalProd = rs.getInt("total_prod");
                int totalCat = rs.getInt("total_cat");
                int totalUnits = rs.getInt("total_units");
                double totalVal = rs.getDouble("total_val");

                lblValTotalProductos.setText(String.valueOf(totalProd));
                lblValCategorias.setText(String.valueOf(totalCat));
                lblValArticulosStock.setText(String.valueOf(totalUnits));
                lblValValorInventario.setText(String.format("$%.2f", totalVal));
            }
        } catch (Exception ex) {
            // Manejo silencioso en caso de error
        }

        modeloBajoStock.setRowCount(0);
        String sqlDetalle = "SELECT codigo, nombre, categoria, stock FROM productos ORDER BY stock ASC";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sqlDetalle);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modeloBajoStock.addRow(new Object[]{
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("stock")
                });
            }
        } catch (Exception ex) {
            // Manejo silencioso en caso de error
        }
    }

    private void cargarCategoriasEnCombo() {
        comboAltaCategoria.removeAllItems();

        TreeSet<String> listaCategorias = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        listaCategorias.add("General");
        listaCategorias.add("Bebidas");
        listaCategorias.add("Botanas");
        listaCategorias.add("Lácteos");
        listaCategorias.add("Enlatados");
        listaCategorias.add("Galletas");
        listaCategorias.add("Cuidado Personal");
        listaCategorias.add("Limpieza del Hogar");
        listaCategorias.add("Panadería");

        String sql = "SELECT DISTINCT categoria FROM productos WHERE categoria IS NOT NULL AND categoria != '' AND LOWER(categoria) != 'abarrotes'";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String cat = rs.getString("categoria").trim();
                if (!cat.isEmpty()) {
                    listaCategorias.add(cat);
                }
            }
        } catch (Exception ex) {
            // Manejo silencioso en caso de error
        }

        for (String cat : listaCategorias) {
            comboAltaCategoria.addItem(cat);
        }
    }

    // --- MÉTODOS DE BASE DE DATOS ---

    private void registrarProductoBD() {
        String codigo = txtAltaCodigo.getText().trim();
        String nombre = txtAltaNombre.getText().trim();
        String strPrecio = txtAltaPrecio.getText().trim();
        String categoria = (String) comboAltaCategoria.getSelectedItem();
        String strStock = txtAltaStock.getText().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || strPrecio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código, Nombre y Precio son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double precio = Double.parseDouble(strPrecio);
            int stock = strStock.isEmpty() ? 0 : Integer.parseInt(strStock);

            String sql = "INSERT INTO productos (codigo, nombre, precio, categoria, stock) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, codigo);
                ps.setString(2, nombre);
                ps.setDouble(3, precio);
                ps.setString(4, (categoria == null || categoria.isEmpty()) ? "General" : categoria);
                ps.setInt(5, stock);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Producto '" + nombre + "' registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                txtAltaCodigo.setText("");
                txtAltaNombre.setText("");
                txtAltaPrecio.setText("");
                txtAltaStock.setText("");

                cargarTablaInventario();
                cargarCategoriasEnCombo();
                cargarReportesEfectivos();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precio y Stock deben ser numéricos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en BD (Posible código duplicado): " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aumentarStockBD() {
        String codigo = txtStockCodigo.getText().trim();
        String strCant = txtStockCantidad.getText().trim();

        if (codigo.isEmpty() || strCant.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el código y la cantidad a sumar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cantidad = Integer.parseInt(strCant);
            if (cantidad <= 0) throw new NumberFormatException();

            String sql = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";
            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, cantidad);
                ps.setString(2, codigo);

                int filas = ps.executeUpdate();
                if (filas > 0) {
                    JOptionPane.showMessageDialog(this, "Stock actualizado correctamente (+" + cantidad + ").", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    txtStockCodigo.setText("");
                    txtStockCantidad.setText("");
                    cargarTablaInventario();
                    cargarReportesEfectivos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró un producto con el código '" + codigo + "'.", "Sin Resultados", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un entero mayor a 0.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar stock: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTablaInventario() {
        modeloInventario.setRowCount(0);
        String sql = "SELECT codigo, nombre, precio, categoria, stock FROM productos";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modeloInventario.addRow(new Object[]{
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        String.format("%.2f", rs.getDouble("precio")),
                        rs.getString("categoria"),
                        rs.getInt("stock")
                });
            }
        } catch (Exception ex) {
            // Manejo silencioso en caso de error
        }
    }

    private JLabel crearLabelForm(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private JTextField crearTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_COLOR);
        tf.setCaretColor(TEXT_COLOR);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 66, 80), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JComboBox<String> crearComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_COLOR);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 66, 80), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return cb;
    }

    private JButton crearBotonEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(PANEL_BG);
        tabla.setForeground(TEXT_COLOR);
        tabla.setGridColor(new Color(50, 56, 68));
        tabla.setRowHeight(30);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setSelectionBackground(ACCENT_PRIMARY);
        tabla.setSelectionForeground(Color.WHITE);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(INPUT_BG);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 66, 80)));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(backgroundColor);
            graphics.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            graphics.dispose();
        }
    }
}