package com.textilesandinos.sigpt.datos.planta;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;

/**
 * Implementacion JDBC del acceso a los avances de produccion.
 */
@Repository
public class RegistroAvanceDAOImpl extends DaoBase implements RegistroAvanceDAO {

    private static final String SQL_BUSCAR = "SELECT id_avance, id_asignacion, "
            + "id_orden_produccion, id_operario, id_maquina, id_turno, fecha, metros_producidos, "
            + "metros_merma, varianza_pct, estado_calidad, observacion FROM registro_avance ";

    public RegistroAvanceDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(RegistroAvance avance, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO registro_avance (id_asignacion, id_orden_produccion, "
                            + "id_operario, id_maquina, id_turno, fecha, metros_producidos, "
                            + "metros_merma, varianza_pct, estado_calidad, observacion, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, avance.getIdAsignacion());
                sentencia.setInt(2, avance.getIdOrdenProduccion());
                sentencia.setInt(3, avance.getIdOperario());
                sentencia.setInt(4, avance.getIdMaquina());
                sentencia.setInt(5, avance.getIdTurno());
                sentencia.setDate(6, Date.valueOf(avance.getFecha()));
                sentencia.setBigDecimal(7, avance.getMetrosProducidos());
                sentencia.setBigDecimal(8, avance.getMetrosMerma());
                sentencia.setBigDecimal(9, avance.getVarianzaPct());
                sentencia.setString(10, avance.getEstadoCalidad());
                sentencia.setString(11, avance.getObservacion());
                sentencia.setString(12, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_avance FROM registro_avance WHERE id_asignacion = ? "
                            + "ORDER BY id_avance DESC LIMIT 1")) {
                sentencia.setInt(1, avance.getIdAsignacion());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_avance");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del avance insertado");
        });
    }

    @Override
    public Optional<RegistroAvance> buscarPorId(int idAvance) {
        return consultarUno(SQL_BUSCAR + "WHERE id_avance = ?",
                List.of(ParametroSql.entero("id_avance", idAvance)),
                RegistroAvanceDAOImpl::mapear);
    }

    @Override
    public List<RegistroAvance> listarPorOrden(int idOrdenProduccion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_orden_produccion = ? ORDER BY fecha DESC, "
                        + "id_avance DESC",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                RegistroAvanceDAOImpl::mapear);
    }

    @Override
    public List<RegistroAvance> listarEntreFechas(LocalDate desde, LocalDate hasta) {
        String sql = SQL_BUSCAR + "WHERE 1 = 1 ";
        List<ParametroSql> parametros = new ArrayList<>();
        if (desde != null) {
            sql += "AND fecha >= ? ";
            parametros.add(ParametroSql.fecha("desde", Date.valueOf(desde)));
        }
        if (hasta != null) {
            sql += "AND fecha <= ? ";
            parametros.add(ParametroSql.fecha("hasta", Date.valueOf(hasta)));
        }
        sql += "ORDER BY fecha DESC, id_avance DESC";
        return consultarLista(sql, parametros, RegistroAvanceDAOImpl::mapear);
    }

    @Override
    public List<RegistroAvance> listarPorAsignacion(int idAsignacion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_asignacion = ? ORDER BY fecha DESC, "
                        + "id_avance DESC",
                List.of(ParametroSql.entero("id_asignacion", idAsignacion)),
                RegistroAvanceDAOImpl::mapear);
    }

    @Override
    public int actualizarEstadoCalidad(int idAvance, String estadoCalidad) {
        return ejecutar("UPDATE registro_avance SET estado_calidad = ? WHERE id_avance = ?",
                List.of(ParametroSql.texto("estado_calidad", estadoCalidad),
                        ParametroSql.entero("id_avance", idAvance)));
    }

    private static RegistroAvance mapear(ResultSet resultado) throws SQLException {
        RegistroAvance avance = new RegistroAvance();
        avance.setIdAvance(resultado.getInt("id_avance"));
        avance.setIdAsignacion(resultado.getInt("id_asignacion"));
        avance.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
        avance.setIdOperario(resultado.getInt("id_operario"));
        avance.setIdMaquina(resultado.getInt("id_maquina"));
        avance.setIdTurno(resultado.getInt("id_turno"));
        avance.setFecha(resultado.getDate("fecha") == null ? null
                : resultado.getDate("fecha").toLocalDate());
        avance.setMetrosProducidos(resultado.getBigDecimal("metros_producidos"));
        avance.setMetrosMerma(resultado.getBigDecimal("metros_merma"));
        avance.setVarianzaPct(resultado.getBigDecimal("varianza_pct"));
        avance.setEstadoCalidad(resultado.getString("estado_calidad"));
        avance.setObservacion(resultado.getString("observacion"));
        return avance;
    }
}
