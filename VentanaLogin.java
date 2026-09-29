import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class VentanaLogin extends JFrame {

    private JComboBox<String> comboRol;
    private JPasswordField txtPassword;

    private static final Color BG_DARK = new Color(24, 26, 32);
    private static final Color PANEL_BG = new Color(33, 37, 45);
    private static final Color INPUT_BG = new Color(44, 49, 60);
    private static final Color TEXT_COLOR = new Color(230, 235, 245);
    private static final Color TEXT_MUTED = new Color(140, 147, 160);
    private static final Color ACCENT_PRIMARY = new Color(99, 102, 241);

    public VentanaLogin() {
        setTitle("ACCESO — MISCELÁNEA POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 360);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new EmptyBorder(24, 24, 24, 24));

        RoundedPanel panelTarjeta = new RoundedPanel(16, PANEL_BG);
        panelTarjeta.setLayout(new GridLayout(6, 1, 8, 8));
        panelTarjeta.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("INICIAR SESIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(TEXT_COLOR);

        JLabel lblRol = new JLabel("Seleccione el tipo de usuario:");
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRol.setForeground(TEXT_MUTED);

        comboRol = new JComboBox<>(new String[]{"Personal / Cajero", "Encargado / Administrador"});
        comboRol.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboRol.setBackground(INPUT_BG);
        comboRol.setForeground(TEXT_COLOR);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_MUTED);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setBackground(INPUT_BG);
        txtPassword.setForeground(TEXT_COLOR);
        txtPassword.setCaretColor(TEXT_COLOR);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 66, 80), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton btnIngresar = new JButton("Ingresar al Sistema");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setBackground(ACCENT_PRIMARY);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelTarjeta.add(lblTitulo);
        panelTarjeta.add(lblRol);
        panelTarjeta.add(comboRol);
        panelTarjeta.add(lblPass);
        panelTarjeta.add(txtPassword);
        panelTarjeta.add(btnIngresar);

        panelPrincipal.add(panelTarjeta, BorderLayout.CENTER);
        add(panelPrincipal);

        btnIngresar.addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String rol = (String) comboRol.getSelectedItem();
        String password = new String(txtPassword.getPassword());

        VentaModelo modelo = new VentaModelo();
        VentaControlador controlador = new VentaControlador(modelo);

        if ("Personal / Cajero".equals(rol)) {
            if ("1234".equals(password)) {
                // Abre el módulo exclusivo del trabajador (Módulo de Ventas)
                this.dispose();
                VentanaInventario ventanaPersonal = new VentanaInventario(controlador, "PERSONAL");
                ventanaPersonal.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Contraseña de Personal incorrecta.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            }
        } else if ("Encargado / Administrador".equals(rol)) {
            if ("admin123".equals(password)) {
                // Abre el panel completo del encargado
                this.dispose();
                VentanaAdministrador ventanaAdmin = new VentanaAdministrador(controlador);
                ventanaAdmin.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Contraseña de Administrador incorrecta.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
    }
}