package com.textilesandinos.sigpt.presentacion.pedidos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.AdelantoFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.CotizacionFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.CotizacionLN;
import com.textilesandinos.sigpt.negocio.pedidos.PedidoLN;
import com.textilesandinos.sigpt.negocio.pedidos.ResultadoAdelanto;
import com.textilesandinos.sigpt.negocio.pedidos.ResultadoCotizacion;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Pantalla de cotizaciones y adelantos (EF-1001-3, pasos 6 y 7).
 */
@Controller
public class CotizacionController {

    private final CotizacionLN cotizacionLN;
    private final PedidoLN pedidoLN;

    public CotizacionController(CotizacionLN cotizacionLN, PedidoLN pedidoLN) {
        this.cotizacionLN = cotizacionLN;
        this.pedidoLN = pedidoLN;
    }

    /** Bandeja de cotizaciones con el formulario de emision. */
    @GetMapping("/pedidos/cotizaciones")
    public String bandeja(Model modelo) {
        List<Cotizacion> cotizaciones = cotizacionLN.listarCotizaciones();
        modelo.addAttribute("cotizaciones", cotizaciones);
        modelo.addAttribute("pedidos", pedidoLN.listar(null, "REGISTRADO", null, null, null));
        modelo.addAttribute("cotizacion", new CotizacionFormulario());
        modelo.addAttribute("adelanto", new AdelantoFormulario());
        if (cotizaciones.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "pedidos/cotizaciones";
    }

    /** Emite la cotizacion de un pedido. */
    @PostMapping("/pedidos/cotizaciones/guardar")
    public String guardar(CotizacionFormulario cotizacion, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            ResultadoCotizacion resultado = cotizacionLN.emitir(cotizacion, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", resultado.getMensaje());
            redireccion.addFlashAttribute("montoEnPen", resultado.getMontoEnPen());
            return "redirect:/pedidos/cotizaciones";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("cotizaciones", cotizacionLN.listarCotizaciones());
            modelo.addAttribute("pedidos", pedidoLN.listar(null, "REGISTRADO", null, null, null));
            modelo.addAttribute("cotizacion", cotizacion);
            modelo.addAttribute("adelanto", new AdelantoFormulario());
            return "pedidos/cotizaciones";
        }
    }

    /** Registra el adelanto sobre una cotizacion (Regla R2). */
    @PostMapping("/pedidos/cotizaciones/adelanto")
    public String adelanto(AdelantoFormulario adelanto, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            ResultadoAdelanto resultado = cotizacionLN.registrarAdelanto(adelanto,
                    usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", resultado.getMensaje());
            return "redirect:/pedidos/cotizaciones";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("cotizaciones", cotizacionLN.listarCotizaciones());
            modelo.addAttribute("pedidos", pedidoLN.listar(null, null, null, null, null));
            modelo.addAttribute("cotizacion", new CotizacionFormulario());
            modelo.addAttribute("adelanto", adelanto);
            return "pedidos/cotizaciones";
        }
    }

    /** Pedido asociado a una cotizacion, para la vista. */
    public Pedido pedidoDe(int idPedido) {
        return pedidoLN.consultar(idPedido).getPedido();
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }

    /** Fecha de hoy, para el formulario de adelanto. */
    public LocalDate hoy() {
        return LocalDate.now();
    }
}
