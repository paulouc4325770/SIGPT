package com.textilesandinos.sigpt.presentacion.pedidos;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.ClienteLN;
import com.textilesandinos.sigpt.negocio.pedidos.PedidoLN;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Consulta del estado de los pedidos por parte del cliente externo.
 *
 * <p>Es una pantalla de solo lectura: el cliente ve como avanza su pedido, sin
 * acceso a los datos de otros clientes ni a las pantallas internas.
 *
 * <p>El esquema no tiene una clave foranea entre {@code usuario} y
 * {@code cliente}, asi que la cuenta externa se vincula con el cliente cuya
 * razon social coincide con el nombre del usuario (supuesto documentado).
 */
@Controller
public class ConsultaClienteController {

    private final ClienteLN clienteLN;
    private final PedidoLN pedidoLN;

    public ConsultaClienteController(ClienteLN clienteLN, PedidoLN pedidoLN) {
        this.clienteLN = clienteLN;
        this.pedidoLN = pedidoLN;
    }

    /** Estado de los pedidos del cliente autenticado. */
    @GetMapping("/pedidos/estado")
    public String estado(Model modelo, HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        String nombre = usuario == null ? "" : usuario.getNombres();

        Cliente cliente = clienteLN.listar(null).stream()
                .filter(candidato -> candidato.getRazonSocial() != null
                        && candidato.getRazonSocial().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);

        modelo.addAttribute("cliente", cliente);
        if (cliente == null) {
            modelo.addAttribute("pedidos", List.of());
            modelo.addAttribute("sinVinculo", true);
            return "pedidos/estado";
        }

        List<PedidoFila> pedidos = pedidoLN.listar(null, null, cliente.getIdCliente(), null, null);
        modelo.addAttribute("pedidos", pedidos);
        modelo.addAttribute("sinVinculo", false);
        return "pedidos/estado";
    }
}
