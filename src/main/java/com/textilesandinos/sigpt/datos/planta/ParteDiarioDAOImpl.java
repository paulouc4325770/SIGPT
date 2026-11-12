package com.textilesandinos.sigpt.datos.planta;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;

/**
 * Implementacion JDBC del acceso a partes diarios.
 */
@Repository
public class ParteDiarioDAOImpl extends DaoBase implements ParteDiarioDAO {

    public ParteDiarioDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int guardar(ParteDiarioProduccion parte, String codUsuario) {
        return ejecutar("INSERT INTO parte_diario_produccion (id_orden_produccion, fecha, "
                        + "horas_hombre, horas_maquina, metros, observaciones, usuario_registro) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                        + "horas_hombre = VALUES(horas_hombre), "
                        + "horas_maquina = VALUES(horas_maquina), metros = VALUES(metros), "
                        + "observaciones = VALUES(observaciones)",
                List.of(ParametroSql.entero("id_orden_produccion", parte.getIdOrdenProduccion()),
                        ParametroSql.fecha("fecha", Date.valueOf(parte.getFecha())),
                        ParametroSql.decimal("horas_hombre", parte.getHorasHombre()),
                        ParametroSql.decimal("horas_maquina", parte.getHorasMaquina()),
                        ParametroSql.decimal("metros", parte.getMetros()),
                        ParametroSql.texto("observaciones", parte.getObservaciones()),
                        ParametroSql.texto("usuario_registro",
                                codUsuario == null ? "sistema" : codUsuario)));
    }

    @Override
    public List<ParteDiarioProduccion> listarPorOrden(int idOrdenProduccion) {
        return consultarLista("SELECT id_parte, id_orden_produccion, fecha, horas_hombre, "
                        + "horas_maquina, metros, observaciones FROM parte_diario_produccion "
                        + "WHERE id_orden_produccion = ? ORDER BY fecha DESC",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                ParteDiarioDAOImpl::mapear);
    }

    private static ParteDiarioProduccion mapear(ResultSet resultado) throws SQLException {
        ParteDiarioProduccion parte = new ParteDiarioProduccion();
        parte.setIdParte(resultado.getInt("id_parte"));
        parte.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
        parte.setFecha(resultado.getDate("fecha") == null ? null
                : resultado.getDate("fecha").toLocalDate());
        parte.setHorasHombre(resultado.getBigDecimal("horas_hombre"));
        parte.setHorasMaquina(resultado.getBigDecimal("horas_maquina"));
        parte.setMetros(resultado.getBigDecimal("metros"));
        parte.setObservaciones(resultado.getString("observaciones"));
        return parte;
    }
}
