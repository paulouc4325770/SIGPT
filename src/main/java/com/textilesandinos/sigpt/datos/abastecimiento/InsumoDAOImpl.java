package com.textilesandinos.sigpt.datos.abastecimiento;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;

/**
 * Implementacion JDBC del acceso a insumos.
 */
@Repository
public class InsumoDAOImpl extends DaoBase implements InsumoDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo FROM insumo "
            + "WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_insumo, codigo, id_proveedor, nombre, "
            + "tipo, unidad_medida, stock_minimo, stock_actual, precio_unitario, ruta_msds, "
            + "estado FROM insumo ";

    public InsumoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Insumo> listar(String busqueda) {
        String sql = SQL_BUSCAR;
        List<ParametroSql> parametros = new ArrayList<>();
        if (busqueda != null && !busqueda.isBlank()) {
            sql += "WHERE nombre LIKE ? OR codigo LIKE ? ";
            String patron = "%" + busqueda.trim() + "%";
            parametros.add(ParametroSql.texto("nombre", patron));
            parametros.add(ParametroSql.texto("codigo", patron));
        }
        sql += "ORDER BY nombre";
        return consultarLista(sql, parametros, InsumoDAOImpl::mapear);
    }

    @Override
    public List<Insumo> listarTodos() {
        return consultarLista(SQL_BUSCAR + "ORDER BY nombre", List.of(), InsumoDAOImpl::mapear);
    }

    @Override
    public List<Insumo> listarEnStockMinimo() {
        return consultarLista(SQL_BUSCAR + "WHERE stock_actual <= stock_minimo AND estado = 'ACTIVO' "
                + "ORDER BY stock_actual", List.of(), InsumoDAOImpl::mapear);
    }

    @Override
    public Optional<Insumo> buscarPorId(int idInsumo) {
        return consultarUno(SQL_BUSCAR + "WHERE id_insumo = ?",
                List.of(ParametroSql.entero("id_insumo", idInsumo)), InsumoDAOImpl::mapear);
    }

    @Override
    public Optional<Insumo> buscarPorCodigo(String codigo) {
        return consultarUno(SQL_BUSCAR + "WHERE codigo = ?",
                List.of(ParametroSql.texto("codigo", codigo)), InsumoDAOImpl::mapear);
    }

    @Override
    public int insertar(Insumo insumo, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO insumo (codigo, id_proveedor, nombre, tipo, unidad_medida, "
                            + "stock_minimo, stock_actual, precio_unitario, ruta_msds, estado, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, insumo.getCodigo());
                sentencia.setInt(2, insumo.getIdProveedor());
                sentencia.setString(3, insumo.getNombre());
                sentencia.setString(4, insumo.getTipo());
                sentencia.setString(5, insumo.getUnidadMedida());
                sentencia.setBigDecimal(6, insumo.getStockMinimo());
                sentencia.setBigDecimal(7, insumo.getStockActual());
                sentencia.setBigDecimal(8, insumo.getPrecioUnitario());
                sentencia.setString(9, insumo.getRutaMsds());
                sentencia.setString(10, insumo.getEstado());
                sentencia.setString(11, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_insumo FROM insumo WHERE codigo = ?")) {
                sentencia.setString(1, insumo.getCodigo());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_insumo");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del insumo insertado");
        });
    }

    @Override
    public int actualizar(Insumo insumo, String codUsuario) {
        return ejecutar("UPDATE insumo SET nombre = ?, tipo = ?, unidad_medida = ?, "
                        + "stock_minimo = ?, precio_unitario = ?, ruta_msds = ?, estado = ?, "
                        + "usuario_modificacion = ?, fecha_modificacion = NOW() WHERE id_insumo = ?",
                List.of(ParametroSql.texto("nombre", insumo.getNombre()),
                        ParametroSql.texto("tipo", insumo.getTipo()),
                        ParametroSql.texto("unidad_medida", insumo.getUnidadMedida()),
                        ParametroSql.decimal("stock_minimo", insumo.getStockMinimo()),
                        ParametroSql.decimal("precio_unitario", insumo.getPrecioUnitario()),
                        ParametroSql.texto("ruta_msds", insumo.getRutaMsds()),
                        ParametroSql.texto("estado", insumo.getEstado()),
                        ParametroSql.texto("usuario_modificacion",
                                codUsuario == null ? "sistema" : codUser(codUsuario)),
                        ParametroSql.entero("id_insumo", insumo.getIdInsumo())));
    }

    private static String codUser(String codUsuario) {
        return codUsuario == null ? "sistema" : codUsuario;
    }

    @Override
    public int actualizarStock(int idInsumo, BigDecimal stockActual) {
        return ejecutar("UPDATE insumo SET stock_actual = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_insumo = ?",
                List.of(ParametroSql.decimal("stock_actual", stockActual),
                        ParametroSql.texto("usuario_modificacion", "sistema"),
                        ParametroSql.entero("id_insumo", idInsumo)));
    }

    @Override
    public String siguienteCodigo() {
        return siguienteCodigo(SQL_ULTIMO_CODIGO, "INS-", 3,
                codigo -> consultarUno("SELECT id_insumo FROM insumo WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_insumo")).isPresent());
    }

    private static Insumo mapear(ResultSet resultado) throws SQLException {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(resultado.getInt("id_insumo"));
        insumo.setCodigo(resultado.getString("codigo"));
        insumo.setIdProveedor(resultado.getInt("id_proveedor"));
        insumo.setNombre(resultado.getString("nombre"));
        insumo.setTipo(resultado.getString("tipo"));
        insumo.setUnidadMedida(resultado.getString("unidad_medida"));
        insumo.setStockMinimo(resultado.getBigDecimal("stock_minimo"));
        insumo.setStockActual(resultado.getBigDecimal("stock_actual"));
        insumo.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
        insumo.setRutaMsds(resultado.getString("ruta_msds"));
        insumo.setEstado(resultado.getString("estado"));
        return insumo;
    }
}
