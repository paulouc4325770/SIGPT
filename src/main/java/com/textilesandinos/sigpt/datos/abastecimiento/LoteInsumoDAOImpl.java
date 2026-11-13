package com.textilesandinos.sigpt.datos.abastecimiento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.LoteInsumo;

/**
 * Implementacion JDBC del acceso a la trazabilidad de insumos por lote.
 */
@Repository
public class LoteInsumoDAOImpl extends DaoBase implements LoteInsumoDAO {

    public LoteInsumoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(LoteInsumo trazabilidad, String codUsuario) {
        return ejecutar("INSERT INTO lote_insumo (id_lote, id_movimiento_kardex, "
                        + "cantidad_consumida) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE cantidad_consumida = VALUES(cantidad_consumida)",
                List.of(ParametroSql.entero("id_lote", trazabilidad.getIdLote()),
                        ParametroSql.entero("id_movimiento_kardex",
                                trazabilidad.getIdMovimientoKardex()),
                        ParametroSql.decimal("cantidad_consumida",
                                trazabilidad.getCantidadConsumida())));
    }

    @Override
    public List<LoteInsumo> listarPorLote(int idLote) {
        return consultarLista("SELECT id_lote, id_movimiento_kardex, cantidad_consumida "
                        + "FROM lote_insumo WHERE id_lote = ? ORDER BY id_movimiento_kardex",
                List.of(ParametroSql.entero("id_lote", idLote)),
                resultado -> {
                    LoteInsumo trazabilidad = new LoteInsumo();
                    trazabilidad.setIdLote(resultado.getInt("id_lote"));
                    trazabilidad.setIdMovimientoKardex(resultado.getInt("id_movimiento_kardex"));
                    trazabilidad.setCantidadConsumida(resultado.getBigDecimal("cantidad_consumida"));
                    return trazabilidad;
                });
    }
}
