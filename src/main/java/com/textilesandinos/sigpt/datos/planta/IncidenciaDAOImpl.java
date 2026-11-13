package com.textilesandinos.sigpt.datos.planta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.Incidencia;

/**
 * Implementacion JDBC del acceso a incidencias.
 */
@Repository
public class IncidenciaDAOImpl extends DaoBase implements IncidenciaDAO {

    private static final String SQL_BUSCAR = "SELECT id_incidencia, id_avance, id_maquina, tipo, "
            + "descripcion, estado FROM incidencia ";

    public IncidenciaDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(Incidencia incidencia, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO incidencia (id_avance, id_maquina, tipo, descripcion, estado, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?)")) {
                if (incidencia.getIdAvance() == null) {
                    sentencia.setNull(1, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(1, incidencia.getIdAvance());
                }
                if (incidencia.getIdMaquina() == null) {
                    sentencia.setNull(2, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(2, incidencia.getIdMaquina());
                }
                sentencia.setString(3, incidencia.getTipo());
                sentencia.setString(4, incidencia.getDescripcion());
                sentencia.setString(5, incidencia.getEstado());
                sentencia.setString(6, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_incidencia FROM incidencia WHERE descripcion = ? "
                            + "ORDER BY id_incidencia DESC LIMIT 1")) {
                sentencia.setString(1, incidencia.getDescripcion());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_incidencia");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la incidencia insertada");
        });
    }

    @Override
    public int actualizarEstado(int idIncidencia, String estado) {
        return ejecutar("UPDATE incidencia SET estado = ? WHERE id_incidencia = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.entero("id_incidencia", idIncidencia)));
    }

    @Override
    public List<Incidencia> listarPorMaquina(int idMaquina) {
        return consultarLista(SQL_BUSCAR + "WHERE id_maquina = ? ORDER BY id_incidencia DESC",
                List.of(ParametroSql.entero("id_maquina", idMaquina)), IncidenciaDAOImpl::mapear);
    }

    @Override
    public List<Incidencia> listarTodas() {
        return consultarLista(SQL_BUSCAR + "ORDER BY id_incidencia DESC", List.of(),
                IncidenciaDAOImpl::mapear);
    }

    private static Incidencia mapear(ResultSet resultado) throws SQLException {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(resultado.getInt("id_incidencia"));
        int idAvance = resultado.getInt("id_avance");
        incidencia.setIdAvance(resultado.wasNull() ? null : idAvance);
        int idMaquina = resultado.getInt("id_maquina");
        incidencia.setIdMaquina(resultado.wasNull() ? null : idMaquina);
        incidencia.setTipo(resultado.getString("tipo"));
        incidencia.setDescripcion(resultado.getString("descripcion"));
        incidencia.setEstado(resultado.getString("estado"));
        return incidencia;
    }
}
