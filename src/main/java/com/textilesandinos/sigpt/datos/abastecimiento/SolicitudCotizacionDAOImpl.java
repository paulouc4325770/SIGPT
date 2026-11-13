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
import com.textilesandinos.sigpt.entidades.abastecimiento.SolicitudCotizacion;

/**
 * Implementacion JDBC del acceso a solicitudes de cotizacion.
 */
@Repository
public class SolicitudCotizacionDAOImpl extends DaoBase implements SolicitudCotizacionDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo "
            + "FROM solicitud_cotizacion WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_solicitud, codigo, id_orden_produccion, "
            + "fecha_emision, fecha_cierre, estado, observaciones FROM solicitud_cotizacion ";

    public SolicitudCotizacionDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(SolicitudCotizacion solicitud, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO solicitud_cotizacion (codigo, id_orden_produccion, fecha_emision, "
                            + "estado, observaciones, usuario_registro) VALUES (?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, solicitud.getCodigo());
                sentencia.setInt(2, solicitud.getIdOrdenProduccion());
                sentencia.setDate(3, Date.valueOf(solicitud.getFechaEmision()));
                sentencia.setString(4, solicitud.getEstado());
                sentencia.setString(5, solicitud.getObservaciones());
                sentencia.setString(6, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_solicitud FROM solicitud_cotizacion WHERE codigo = ?")) {
                sentencia.setString(1, solicitud.getCodigo());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_solicitud");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la solicitud insertada");
        });
    }

    @Override
    public List<SolicitudCotizacion> listarTodas() {
        return consultarLista(SQL_BUSCAR + "ORDER BY fecha_emision DESC, id_solicitud DESC",
                List.of(), SolicitudCotizacionDAOImpl::mapear);
    }

    @Override
    public List<SolicitudCotizacion> listarPorOrden(int idOrdenProduccion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_orden_produccion = ? ORDER BY id_solicitud DESC",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                SolicitudCotizacionDAOImpl::mapear);
    }

    @Override
    public Optional<SolicitudCotizacion> buscarPorId(int idSolicitud) {
        return consultarUno(SQL_BUSCAR + "WHERE id_solicitud = ?",
                List.of(ParametroSql.entero("id_solicitud", idSolicitud)),
                SolicitudCotizacionDAOImpl::mapear);
    }

    @Override
    public int actualizarEstado(int idSolicitud, String estado) {
        return ejecutar("UPDATE solicitud_cotizacion SET estado = ?, fecha_cierre = "
                        + "IF(? IN ('CERRADA', 'CANCELADA'), CURDATE(), fecha_cierre) "
                        + "WHERE id_solicitud = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("estado_cierre", estado),
                        ParametroSql.entero("id_solicitud", idSolicitud)));
    }

    @Override
    public String siguienteCodigo() {
        return siguienteCodigo(SQL_ULTIMO_CODIGO, "RFQ-" + LocalDate.now().getYear() + "-", 3,
                codigo -> consultarUno("SELECT id_solicitud FROM solicitud_cotizacion "
                        + "WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_solicitud")).isPresent());
    }

    private static SolicitudCotizacion mapear(ResultSet resultado) throws SQLException {
        SolicitudCotizacion solicitud = new SolicitudCotizacion();
        solicitud.setIdSolicitud(resultado.getInt("id_solicitud"));
        solicitud.setCodigo(resultado.getString("codigo"));
        solicitud.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
        solicitud.setFechaEmision(resultado.getDate("fecha_emision") == null ? null
                : resultado.getDate("fecha_emision").toLocalDate());
        solicitud.setFechaCierre(resultado.getDate("fecha_cierre") == null ? null
                : resultado.getDate("fecha_cierre").toLocalDate());
        solicitud.setEstado(resultado.getString("estado"));
        solicitud.setObservaciones(resultado.getString("observaciones"));
        return solicitud;
    }
}
