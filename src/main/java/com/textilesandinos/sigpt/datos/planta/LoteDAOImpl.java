package com.textilesandinos.sigpt.datos.planta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.Lote;

/**
 * Implementacion JDBC del acceso a lotes.
 */
@Repository
public class LoteDAOImpl extends DaoBase implements LoteDAO {

    private static final String SQL_ULTIMO_CODIGO = "SELECT COALESCE(MAX(CAST("
            + "SUBSTRING_INDEX(codigo, '-', -1) AS UNSIGNED)), 0) AS ultimo FROM lote "
            + "WHERE codigo LIKE ?";

    private static final String SQL_BUSCAR = "SELECT id_lote, codigo, id_orden_produccion, "
            + "id_asignacion, metros, estado FROM lote ";

    public LoteDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public int insertar(Lote lote, String codUsuario) {
        return enTransaccion(conexion -> {
            try (var sentencia = conexion.prepareStatement(
                    "INSERT INTO lote (codigo, id_orden_produccion, id_asignacion, metros, estado, "
                            + "usuario_registro) VALUES (?, ?, ?, ?, ?, ?)")) {
                sentencia.setString(1, lote.getCodigo());
                sentencia.setInt(2, lote.getIdOrdenProduccion());
                sentencia.setInt(3, lote.getIdAsignacion());
                sentencia.setBigDecimal(4, lote.getMetros());
                sentencia.setString(5, lote.getEstado());
                sentencia.setString(6, codUsuario == null ? "sistema" : codUsuario);
                sentencia.executeUpdate();
            }
            try (var sentencia = conexion.prepareStatement(
                    "SELECT id_lote FROM lote WHERE codigo = ?")) {
                sentencia.setString(1, lote.getCodigo());
                try (var resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        return resultado.getInt("id_lote");
                    }
                }
            }
            throw new SQLException("No se pudo recuperar el id del lote insertado");
        });
    }

    @Override
    public Optional<Lote> buscarPorId(int idLote) {
        return consultarUno(SQL_BUSCAR + "WHERE id_lote = ?",
                List.of(ParametroSql.entero("id_lote", idLote)), LoteDAOImpl::mapear);
    }

    @Override
    public List<Lote> listarPendientes() {
        return consultarLista(SQL_BUSCAR + "WHERE estado = 'PENDIENTE_INSPECCION' ORDER BY codigo",
                List.of(), LoteDAOImpl::mapear);
    }

    @Override
    public List<Lote> listarPorOrden(int idOrdenProduccion) {
        return consultarLista(SQL_BUSCAR + "WHERE id_orden_produccion = ? ORDER BY codigo",
                List.of(ParametroSql.entero("id_orden_produccion", idOrdenProduccion)),
                LoteDAOImpl::mapear);
    }

    @Override
    public int actualizarEstado(int idLote, String estado) {
        return ejecutar("UPDATE lote SET estado = ? WHERE id_lote = ?",
                List.of(ParametroSql.texto("estado", estado),
                        ParametroSql.entero("id_lote", idLote)));
    }

    @Override
    public String siguienteCodigo() {
        String prefijo = "LOTE-" + LocalDate.now().getYear() + "-";
        return siguienteCodigo(SQL_ULTIMO_CODIGO, prefijo, 3,
                codigo -> consultarUno("SELECT id_lote FROM lote WHERE codigo = ?",
                        List.of(ParametroSql.texto("codigo", codigo)),
                        resultado -> resultado.getInt("id_lote")).isPresent());
    }

    private static Lote mapear(ResultSet resultado) throws SQLException {
        Lote lote = new Lote();
        lote.setIdLote(resultado.getInt("id_lote"));
        lote.setCodigo(resultado.getString("codigo"));
        lote.setIdOrdenProduccion(resultado.getInt("id_orden_produccion"));
        lote.setIdAsignacion(resultado.getInt("id_asignacion"));
        lote.setMetros(resultado.getBigDecimal("metros"));
        lote.setEstado(resultado.getString("estado"));
        return lote;
    }
}
