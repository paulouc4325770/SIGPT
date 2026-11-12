package com.textilesandinos.sigpt.presentacion.pedidos;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.pedidos.PedidoDetalle;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.ClienteLN;
import com.textilesandinos.sigpt.negocio.pedidos.PedidoFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.PedidoLN;
import com.textilesandinos.sigpt.utiles.exportacion.ExportadorTabla;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Pantallas de pedidos (EF-1001-3).
 *
 * <p>Bandeja, alta de pedido con su ficha tecnica, consulta, anulacion y
 * exportacion del resultado.
 */
@Controller
public class PedidoController {

    private final PedidoLN pedidoLN;
    private final ClienteLN clienteLN;

    public PedidoController(PedidoLN pedidoLN, ClienteLN clienteLN) {
        this.pedidoLN = pedidoLN;
        this.clienteLN = clienteLN;
    }

    /** Bandeja de pedidos con el semaforo de control. */
    @GetMapping("/pedidos/bandeja")
    public String bandeja(@RequestParam(required = false) String codigo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            Model modelo) {
        List<PedidoFila> pedidos = pedidoLN.listar(codigo, estado, idCliente, desde, hasta);
        ResumenPedidos resumen = pedidoLN.resumir();
        modelo.addAttribute("pedidos", pedidos);
        modelo.addAttribute("resumen", resumen);
        modelo.addAttribute("clientes", clienteLN.listar(null));
        modelo.addAttribute("filtroCodigo", codigo == null ? "" : codigo);
        modelo.addAttribute("filtroEstado", estado == null ? "" : estado);
        if (pedidos.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "pedidos/bandeja";
    }

    /** Formulario de nuevo pedido (ficha tecnica). */
    @GetMapping("/pedidos/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("pedido", new PedidoFormulario());
        modelo.addAttribute("clientes", clienteLN.listar(null));
        return "pedidos/formulario";
    }

    /** Registra el pedido (pasos 1 a 5 del flujo de EF-1001-3). */
    @PostMapping("/pedidos/nuevo/guardar")
    public String guardar(PedidoFormulario pedido, HttpSession sesion, Model modelo,
            RedirectAttributes redireccion) {
        try {
            int idPedido = pedidoLN.registrar(pedido, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Pedido registrado satisfactoriamente");
            return "redirect:/pedidos/bandeja/consultar/" + idPedido;
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("pedido", pedido);
            modelo.addAttribute("clientes", clienteLN.listar(null));
            return "pedidos/formulario";
        }
    }

    /** Ficha completa del pedido. */
    @GetMapping("/pedidos/bandeja/consultar/{idPedido}")
    public String consultar(@PathVariable int idPedido, Model modelo,
            RedirectAttributes redireccion) {
        try {
            PedidoDetalle detalle = pedidoLN.consultar(idPedido);
            modelo.addAttribute("detalle", detalle);
            modelo.addAttribute("pedido", detalle.getPedido());
            modelo.addAttribute("ficha", detalle.getFichaTecnica());
            return "pedidos/consulta";
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/pedidos/bandeja";
        }
    }

    /** Anula un pedido que aun no entro a planta. */
    @PostMapping("/pedidos/bandeja/anular")
    public String anular(@RequestParam int idPedido,
            @RequestParam(required = false) String motivo,
            HttpSession sesion, RedirectAttributes redireccion) {
        try {
            pedidoLN.anular(idPedido, motivo, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Pedido anulado");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/pedidos/bandeja";
    }

    /** Exporta el resultado de la consulta (CSV o Excel). */
    @GetMapping("/pedidos/bandeja/exportar")
    public void exportar(@RequestParam(required = false) String codigo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(defaultValue = "CSV") String formato,
            HttpServletResponse respuesta) throws java.io.IOException {
        List<PedidoFila> pedidos = pedidoLN.listar(codigo, estado, idCliente, desde, hasta);
        List<String> columnas = List.of("Codigo", "Cliente", "Tipo de hilado", "Gramaje (g/m2)",
                "Color Pantone", "Metros", "Metros consolidados", "Fecha de entrega", "Estado");
        List<List<Object>> filas = pedidos.stream()
                .map(pedido -> List.<Object>of(
                        pedido.getCodPedido(),
                        pedido.getNombreCliente(),
                        pedido.getTipoHilado(),
                        pedido.getGramajeM2(),
                        pedido.getColorPantone(),
                        pedido.getMetros(),
                        pedido.getMetrosConsolidados(),
                        pedido.getFechaEntrega(),
                        pedido.getEstado()))
                .toList();

        if ("EXCEL".equalsIgnoreCase(formato)) {
            respuesta.setContentType("application/vnd.ms-excel");
            respuesta.setHeader("Content-Disposition", "attachment; filename=\""
                    + ExportadorTabla.nombreArchivo("pedidos", "xls") + "\"");
            respuesta.getOutputStream().write(ExportadorTabla.excelHtml(columnas, filas));
            return;
        }
        respuesta.setContentType("text/csv;charset=UTF-8");
        respuesta.setHeader("Content-Disposition", "attachment; filename=\""
                + ExportadorTabla.nombreArchivo("pedidos", "csv") + "\"");
        respuesta.getOutputStream()
                .write(ExportadorTabla.csv(columnas, filas).getBytes(StandardCharsets.UTF_8));
    }

    /** Codigo del usuario autenticado, para la auditoria. */
    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
