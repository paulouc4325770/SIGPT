package com.textilesandinos.sigpt.presentacion.abastecimiento;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorLN;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Padron de proveedores homologados (EF-1001-5).
 */
@Controller
public class ProveedorController {

    private final ProveedorLN proveedorLN;

    public ProveedorController(ProveedorLN proveedorLN) {
        this.proveedorLN = proveedorLN;
    }

    /** Bandeja de proveedores con el formulario de alta. */
    @GetMapping("/abastecimiento/proveedores")
    public String bandeja(@RequestParam(required = false) String busqueda, Model modelo) {
        List<Proveedor> proveedores = proveedorLN.listar(busqueda);
        modelo.addAttribute("proveedores", proveedores);
        modelo.addAttribute("proveedor", new ProveedorFormulario());
        modelo.addAttribute("busqueda", busqueda == null ? "" : busqueda);
        if (proveedores.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "abastecimiento/proveedores";
    }

    /** Registra el proveedor. */
    @PostMapping("/abastecimiento/proveedores/guardar")
    public String guardar(ProveedorFormulario formulario, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            proveedorLN.registrar(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Proveedor registrado satisfactoriamente");
            return "redirect:/abastecimiento/proveedores";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("proveedores", proveedorLN.listar(null));
            modelo.addAttribute("proveedor", formulario);
            return "abastecimiento/proveedores";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
