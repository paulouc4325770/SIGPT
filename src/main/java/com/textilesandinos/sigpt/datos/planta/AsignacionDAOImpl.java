package com.textilesandinos.sigpt.datos.planta;

import java.math.BigDecimal;
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
import com.textilesandinos.sigpt.entidades.planta.Asignacion;

/**
 * Implementacion JDBC del acceso a asignaciones.
 */
@Repository
public class AsignacionDAOImpl extends DaoBase implements AsignacionDAO {

    private static final String SQL_BUSCAR = "SELECT id_asignacion, id_operario, id_maquina, "
            + "id_turno, fecha, id_orden_produccion, meta_metros, horas_asignadas, estado "
            + "FROM asignacion ";

    public AsignacionDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(Asignacion asignacion, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO asignacion (id_operario, id_maquina, id_turno, fecha, "
                            + "id_orden_produccion, meta_metros, horas_asignadas, estado, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, asignacion.getIdOperario());
                sentencia.setInt(2, asignacion.getIdMaquina());
                sentencia.setInt(3, asignacion.getIdTurno());
                sentencia.setDate(4, Date.valueOf(asignacion.getFecha()));
                sentencia.setInt(5, asignacion.getIdOrdenProduccion());
                sentencia.setBigDecimal(6, asignacion.getMetaMetros());
                sentencia.setBigDecimal(7, asignacion.getHorasAsignadas());
                sentencia.setString(8, asignacion.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_asignacion FROM asignacion WHERE id_orden_produccion = ? "
                            + "ORDER BY id_asignacion DESC LIMIT 1")) {
                sentencia.setInt(1, asignacion.getIdOrdenProduccion());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_asignacion");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la asignacion insertada");
        });
    }

    @Override
    public Optional<Asignacion> buscarPorId(int idAsignacion) {
        return consultarUno(SQL_BUSCAR + "WHERE id_asignacion = ?",
                List.of(ParametroSql.entero("id_asignacion", idAsignacion)),
                AsignacionDAOImpl::mapear);
    }

    @Override
    public List<Asignacion> listarPorOperarioYFecha(int idOperario, LocalDate fecha) {
        return consultarLista(SQL_BUSCAR + "WHERE id_operario = ? AND fecha = ? "
                        + "ORDER BY id_asignacion",
                List.of(ParametroSql.entero("id_operario", idOperario),
                        ParametroSql.fecha("fecha", Date.valueOf(fecha))),
                AsignacionDAOImpl::mapear);
    }

    @Override
    public List<Asignacion> listarPorMaquinaYFecha(int idMaquina, LocalDate fecha) {
        return consultarLista(SQL_BUSCAR + "WHERE id_maquina = ? AND fecha = ? "
                        + "ORDER BY id_asignacion",
                List.of(ParametroSql.entero("id_maquina", idMaquina),
                        ParametroSql.fecha("fecha", Date.valueOf(fecha))),
                AsignacionDAOImpl::mapear);
    }

    @Override
    public List<Asignacion> listarPorOrden(int idOrdenProduccion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_orden_produccion = ? ORDER BY id_asignacion",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                AsignacionDAOImpl::mapear);
    }

    @Override
    public BigDecimal horasAsignadas(int idOperario, LocalDate fecha) {
        return consultarUno("SELECT COALESCE(SUM(horas_asignadas), 0) AS total FROM asignacion "
                        + "WHERE id_operario = ? AND fecha = ? AND estado <> 'CANCELADA'",
                List.of(ParametroSql.entero("id_operario", idOperario),
                        ParametroSql.fecha("fecha", Date.valueOf(fecha))),
                resultado -> resultado.getBigDecimal("total"))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public int actualizarEstado(int idAsignacion, String estado) {
        return ejecutar("UPDATE asignacion SET estado = ? WHERE id_asignacion = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.entero("id_asignacion", idAsignacion)));
    }

    private static Asignacion mapear(ResultSet resultado) throws SQLException {
        Asignacion asignacion = new Asignacion();
        asignacion.setIdAsignacion(resultado.getInt("id_asignacion"));
        asignacion.setIdOperario(resultado.getInt("id_operario"));
        asignacion.setIdMaquina(resultado.getInt("id_maquina"));
        asignacion.setIdTurno(resultado.getInt("id_turno"));
        asignacion.setFecha(resultado.getDate("fecha") == null ? null
                : resultado.getDate("fecha").toLocalDate());
        asignacion.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
        asignacion.setMetaMetros(resultado.getBigDecimal("meta_metros"));
        asignacion.setHorasAsignadas(resultado.getBigDecimal("horas_asignadas"));
        asignacion.setEstado(resultado.getString("estado"));
        return asignacion;
    }
}
