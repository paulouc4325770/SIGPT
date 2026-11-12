package com.textilesandinos.sigpt.negocio.planta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.IncidenciaDAO;
import com.textilesandinos.sigpt.datos.planta.LoteDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.Incidencia;
import com.textilesandinos.sigpt.entidades.planta.Lote;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Registro de avance del operario (EF-1001-7, pasos 1 a 5).
 *
 * <p>El sistema calcula la varianza del avance frente a la velocidad teorica
 * del telar y avisa cuando supera el 15 % (Regla R-Varianza). El avance
 * genera un lote que queda pendiente de inspeccion de calidad.
 */
@Service
public class AvanceLN {

    /** Texto de la alerta de la Regla R-Varianza. */
    public static final String ALERTA_VARIANZA =
            "Baja Productividad o Parada no Reportada";

    private final RegistroAvanceDAO avanceDAO;
    private final AsignacionDAO asignacionDAO;
    private final LoteDAO loteDAO;
    private final MaquinaDAO maquinaDAO;
    private final IncidenciaDAO incidenciaDAO;
    private final OrdenProduccionDAO ordenDAO;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;

    public AvanceLN(RegistroAvanceDAO avanceDAO, AsignacionDAO asignacionDAO, LoteDAO loteDAO,
            MaquinaDAO maquinaDAO, IncidenciaDAO incidenciaDAO, OrdenProduccionDAO ordenDAO,
            ParametrosNegocio parametros, AuditoriaHelper auditoria) {
        this.avanceDAO = avanceDAO;
        this.asignacionDAO = asignacionDAO;
        this.loteDAO = loteDAO;
        this.maquinaDAO = maquinaDAO;
        this.incidenciaDAO = incidenciaDAO;
        this.ordenDAO = ordenDAO;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    /** Asignaciones vigentes del operario, para el terminal tactil. */
    public List<Asignacion> asignacionesDe(int idOrdenProduccion) {
        return asignacionDAO.listarPorOrden(idOrdenProduccion);
    }

    /** Avances registrados de una orden. */
    public List<RegistroAvance> avancesDe(int idOrdenProduccion) {
        return avanceDAO.listarPorOrden(idOrdenProduccion);
    }

    /**
     * Registra el avance del turno (paso 5 del flujo de EF-1001-7).
     *
     * @throws ReglaNegocioException si el operario no tiene asignacion vigente
     */
    public ResultadoAvance registrar(AvanceFormulario form, String codUsuario) {
        if (form == null || form.getIdAsignacion() == null) {
            throw new ReglaNegocioException("No tiene una asignacion vigente: "
                    + "no puede registrar avance.");
        }
        if (form.getMetrosProducidos() == null
                || form.getMetrosProducidos().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("Debe registrar los metros producidos en el turno.");
        }
        Asignacion asignacion = asignacionDAO.buscarPorId(form.getIdAsignacion())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
        if ("CANCELADA".equals(asignacion.getEstado())) {
            throw new ReglaNegocioException("La asignacion esta cancelada: "
                    + "no admite registro de avance.");
        }

        Maquina maquina = maquinaDAO.buscarPorId(asignacion.getIdMaquina())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));

        BigDecimal metrosMerma = form.getMetrosMerma() == null ? BigDecimal.ZERO
                : form.getMetrosMerma();
        if (metrosMerma.compareTo(form.getMetrosProducidos()) > 0) {
            throw new ReglaNegocioException("La merma no puede superar los metros producidos.");
        }

        // Metraje teorico del turno: velocidad teorica del telar por horas de turno.
        BigDecimal horasTurno = maquina.getHorasPorTurno() == null ? BigDecimal.valueOf(8)
                : maquina.getHorasPorTurno();
        BigDecimal velocidad = maquina.getVelocidadTeoricaMtsHora() == null ? BigDecimal.ZERO
                : maquina.getVelocidadTeoricaMtsHora();
        BigDecimal teorico = velocidad.multiply(horasTurno);

        BigDecimal varianza = BigDecimal.ZERO;
        if (teorico.compareTo(BigDecimal.ZERO) > 0) {
            varianza = form.getMetrosProducidos().subtract(teorico)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(teorico, 2, RoundingMode.HALF_UP);
        }

        BigDecimal limite = parametros.decimal("VARIANZA_MAXIMA_AVANCE_PORCENTAJE");
        boolean alerta = varianza.compareTo(limite.negate()) < 0;

        LocalDate fecha = form.getFecha() == null ? LocalDate.now() : form.getFecha();

        RegistroAvance avance = new RegistroAvance();
        avance.setIdAsignacion(asignacion.getIdAsignacion());
        avance.setIdOrdenProduccion(asignacion.getIdOrdenProduccion());
        avance.setIdOperario(asignacion.getIdOperario());
        avance.setIdMaquina(asignacion.getIdMaquina());
        avance.setIdTurno(asignacion.getIdTurno());
        avance.setFecha(fecha);
        avance.setMetrosProducidos(form.getMetrosProducidos());
        avance.setMetrosMerma(metrosMerma);
        avance.setVarianzaPct(varianza);
        avance.setEstadoCalidad("PENDIENTE");
        avance.setObservacion(form.getObservacion());
        int idAvance = avanceDAO.insertar(avance, codUsuario);
        avance.setIdAvance(idAvance);

        // El avance genera un lote pendiente de inspeccion.
        Lote lote = new Lote();
        lote.setCodigo(loteDAO.siguienteCodigo());
        lote.setIdOrdenProduccion(asignacion.getIdOrdenProduccion());
        lote.setIdAsignacion(asignacion.getIdAsignacion());
        lote.setMetros(form.getMetrosProducidos());
        lote.setEstado("PENDIENTE_INSPECCION");
        int idLote = loteDAO.insertar(lote, codUsuario);
        lote.setIdLote(idLote);

        if (form.getTipoIncidencia() != null && !form.getTipoIncidencia().isBlank()
                && form.getDescripcionIncidencia() != null
                && !form.getDescripcionIncidencia().isBlank()) {
            Incidencia incidencia = new Incidencia();
            incidencia.setIdAvance(idAvance);
            incidencia.setIdMaquina(asignacion.getIdMaquina());
            incidencia.setTipo(form.getTipoIncidencia());
            incidencia.setDescripcion(form.getDescripcionIncidencia());
            incidencia.setEstado("ABIERTA");
            incidenciaDAO.insertar(incidencia, codUsuario);
        }

        auditoria.registrar(codUsuario, "PLANTA", "REGISTRAR_AVANCE", "registro_avance",
                String.valueOf(idAvance), "Metros: "
                        + form.getMetrosProducidos().toPlainString() + " | Merma: "
                        + metrosMerma.toPlainString() + " | Varianza: "
                        + varianza.toPlainString() + " %");

        ResultadoAvance resultado = new ResultadoAvance();
        resultado.setIdAvance(idAvance);
        resultado.setIdLote(idLote);
        resultado.setMetrosTeoricos(teorico);
        resultado.setVarianzaPct(varianza);
        resultado.setAlertaVarianza(alerta);
        resultado.setMensajeAlerta(alerta ? ALERTA_VARIANZA : null);
        resultado.setMensaje("Avance registrado. El lote " + lote.getCodigo()
                + " queda pendiente de inspeccion de calidad.");
        return resultado;
    }

    /** Metros ya consolidados por la orden de produccion. */
    public BigDecimal metrosConsolidados(int idOrdenProduccion) {
        return ordenDAO.buscarPorId(idOrdenProduccion)
                .map(orden -> orden.getMetrosConsolidados() == null ? BigDecimal.ZERO
                        : orden.getMetrosConsolidados())
                .orElse(BigDecimal.ZERO);
    }
}
