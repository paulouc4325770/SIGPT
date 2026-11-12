package com.textilesandinos.sigpt.datos.pedidos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;

/**
 * Implementacion JDBC del acceso a clientes.
 */
@Repository
public class ClienteDAOImpl extends DaoBase implements ClienteDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo FROM cliente "
            + "WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_cliente, codigo, razon_social, ruc_tax_id, "
            + "pais, contacto, correo, telefono, estado FROM cliente ";

    public ClienteDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Cliente> listar(String busqueda) {
        String sql = SQL_BUSCAR;
        List<ParametroSql> parametros = new java.util.ArrayList<>();
        if (busqueda != null && !busqueda.isBlank()) {
            sql += "WHERE razon_social LIKE ? OR ruc_tax_id LIKE ? ";
            String patron = "%" + busqueda.trim() + "%";
            parametros.add(ParametroSql.texto("razon_social", patron));
            parametros.add(ParametroSql.texto("ruc_tax_id", patron));
        }
        sql += "ORDER BY razon_social";
        return consultarLista(sql, parametros, ClienteDAOImpl::mapear);
    }

    @Override
    public Optional<Cliente> buscarPorId(int idCliente) {
        return consultarUno(SQL_BUSCAR + "WHERE id_cliente = ?",
                List.of(ParametroSql.entero("id_cliente", idCliente)), ClienteDAOImpl::mapear);
    }

    @Override
    public Optional<Cliente> buscarPorRuc(String rucTaxId) {
        return consultarUno(SQL_BUSCAR + "WHERE ruc_tax_id = ?",
                List.of(ParametroSql.texto("ruc_tax_id", rucTaxId)), ClienteDAOImpl::mapear);
    }

    @Override
    public boolean existeRuc(String rucTaxId, Integer idClienteExcluido) {
        String sql = "SELECT id_cliente FROM cliente WHERE ruc_tax_id = ?";
        List<ParametroSql> parametros = new java.util.ArrayList<>();
        parametros.add(ParametroSql.texto("ruc_tax_id", rucTaxId));
        if (idClienteExcluido != null) {
            sql += " AND id_cliente <> ?";
            parametros.add(ParametroSql.entero("id_cliente", idClienteExcluido));
        }
        return consultarUno(sql, parametros,
                resultado -> resultado.getInt("id_cliente")).isPresent();
    }

    @Override
    public int insertar(Cliente cliente, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO cliente (codigo, razon_social, ruc_tax_id, pais, contacto, "
                            + "correo, telefono, estado, usuario_registro) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, cliente.getCodigo());
                sentencia.setString(2, cliente.getRazonSocial());
                sentencia.setString(3, cliente.getRucTaxId());
                sentencia.setString(4, cliente.getPais());
                sentencia.setString(5, cliente.getContacto());
                sentencia.setString(6, cliente.getCorreo());
                sentencia.setString(7, cliente.getTelefono());
                sentencia.setString(8, cliente.getEstado());
                sentencia.setString(9, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_cliente FROM cliente WHERE ruc_tax_id = ?")) {
                sentencia.setString(1, cliente.getRucTaxId());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_cliente");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del cliente insertado");
        });
    }

    @Override
    public int actualizar(Cliente cliente, String codUsuario) {
        return ejecutar("UPDATE cliente SET razon_social = ?, ruc_tax_id = ?, pais = ?, "
                        + "contacto = ?, correo = ?, telefono = ?, estado = ?, "
                        + "usuario_modificacion = ?, fecha_modificacion = NOW() WHERE id_cliente = ?",
                List.of(ParametroSql.texto("razon_social", cliente.getRazonSocial()),
                        ParametroSql.texto("ruc_tax_id", cliente.getRucTaxId()),
                        ParametroSql.texto("pais", cliente.getPais()),
                        ParametroSql.texto("contacto", cliente.getContacto()),
                        ParametroSql.texto("correo", cliente.getCorreo()),
                        ParametroSql.texto("telefono", cliente.getTelefono()),
                        ParametroSql.texto("estado", cliente.getEstado()),
                        ParametroSql.texto("usuario_modificacion",
                                codUsuario == null ? "sistema" : codUsuario),
                        ParametroSql.entero("id_cliente", cliente.getIdCliente())));
    }

    @Override
    public String siguienteCodigo() {
        return siguienteCodigo(SQL_ULTIMO_CODIGO, "CLI-", 3,
                codigo -> consultarUno("SELECT id_cliente FROM cliente WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_cliente")).isPresent());
    }

    @Override
    public int contar() {
        return consultarUno("SELECT COUNT(*) AS total FROM cliente", List.of(),
                resultado -> resultado.getInt("total")).orElse(0);
    }

    private static Cliente mapear(ResultSet resultado) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(resultado.getInt("id_cliente"));
        cliente.setCodigo(resultado.getString("codigo"));
        cliente.setRazonSocial(resultado.getString("razon_social"));
        cliente.setRucTaxId(resultado.getString("ruc_tax_id"));
        cliente.setPais(resultado.getString("pais"));
        cliente.setContacto(resultado.getString("contacto"));
        cliente.setCorreo(resultado.getString("correo"));
        cliente.setTelefono(resultado.getString("telefono"));
        cliente.setEstado(resultado.getString("estado"));
        return cliente;
    }
}
