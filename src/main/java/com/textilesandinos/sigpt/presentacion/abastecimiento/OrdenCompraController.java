package com.textilesandinos.sigpt.presentacion.abastecimiento;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.abastecimiento.InsumoLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.OrdenCompraFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.OrdenCompraLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorLN;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Ordenes de compra a los proveedores (EF-1001-5).
 */
@Controller
public class OrdenCompraController {

    private final OrdenCompraLN ordenCompraLN;
    private final ProveedorLN proveedorLN;
    private final InsumoLN insumoLN;

    public OrdenCompraController(OrdenCompraLN ordenCompraLN, ProveedorLN proveedorLN,
            InsumoLN insumoLN) {
        this.ordenCompraLN = ordenCompraLN;
        this.proveedorLN = proveedorLN;
        this.insumoLN = insumoLN;
    }

    /** Bandeja de ordenes de compra. */
    @GetMapping("/abastecimiento/ordenes-compra")
    public String bandeja(Model modelo) {
        List<OrdenCompra> ordenes = ordenCompraLN.listar();
        modelo.addAttribute("ordenesCompra", ordenes);
        modelo.addAttribute("proveedores", proveedorLN.listar(null));
        modelo.addAttribute("insumos", insumoLN.listar(null));
        modelo.addAttribute("ordenForm", new OrdenCompraFormulario());
        if (ordenes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "abastecimiento/ordenes-compra";
    }

    /** Emite la orden de compra, genera el PDF y la envia al proveedor. */
    @PostMapping("/abastecimiento/ordenes-compra/guardar")
    public String guardar(OrdenCompraFormulario formulario, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            OrdenCompra orden = ordenCompraLN.emitir(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Orden de compra " + orden.getCodigo()
                    + " emitida y enviada al proveedor");
            return "redirect:/abastecimiento/ordenes-compra";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/abastecimiento/ordenes-compra";
        }
    }

    /** Confirma la orden con el proveedor. */
    @PostMapping("/abastecimiento/ordenes-compra/confirmar")
    public String confirmar(@RequestParam int idOrdenCompra, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            ordenCompraLN.confirmar(idOrdenCompra, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Orden confirmada con el proveedor");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/abastecimiento/ordenes-compra";
    }

    /** Registra la recepcion: genera los ingresos de kardex. */
    @PostMapping("/abastecimiento/ordenes-compra/recibir")
    public String recibir(@RequestParam int idOrdenCompra, HttpSession sesion,
            RedirectAttributes redireccion) {
        try {
            ordenCompraLN.recibir(idOrdenCompra, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito",
                    "Recepcion registrada: el stock quedo actualizado en el kardex");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/abastecimiento/ordenes-compra";
    }

    /** Descarga el PDF de la orden de compra. */
    @GetMapping("/abastecimiento/ordenes-compra/pdf/{idOrdenCompra}")
    public void pdf(@PathVariable int idOrdenCompra, HttpServletResponse respuesta)
            throws java.io.IOException {
        try {
            byte[] pdf = ordenCompraLN.pdf(idOrdenCompra);
            respuesta.setContentType("application/pdf");
            respuesta.setHeader("Content-Disposition", "attachment; filename=\"orden-compra.pdf\"");
            respuesta.getOutputStream().write(pdf);
        } catch (ReglaNegocioException e) {
            // No es un fallo del sistema: la orden no existe o no se puede emitir.
            respuesta.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
