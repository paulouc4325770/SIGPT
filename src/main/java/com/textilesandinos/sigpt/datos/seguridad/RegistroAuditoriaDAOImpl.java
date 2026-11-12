package com.textilesandinos.sigpt.datos.seguridad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.seguridad.RegistroAuditoria;

/**
 * Implementacion JDBC del acceso a la bitacora de auditoria.
 */
@Repository
public class RegistroAuditoriaDAOImpl extends DaoBase implements RegistroAuditoriaDAO {

    private static final String SQL_BUSCAR = """
            SELECT id_registro, cod_usuario, fecha_hora, modulo, operacion, entidad,
                   id_entidad, detalle
            FROM registro_auditoria
            """;

    public RegistroAuditoriaDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int registrar(RegistroAuditoria registro) {
        Timestamp fecha = registro.getFechaHora() == null
                ? new Timestamp(System.currentTimeMillis())
                : Timestamp.valueOf(registro.getFechaHora());
        return ejecutar("INSERT INTO registro_auditoria (cod_usuario, fecha_hora, modulo, "
                        + "operacion, entidad, id_entidad, detalle) VALUES (?, ?, ?, ?, ?, ?, ?)",
                List.of(ParametroSql.texto("cod_usuario", registro.getCodUsuario()),
                        ParametroSql.fechaHora("fecha_hora", fecha),
                        ParametroSql.texto("modulo", registro.getModulo()),
                        ParametroSql.texto("operacion", registro.getOperacion()),
                        ParametroSql.texto("entidad", registro.getEntidad()),
                        ParametroSql.texto("id_entidad", registro.getIdEntidad()),
                        ParametroSql.texto("detalle", registro.getDetalle())));
    }

    @Override
    public List<RegistroAuditoria> listarPorUsuario(String codUsuario, int limite) {
        return consultarLista(SQL_BUSCAR + " WHERE cod_usuario = ? ORDER BY fecha_hora DESC LIMIT ?",
                List.of(ParametroSql.texto("cod_usuario", codUsuario),
                        ParametroSql.entero("limite", limite)),
                RegistroAuditoriaDAOImpl::mapear);
    }

    @Override
    public List<RegistroAuditoria> listarPorEntidad(String entidad, String idEntidad) {
        return consultarLista(SQL_BUSCAR + " WHERE entidad = ? AND id_entidad = ? "
                        + "ORDER BY fecha_hora DESC",
                List.of(ParametroSql.texto("entidad", entidad),
                        ParametroSql.texto("id_entidad", idEntidad)),
                RegistroAuditoriaDAOImpl::mapear);
    }

    @Override
    public int contar() {
        return consultarUno("SELECT COUNT(*) AS total FROM registro_auditoria", List.of(),
                resultado -> resultado.getInt("total")).orElse(0);
    }

    private static RegistroAuditoria mapear(ResultSet resultado) throws SQLException {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setIdRegistro(resultado.getLong("id_registro"));
        registro.setCodUsuario(resultado.getString("cod_usuario"));
        Timestamp fecha = resultado.getTimestamp("fecha_hora");
        registro.setFechaHora(fecha == null ? null : fecha.toLocalDateTime());
        registro.setModulo(resultado.getString("modulo"));
        registro.setOperacion(resultado.getString("operacion"));
        registro.setEntidad(resultado.getString("entidad"));
        registro.setIdEntidad(resultado.getString("id_entidad"));
        registro.setDetalle(resultado.getString("detalle"));
        return registro;
    }
}
