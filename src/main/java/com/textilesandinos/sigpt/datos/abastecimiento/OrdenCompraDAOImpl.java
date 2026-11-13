package com.textilesandinos.sigpt.datos.abastecimiento;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompraDetalle;

/**
 * Implementacion JDBC del acceso a ordenes de compra.
 */
@Repository
public class OrdenCompraDAOImpl extends DaoBase implements OrdenCompraDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo "
            + "FROM orden_compra WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_orden_compra, codigo, id_proveedor, "
            + "id_solicitud, id_cotizacion_proveedor, monto_total, moneda, fecha_emision, "
            + "fecha_entrega_esperada, ruta_pdf, estado, observaciones FROM orden_compra ";

    public OrdenCompraDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertarConDetalle(OrdenCompra orden, List<OrdenCompraDetalle> detalle,
            String codUsuario) {
        return enTransaccion(conexion -> {
            int idOrden;
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO orden_compra (codigo, id_proveedor, id_solicitud, "
                            + "id_cotizacion_proveedor, monto_total, moneda, fecha_emision, "
                            + "fecha_entrega_esperada, estado, observaciones, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, orden.getCodigo());
                sentencia.setInt(2, orden.getIdProveedor());
                if (orden.getIdSolicitud() == null) {
                    sentencia.setNull(3, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(3, orden.getIdSolicitud());
                }
                if (orden.getIdCotizacionProveedor() == null) {
                    sentencia.setNull(4, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(4, orden.getIdCotizacionProveedor());
                }
                sentencia.setBigDecimal(5, orden.getMontoTotal());
                sentencia.setString(6, orden.getMoneda());
                sentencia.setDate(7, Date.valueOf(orden.getFechaEmision()));
                if (orden.getFechaEntregaEsperada() == null) {
                    sentencia.setNull(8, java.sql.Types.DATE);
                } else {
                    sentencia.setDate(8, Date.valueOf(orden.getFechaEntregaEsperada()));
                }
                sentencia.setString(9, orden.getEstado());
                sentencia.setString(10, orden.getObservaciones());
                sentencia.setString(11, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_orden_compra FROM orden_compra WHERE codigo = ?")) {
                sentencia.setString(1, orden.getCodigo());
                try (var resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        throw new SQLException("No se pudo recuperar el id de la orden insertada");
                    }
                    idOrden = resultado.getInt("id_orden_compra");
                }
            }
            for (OrdenCompraDetalle linea : detalle) {
                try (var sentencia = conexion.prepareStatement(
                        "INSERT INTO orden_compra_detalle (id_orden_compra, id_insumo, cantidad, "
                                + "precio_unitario, usuario_registro) VALUES (?, ?, ?, ?, ?)")) {
                    sentencia.setInt(1, idOrden);
                    sentencia.setInt(2, linea.getIdInsumo());
                    sentencia.setBigDecimal(3, linea.getCantidad());
                    sentencia.setBigDecimal(4, linea.getPrecioUnitario());
                    sentencia.setString(5, codUsuario == null ? "sistema" : codUsuario);
                    sentencia.executeUpdate();
                }
            }
            return idOrden;
        });
    }

    @Override
    public List<OrdenCompra> listarTodas() {
        return consultarLista(SQL_BUSCAR + "ORDER BY fecha_emision DESC, id_orden_compra DESC",
                List.of(), OrdenCompraDAOImpl::mapear);
    }

    @Override
    public Optional<OrdenCompra> buscarPorId(int idOrdenCompra) {
        return consultarUno(SQL_BUSCAR + "WHERE id_orden_compra = ?",
                List.of(ParametroSql.entero("id_orden_compra", idOrdenCompra)),
                OrdenCompraDAOImpl::mapear);
    }

    @Override
    public List<OrdenCompraDetalle> listarDetalle(int idOrdenCompra) {
        return consultarLista("SELECT id_orden_compra, id_insumo, cantidad, precio_unitario "
                        + "FROM orden_compra_detalle WHERE id_orden_compra = ? ORDER BY id_insumo",
                List.of(ParametroSql.entero("id_orden_compra", idOrdenCompra)),
                resultado -> {
                    OrdenCompraDetalle linea = new OrdenCompraDetalle();
                    linea.setIdOrdenCompra(resultado.getInt("id_orden_compra"));
                    linea.setIdInsumo(resultado.getInt("id_insumo"));
                    linea.setCantidad(resultado.getBigDecimal("cantidad"));
                    linea.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
                    return linea;
                });
    }

    @Override
    public int actualizarEstado(int idOrdenCompra, String estado) {
        return ejecutar("UPDATE orden_compra SET estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_orden_compra = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_orden_compra", idOrdenCompra)));
    }

    @Override
    public int actualizarRutaPdf(int idOrdenCompra, String rutaPdf) {
        return ejecutar("UPDATE orden_compra SET ruta_pdf = ? WHERE id_orden_compra = ?",
                List.of(ParametroSql.texto("ruta_pdf", rutaPdf),
                        ParametroSql.entero("id_orden_compra", idOrdenCompra)));
    }

    @Override
    public String siguienteCodigo() {
        return siguienteCodigo(SQL_ULTIMO_CODIGO, "OC-" + LocalDate.now().getYear() + "-", 3,
                codigo -> consultarUno("SELECT id_orden_compra FROM orden_compra WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_orden_compra")).isPresent());
    }

    private static OrdenCompra mapear(ResultSet resultado) throws SQLException {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(resultado.getInt("id_orden_compra"));
        orden.setCodigo(resultado.getString("codigo"));
        orden.setIdProveedor(resultado.getInt("id_proveedor"));
        int idSolicitud = resultado.getInt("id_solicitud");
        orden.setIdSolicitud(resultado.wasNull() ? null : idSolicitud);
        int idCotizacion = resultado.getInt("id_cotizacion_proveedor");
        orden.setIdCotizacionProveedor(resultado.wasNull() ? null : idCotizacion);
        orden.setMontoTotal(resultado.getBigDecimal("monto_total"));
        orden.setMoneda(resultado.getString("moneda"));
        orden.setFechaEmision(resultado.getDate("fecha_emision") == null ? null
                : resultado.getDate("fecha_emision").toLocalDate());
        orden.setFechaEntregaEsperada(resultado.getDate("fecha_entrega_esperada") == null ? null
                : resultado.getDate("fecha_entrega_esperada").toLocalDate());
        orden.setRutaPdf(resultado.getString("ruta_pdf"));
        orden.setEstado(resultado.getString("estado"));
        orden.setObservaciones(resultado.getString("observaciones"));
        return orden;
    }
}
