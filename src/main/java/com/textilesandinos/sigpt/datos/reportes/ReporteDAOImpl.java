package com.textilesandinos.sigpt.datos.reportes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.reportes.FormatoSalida;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGerencial;

/**
 * Implementacion JDBC del acceso a reportes gerenciales.
 */
@Repository
public class ReporteDAOImpl extends DaoBase implements ReporteDAO {

    public ReporteDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<ReporteGerencial> listarReportes() {
        return consultarLista("SELECT id_reporte, codigo, nombre, descripcion, estado "
                + "FROM reporte_gerencial WHERE estado = TRUE ORDER BY id_reporte", List.of(),
                resultado -> {
                    ReporteGerencial reporte = new ReporteGerencial();
                    reporte.setIdReporte(resultado.getInt("id_reporte"));
                    reporte.setCodigo(resultado.getString("codigo"));
                    reporte.setNombre(resultado.getString("nombre"));
                    reporte.setDescripcion(resultado.getString("descripcion"));
                    reporte.setEstado(resultado.getBoolean("estado"));
                    return reporte;
                });
    }

    @Override
    public List<FormatoSalida> listarFormatos() {
        return consultarLista("SELECT id_formato, codigo, nombre, clase_implementacion, estado "
                + "FROM formato_salida WHERE estado = TRUE ORDER BY id_formato", List.of(),
                resultado -> {
                    FormatoSalida formato = new FormatoSalida();
                    formato.setIdFormato(resultado.getInt("id_formato"));
                    formato.setCodigo(resultado.getString("codigo"));
                    formato.setNombre(resultado.getString("nombre"));
                    formato.setClaseImplementacion(resultado.getString("clase_implementacion"));
                    formato.setEstado(resultado.getBoolean("estado"));
                    return formato;
                });
    }

    @Override
    public int registrarGenerado(ReporteGenerado reporte) {
        LocalDateTime momento = reporte.getFechaGeneracion() == null ? LocalDateTime.now()
                : reporte.getFechaGeneracion();
        return ejecutar("INSERT INTO reporte_generado (id_reporte, cod_usuario, filtros, "
                        + "formato_salida, fecha_generacion) VALUES (?, ?, ?, ?, ?)",
                List.of(ParametroSql.entero("id_reporte", reporte.getIdReporte()),
                        ParametroSql.texto("cod_usuario", reporte.getCodUsuario()),
                        ParametroSql.texto("filtros", reporte.getFiltros()),
                        ParametroSql.texto("formato_salida", reporte.getFormatoSalida()),
                        ParametroSql.fechaHora("fecha_generacion", Timestamp.valueOf(momento))));
    }

    @Override
    public List<ReporteGenerado> listarGenerados(int limite) {
        return consultarLista("SELECT id_reporte_generado, id_reporte, cod_usuario, filtros, "
                        + "formato_salida, fecha_generacion FROM reporte_generado "
                        + "ORDER BY fecha_generacion DESC LIMIT ?",
                List.of(ParametroSql.entero("limite", limite)),
                resultado -> {
                    ReporteGenerado generado = new ReporteGenerado();
                    generado.setIdReporteGenerado(resultado.getLong("id_reporte_generado"));
                    generado.setIdReporte(resultado.getInt("id_reporte"));
                    generado.setCodUsuario(resultado.getString("cod_usuario"));
                    generado.setFiltros(resultado.getString("filtros"));
                    generado.setFormatoSalida(resultado.getString("formato_salida"));
                    Timestamp fecha = resultado.getTimestamp("fecha_generacion");
                    generado.setFechaGeneracion(fecha == null ? null : fecha.toLocalDateTime());
                    return generado;
                });
    }
}
