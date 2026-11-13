package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;

/**
 * Implementacion JDBC del acceso a pedidos y fichas tecnicas.
 */
@Repository
public class PedidoDAOImpl extends DaoBase implements PedidoDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(cod_pedido, '-', -1) AS UNSIGNED)), 0) AS ultimo FROM pedido "
            + "WHERE cod_pedido LIKE ?";

    private static final String SQL_LISTADO = "SELECT p.id_pedido, p.cod_pedido, p.id_cliente, "
            + "c.razon_social AS nombre_cliente, p.metros, p.metros_consolidados, "
            + "p.fecha_entrega, p.estado, f.tipo_hilado, f.gramaje_m2, f.color_pantone "
            + "FROM pedido p "
            + "JOIN cliente c ON c.id_cliente = p.id_cliente "
            + "LEFT JOIN ficha_tecnica f ON f.id_pedido = p.id_pedido ";

    public PedidoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<PedidoFila> listar(String codigo, String estado, Integer idCliente,
            LocalDate desde, LocalDate hasta) {
        String sql = SQL_LISTADO + "WHERE 1 = 1 ";
        List<ParametroSql> parametros = new ArrayList<>();
        if (codigo != null && !codigo.isBlank()) {
            sql += "AND p.cod_pedido LIKE ? ";
            parametros.add(ParametroSql.texto("cod_pedigo", "%" + codigo.trim() + "%"));
        }
        if (estado != null && !estado.isBlank()) {
            sql += "AND p.estado = ? ";
            parametros.add(ParametroSql.texto("estado", estado));
        }
        if (idCliente != null) {
            sql += "AND p.id_cliente = ? ";
            parametros.add(ParametroSql.entero("id_cliente", idCliente));
        }
        if (desde != null) {
            sql += "AND p.fecha_entrega >= ? ";
            parametros.add(ParametroSql.fecha("desde", Date.valueOf(desde)));
        }
        if (hasta != null) {
            sql += "AND p.fecha_entrega <= ? ";
            parametros.add(ParametroSql.fecha("hasta", Date.valueOf(hasta)));
        }
        sql += "ORDER BY p.fecha_registro DESC, p.id_pedido DESC";
        return consultarLista(sql, parametros, PedidoDAOImpl::mapearFila);
    }

    @Override
    public ResumenPedidos resumir() {
        return consultarUno("SELECT COUNT(*) AS total, "
                        + "COALESCE(SUM(estado = 'REGISTRADO'), 0) AS registrados, "
                        + "COALESCE(SUM(estado = 'COTIZADO'), 0) AS cotizados, "
                        + "COALESCE(SUM(estado = 'EN_PRODUCCION'), 0) AS en_produccion, "
                        + "COALESCE(SUM(estado = 'TERMINADO'), 0) AS terminados, "
                        + "COALESCE(SUM(estado = 'ANULADO'), 0) AS anulados FROM pedido",
                List.of(), resultado -> {
                    ResumenPedidos resumen = new ResumenPedidos();
                    resumen.setTotal(resultado.getInt("total"));
                    resumen.setRegistrados(resultado.getInt("registrados"));
                    resumen.setCotizados(resultado.getInt("cotizados"));
                    resumen.setEnProduccion(resultado.getInt("en_produccion"));
                    resumen.setTerminados(resultado.getInt("terminados"));
                    resumen.setAnulados(resultado.getInt("anulados"));
                    return resumen;
                }).orElseGet(ResumenPedidos::new);
    }

    @Override
    public Optional<Pedido> buscarPorId(int idPedido) {
        return consultarUno("SELECT id_pedido, cod_pedido, id_cliente, metros, fecha_entrega, "
                + "estado, metros_consolidados, fecha_registro_operativa FROM pedido "
                + "WHERE id_pedido = ?",
                List.of(ParametroSql.entero("id_pedido", idPedido)),
                PedidoDAOImpl::mapearPedido);
    }

    @Override
    public Optional<FichaTecnica> buscarFichaTecnica(int idPedido) {
        return consultarUno("SELECT id_ficha, id_pedido, tipo_hilado, gramaje_m2, color_pantone, "
                + "composicion, metros, tolerancia_color, observaciones FROM ficha_tecnica "
                + "WHERE id_pedido = ?",
                List.of(ParametroSql.entero("id_pedido", idPedido)),
                PedidoDAOImpl::mapearFicha);
    }

    @Override
    public int insertarConFicha(Pedido pedido, FichaTecnica ficha, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO pedido (cod_pedido, id_cliente, metros, fecha_entrega, estado, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, pedido.getCodPedido());
                sentencia.setInt(2, pedido.getIdCliente());
                sentencia.setBigDecimal(3, pedido.getMetros());
                sentencia.setDate(4, Date.valueOf(pedido.getFechaEntrega()));
                sentencia.setString(5, pedido.getEstado());
                sentencia.setString(6, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            int idPedido;
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_pedido FROM pedido WHERE cod_pedido = ?")) {
                sentencia.setString(1, pedido.getCodPedido());
                try (var resultado = sentencia.executeQuery()) {
                    if (!resultado.next()) {
                        throw new SQLException("No se pudo recuperar el id del pedido insertado");
                    }
                    idPedido = resultado.getInt("id_pedido");
                }
            }
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO ficha_tecnica (id_pedido, tipo_hilado, gramaje_m2, color_pantone, "
                            + "composicion, metros, tolerancia_color, observaciones, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setInt(1, idPedido);
                sentencia.setString(2, ficha.getTipoHilado());
                sentencia.setBigDecimal(3, ficha.getGramajeM2());
                sentencia.setString(4, ficha.getColorPantone());
                sentencia.setString(5, ficha.getComposicion());
                sentencia.setBigDecimal(6, ficha.getMetros());
                sentencia.setBigDecimal(7, ficha.getToleranciaColor());
                sentencia.setString(8, ficha.getObservaciones());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            return idPedido;
        });
    }

    @Override
    public int actualizarEstado(int idPedido, String estado, String codUsuario) {
        return ejecutar("UPDATE pedido SET estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW(), fecha_registro_operativa = "
                        + "IF(? = 'EN_PRODUCCION', CURDATE(), fecha_registro_operativa) "
                        + "WHERE id_pedido = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.texto("usuario_modificacion",
                                codUsuario == null ? "sistema" : codUsuario),
                        ParametroSql.texto("estado_operativa", estado),
                        ParametroSql.entero("id_pedido", idPedido)));
    }

    @Override
    public int actualizarMetrosConsolidados(int idPedido, BigDecimal metros) {
        return ejecutar("UPDATE pedido SET metros_consolidados = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_pedido = ?",
                List.of(ParametroSql.decimal("metros_consolidados", metros),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_pedido", idPedido)));
    }

    @Override
    public String siguienteCodigo() {
        String prefijo = "PED-" + LocalDate.now().getYear() + "-";
        return siguienteCodigo(SQL_ULTIMO_CODIGO, prefijo, 3,
                codigo -> consultarUno("SELECT id_pedido FROM pedido WHERE cod_pedido = ?",
                        List.of(ParametroSql.texto("cod_pedido", codigo)),
                        resultado -> resultado.getInt("id_pedido")).isPresent());
    }

    private static PedidoFila mapearFila(ResultSet resultado) throws SQLException {
        PedidoFila fila = new PedidoFila();
        fila.setIdPedido(resultado.getInt("id_pedido"));
        fila.setCodPedido(resultado.getString("cod_pedido"));
        fila.setIdCliente(resultado.getInt("id_cliente"));
        fila.setNombreCliente(resultado.getString("nombre_cliente"));
        fila.setTipoHilado(resultado.getString("tipo_hilado"));
        fila.setGramajeM2(resultado.getBigDecimal("gramaje_m2"));
        fila.setColorPantone(resultado.getString("color_pantone"));
        fila.setMetros(resultado.getBigDecimal("metros"));
        fila.setMetrosConsolidados(resultado.getBigDecimal("metros_consolidados"));
        fila.setFechaEntrega(resultado.getDate("fecha_entrega") == null ? null
                : resultado.getDate("fecha_entrega").toLocalDate());
        fila.setEstado(resultado.getString("estado"));
        BigDecimal metros = fila.getMetros() == null ? BigDecimal.ZERO : fila.getMetros();
        BigDecimal consolidado = fila.getMetrosConsolidados() == null ? BigDecimal.ZERO
                : fila.getMetrosConsolidados();
        fila.setPorcentajeAvance(metros.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : consolidado.multiply(BigDecimal.valueOf(100))
                        .divide(metros, 2, java.math.RoundingMode.HALF_UP));
        return fila;
    }

    private static Pedido mapearPedido(ResultSet resultado) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(resultado.getInt("id_pedido"));
        pedido.setCodPedido(resultado.getString("cod_pedido"));
        pedido.setIdCliente(resultado.getInt("id_cliente"));
        pedido.setMetros(resultado.getBigDecimal("metros"));
        pedido.setFechaEntrega(resultado.getDate("fecha_entrega") == null ? null
                : resultado.getDate("fecha_entrega").toLocalDate());
        pedido.setEstado(resultado.getString("estado"));
        pedido.setMetrosConsolidados(resultado.getBigDecimal("metros_consolidados"));
        pedido.setFechaRegistroOperativa(resultado.getDate("fecha_registro_operativa") == null ? null
                : resultado.getDate("fecha_registro_operativa").toLocalDate());
        return pedido;
    }

    private static FichaTecnica mapearFicha(ResultSet resultado) throws SQLException {
        FichaTecnica ficha = new FichaTecnica();
        ficha.setIdFicha(resultado.getInt("id_ficha"));
        ficha.setIdPedido(resultado.getInt("id_pedido"));
        ficha.setTipoHilado(resultado.getString("tipo_hilado"));
        ficha.setGramajeM2(resultado.getBigDecimal("gramaje_m2"));
        ficha.setColorPantone(resultado.getString("color_pantone"));
        ficha.setComposicion(resultado.getString("composicion"));
        ficha.setMetros(resultado.getBigDecimal("metros"));
        ficha.setToleranciaColor(resultado.getBigDecimal("tolerancia_color"));
        ficha.setObservaciones(resultado.getString("observaciones"));
        return ficha;
    }
}
