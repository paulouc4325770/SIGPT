package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;

/**
 * Implementacion JDBC del acceso a ordenes de produccion.
 */
@Repository
public class OrdenProduccionDAOImpl extends DaoBase implements OrdenProduccionDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(cod_orden, '-', -1) AS UNSIGNED)), 0) AS ultimo "
            + "FROM orden_produccion WHERE cod_orden LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_orden, cod_orden, id_pedido, "
            + "metros_totales, metros_consolidados, horas_maquina_previstas, "
            + "horas_hombre_previstas, fecha_inicio, fecha_fin, estado FROM orden_produccion ";

    public OrdenProduccionDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(OrdenProduccion orden, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO orden_produccion (cod_orden, id_pedido, metros_totales, "
                            + "metros_consolidados, horas_maquina_previstas, "
                            + "horas_hombre_previstas, fecha_inicio, estado, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, orden.getCodOrden());
                sentencia.setInt(2, orden.getIdPedido());
                sentencia.setBigDecimal(3, orden.getMetrosTotales());
                sentencia.setBigDecimal(4, orden.getMetrosConsolidados());
                sentencia.setBigDecimal(5, orden.getHorasMaquinaPrevistas());
                sentencia.setBigDecimal(6, orden.getHorasHombrePrevistas());
                sentencia.setDate(7, orden.getFechaInicio() == null ? null
                        : Date.valueOf(orden.getFechaInicio()));
                sentencia.setString(8, orden.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_orden FROM orden_produccion WHERE cod_orden = ?")) {
                sentencia.setString(1, orden.getCodOrden());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_orden");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id de la orden insertada");
        });
    }

    @Override
    public Optional<OrdenProduccion> buscarPorId(int idOrden) {
        return consultarUno(SQL_BUSCAR + "WHERE id_orden = ?",
                List.of(ParametroSql.entero("id_orden", idOrden)),
                OrdenProduccionDAOImpl::mapear);
    }

    @Override
    public Optional<OrdenProduccion> buscarPorPedido(int idPedido) {
        return consultarUno(SQL_BUSCAR + "WHERE id_pedido = ? ORDER BY id_orden DESC LIMIT 1",
                List.of(ParametroSql.entero("id_pedido", idPedido)),
                OrdenProduccionDAOImpl::mapear);
    }

    @Override
    public List<OrdenFila> listarEnProduccion() {
        return consultarLista("SELECT o.id_orden, o.cod_orden, o.id_pedido, p.cod_pedido, "
                + "c.razon_social AS nombre_cliente, o.metros_totales, o.metros_consolidados, "
                + "o.estado FROM orden_produccion o "
                + "JOIN pedido p ON p.id_pedido = o.id_pedido "
                + "JOIN cliente c ON c.id_cliente = p.id_cliente "
                + "WHERE o.estado = 'EN_PRODUCCION' ORDER BY o.cod_orden",
                List.of(), OrdenProduccionDAOImpl::mapearFila);
    }

    @Override
    public int actualizarMetrosConsolidados(int idOrden, BigDecimal metros) {
        return ejecutar("UPDATE orden_produccion SET metros_consolidados = ?, "
                        + "usuario_modificacion = ?, fecha_modificacion = NOW() WHERE id_orden = ?",
                List.of(ParametroSql.decimal("metros_consolidados", metros),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_orden", idOrden)));
    }

    @Override
    public int actualizarEstado(int idOrden, String estado) {
        return ejecutar("UPDATE orden_produccion SET estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_orden = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_orden", idOrden)));
    }

    @Override
    public String siguienteCodigo() {
        String prefijo = "OP-" + LocalDate.now().getYear() + "-";
        return siguienteCodigo(SQL_ULTIMO_CODIGO, prefijo, 3,
                codigo -> consultarUno("SELECT id_orden FROM orden_produccion WHERE cod_orden = ?",
                        List.of(ParametroSql.texto("cod_orden", codigo)),
                        resultado -> resultado.getInt("id_orden")).isPresent());
    }

    private static OrdenProduccion mapear(ResultSet resultado) throws SQLException {
        OrdenProduccion orden = new OrdenProduccion();
        orden.setIdOrden(resultado.getInt("id_orden"));
        orden.setCodOrden(resultado.getString("cod_orden"));
        orden.setIdPedido(resultado.getInt("id_pedido"));
        orden.setMetrosTotales(resultado.getBigDecimal("metros_totales"));
        orden.setMetrosConsolidados(resultado.getBigDecimal("metros_consolidados"));
        orden.setHorasMaquinaPrevistas(resultado.getBigDecimal("horas_maquina_previstas"));
        orden.setHorasHombrePrevistas(resultado.getBigDecimal("horas_hombre_previstas"));
        orden.setFechaInicio(resultado.getDate("fecha_inicio") == null ? null
                : resultado.getDate("fecha_inicio").toLocalDate());
        orden.setFechaFin(resultado.getDate("fecha_fin") == null ? null
                : resultado.getDate("fecha_fin").toLocalDate());
        orden.setEstado(resultado.getString("estado"));
        return orden;
    }

    private static OrdenFila mapearFila(ResultSet resultado) throws SQLException {
        OrdenFila fila = new OrdenFila();
        fila.setIdOrden(resultado.getInt("id_orden"));
        fila.setCodOrden(resultado.getString("cod_orden"));
        fila.setIdPedido(resultado.getInt("id_pedido"));
        fila.setCodPedido(resultado.getString("cod_pedido"));
        fila.setNombreCliente(resultado.getString("nombre_cliente"));
        fila.setMetrosTotales(resultado.getBigDecimal("metros_totales"));
        fila.setMetrosConsolidados(resultado.getBigDecimal("metros_consolidados"));
        fila.setEstado(resultado.getString("estado"));
        return fila;
    }
}
