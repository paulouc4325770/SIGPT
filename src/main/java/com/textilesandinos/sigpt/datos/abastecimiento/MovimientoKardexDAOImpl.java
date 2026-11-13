package com.textilesandinos.sigpt.datos.abastecimiento;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;

/**
 * Implementacion JDBC del acceso al kardex.
 */
@Repository
public class MovimientoKardexDAOImpl extends DaoBase implements MovimientoKardexDAO {

    private static final String SQL_BUSCAR = "SELECT id_movimiento, id_insumo, tipo_movimiento, "
            + "cantidad, precio_unitario, id_orden_compra, id_orden_produccion, fecha, "
            + "observaciones FROM movimiento_kardex ";

    public MovimientoKardexDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int registrar(MovimientoKardex movimiento, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO movimiento_kardex (id_insumo, tipo_movimiento, cantidad, "
                            + "precio_unitario, id_orden_compra, id_orden_produccion, fecha, "
                            + "observaciones, usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, movimiento.getIdInsumo());
                sentencia.setString(2, movimiento.getTipoMovimiento());
                sentencia.setBigDecimal(3, movimiento.getCantidad());
                sentencia.setBigDecimal(4, movimiento.getPrecioUnitario());
                if (movimiento.getIdOrdenCompra() == null) {
                    sentencia.setNull(5, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(5, movimiento.getIdOrdenCompra());
                }
                if (movimiento.getIdOrdenProduccion() == null) {
                    sentencia.setNull(6, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(6, movimiento.getIdOrdenProduccion());
                }
                sentencia.setDate(7, Date.valueOf(movimiento.getFecha()));
                sentencia.setString(8, movimiento.getObservaciones());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_movimiento FROM movimiento_kardex WHERE id_insumo = ? "
                            + "ORDER BY id_movimiento DESC LIMIT 1")) {
                sentencia.setInt(1, movimiento.getIdInsumo());
                try (var resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        throw new SQLException("No se pudo recuperar el id del movimiento");
                    }
                    int idMovimiento = resultado.getInt("id_movimiento");
                    actualizarStock(conexion, movimiento);
                    return idMovimiento;
                }
            }
        });
    }

    /** Actualiza el stock del insumo segun el tipo de movimiento. */
    private void actualizarStock(java.sql.Connection conexion, MovimientoKardex movimiento)
            throws SQLException {
        try (var sentencia = conexion.prepareStatement(
                "SELECT stock_actual FROM insumo WHERE id_insumo = ? FOR UPDATE")) {
            sentencia.setInt(1, movimiento.getIdInsumo());
            BigDecimal actual;
            try (var resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("El insumo no existe en el catalogo");
                }
                actual = resultado.getBigDecimal("stock_actual");
            }
            BigDecimal cantidad = movimiento.getCantidad();
            BigDecimal nuevo = switch (movimiento.getTipoMovimiento()) {
                case "INGRESO" -> actual.add(cantidad);
                case "SALIDA" -> actual.subtract(cantidad);
                default -> cantidad;
            };
            try (var actualizacion = conexion.prepareStatement(
                    "UPDATE insumo SET stock_actual = ?, usuario_modificacion = ?, "
                            + "fecha_modificacion = NOW() WHERE id_insumo = ?")) {
                actualizacion.setBigDecimal(1, nuevo);
                actualizacion.setString(2, "sistema");
                actualizacion.setInt(3, movimiento.getIdInsumo());
                actualizacion.executeUpdate();
            }
        }
    }

    @Override
    public List<MovimientoKardex> listarPorInsumo(int idInsumo) {
        return consultarLista(SQL_BUSCAR + "WHERE id_insumo = ? ORDER BY fecha DESC, "
                + "id_movimiento DESC",
                List.of(ParametroSql.entero("id_insumo", idInsumo)),
                MovimientoKardexDAOImpl::mapear);
    }

    @Override
    public List<MovimientoKardex> listarTodos() {
        return consultarLista(SQL_BUSCAR + "ORDER BY fecha DESC, id_movimiento DESC", List.of(),
                MovimientoKardexDAOImpl::mapear);
    }

    @Override
    public List<MovimientoKardex> listarPorOrden(int idOrdenProduccion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_orden_produccion = ? ORDER BY fecha DESC",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                MovimientoKardexDAOImpl::mapear);
    }

    private static MovimientoKardex mapear(ResultSet resultado) throws SQLException {
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setIdMovimiento(resultado.getInt("id_movimiento"));
        movimiento.setIdInsumo(resultado.getInt("id_insumo"));
        movimiento.setTipoMovimiento(resultado.getString("tipo_movimiento"));
        movimiento.setCantidad(resultado.getBigDecimal("cantidad"));
        movimiento.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
        int idOrdenCompra = resultado.getInt("id_orden_compra");
        movimiento.setIdOrdenCompra(resultado.wasNull() ? null : idOrdenCompra);
        int idOrden = resultado.getInt("id_orden_produccion");
        movimiento.setIdOrdenProduccion(resultado.wasNull() ? null : idOrden);
        movimiento.setFecha(resultado.getDate("fecha") == null ? null
                : resultado.getDate("fecha").toLocalDate());
        movimiento.setObservaciones(resultado.getString("observaciones"));
        return movimiento;
    }
}
