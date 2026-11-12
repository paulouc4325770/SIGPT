package com.textilesandinos.sigpt.datos.pedidos;

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
import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;

/**
 * Implementacion JDBC del acceso a cotizaciones.
 */
@Repository
public class CotizacionDAOImpl extends DaoBase implements CotizacionDAO {

    private static final String SQL_BUSCAR = "SELECT id_cotizacion, id_pedido, monto_total, moneda, "
            + "costo_insumos, horas_ciclo, fecha_emision, fecha_vigencia, estado FROM cotizacion ";

    public CotizacionDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Cotizacion> listarPorPedido(int idPedido) {
        return consultarLista(SQL_BUSCAR + "WHERE id_pedido = ? ORDER BY fecha_emision DESC, "
                + "id_cotizacion DESC",
                List.of(ParametroSql.entero("id_pedido", idPedido)), CotizacionDAOImpl::mapear);
    }

    @Override
    public List<Cotizacion> listarTodas() {
        return consultarLista(SQL_BUSCAR + "ORDER BY fecha_emision DESC, id_cotizacion DESC",
                List.of(), CotizacionDAOImpl::mapear);
    }

    @Override
    public Optional<Cotizacion> buscarPorId(int idCotizacion) {
        return consultarUno(SQL_BUSCAR + "WHERE id_cotizacion = ?",
                List.of(ParametroSql.entero("id_cotizacion", idCotizacion)),
                CotizacionDAOImpl::mapear);
    }

    @Override
    public int insertar(Cotizacion cotizacion, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO cotizacion (id_pedido, monto_total, moneda, costo_insumos, "
                            + "horas_ciclo, fecha_emision, fecha_vigencia, estado, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, cotizacion.getIdPedido());
                sentencia.setBigDecimal(2, cotizacion.getMontoTotal());
                sentencia.setString(3, cotizacion.getMoneda());
                sentencia.setBigDecimal(4, cotizacion.getCostoInsumos());
                sentencia.setBigDecimal(5, cotizacion.getHorasCiclo());
                sentencia.setDate(6, Date.valueOf(cotizacion.getFechaEmision()));
                sentencia.setDate(7, Date.valueOf(cotizacion.getFechaVigencia()));
                sentencia.setString(8, cotizacion.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_cotizacion FROM cotizacion WHERE id_pedido = ? "
                            + "ORDER BY id_cotizacion DESC LIMIT 1")) {
                sentencia.setInt(1, cotizacion.getIdPedido());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_cotizacion");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la cotizacion insertada");
        });
    }

    @Override
    public int actualizarEstado(int idCotizacion, String estado) {
        return ejecutar("UPDATE cotizacion SET estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_cotizacion = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_cotizacion", idCotizacion)));
    }

    @Override
    public int marcarVencidas(LocalDate hoy) {
        return ejecutar("UPDATE cotizacion SET estado = 'VENCIDA' WHERE estado = 'EMITIDA' "
                        + "AND fecha_vigencia < ?",
                List.of(ParametroSql.fecha("hoy", Date.valueOf(hoy))));
    }

    private static Cotizacion mapear(ResultSet resultado) throws SQLException {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(resultado.getInt("id_cotizacion"));
        cotizacion.setIdPedido(resultado.getInt("id_pedido"));
        cotizacion.setMontoTotal(resultado.getBigDecimal("monto_total"));
        cotizacion.setMoneda(resultado.getString("moneda"));
        cotizacion.setCostoInsumos(resultado.getBigDecimal("costo_insumos"));
        cotizacion.setHorasCiclo(resultado.getBigDecimal("horas_ciclo"));
        cotizacion.setFechaEmision(resultado.getDate("fecha_emision") == null ? null
                : resultado.getDate("fecha_emision").toLocalDate());
        cotizacion.setFechaVigencia(resultado.getDate("fecha_vigencia") == null ? null
                : resultado.getDate("fecha_vigencia").toLocalDate());
        cotizacion.setEstado(resultado.getString("estado"));
        return cotizacion;
    }
}
