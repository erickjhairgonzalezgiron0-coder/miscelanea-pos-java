import java.awt.Component;

import javax.swing.SwingUtilities;

public class InterfaceMiscelanea {
    public InterfaceMiscelanea() {
        // Redirige directamente al diseño de la ventana de inventario
    }

    public void setVisible(boolean b) {
        SwingUtilities.invokeLater(() -> {
            // Pasa null si no tienes un VentaControlador instanciado aquí
            new VentanaInventario(null).setVisible(b);
        });
    }

    public void setLocationRelativeTo(Component c) {
        // Método de compatibilidad
    }
}