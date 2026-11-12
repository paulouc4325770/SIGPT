package com.textilesandinos.sigpt.presentacion.planta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.OperarioDAO;
import com.textilesandinos.sigpt.datos.planta.TurnoDAO;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.negocio.planta.AvanceFormulario;
import com.textilesandinos.sigpt.negocio.planta.AvanceLN;
import com.textilesandinos.sigpt.negocio.planta.ResultadoAvance;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Terminal tactil del operario: registro de avance (EF-1001-7).
 */
@Controller
public class AvanceController {

    private final AvanceLN avanceLN;
    private final AsignacionLN asignacionLN;
    private final OperarioDAO operarioDAO;
    private final MaquinaDAO maquinaDAO;
    private final TurnoDAO turnoDAO;

    public AvanceController(AvanceLN avanceLN, AsignacionLN asignacionLN,
            OperarioDAO operarioDAO, MaquinaDAO maquinaDAO, TurnoDAO turnoDAO) {
        this.avanceLN = avanceLN;
        this.asignacionLN = asignacionLN;
        this.operarioDAO = operarioDAO;
        this.maquinaDAO = maquinaDAO;
        this.turnoDAO = turnoDAO;
    }

    /** Pantalla del terminal. */
    @GetMapping("/planta/avance")
    public String terminal(@RequestParam(required = false) Integer orden,
            @RequestParam(required = false) Integer asignacion, Model modelo) {
        List<OrdenFila> ordenes = asignacionLN.ordenesEnProduccion();
        modelo.addAttribute("ordenes", ordenes);
        modelo.addAttribute("avance", new AvanceFormulario());

        List<Asignacion> asignaciones = List.of();
        if (orden != null) {
            asignaciones = asignacionLN.asignacionesDe(orden);
        }
        modelo.addAttribute("asignaciones", asignaciones);

        Asignacion vigente = null;
        if (asignacion != null) {
            for (Asignacion candidata : asignaciones) {
                if (candidata.getIdAsignacion().equals(asignacion)) {
                    vigente = candidata;
                    break;
                }
            }
        }
        modelo.addAttribute("asignacion", vigente);
        modelo.addAttribute("codOperario", vigente == null ? "" : operarioDAO
                .buscarPorId(vigente.getIdOperario()).map(o -> o.getCodigo()).orElse(""));
        modelo.addAttribute("codMaquina", vigente == null ? "" : maquinaDAO
                .buscarPorId(vigente.getIdMaquina()).map(m -> m.getCodigo()).orElse(""));
        modelo.addAttribute("nombreTurno", vigente == null ? "" : turnoDAO
                .buscarPorId(vigente.getIdTurno()).map(t -> t.getNombre()).orElse(""));
        modelo.addAttribute("metrosMeta", vigente == null ? BigDecimal.ZERO
                : vigente.getMetaMetros());
        modelo.addAttribute("metrosConsolidados", orden == null ? BigDecimal.ZERO
                : avanceLN.metrosConsolidados(orden));
        modelo.addAttribute("porcentajeAvance", 0);
        modelo.addAttribute("avances", orden == null ? List.<RegistroAvance>of()
                : avanceLN.avancesDe(orden));

        if (vigente != null) {
            modelo.addAttribute("fechaAvance", LocalDate.now());
        }
        if (ordenes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos", "No tiene asignaciones vigentes: "
                    + "no hay Ordenes de Produccion en produccion asignadas a su perfil.");
        }
        return "planta/avance";
    }

    /** Registra el avance del turno (pasos 1 a 5 del flujo de EF-1001-7). */
    @PostMapping("/planta/avance/guardar")
    public String guardar(AvanceFormulario avance, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            ResultadoAvance resultado = avanceLN.registrar(avance, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", resultado.getMensaje());
            if (resultado.isAlertaVarianza()) {
                redireccion.addFlashAttribute("mensajeAlerta", resultado.getMensajeAlerta()
                        + ": el avance presenta una varianza de "
                        + resultado.getVarianzaPct().toPlainString() + " % frente a la velocidad "
                        + "teorica del telar.");
            }
            return "redirect:/planta/avance";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/planta/avance";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
