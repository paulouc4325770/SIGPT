package com.textilesandinos.sigpt.datos.planta;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.InspeccionCalidad;

/**
 * Implementacion JDBC del acceso a inspecciones de calidad.
 */
@Repository
public class InspeccionCalidadDAOImpl extends DaoBase implements InspeccionCalidadDAO {

    public InspeccionCalidadDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(InspeccionCalidad inspeccion, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO inspeccion_calidad (id_lote, id_avance, fecha, varianza_color_pct, "
                            + "tolerancia_aplicada, resultado, observacion, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, inspeccion.getIdLote());
                if (inspeccion.getIdAvance() == null) {
                    sentencia.setNull(2, java.sql.Types.INTEGER);
                } else {
                    sentencia.setInt(2, inspeccion.getIdAvance());
                }
                sentencia.setDate(3, Date.valueOf(inspeccion.getFecha()));
                sentencia.setBigDecimal(4, inspeccion.getVarianzaColorPct());
                sentencia.setBigDecimal(5, inspeccion.getToleranciaAplicada());
                sentencia.setString(6, inspeccion.getResultado());
                sentencia.setString(7, inspeccion.getObservacion());
                sentencia.setString(8, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_inspeccion FROM inspeccion_calidad WHERE id_lote = ? "
                            + "ORDER BY id_inspeccion DESC LIMIT 1")) {
                sentencia.setInt(1, inspeccion.getIdLote());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_inspeccion");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la inspeccion insertada");
        });
    }

    @Override
    public List<InspeccionCalidad> listarPorLote(int idLote) {
        return consultarLista("SELECT id_inspeccion, id_lote, id_avance, fecha, "
                        + "varianza_color_pct, tolerancia_aplicada, resultado, observacion "
                        + "FROM inspeccion_calidad WHERE id_lote = ? ORDER BY id_inspeccion DESC",
                List.of(ParametroSql.entero("id_lote", idLote)),
                InspeccionCalidadDAOImpl::mapear);
    }

    private static InspeccionCalidad mapear(ResultSet resultado) throws SQLException {
        InspeccionCalidad inspeccion = new InspeccionCalidad();
        inspeccion.setIdInspeccion(resultado.getInt("id_inspeccion"));
        inspeccion.setIdLote(resultado.getInt("id_lote"));
        int idAvance = resultado.getInt("id_avance");
        inspeccion.setIdAvance(resultado.wasNull() ? null : idAvance);
        inspeccion.setFecha(resultado.getDate("fecha") == null ? null
                : resultado.getDate("fecha").toLocalDate());
        inspeccion.setVarianzaColorPct(resultado.getBigDecimal("varianza_color_pct"));
        inspeccion.setToleranciaAplicada(resultado.getBigDecimal("tolerancia_aplicada"));
        inspeccion.setResultado(resultado.getString("resultado"));
        inspeccion.setObservacion(resultado.getString("observacion"));
        return inspeccion;
    }
}
