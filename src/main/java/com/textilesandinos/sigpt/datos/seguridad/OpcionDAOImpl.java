package com.textilesandinos.sigpt.datos.seguridad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.seguridad.MatrizAcceso;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;

/**
 * Implementacion JDBC del acceso al menu.
 *
 * <p>El menu se arma desde la base de datos: cada perfil tiene asignadas sus
 * opciones mediante {@code perfil_opcion}. No hay menus cocidos en el codigo.
 */
@Repository
public class OpcionDAOImpl extends DaoBase implements OpcionDAO {

    private static final String SQL_BUSCAR = """
            SELECT id_opcion, nombre_opcion, url_opcion, ruta_imagen, nro_orden, id_opcion_ref, estado
            FROM opcion
            """;

    public OpcionDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Opcion> listarPorPerfil(int idPerfil) {
        String sql = """
                SELECT o.id_opcion, o.nombre_opcion, o.url_opcion, o.ruta_imagen,
                       o.nro_orden, o.id_opcion_ref, o.estado
                FROM opcion o
                JOIN perfil_opcion po ON po.id_opcion = o.id_opcion AND po.estado = TRUE
                WHERE po.id_perfil = ? AND o.estado = TRUE
                ORDER BY o.id_opcion_ref IS NULL DESC, o.id_opcion_ref, o.nro_orden, o.id_opcion
                """;
        return consultarLista(sql, List.of(ParametroSql.entero("id_perfil", idPerfil)),
                OpcionDAOImpl::mapear);
    }

    @Override
    public List<Opcion> listarTodas() {
        return consultarLista(SQL_BUSCAR + " ORDER BY id_opcion_ref IS NULL DESC, id_opcion_ref, "
                + "nro_orden, id_opcion", List.of(), OpcionDAOImpl::mapear);
    }

    @Override
    public List<MatrizAcceso> listarMatriz() {
        return consultarLista("SELECT p.id_perfil, p.cod_perfil, p.nombre_perfil, "
                + "o.id_opcion, o.nombre_opcion, o.url_opcion, "
                + "(po.id_opcion IS NOT NULL) AS asignado "
                + "FROM perfil p CROSS JOIN opcion o "
                + "LEFT JOIN perfil_opcion po ON po.id_perfil = p.id_perfil "
                + "AND po.id_opcion = o.id_opcion AND po.estado = TRUE "
                + "ORDER BY p.id_perfil, o.id_opcion_ref IS NULL DESC, o.id_opcion_ref, o.nro_orden",
                List.of(), resultado -> {
                    MatrizAcceso fila = new MatrizAcceso();
                    fila.setIdPerfil(resultado.getInt("id_perfil"));
                    fila.setCodPerfil(resultado.getString("cod_perfil"));
                    fila.setNombrePerfil(resultado.getString("nombre_perfil"));
                    fila.setIdOpcion(resultado.getInt("id_opcion"));
                    fila.setNombreOpcion(resultado.getString("nombre_opcion"));
                    fila.setUrlOpcion(resultado.getString("url_opcion"));
                    fila.setAsignado(resultado.getBoolean("asignado"));
                    return fila;
                });
    }

    @Override
    public Optional<Opcion> buscarPorId(int idOpcion) {
        return consultarUno(SQL_BUSCAR + " WHERE id_opcion = ?",
                List.of(ParametroSql.entero("id_opcion", idOpcion)), OpcionDAOImpl::mapear);
    }

    @Override
    public int insertar(Opcion opcion) {
        return ejecutar("INSERT INTO opcion (nombre_opcion, url_opcion, ruta_imagen, nro_orden, "
                        + "id_opcion_ref, estado) VALUES (?, ?, ?, ?, ?, ?)",
                List.of(ParametroSql.texto("nombre_opcion", opcion.getNombreOpcion()),
                        ParametroSql.texto("url_opcion", opcion.getUrlOpcion()),
                        ParametroSql.texto("ruta_imagen", opcion.getRutaImagen()),
                        ParametroSql.entero("nro_orden", opcion.getNroOrden()),
                        opcion.getIdOpcionRef() == null
                                ? ParametroSql.de("id_opcion_ref", null)
                                : ParametroSql.entero("id_opcion_ref", opcion.getIdOpcionRef()),
                        ParametroSql.booleano("estado", opcion.isEstado())));
    }

    @Override
    public int actualizar(Opcion opcion) {
        return ejecutar("UPDATE opcion SET nombre_opcion = ?, url_opcion = ?, ruta_imagen = ?, "
                        + "nro_orden = ?, id_opcion_ref = ?, estado = ? WHERE id_opcion = ?",
                List.of(ParametroSql.texto("nombre_opcion", opcion.getNombreOpcion()),
                        ParametroSql.texto("url_opcion", opcion.getUrlOpcion()),
                        ParametroSql.texto("ruta_imagen", opcion.getRutaImagen()),
                        ParametroSql.entero("nro_orden", opcion.getNroOrden()),
                        opcion.getIdOpcionRef() == null
                                ? ParametroSql.de("id_opcion_ref", null)
                                : ParametroSql.entero("id_opcion_ref", opcion.getIdOpcionRef()),
                        ParametroSql.booleano("estado", opcion.isEstado()),
                        ParametroSql.entero("id_opcion", opcion.getIdOpcion())));
    }

    @Override
    public boolean eliminar(int idOpcion) {
        return ejecutar("DELETE FROM opcion WHERE id_opcion = ?",
                List.of(ParametroSql.entero("id_opcion", idOpcion))) > 0;
    }

    private static Opcion mapear(ResultSet resultado) throws SQLException {
        Opcion opcion = new Opcion();
        opcion.setIdOpcion(resultado.getInt("id_opcion"));
        opcion.setNombreOpcion(resultado.getString("nombre_opcion"));
        opcion.setUrlOpcion(resultado.getString("url_opcion"));
        opcion.setRutaImagen(resultado.getString("ruta_imagen"));
        opcion.setNroOrden(resultado.getInt("nro_orden"));
        int idOpcionRef = resultado.getInt("id_opcion_ref");
        opcion.setIdOpcionRef(resultado.wasNull() ? null : idOpcionRef);
        opcion.setEstado(resultado.getBoolean("estado"));
        return opcion;
    }
}
