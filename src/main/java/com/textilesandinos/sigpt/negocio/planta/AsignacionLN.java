package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.OperarioDAO;
import com.textilesandinos.sigpt.datos.planta.TurnoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.entidades.planta.Operario;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.Turno;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Asignacion de operarios y maquinaria (EF-1001-6).
 *
 * <p>Aplica la Regla R-Turno: un operario no puede recibir turnos consecutivos
 * que superen las 16 horas (RD3). Tambien valida la disponibilidad de la
 * maquina y la existencia de una Orden de Produccion en produccion.
 */
@Service
public class AsignacionLN {

    private final AsignacionDAO asignacionDAO;
    private final OperarioDAO operarioDAO;
    private final MaquinaDAO maquinaDAO;
    private final TurnoDAO turnoDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;

    public AsignacionLN(AsignacionDAO asignacionDAO, OperarioDAO operarioDAO,
            MaquinaDAO maquinaDAO, TurnoDAO turnoDAO, OrdenProduccionDAO ordenDAO,
            ParametrosNegocio parametros, AuditoriaHelper auditoria) {
        this.asignacionDAO = asignacionDAO;
        this.operarioDAO = operarioDAO;
        this.maquinaDAO = maquinaDAO;
        this.turnoDAO = turnoDAO;
        this.ordenDAO = ordenDAO;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    /** Ordenes de produccion que pueden recibir asignaciones. */
    public List<OrdenFila> ordenesEnProduccion() {
        return ordenDAO.listarEnProduccion();
    }

    /** Operarios activos del padron. */
    public List<Operario> operarios() {
        return operarioDAO.listarActivos();
    }

    /** Maquinas del parque con su estado actual. */
    public List<Maquina> maquinas() {
        return maquinaDAO.listarTodas();
    }

    /** Turnos de la jornada. */
    public List<Turno> turnos() {
        return turnoDAO.listarTodos();
    }

    /** Asignaciones ya registradas de una orden. */
    public List<Asignacion> asignacionesDe(int idOrdenProduccion) {
        return asignacionDAO.listarPorOrden(idOrdenProduccion);
    }

    /**
     * Registra la asignacion de un operario a una maquina en un turno (paso 5 y 6
     * del flujo de EF-1001-6).
     */
    public Asignacion asignar(AsignacionFormulario form, String codUsuario) {
        validar(form);

        Operario operario = operarioDAO.buscarPorId(form.getIdOperario())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El operario seleccionado no esta registrado."));
        if (!"ACTIVO".equals(operario.getEstado())) {
            throw new ReglaNegocioException("El operario " + operario.getCodigo()
                    + " esta inactivo: no puede recibir asignaciones.");
        }

        Maquina maquina = maquinaDAO.buscarPorId(form.getIdMaquina())
                .orElseThrow(() -> new ReglaNegocioException(
                        "La maquina seleccionada no esta registrada."));
        if ("MANTENIMIENTO".equals(maquina.getEstado())) {
            throw new ReglaNegocioException("La maquina " + maquina.getCodigo()
                    + " esta en mantenimiento: no puede recibir asignaciones.");
        }
        if (!"LIBRE".equals(maquina.getEstado()) && !"OCUPADA".equals(maquina.getEstado())) {
            throw new ReglaNegocioException("La maquina " + maquina.getCodigo() + " esta en estado "
                    + maquina.getEstado() + ": no puede recibir asignaciones.");
        }

        Turno turno = turnoDAO.buscarPorId(form.getIdTurno())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El turno seleccionado no esta registrado."));

        OrdenProduccion orden = ordenDAO.buscarPorId(form.getIdOrdenProduccion())
                .orElseThrow(() -> new ReglaNegocioException(
                        "La Orden de Produccion seleccionada no esta registrada."));
        if (!"EN_PRODUCCION".equals(orden.getEstado())) {
            throw new ReglaNegocioException("La Orden de Produccion " + orden.getCodOrden()
                    + " no esta en produccion: no admite asignaciones.");
        }

        LocalDate fecha = form.getFecha() == null ? LocalDate.now() : form.getFecha();

        // Maquina ocupada en el mismo turno: se sugiere otra.
        for (Asignacion existente : asignacionDAO.listarPorMaquinaYFecha(maquina.getIdMaquina(),
                fecha)) {
            if ("CANCELADA".equals(existente.getEstado())) {
                continue;
            }
            Turno turnoExistente = turnoDAO.buscarPorId(existente.getIdTurno()).orElse(null);
            if (turnoExistente != null && turnoExistente.getIdTurno().equals(turno.getIdTurno())) {
                throw new ReglaNegocioException("La maquina " + maquina.getCodigo()
                        + " ya esta ocupada en el turno " + turno.getNombre()
                        + ". Seleccione otra maquina.");
            }
        }

        // Regla R-Turno / RD3: jornada maxima de horas consecutivas.
        BigDecimal maximo = parametros.decimal("JORNADA_MAXIMA_HORAS");
        BigDecimal yaAsignadas = asignacionDAO.horasAsignadas(operario.getIdOperario(), fecha);
        BigDecimal horasTurno = turno.getHoras() == null ? BigDecimal.valueOf(8) : turno.getHoras();
        BigDecimal total = yaAsignadas.add(horasTurno);
        if (total.compareTo(maximo) > 0) {
            throw new ReglaNegocioException("El operario " + operario.getCodigo() + " ya tiene "
                    + yaAsignadas.toPlainString() + " h asignadas: con el turno "
                    + turno.getNombre() + " superaria las " + maximo.toPlainString()
                    + " horas consecutivas (Regla R-Turno).", "RD3");
        }

        Asignacion asignacion = new Asignacion();
        asignacion.setIdOperario(operario.getIdOperario());
        asignacion.setIdMaquina(maquina.getIdMaquina());
        asignacion.setIdTurno(turno.getIdTurno());
        asignacion.setFecha(fecha);
        asignacion.setIdOrdenProduccion(orden.getIdOrden());
        asignacion.setMetaMetros(form.getMetaMetros() == null ? BigDecimal.ZERO
                : form.getMetaMetros());
        asignacion.setHorasAsignadas(horasTurno);
        asignacion.setEstado("ASIGNADA");

        int idAsignacion = asignacionDAO.insertar(asignacion, codUsuario);
        asignacion.setIdAsignacion(idAsignacion);

        if ("LIBRE".equals(maquina.getEstado())) {
            maquinaDAO.actualizarEstado(maquina.getIdMaquina(), "OCUPADA");
        }

        auditoria.registrar(codUsuario, "PLANTA", "ASIGNAR_OPERARIO", "asignacion",
                String.valueOf(idAsignacion), "Orden " + orden.getCodOrden() + " | Operario "
                        + operario.getCodigo() + " | Maquina " + maquina.getCodigo() + " | Turno "
                        + turno.getNombre());
        return asignacion;
    }

    private void validar(AsignacionFormulario form) {
        if (form == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (form.getIdOperario() == null) {
            throw new ReglaNegocioException("Debe seleccionar un operario.");
        }
        if (form.getIdMaquina() == null) {
            throw new ReglaNegocioException("Debe seleccionar una maquina.");
        }
        if (form.getIdTurno() == null) {
            throw new ReglaNegocioException("Debe seleccionar un turno.");
        }
        if (form.getIdOrdenProduccion() == null) {
            throw new ReglaNegocioException("Debe seleccionar una Orden de Produccion.");
        }
    }
}
