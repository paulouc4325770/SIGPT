package com.textilesandinos.sigpt.datos.comun;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.entidades.comun.Parametro;

/**
 * Implementacion JDBC del acceso a los parametros de negocio.
 */
@Repository
public class ParametroDAOImpl extends DaoBase implements ParametroDAO {

    private static final String SQL_BUSCAR = """
            SELECT id_parametro, codigo, nombre, valor, tipo_dato, descripcion, estado
            FROM parametro
            """;

    public ParametroDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Parametro> listarActivos() {
        return consultarLista(SQL_BUSCAR + " WHERE estado = TRUE ORDER BY codigo", List.of(),
                ParametroDAOImpl::mapear);
    }

    @Override
    public Optional<Parametro> buscarPorCodigo(String codigo) {
        return consultarUno(SQL_BUSCAR + " WHERE codigo = ?",
                List.of(ParametroSql.texto("codigo", codigo)), ParametroDAOImpl::mapear);
    }

    @Override
    public int insertar(Parametro parametro) {
        return ejecutar("INSERT INTO parametro (codigo, nombre, valor, tipo_dato, descripcion, "
                        + "estado) VALUES (?, ?, ?, ?, ?, ?)",
                List.of(ParametroSql.texto("codigo", parametro.getCodigo()),
                        ParametroSql.texto("nombre", parametro.getNombre()),
                        ParametroSql.texto("valor", parametro.getValor()),
                        ParametroSql.texto("tipo_dato", parametro.getTipoDato()),
                        ParametroSql.texto("descripcion", parametro.getDescripcion()),
                        ParametroSql.booleano("estado", parametro.isEstado())));
    }

    @Override
    public int actualizar(Parametro parametro) {
        return ejecutar("UPDATE parametro SET nombre = ?, valor = ?, tipo_dato = ?, "
                        + "descripcion = ?, estado = ? WHERE id_parametro = ?",
                List.of(ParametroSql.texto("nombre", parametro.getNombre()),
                        ParametroSql.texto("valor", parametro.getValor()),
                        ParametroSql.texto("tipo_dato", parametro.getTipoDato()),
                        ParametroSql.texto("descripcion", parametro.getDescripcion()),
                        ParametroSql.booleano("estado", parametro.isEstado()),
                        ParametroSql.entero("id_parametro", parametro.getIdParametro())));
    }

    private static Parametro mapear(ResultSet resultado) throws SQLException {
        Parametro parametro = new Parametro();
        parametro.setIdParametro(resultado.getInt("id_parametro"));
        parametro.setCodigo(resultado.getString("codigo"));
        parametro.setNombre(resultado.getString("nombre"));
        parametro.setValor(resultado.getString("valor"));
        parametro.setTipoDato(resultado.getString("tipo_dato"));
        parametro.setDescripcion(resultado.getString("descripcion"));
        parametro.setEstado(resultado.getBoolean("estado"));
        return parametro;
    }
}
