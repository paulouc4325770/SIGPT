package com.textilesandinos.sigpt.datos.planta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.textilesandinos.sigpt.datos.comun.DaoBase;
import com.textilesandinos.sigpt.datos.comun.ParametroSql;
import com.textilesandinos.sigpt.entidades.planta.Turno;

/**
 * Implementacion JDBC del acceso a turnos.
 */
@Repository
public class TurnoDAOImpl extends DaoBase implements TurnoDAO {

    private static final String SQL_BUSCAR = "SELECT id_turno, codigo, nombre, hora_inicio, "
            + "hora_fin, horas FROM turno ";

    public TurnoDAOImpl(DataSource origenDatos) {
        super(origenDatos);
    }

    @Override
    public List<Turno> listarTodos() {
        return consultarLista(SQL_BUSCAR + "ORDER BY hora_inicio", List.of(), TurnoDAOImpl::mapear);
    }

    @Override
    public Optional<Turno> buscarPorId(int idTurno) {
        return consultarUno(SQL_BUSCAR + "WHERE id_turno = ?",
                List.of(ParametroSql.entero("id_turno", idTurno)), TurnoDAOImpl::mapear);
    }

    private static Turno mapear(ResultSet resultado) throws SQLException {
        Turno turno = new Turno();
        turno.setIdTurno(resultado.getInt("id_turno"));
        turno.setCodigo(resultado.getString("codigo"));
        turno.setNombre(resultado.getString("nombre"));
        turno.setHoraInicio(resultado.getTime("hora_inicio") == null ? null
                : resultado.getTime("hora_inicio").toLocalTime());
        turno.setHoraFin(resultado.getTime("hora_fin") == null ? null
                : resultado.getTime("hora_fin").toLocalTime());
        turno.setHoras(resultado.getBigDecimal("horas"));
        return turno;
    }
}
