package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;

/**
 * Implementacion JDBC del acceso a adelantos.
 */
@Repository
public class AdelantoDAOImpl extends DaoBase implements AdelantoDAO {

    private static final String SQL_BUSCAR = "SELECT id_adelanto, id_cotizacion, monto, moneda, "
            + "fecha_adelanto, verificado, referencia FROM adelanto ";

    public AdelantoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(Adelanto adelanto, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO adelanto (id_cotizacion, monto, moneda, fecha_adelanto, "
                            + "verificado, referencia, usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, adelanto.getIdCotizacion());
                sentencia.setBigDecimal(2, adelanto.getMonto());
                sentencia.setString(3, adelanto.getMoneda());
                sentencia.setDate(4, Date.valueOf(adelanto.getFechaAdelanto()));
                sentencia.setBoolean(5, adelanto.isVerificado());
                sentencia.setString(6, adelanto.getReferencia());
                sentencia.setString(7, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_adelanto FROM adelanto WHERE id_cotizacion = ? "
                            + "ORDER BY id_adelanto DESC LIMIT 1")) {
                sentencia.setInt(1, adelanto.getIdCotizacion());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_adelanto");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del adelanto insertado");
        });
    }

    @Override
    public List<Adelanto> listarPorCotizacion(int idCotizacion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_cotizacion = ? ORDER BY fecha_adelanto DESC",
                List.of(ParametroSql.entero("id_cotizacion", idCotizacion)),
                AdelantoDAOImpl::mapear);
    }

    @Override
    public BigDecimal sumaVerificada(int idCotizacion) {
        return consultarUno("SELECT COALESCE(SUM(monto), 0) AS total FROM adelanto "
                        + "WHERE id_cotizacion = ? AND verificado = TRUE",
                List.of(ParametroSql.entero("id_cotizacion", idCotizacion)),
                resultado -> resultado.getBigDecimal("total"))
                .orElse(BigDecimal.ZERO);
    }

    private static Adelanto mapear(ResultSet resultado) throws SQLException {
        Adelanto adelanto = new Adelanto();
        adelanto.setIdAdelanto(resultado.getInt("id_adelanto"));
        adelanto.setIdCotizacion(resultado.getInt("id_cotizacion"));
        adelanto.setMonto(resultado.getBigDecimal("monto"));
        adelanto.setMoneda(resultado.getString("moneda"));
        adelanto.setFechaAdelanto(resultado.getDate("fecha_adelanto") == null ? null
                : resultado.getDate("fecha_adelanto").toLocalDate());
        adelanto.setVerificado(resultado.getBoolean("verificado"));
        adelanto.setReferencia(resultado.getString("referencia"));
        return adelanto;
    }
}
