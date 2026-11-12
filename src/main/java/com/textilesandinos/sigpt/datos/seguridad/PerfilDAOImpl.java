package com.textilesandinos.sigpt.datos.seguridad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;

/**
 * Implementacion JDBC del acceso a perfiles.
 */
@Repository
public class PerfilDAOImpl extends DaoBase implements PerfilDAO {

    private static final String SQL_BUSCAR = """
            SELECT id_perfil, cod_perfil, nombre_perfil, descripcion, estado
            FROM perfil
            """;

    public PerfilDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Perfil> listar() {
        return consultarLista(SQL_BUSCAR + " ORDER BY id_perfil", List.of(),
                PerfilDAOImpl::mapear);
    }

    @Override
    public Optional<Perfil> buscarPorId(int idPerfil) {
        return consultarUno(SQL_BUSCAR + " WHERE id_perfil = ?",
                List.of(ParametroSql.entero("id_perfil", idPerfil)), PerfilDAOImpl::mapear);
    }

    @Override
    public Optional<Perfil> buscarPorCodigo(String codPerfil) {
        return consultarUno(SQL_BUSCAR + " WHERE cod_perfil = ?",
                List.of(ParametroSql.texto("cod_perfil", codPerfil)), PerfilDAOImpl::mapear);
    }

    @Override
    public int insertar(Perfil perfil) {
        return ejecutar("INSERT INTO perfil (cod_perfil, nombre_perfil, descripcion, estado) "
                        + "VALUES (?, ?, ?, ?)",
                List.of(ParametroSql.texto("cod_perfil", perfil.getCodPerfil()),
                        ParametroSql.texto("nombre_perfil", perfil.getNombrePerfil()),
                        ParametroSql.texto("descripcion", perfil.getDescripcion()),
                        ParametroSql.booleano("estado", perfil.isEstado())));
    }

    @Override
    public int actualizar(Perfil perfil) {
        return ejecutar("UPDATE perfil SET cod_perfil = ?, nombre_perfil = ?, descripcion = ?, "
                        + "estado = ? WHERE id_perfil = ?",
                List.of(ParametroSql.texto("cod_perfil", perfil.getCodPerfil()),
                        ParametroSql.texto("nombre_perfil", perfil.getNombrePerfil()),
                        ParametroSql.texto("descripcion", perfil.getDescripcion()),
                        ParametroSql.booleano("estado", perfil.isEstado()),
                        ParametroSql.entero("id_perfil", perfil.getIdPerfil())));
    }

    private static Perfil mapear(ResultSet resultado) throws SQLException {
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(resultado.getInt("id_perfil"));
        perfil.setCodPerfil(resultado.getString("cod_perfil"));
        perfil.setNombrePerfil(resultado.getString("nombre_perfil"));
        perfil.setDescripcion(resultado.getString("descripcion"));
        perfil.setEstado(resultado.getBoolean("estado"));
        return perfil;
    }
}
