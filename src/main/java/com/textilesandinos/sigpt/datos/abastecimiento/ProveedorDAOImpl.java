package com.textilesandinos.sigpt.datos.abastecimiento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;

/**
 * Implementacion JDBC del acceso a proveedores.
 */
@Repository
public class ProveedorDAOImpl extends DaoBase implements ProveedorDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo FROM proveedor "
            + "WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_proveedor, codigo, razon_social, ruc, tipo, "
            + "contacto, correo, telefono, estado FROM proveedor ";

    public ProveedorDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Proveedor> listar(String busqueda) {
        String sql = SQL_BUSCAR;
        List<ParametroSql> parametros = new ArrayList<>();
        if (busqueda != null && !busqueda.isBlank()) {
            sql += "WHERE razon_social LIKE ? OR codigo LIKE ? ";
            String patron = "%" + busqueda.trim() + "%";
            parametros.add(ParametroSql.texto("razon_social", patron));
            parametros.add(ParametroSql.texto("codigo", patron));
        }
        sql += "ORDER BY razon_social";
        return consultarLista(sql, parametros, ProveedorDAOImpl::mapear);
    }

    @Override
    public Optional<Proveedor> buscarPorId(int idProveedor) {
        return consultarUno(SQL_BUSCAR + "WHERE id_proveedor = ?",
                List.of(ParametroSql.entero("id_proveedor", idProveedor)), ProveedorDAOImpl::mapear);
    }

    @Override
    public Optional<Proveedor> buscarPorCodigo(String codigo) {
        return consultarUno(SQL_BUSCAR + "WHERE codigo = ?",
                List.of(ParametroSql.texto("codigo", codigo)), ProveedorDAOImpl::mapear);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return consultarUno("SELECT id_proveedor FROM proveedor WHERE codigo = ?",
                List.of(ParametroSql.texto("codigo", codigo)),
                resultado -> resultado.getInt("id_proveedor")).isPresent();
    }

    @Override
    public int insertar(Proveedor proveedor, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO proveedor (codigo, razon_social, ruc, tipo, contacto, correo, "
                            + "telefono, estado, usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, proveedor.getCodigo());
                sentencia.setString(2, proveedor.getRazonSocial());
                sentencia.setString(3, proveedor.getRuc());
                sentencia.setString(4, proveedor.getTipo());
                sentencia.setString(5, proveedor.getContacto());
                sentencia.setString(6, proveedor.getCorreo());
                sentencia.setString(7, proveedor.getTelefono());
                sentencia.setString(8, proveedor.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_proveedor FROM proveedor WHERE codigo = ?")) {
                sentencia.setString(1, proveedor.getCodigo());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_proveedor");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del proveedor insertado");
        });
    }

    @Override
    public int actualizar(Proveedor proveedor, String codUsuario) {
        return ejecutar("UPDATE proveedor SET razon_social = ?, ruc = ?, tipo = ?, contacto = ?, "
                        + "correo = ?, telefono = ?, estado = ?, usuario_modificacion = ?, "
                        + "fecha_modificacion = NOW() WHERE id_proveedor = ?",
                List.of(ParametroSql.texto("razon_social", proveedor.getRazonSocial()),
                        ParametroSql.texto("ruc", proveedor.getRuc()),
                        ParametroSql.texto("tipo", proveedor.getTipo()),
                        ParametroSql.texto("contacto", proveedor.getContacto()),
                        ParametroSql.texto("correo", proveedor.getCorreo()),
                        ParametroSql.texto("telefono", proveedor.getTelefono()),
                        ParametroSql.texto("estado", proveedor.getEstado()),
                        ParametroSql.texto("usuario_modificacion",
                                codUsuario == null ? "sistema" : codUsuario),
                        ParametroSql.entero("id_proveedor", proveedor.getIdProveedor())));
    }

    @Override
    public String siguienteCodigo() {
        return siguienteCodigo(SQL_ULTIMO_CODIGO, "PRV-", 3,
                codigo -> consultarUno("SELECT id_proveedor FROM proveedor WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_proveedor")).isPresent());
    }

    private static Proveedor mapear(ResultSet resultado) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(resultado.getInt("id_proveedor"));
        proveedor.setCodigo(resultado.getString("codigo"));
        proveedor.setRazonSocial(resultado.getString("razon_social"));
        proveedor.setRuc(resultado.getString("ruc"));
        proveedor.setTipo(resultado.getString("tipo"));
        proveedor.setContacto(resultado.getString("contacto"));
        proveedor.setCorreo(resultado.getString("correo"));
        proveedor.setTelefono(resultado.getString("telefono"));
        proveedor.setEstado(resultado.getString("estado"));
        return proveedor;
    }
}
