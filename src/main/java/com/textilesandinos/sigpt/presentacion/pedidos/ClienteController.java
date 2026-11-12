package com.textilesandinos.sigpt.presentacion.pedidos;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.ClienteLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Pantalla de clientes (EF-1001-4).
 *
 * <p>Alta, edicion y busqueda de la cartera de clientes.
 */
@Controller
public class ClienteController {

    private final ClienteLN clienteLN;

    public ClienteController(ClienteLN clienteLN) {
        this.clienteLN = clienteLN;
    }

    /** Bandeja de clientes con el formulario de registro. */
    @GetMapping("/pedidos/clientes")
    public String bandeja(@RequestParam(required = false) String busqueda, Model modelo) {
        List<Cliente> clientes = clienteLN.listar(busqueda);
        modelo.addAttribute("clientes", clientes);
        modelo.addAttribute("busqueda", busqueda == null ? "" : busqueda);
        modelo.addAttribute("cliente", new Cliente());
        if (clientes.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "pedidos/clientes";
    }

    /** Carga un cliente en el formulario para editarlo. */
    @GetMapping("/pedidos/clientes/editar/{idCliente}")
    public String editar(@PathVariable int idCliente, Model modelo,
            RedirectAttributes redireccion) {
        try {
            Cliente cliente = clienteLN.buscar(idCliente);
            modelo.addAttribute("cliente", cliente);
            modelo.addAttribute("clienteSeleccionado", cliente);
            modelo.addAttribute("clientes", clienteLN.listar(null));
            return "pedidos/clientes";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/pedidos/clientes";
        }
    }

    /** Registra o actualiza el cliente. */
    @PostMapping("/pedidos/clientes/guardar")
    public String guardar(Cliente cliente, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        boolean esNuevo = cliente.getIdCliente() == null;
        try {
            clienteLN.guardar(cliente, esNuevo, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", ClienteLN.MENSAJE_REGISTRO);
            return "redirect:/pedidos/clientes";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("cliente", cliente);
            modelo.addAttribute("clienteSeleccionado", esNuevo ? null : cliente);
            modelo.addAttribute("clientes", clienteLN.listar(null));
            return "pedidos/clientes";
        }
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
