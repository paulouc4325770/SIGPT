package com.textilesandinos.sigpt.datos.planta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.Operario;

/**
 * Implementacion JDBC del acceso a operarios.
 */
@Repository
public class OperarioDAOImpl extends DaoBase implements OperarioDAO {

    private static final String SQL_BUSCAR = "SELECT id_operario, codigo, nombres, especialidad, "
            + "id_usuario, estado FROM operario ";

    public OperarioDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Operario> listarActivos() {
        return consultarLista(SQL_BUSCAR + "WHERE estado = 'ACTIVO' ORDER BY nombres",
                List.of(), OperarioDAOImpl::mapear);
    }

    @Override
    public List<Operario> listarTodos() {
        return consultarLista(SQL_BUSCAR + "ORDER BY nombres", List.of(), OperarioDAOImpl::mapear);
    }

    @Override
    public Optional<Operario> buscarPorId(int idOperario) {
        return consultarUno(SQL_BUSCAR + "WHERE id_operario = ?",
                List.of(ParametroSql.entero("id_operario", idOperario)), OperarioDAOImpl::mapear);
    }

    private static Operario mapear(ResultSet resultado) throws SQLException {
        Operario operario = new Operario();
        operario.setIdOperario(resultado.getInt("id_operario"));
        operario.setCodigo(resultado.getString("codigo"));
        operario.setNombres(resultado.getString("nombres"));
        operario.setEspecialidad(resultado.getString("especialidad"));
        int idUsuario = resultado.getInt("id_usuario");
        operario.setIdUsuario(resultado.wasNull() ? null : idUsuario);
        operario.setEstado(resultado.getString("estado"));
        return operario;
    }
}
