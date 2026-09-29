import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VentaModelo {

    private List<DetalleVenta> listaProductos;

    public VentaModelo() {
        this.listaProductos = new ArrayList<>();
    }

    public void agregarProducto(DetalleVenta producto) {
        this.listaProductos.add(producto);
    }

    public List<DetalleVenta> getListaProductos() {
        return listaProductos;
    }

    public void limpiarVenta() {
        this.listaProductos.clear();
    }

    public double getTotalAcumulado() {
        double total = 0.0;
        for (DetalleVenta item : listaProductos) {
            total += item.getSubtotal();
        }
        return total;
    }

    // --- 1. REGISTRAR CABECERA EN 'ventas' ---
    public int registrarVentaBD(double total, String metodoPago) {
        String sql = "INSERT INTO ventas (fecha, total, metodo_pago) VALUES (?, ?, ?)";
        int idGenerado = -1;

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setDouble(2, total);
            ps.setString(3, metodoPago);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Obtiene el id_venta generado
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al registrar venta: " + e.getMessage());
            e.printStackTrace();
        }
        return idGenerado;
    }

    // --- 2. REGISTRAR DETALLE EN 'detalle_ventas' (Plural y con 'precio_total') ---
    public boolean registrarDetalleVentaBD(int idVenta, DetalleVenta item) {
        // Corregido: detalle_ventas y precio_total
        String sql = "INSERT INTO detalle_ventas (id_venta, codigo_producto, cantidad, precio_unitario, precio_total) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            ps.setString(2, item.getCodigo());
            ps.setInt(3, item.getCantidad());
            ps.setDouble(4, item.getPrecio());
            ps.setDouble(5, item.getSubtotal());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar detalle_ventas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- 3. ACTUALIZAR STOCK EN 'productos' ---
    public boolean actualizarStockBD(String codigoProducto, int cantidadVendida) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE codigo = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cantidadVendida);
            ps.setString(2, codigoProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}