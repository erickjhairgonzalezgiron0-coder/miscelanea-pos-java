import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class VentanaInventario extends JFrame {

    private JTextField txtEscanerCodigo, txtEscanerCant;
    private JTextField txtBuscarNombre, txtBuscarCant;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado, lblTotalCalculado;

    private VentaControlador controlador;
    private double totalAcumulado = 0.0;

    private static final Color BG_DARK = new Color(24, 26, 32);
    private static final Color PANEL_BG = new Color(33, 37, 45);
    private static final Color INPUT_BG = new Color(44, 49, 60);
    private static final Color TEXT_COLOR = new Color(230, 235, 245);
    private static final Color TEXT_MUTED = new Color(140, 147, 160);
    private static final Color ACCENT_PRIMARY = new Color(99, 102, 241);
    private static final Color ACCENT_SUCCESS = new Color(16, 185, 129);
    private static final Color ACCENT_DANGER = new Color(239, 68, 68);

    public VentanaInventario(VentaControlador controlador) {
        this(controlador, "PERSONAL");
    }

    public VentanaInventario(VentaControlador controlador, String rolUsuario) {
        this.controlador = controlador;

        setTitle("PUNTO DE VENTA & INVENTARIO — MISCELÁNEA (" + rolUsuario + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(BG_DARK);
        panelPrincipal.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Header
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);
        panelHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitulo = new JLabel("MISCELÁNEA POS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(TEXT_COLOR);

        JLabel lblSubtitulo = new JLabel("Arquitectura MVC — Sesión: " + rolUsuario);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TEXT_MUTED);

        JPanel panelTextosHeader = new JPanel(new GridLayout(2, 1));
        panelTextosHeader.setOpaque(false);
        panelTextosHeader.add(lblTitulo);
        panelTextosHeader.add(lblSubtitulo);
        panelHeader.add(panelTextosHeader, BorderLayout.WEST);

        // Pestañas (Código de Barras vs Búsqueda)
        JTabbedPane pestañasModos = new JTabbedPane();
        pestañasModos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pestañasModos.setBackground(PANEL_BG);
        pestañasModos.setForeground(TEXT_COLOR);

        // Pestaña 1: Código de Barras
        JPanel panelEscaner = crearPanelEscaner();
        pestañasModos.addTab("📷 Código de Barras", panelEscaner);

        // Pestaña 2: Búsqueda Manual
        JPanel panelBusqueda = crearPanelBusquedaManual();
        pestañasModos.addTab("🔍 Búsqueda de Productos", panelBusqueda);

        // Tabla Central de Productos Agregados
        RoundedPanel panelCentral = new RoundedPanel(16, PANEL_BG);
        panelCentral.setLayout(new BorderLayout(10, 10));
        panelCentral.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] columnas = {"ID", "Descripción", "Categoría", "Cant.", "P.Unit ($)", "Total ($)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaProductos = new JTable(modeloTabla);
        estilizarTabla(tablaProductos);

        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.getViewport().setBackground(PANEL_BG);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setPreferredSize(new Dimension(400, 220));
        panelCentral.add(scrollTabla, BorderLayout.CENTER);

        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTotal.setOpaque(false);
        lblTotalCalculado = new JLabel("Total: $0.00");
        lblTotalCalculado.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalCalculado.setForeground(ACCENT_SUCCESS);
        panelTotal.add(lblTotalCalculado);
        panelCentral.add(panelTotal, BorderLayout.SOUTH);

        // Panel Inferior (Acciones sin botón "+ Nuevo")
        RoundedPanel panelInferior = new RoundedPanel(16, PANEL_BG);
        panelInferior.setLayout(new BorderLayout(10, 10));
        panelInferior.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel panelAccionesBotones = new JPanel(new GridLayout(1, 2, 12, 0));
        panelAccionesBotones.setOpaque(false);

        JButton btnConfirmar = crearBotonEstilizado("✓ Procesar Venta", ACCENT_SUCCESS, Color.WHITE);
        JButton btnCancelar = crearBotonEstilizado("✕ Cancelar Venta", ACCENT_DANGER, Color.WHITE);

        panelAccionesBotones.add(btnConfirmar);
        panelAccionesBotones.add(btnCancelar);

        lblEstado = new JLabel("Estado: Escanee un código de barras o busque un producto...");
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstado.setForeground(TEXT_MUTED);

        panelInferior.add(panelAccionesBotones, BorderLayout.CENTER);
        panelInferior.add(lblEstado, BorderLayout.SOUTH);

        panelPrincipal.add(panelHeader);
        panelPrincipal.add(pestañasModos);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));
        panelPrincipal.add(panelCentral);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));
        panelPrincipal.add(panelInferior);

        add(panelPrincipal);

        // Eventos
        btnCancelar.addActionListener((ActionEvent e) -> cancelarVenta());
        btnConfirmar.addActionListener((ActionEvent e) -> procesarVenta());
    }

    private JPanel crearPanelEscaner() {
        RoundedPanel panel = new RoundedPanel(16, PANEL_BG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEscanerCodigo = crearTextField();
        txtEscanerCant = crearTextField();
        txtEscanerCant.setText("1");

        JButton btnAgregarEscaner = crearBotonEstilizado("+ Agregar", ACCENT_PRIMARY, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(crearLabelForm("Código de Barras:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(txtEscanerCodigo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(crearLabelForm("Cantidad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(txtEscanerCant, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnAgregarEscaner, gbc);

        // Escanear al presionar Enter en el campo de código
        txtEscanerCodigo.addActionListener(e -> buscarYAgregarBD(txtEscanerCodigo.getText().trim(), txtEscanerCant.getText().trim()));
        btnAgregarEscaner.addActionListener(e -> buscarYAgregarBD(txtEscanerCodigo.getText().trim(), txtEscanerCant.getText().trim()));

        return panel;
    }

    private JPanel crearPanelBusquedaManual() {
        RoundedPanel panel = new RoundedPanel(16, PANEL_BG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtBuscarNombre = crearTextField();
        txtBuscarCant = crearTextField();
        txtBuscarCant.setText("1");

        JButton btnBuscar = crearBotonEstilizado("🔍 Buscar en BD", ACCENT_PRIMARY, Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(crearLabelForm("Producto / Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(txtBuscarNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(crearLabelForm("Cantidad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(txtBuscarCant, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnBuscar, gbc);

        btnBuscar.addActionListener(e -> buscarYAgregarBD(txtBuscarNombre.getText().trim(), txtBuscarCant.getText().trim()));

        return panel;
    }

    // Consulta a MySQL y suma de productos repetidos
    private void buscarYAgregarBD(String termino, String strCant) {
        if (termino.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código de barras o nombre.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = 1;
        try {
            cantidad = Integer.parseInt(strCant);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT codigo, nombre, precio, categoria FROM productos WHERE codigo = ? OR nombre LIKE ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, termino);
            ps.setString(2, "%" + termino + "%");

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String codigo = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");
                String categoria = rs.getString("categoria");
                if (categoria == null || categoria.trim().isEmpty()) {
                    categoria = "General";
                }

                agregarOSumarProductoTabla(codigo, nombre, categoria, cantidad, precio);
                lblEstado.setText("Estado: Producto '" + nombre + "' agregado.");

                txtEscanerCodigo.setText("");
                txtBuscarNombre.setText("");
                txtEscanerCodigo.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Producto '" + termino + "' no encontrado en la Base de Datos.", "Sin Resultados", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al conectar a la Base de Datos: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Si el producto ya existe en la tabla, suma la cantidad en vez de crear otra fila
    private void agregarOSumarProductoTabla(String codigo, String nombre, String categoria, int cantidad, double precio) {
        boolean existe = false;
        int filas = modeloTabla.getRowCount();

        for (int i = 0; i < filas; i++) {
            String codTabla = (String) modeloTabla.getValueAt(i, 0);
            if (codTabla.equalsIgnoreCase(codigo)) {
                int cantActual = (int) modeloTabla.getValueAt(i, 3);
                int nuevaCant = cantActual + cantidad;
                double nuevoSubtotal = nuevaCant * precio;

                modeloTabla.setValueAt(nuevaCant, i, 3);
                modeloTabla.setValueAt(String.format("%.2f", nuevoSubtotal), i, 5);
                existe = true;
                break;
            }
        }

        if (!existe) {
            double subtotal = cantidad * precio;
            modeloTabla.addRow(new Object[]{
                    codigo,
                    nombre,
                    categoria,
                    cantidad,
                    String.format("%.2f", precio),
                    String.format("%.2f", subtotal)
            });
        }

        recalcularTotal();
    }

    private void recalcularTotal() {
        totalAcumulado = 0.0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String strSub = (String) modeloTabla.getValueAt(i, 5);
            totalAcumulado += Double.parseDouble(strSub.replace(",", "."));
        }
        lblTotalCalculado.setText(String.format("Total: $%.2f", totalAcumulado));
    }

    private void cancelarVenta() {
        modeloTabla.setRowCount(0);
        lblTotalCalculado.setText("Total: $0.00");
        lblEstado.setText("Estado: Transacción cancelada.");
    }

    private void procesarVenta() {
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "La lista de venta está vacía.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sqlVenta = "INSERT INTO ventas (fecha, total, metodo_pago) VALUES (NOW(), ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_ventas (id_venta, codigo_producto, cantidad, precio_unitario, precio_total) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE productos SET stock = stock - ? WHERE codigo = ?";

        Connection con = null;

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false); // Iniciar transacción segura

            // 1. Insertar el registro principal en la tabla 'ventas'
            int idVentaGenerado = -1;
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psVenta.setDouble(1, totalAcumulado);
                psVenta.setString(2, "Efectivo");
                psVenta.executeUpdate();

                ResultSet rsKeys = psVenta.getGeneratedKeys();
                if (rsKeys.next()) {
                    idVentaGenerado = rsKeys.getInt(1);
                }
            }

            if (idVentaGenerado == -1) {
                throw new Exception("No se pudo obtener el ID de la venta generada.");
            }

            // 2. Insertar los elementos en 'detalle_ventas' y actualizar existencias
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlUpdateStock)) {

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String codigo = (String) modeloTabla.getValueAt(i, 0);
                    int cantidad = (int) modeloTabla.getValueAt(i, 3);
                    
                    String strPrecioUnit = (String) modeloTabla.getValueAt(i, 4);
                    double precioUnit = Double.parseDouble(strPrecioUnit.replace(",", "."));
                    
                    String strPrecioTotal = (String) modeloTabla.getValueAt(i, 5);
                    double precioTotal = Double.parseDouble(strPrecioTotal.replace(",", "."));

                    // Cargar batch para la tabla detalle_ventas
                    psDetalle.setInt(1, idVentaGenerado);
                    psDetalle.setString(2, codigo);
                    psDetalle.setInt(3, cantidad);
                    psDetalle.setDouble(4, precioUnit);
                    psDetalle.setDouble(5, precioTotal);
                    psDetalle.addBatch();

                    // Cargar batch para actualizar stock
                    psStock.setInt(1, cantidad);
                    psStock.setString(2, codigo);
                    psStock.addBatch();
                }

                psDetalle.executeBatch();
                psStock.executeBatch();
            }

            // Guardar cambios en la Base de Datos
            con.commit();

            JOptionPane.showMessageDialog(this, 
                "Venta procesada y guardada en MySQL con éxito.\nTotal: $" + String.format("%.2f", totalAcumulado), 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);

            cancelarVenta();

        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (Exception ignored) {}
            }
            JOptionPane.showMessageDialog(this, 
                "Error al guardar la venta en la Base de Datos: " + ex.getMessage(), 
                "Error BD", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                } catch (Exception ignored) {}
            }
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

    private JButton crearBotonEstilizado(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setBackground(PANEL_BG);
        tabla.setForeground(TEXT_COLOR);
        tabla.setGridColor(new Color(50, 56, 68));
        tabla.setRowHeight(32);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setSelectionBackground(ACCENT_PRIMARY);
        tabla.setSelectionForeground(Color.WHITE);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(INPUT_BG);
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 36));
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

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            VentanaLogin login = new VentanaLogin();
            login.setVisible(true);
        });
    }
}