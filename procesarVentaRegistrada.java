import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.swing.JOptionPane;

public class procesarVentaRegistrada {

    // REGISTRAR PRODUCTO EN MYSQL CON VALIDACIÓN
    public static boolean registrarProductoEnExcel(String codigo, String nombre, double precio, String categoria, int stockInicial) {
        String codLimpio = codigo.trim();
        String nomLimpio = nombre.trim();

        String queryExiste = "SELECT codigo, nombre FROM productos WHERE codigo = ? OR nombre = ?";
        String queryInsert = "INSERT INTO productos (codigo, nombre, precio, categoria, stock) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement psExiste = con.prepareStatement(queryExiste)) {

            psExiste.setString(1, codLimpio);
            psExiste.setString(2, nomLimpio);
            ResultSet rs = psExiste.executeQuery();

            if (rs.next()) {
                String codExist = rs.getString("codigo");
                if (codExist.equalsIgnoreCase(codLimpio)) {
                    JOptionPane.showMessageDialog(null, "❌ CÓDIGO REPETIDO:\nEl código '" + codLimpio + "' ya está registrado.", "Registro Rechazado", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "❌ NOMBRE REPETIDO:\nEl producto '" + nomLimpio + "' ya existe en el inventario.", "Registro Rechazado", JOptionPane.WARNING_MESSAGE);
                }
                return false;
            }

            try (PreparedStatement psInsert = con.prepareStatement(queryInsert)) {
                psInsert.setString(1, codLimpio);
                psInsert.setString(2, nomLimpio);
                psInsert.setDouble(3, precio);
                psInsert.setString(4, categoria.trim());
                psInsert.setInt(5, stockInicial);
                psInsert.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "⚠️ ERROR DE BASE DE DATOS:\nVerifica que XAMPP y MySQL estén activos.\n\nDetalle: " + e.getMessage(), "Error en Base de Datos", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // BUSCAR PRODUCTO EN MYSQL POR CÓDIGO
    public String[] buscarProductoPorCodigo(String codigoBuscado) {
        String query = "SELECT codigo, nombre, precio, categoria, stock FROM productos WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codigoBuscado.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[]{
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    String.valueOf(rs.getDouble("precio")),
                    rs.getString("categoria"),
                    String.valueOf(rs.getInt("stock"))
                };
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    // ACTUALIZAR STOCK ABSOLUTO DE UN PRODUCTO EXISTENTE EN MYSQL
    public boolean actualizarStock(String codigo, int nuevoStock) {
        String sql = "UPDATE productos SET stock = ? WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevoStock);
            ps.setString(2, codigo.trim());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el stock: " + e.getMessage());
            return false;
        }
    }

    // SUMAR STOCK A UN PRODUCTO EXISTENTE
    public boolean sumarStock(String codigo, int cantidadASumar) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cantidadASumar);
            ps.setString(2, codigo.trim());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al sumar stock: " + e.getMessage());
            return false;
        }
    }

    // MÉTODO PARA REABASTECER STOCK DESDE LA INTERFAZ
    public boolean actualizarStockProducto(String codigo, int cantidadAñadir) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, cantidadAñadir);
            ps.setString(2, codigo.trim());

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // REGISTRAR VENTA Y DESCONTAR STOCK EN MYSQL
    public void procesar(List<DetalleVenta> listaProductos, double total, String metodoPago) throws Exception {
        String insertVenta = "INSERT INTO ventas (total, metodo_pago) VALUES (?, ?)";
        String insertDetalle = "INSERT INTO detalle_ventas (id_venta, codigo_producto, cantidad, precio_unitario, precio_total) VALUES (?, ?, ?, ?, ?)";
        String updateStock = "UPDATE productos SET stock = stock - ? WHERE codigo = ? AND stock >= ?";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false); // Transacción atómica

            try {
                // 1. Verificar stock suficiente
                for (DetalleVenta det : listaProductos) {
                    String[] prod = buscarProductoPorCodigo(det.getIdProducto());
                    if (prod == null) {
                        throw new Exception("El producto con código " + det.getIdProducto() + " no existe.");
                    }
                    int stockActual = Integer.parseInt(prod[4]);
                    if (stockActual < det.getCantidad()) {
                        throw new Exception("Stock insuficiente para '" + prod[1] + "'. Disponible: " + stockActual + ", Solicitado: " + det.getCantidad());
                    }
                }

                // 2. Registrar encabezado de Venta
                int idVentaGenerado = -1;
                try (PreparedStatement psVenta = con.prepareStatement(insertVenta, Statement.RETURN_GENERATED_KEYS)) {
                    psVenta.setDouble(1, total);
                    psVenta.setString(2, metodoPago);
                    psVenta.executeUpdate();

                    ResultSet rsKey = psVenta.getGeneratedKeys();
                    if (rsKey.next()) {
                        idVentaGenerado = rsKey.getInt(1);
                    }
                }

                // 3. Registrar detalles de Venta y actualizar stock
                try (PreparedStatement psDetalle = con.prepareStatement(insertDetalle);
                     PreparedStatement psStock = con.prepareStatement(updateStock)) {

                    for (DetalleVenta det : listaProductos) {
                        double subtotal = det.getCantidad() * det.getPrecioUnitario();

                        psDetalle.setInt(1, idVentaGenerado);
                        psDetalle.setString(2, det.getIdProducto());
                        psDetalle.setInt(3, det.getCantidad());
                        psDetalle.setDouble(4, det.getPrecioUnitario());
                        psDetalle.setDouble(5, subtotal);
                        psDetalle.addBatch();

                        psStock.setInt(1, det.getCantidad());
                        psStock.setString(2, det.getIdProducto());
                        psStock.setInt(3, det.getCantidad());
                        psStock.addBatch();
                    }

                    psDetalle.executeBatch();
                    psStock.executeBatch();
                }

                con.commit(); // Confirmar cambios
            } catch (Exception ex) {
                con.rollback(); // Revertir en caso de fallas
                throw ex;
            }
        }
    }
}