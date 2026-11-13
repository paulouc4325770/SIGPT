package com.textilesandinos.sigpt.datos.abastecimiento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenProduccionInsumo;

/**
 * Implementacion JDBC del acceso al consumo teorico de insumos.
 */
@Repository
public class OrdenProduccionInsumoDAOImpl extends DaoBase implements OrdenProduccionInsumoDAO {

    public OrdenProduccionInsumoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<OrdenProduccionInsumo> listarPorOrden(int idOrdenProduccion) {
        return consultarLista("SELECT id_orden_produccion, id_insumo, cantidad_teorica "
                        + "FROM orden_produccion_insumo WHERE id_orden_produccion = ? "
                        + "ORDER BY id_insumo",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                resultado -> {
                    OrdenProduccionInsumo consumo = new OrdenProduccionInsumo();
                    consumo.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
                    consumo.setIdInsumo(resultado.getInt("id_insumo"));
                    consumo.setCantidadTeorica(resultado.getBigDecimal("cantidad_teorica"));
                    return consumo;
                });
    }

    @Override
    public int insertar(OrdenProduccionInsumo consumo, String codUsuario) {
        return ejecutar("INSERT INTO orden_produccion_insumo (id_orden_produccion, id_insumo, "
                        + "cantidad_teorica, usuario_registro) VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE cantidad_teorica = VALUES(cantidad_teorica)",
                List.of(ParametroSql.entero("id_orden_produccion", consumo.getIdOrdenProduccion()),
                        ParametroSql.entero("id_insumo", consumo.getIdInsumo()),
                        ParametroSql.decimal("cantidad_teorica", consumo.getCantidadTeorica()),
                        ParametroSql.texto("usuario_registro",
                                codUsuario == null ? "sistema" : codUsuario)));
    }
}
