package com.textilesandinos.sigpt.datos.planta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.Maquina;

/**
 * Implementacion JDBC del acceso a maquinas.
 */
@Repository
public class MaquinaDAOImpl extends DaoBase implements MaquinaDAO {

    private static final String SQL_BUSCAR = "SELECT id_maquina, codigo, nombre, tipo, estado, "
            + "velocidad_teorica_mts_hora, horas_por_turno, observaciones FROM maquina ";

    public MaquinaDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Maquina> listarTodas() {
        return consultarLista(SQL_BUSCAR + "ORDER BY codigo", List.of(), MaquinaDAOImpl::mapear);
    }

    @Override
    public Optional<Maquina> buscarPorId(int idMaquina) {
        return consultarUno(SQL_BUSCAR + "WHERE id_maquina = ?",
                List.of(ParametroSql.entero("id_maquina", idMaquina)), MaquinaDAOImpl::mapear);
    }

    @Override
    public int actualizarEstado(int idMaquina, String estado) {
        return ejecutar("UPDATE maquina SET estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_maquina = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_maquina", idMaquina)));
    }

    private static Maquina mapear(ResultSet resultado) throws SQLException {
        Maquina maquina = new Maquina();
        maquina.setIdMaquina(resultado.getInt("id_maquina"));
        maquina.setCodigo(resultado.getString("codigo"));
        maquina.setNombre(resultado.getString("nombre"));
        maquina.setTipo(resultado.getString("tipo"));
        maquina.setEstado(resultado.getString("estado"));
        maquina.setVelocidadTeoricaMtsHora(resultado.getBigDecimal("velocidad_teorica_mts_hora"));
        maquina.setHorasPorTurno(resultado.getBigDecimal("horas_por_turno"));
        maquina.setObservaciones(resultado.getString("observaciones"));
        return maquina;
    }
}
