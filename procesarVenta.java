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

            // 1. Insertar la venta general (Obteniendo el id_venta generado)
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
                throw new Exception("No se pudo generar el ID de la venta.");
            }

            // 2. Insertar cada producto en 'detalle_ventas' y actualizar el stock
            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlUpdateStock)) {

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String codigo = (String) modeloTabla.getValueAt(i, 0);
                    int cantidad = (int) modeloTabla.getValueAt(i, 3);
                    
                    String strPrecioUnit = (String) modeloTabla.getValueAt(i, 4);
                    double precioUnit = Double.parseDouble(strPrecioUnit.replace(",", "."));
                    
                    String strPrecioTotal = (String) modeloTabla.getValueAt(i, 5);
                    double precioTotal = Double.parseDouble(strPrecioTotal.replace(",", "."));

                    // Detalle de la venta
                    psDetalle.setInt(1, idVentaGenerado);
                    psDetalle.setString(2, codigo);
                    psDetalle.setInt(3, cantidad);
                    psDetalle.setDouble(4, precioUnit);
                    psDetalle.setDouble(5, precioTotal);
                    psDetalle.addBatch();

                    // Restar stock
                    psStock.setInt(1, cantidad);
                    psStock.setString(2, codigo);
                    psStock.addBatch();
                }

                psDetalle.executeBatch();
                psStock.executeBatch();
            }

            // Confirmar cambios en MySQL
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