import java.util.List;
import javax.swing.JOptionPane;

public class VentaControlador {
    private VentaModelo modelo;

    // Constructor original para mantener compatibilidad con VentanaLogin
    public VentaControlador(VentaModelo modelo) {
        this.modelo = modelo;
    }

    public DetalleVenta registrarProducto(String codigo, String nombre, String strPrecio, String strCantidad) throws Exception {
        if (codigo.isEmpty() || nombre.isEmpty()) {
            throw new Exception("Debe ingresar el código y nombre del producto.");
        }
        
        double precio = Double.parseDouble(strPrecio);
        int cantidad = Integer.parseInt(strCantidad);
        
        if (precio <= 0 || cantidad <= 0) {
            throw new Exception("El precio y la cantidad deben ser mayores a cero.");
        }

        DetalleVenta producto = new DetalleVenta(codigo, nombre, cantidad, precio);
        modelo.agregarProducto(producto);
        return producto;
    }

    /**
     * Procesa la compra, guarda la Venta principal y el Detalle_Venta en MySQL.
     * 
     * @param pagoCliente Monto entregado por el cliente.
     * @return boolean Verdadero si la venta y sus detalles se guardaron correctamente.
     */
    public boolean procesarTransaccionConDetalle(double pagoCliente) {
        List<DetalleVenta> lista = modelo.getListaProductos();

        if (lista == null || lista.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El carrito de compras está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        double total = modelo.getTotalAcumulado();

        if (pagoCliente < total) {
            JOptionPane.showMessageDialog(null, "El monto pagado es menor al total de la compra.", "Efectivo Insuficiente", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double cambio = pagoCliente - total;

        // 1. Guardar la Venta principal en MySQL y obtener su ID generado
        int idVentaGenerado = modelo.registrarVentaBD(total, pagoCliente, cambio);

        if (idVentaGenerado > 0) {
            // 2. Guardar cada ítem de la lista en la tabla detalle_venta y actualizar el Stock
            boolean detallesGuardados = true;

            for (DetalleVenta item : lista) {
                boolean exitoDetalle = modelo.registrarDetalleVentaBD(idVentaGenerado, item);
                boolean exitoStock = modelo.actualizarStockBD(item.getCodigo(), item.getCantidad());

                if (!exitoDetalle || !exitoStock) {
                    detallesGuardados = false;
                }
            }

            if (detallesGuardados) {
                JOptionPane.showMessageDialog(null, 
                    String.format("¡Venta #%d registrada exitosamente!\n\nTotal: $%.2f\nPago: $%.2f\nCambio: $%.2f", 
                        idVentaGenerado, total, pagoCliente, cambio),
                    "Venta Completada", 
                    JOptionPane.INFORMATION_MESSAGE);

                modelo.limpiarVenta();
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "La venta se creó pero hubo un inconveniente al guardar algunos detalles de productos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                modelo.limpiarVenta();
                return true;
            }
        } else {
            JOptionPane.showMessageDialog(null, "Error al registrar la transacción en la Base de Datos.", "Error BD", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void cancelarTransaccion() {
        modelo.limpiarVenta();
    }

    public double getTotal() {
        return modelo.getTotalAcumulado();
    }
}