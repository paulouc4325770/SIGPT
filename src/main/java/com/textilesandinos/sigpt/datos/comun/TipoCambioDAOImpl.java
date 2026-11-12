package com.textilesandinos.sigpt.datos.comun;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.entidades.comun.TipoCambio;

/**
 * Implementacion JDBC del acceso al tipo de cambio.
 */
@Repository
public class TipoCambioDAOImpl extends DaoBase implements TipoCambioDAO {

    public TipoCambioDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public Optional<TipoCambio> buscarVigente(String monedaOrigen, String monedaDestino,
            LocalDate fecha) {
        return consultarUno("SELECT id_tipo_cambio, moneda_origen, moneda_destino, valor, "
                        + "fecha_vigencia, estado FROM tipo_cambio WHERE moneda_origen = ? "
                        + "AND moneda_destino = ? AND estado = TRUE AND fecha_vigencia <= ? "
                        + "ORDER BY fecha_vigencia DESC LIMIT 1",
                List.of(ParametroSql.texto("moneda_origen", monedaOrigen),
                        ParametroSql.texto("moneda_destino", monedaDestino),
                        ParametroSql.fecha("fecha_vigencia", Date.valueOf(fecha))),
                TipoCambioDAOImpl::mapear);
    }

    private static TipoCambio mapear(ResultSet resultado) throws SQLException {
        TipoCambio tipoCambio = new TipoCambio();
        tipoCambio.setIdTipoCambio(resultado.getInt("id_tipo_cambio"));
        tipoCambio.setMonedaOrigen(resultado.getString("moneda_origen"));
        tipoCambio.setMonedaDestino(resultado.getString("moneda_destino"));
        tipoCambio.setValor(resultado.getBigDecimal("valor"));
        tipoCambio.setFechaVigencia(resultado.getDate("fecha_vigencia") == null ? null
                : resultado.getDate("fecha_vigencia").toLocalDate());
        tipoCambio.setEstado(resultado.getBoolean("estado"));
        return tipoCambio;
    }
}
