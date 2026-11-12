package com.textilesandinos.sigpt.datos.seguridad;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;

/**
 * Implementacion JDBC del acceso a usuarios.
 */
@Repository
public class UsuarioDAOImpl extends DaoBase implements UsuarioDAO {

    private static final String SQL_BUSCAR = """
            SELECT u.id_usuario, u.cod_usuario, u.clave_hash, u.nombres, u.area, u.id_perfil,
                   u.intentos_fallidos, u.bloqueado_hasta, u.ultimo_acceso, u.estado,
                   p.cod_perfil, p.nombre_perfil, p.descripcion, p.estado AS perfil_estado
            FROM usuario u
            JOIN perfil p ON p.id_perfil = u.id_perfil
            """;

    public UsuarioDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public Optional<Usuario> buscarPorCodigo(String codUsuario) {
        return consultarUno(SQL_BUSCAR + " WHERE u.cod_usuario = ?",
                List.of(ParametroSql.texto("cod_usuario", codUsuario)),
                UsuarioDAOImpl::mapear);
    }

    @Override
    public Optional<Usuario> buscarPorCodigoYClaveHash(String codUsuario, String claveHash) {
        return consultarUno(SQL_BUSCAR + " WHERE u.cod_usuario = ? AND u.clave_hash = ?",
                List.of(ParametroSql.texto("cod_usuario", codUsuario),
                        ParametroSql.texto("clave_hash", claveHash)),
                UsuarioDAOImpl::mapear);
    }

    @Override
    public int actualizarIntentos(int idUsuario, int intentosFallidos,
            LocalDateTime bloqueadoHasta) {
        return ejecutar("UPDATE usuario SET intentos_fallidos = ?, bloqueado_hasta = ? "
                        + "WHERE id_usuario = ?",
                List.of(ParametroSql.entero("intentos_fallidos", intentosFallidos),
                        bloqueadoHasta == null
                                ? ParametroSql.fechaHora("bloqueado_hasta", null)
                                : ParametroSql.fechaHora("bloqueado_hasta",
                                        Timestamp.valueOf(bloqueadoHasta)),
                        ParametroSql.entero("id_usuario", idUsuario)));
    }

    @Override
    public int registrarAcceso(int idUsuario, LocalDateTime momento) {
        return ejecutar("UPDATE usuario SET ultimo_acceso = ? WHERE id_usuario = ?",
                List.of(ParametroSql.fechaHora("ultimo_acceso", Timestamp.valueOf(momento)),
                        ParametroSql.entero("id_usuario", idUsuario)));
    }

    @Override
    public int actualizarClave(int idUsuario, String claveHash) {
        return ejecutar("UPDATE usuario SET clave_hash = ? WHERE id_usuario = ?",
                List.of(ParametroSql.texto("clave_hash", claveHash),
                        ParametroSql.entero("id_usuario", idUsuario)));
    }

    @Override
    public List<Usuario> listar() {
        return consultarLista(SQL_BUSCAR + " ORDER BY u.cod_usuario", List.of(),
                UsuarioDAOImpl::mapear);
    }

    @Override
    public int insertar(Usuario usuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO usuario (cod_usuario, clave_hash, nombres, area, id_perfil, estado) "
                            + "VALUES (?, ?, ?, ?, ?, TRUE)")) {
                sentencia.setString(1, usuario.getCodUsuario());
                sentencia.setString(2, usuario.getClaveHash());
                sentencia.setString(3, usuario.getNombres());
                sentencia.setString(4, usuario.getArea());
                sentencia.setInt(5, usuario.getIdPerfil());
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_usuario FROM usuario WHERE cod_usuario = ?")) {
                sentencia.setString(1, usuario.getCodUsuario());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_usuario");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del usuario insertado");
        });
    }

    @Override
    public int actualizar(Usuario usuario) {
        return ejecutar("UPDATE usuario SET nombres = ?, area = ?, id_perfil = ?, estado = ? "
                        + "WHERE id_usuario = ?",
                List.of(ParametroSql.texto("nombres", usuario.getNombres()),
                        ParametroSql.texto("area", usuario.getArea()),
                        ParametroSql.entero("id_perfil", usuario.getIdPerfil()),
                        ParametroSql.booleano("estado", usuario.isEstado()),
                        ParametroSql.entero("id_usuario", usuario.getIdUsuario())));
    }

    @Override
    public boolean existePorCodigo(String codUsuario) {
        return consultarUno("SELECT id_usuario FROM usuario WHERE cod_usuario = ?",
                List.of(ParametroSql.texto("cod_usuario", codUsuario)),
                resultado -> resultado.getInt("id_usuario")).isPresent();
    }

    @Override
    public int contar() {
        return consultarUno("SELECT COUNT(*) AS total FROM usuario", List.of(),
                resultado -> resultado.getInt("total")).orElse(0);
    }

    private static Usuario mapear(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setCodUsuario(resultado.getString("cod_usuario"));
        usuario.setClaveHash(resultado.getString("clave_hash"));
        usuario.setNombres(resultado.getString("nombres"));
        usuario.setArea(resultado.getString("area"));
        usuario.setIdPerfil(resultado.getInt("id_perfil"));
        usuario.setIntentosFallidos(resultado.getInt("intentos_fallidos"));
        usuario.setBloqueadoHasta(convertirFechaHora(resultado, "bloqueado_hasta"));
        usuario.setUltimoAcceso(convertirFechaHora(resultado, "ultimo_acceso"));
        usuario.setEstado(resultado.getBoolean("estado"));

        Perfil perfil = new Perfil();
        perfil.setIdPerfil(resultado.getInt("id_perfil"));
        perfil.setCodPerfil(resultado.getString("cod_perfil"));
        perfil.setNombrePerfil(resultado.getString("nombre_perfil"));
        perfil.setDescripcion(resultado.getString("descripcion"));
        perfil.setEstado(resultado.getBoolean("perfil_estado"));
        usuario.setPerfil(perfil);
        return usuario;
    }

    private static LocalDateTime convertirFechaHora(ResultSet resultado, String columna)
            throws SQLException {
        Timestamp marca = resultado.getTimestamp(columna);
        return marca == null ? null : marca.toLocalDateTime();
    }

    /** Evita el warning de import no usado de {@link Date}. */
    private static Date fechaNula() {
        return null;
    }
}
