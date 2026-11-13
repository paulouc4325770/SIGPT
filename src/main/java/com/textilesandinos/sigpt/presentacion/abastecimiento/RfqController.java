package com.textilesandinos.sigpt.presentacion.abastecimiento;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.abastecimiento.CotizacionProveedor;
import com.textilesandinos.sigpt.entidades.abastecimiento.SolicitudCotizacion;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.abastecimiento.CotizacionProveedorFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.CotizacionProveedorLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.SolicitudCotizacionFormulario;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Solicitudes de cotizacion a proveedores (RFQ) y su adjudicacion.
 */
@Controller
public class RfqController {

    private final CotizacionProveedorLN cotizacionLN;
    private final ProveedorLN proveedorLN;

    public RfqController(CotizacionProveedorLN cotizacionLN, ProveedorLN proveedorLN) {
        this.cotizacionLN = cotizacionLN;
        this.proveedorLN = proveedorLN;
    }

    /** Bandeja de solicitudes de cotizacion. */
    @GetMapping("/abastecimiento/rfq")
    public String bandeja(@RequestParam(required = false) Integer solicitud, Model modelo) {
        List<SolicitudCotizacion> solicitudes = cotizacionLN.listarSolicitudes();
        modelo.addAttribute("solicitudes", solicitudes);
        modelo.addAttribute("proveedores", proveedorLN.listar(null));
        modelo.addAttribute("solicitudForm", new SolicitudCotizacionFormulario());
        modelo.addAttribute("cotizacionForm", new CotizacionProveedorFormulario());

        List<CotizacionProveedor> cotizaciones = solicitud == null ? List.of()
                : cotizacionLN.cotizacionesDe(solicitud);
        modelo.addAttribute("cotizacionesProveedor", cotizaciones);
        modelo.addAttribute("solicitudSeleccionada", solicitud);

        if (solicitudes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos", "No hay solicitudes de cotizacion emitidas.");
        }
        return "abastecimiento/rfq";
    }

    /** Emite la solicitud de cotizacion. */
    @PostMapping("/abastecimiento/rfq/guardar")
    public String guardarSolicitud(SolicitudCotizacionFormulario formulario, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            cotizacionLN.emitirSolicitud(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito",
                    "Solicitud de cotizacion emitida a los proveedores");
            return "redirect:/abastecimiento/rfq";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/abastecimiento/rfq";
        }
    }

    /** Registra la cotizacion de un proveedor. */
    @PostMapping("/abastecimiento/rfq/cotizacion")
    public String guardarCotizacion(CotizacionProveedorFormulario formulario, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            cotizacionLN.registrarCotizacion(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Cotizacion del proveedor registrada");
            return "redirect:/abastecimiento/rfq?solicitud=" + formulario.getIdSolicitud();
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/abastecimiento/rfq";
        }
    }

    /** Adjudica la mejor oferta y cierra la solicitud. */
    @PostMapping("/abastecimiento/rfq/adjudicar")
    public String adjudicar(@RequestParam int idSolicitud, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            CotizacionProveedor adjudicada = cotizacionLN.adjudicar(idSolicitud,
                    usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Cotizacion adjudicada al proveedor "
                    + adjudicada.getIdProveedor());
            return "redirect:/abastecimiento/rfq?solicitud=" + idSolicitud;
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/abastecimiento/rfq";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
