package com.textilesandinos.sigpt.datos.abastecimiento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.CotizacionProveedor;

/**
 * Implementacion JDBC del acceso a cotizaciones de proveedores.
 */
@Repository
public class CotizacionProveedorDAOImpl extends DaoBase implements CotizacionProveedorDAO {

    private static final String SQL_BUSCAR = "SELECT id_cotizacion_proveedor, id_solicitud, "
            + "id_proveedor, precio_unitario, unidad, tiempo_entrega_dias, certificaciones, "
            + "observaciones, estado FROM cotizacion_proveedor ";

    public CotizacionProveedorDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(CotizacionProveedor cotizacion, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO cotizacion_proveedor (id_solicitud, id_proveedor, "
                            + "precio_unitario, unidad, tiempo_entrega_dias, certificaciones, "
                            + "observaciones, estado, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, cotizacion.getIdSolicitud());
                sentencia.setInt(2, cotizacion.getIdProveedor());
                sentencia.setBigDecimal(3, cotizacion.getPrecioUnitario());
                sentencia.setString(4, cotizacion.getUnidad());
                sentencia.setInt(5, cotizacion.getTiempoEntregaDias());
                sentencia.setString(6, cotizacion.getCertificaciones());
                sentencia.setString(7, cotizacion.getObservaciones());
                sentencia.setString(8, cotizacion.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_cotizacion_proveedor FROM cotizacion_proveedor "
                            + "WHERE id_solicitud = ? AND id_proveedor = ?")) {
                sentencia.setInt(1, cotizacion.getIdSolicitud());
                sentencia.setInt(2, cotizacion.getIdProveedor());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_cotizacion_proveedor");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la cotizacion insertada");
        });
    }

    @Override
    public List<CotizacionProveedor> listarPorSolicitud(int idSolicitud) {
        return consultarLista(SQL_BUSCAR + "WHERE id_solicitud = ? ORDER BY precio_unitario",
                List.of(ParametroSql.entero("id_solicitud", idSolicitud)),
                CotizacionProveedorDAOImpl::mapear);
    }

    @Override
    public Optional<CotizacionProveedor> buscarPorId(int idCotizacionProveedor) {
        return consultarUno(SQL_BUSCAR + "WHERE id_cotizacion_proveedor = ?",
                List.of(ParametroSql.entero("id_cotizacion_proveedor", idCotizacionProveedor)),
                CotizacionProveedorDAOImpl::mapear);
    }

    @Override
    public int actualizarEstado(int idCotizacionProveedor, String estado) {
        return ejecutar("UPDATE cotizacion_proveedor SET estado = ? WHERE id_cotizacion_proveedor = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.entero("id_cotizacion_proveedor", idCotizacionProveedor)));
    }

    private static CotizacionProveedor mapear(ResultSet resultado) throws SQLException {
        CotizacionProveedor cotizacion = new CotizacionProveedor();
        cotizacion.setIdCotizacionProveedor(resultado.getInt("id_cotizacion_proveedor"));
        cotizacion.setIdSolicitud(resultado.getInt("id_solicitud"));
        cotizacion.setIdProveedor(resultado.getInt("id_proveedor"));
        cotizacion.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
        cotizacion.setUnidad(resultado.getString("unidad"));
        cotizacion.setTiempoEntregaDias(resultado.getInt("tiempo_entrega_dias"));
        cotizacion.setCertificaciones(resultado.getString("certificaciones"));
        cotizacion.setObservaciones(resultado.getString("observaciones"));
        cotizacion.setEstado(resultado.getString("estado"));
        return cotizacion;
    }
}
