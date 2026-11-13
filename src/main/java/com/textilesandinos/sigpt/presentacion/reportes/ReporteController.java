package com.textilesandinos.sigpt.presentacion.reportes;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.ClienteLN;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.negocio.reportes.ReporteLN;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Reportes gerenciales (EF-1001-8).
 *
 * <p>Las cuatro opciones del menu llevan a la misma pantalla: cambia el
 * reporte que se genera.
 */
@Controller
public class ReporteController {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteController.class);

    private final ReporteLN reporteLN;
    private final ClienteLN clienteLN;
    private final AsignacionLN asignacionLN;

    public ReporteController(ReporteLN reporteLN, ClienteLN clienteLN, AsignacionLN asignacionLN) {
        this.reporteLN = reporteLN;
        this.clienteLN = clienteLN;
        this.asignacionLN = asignacionLN;
    }

    @GetMapping("/reportes/avance-pedidos")
    public String avancePedidos(Model modelo, HttpSession sesion, RedirectAttributes redireccion) {
        return generar("AVANCE_PEDIDOS", "/reportes/avance-pedidos", modelo, sesion, redireccion);
    }

    @GetMapping("/reportes/productividad")
    public String productividad(Model modelo, HttpSession sesion, RedirectAttributes redireccion) {
        return generar("PRODUCTIVIDAD", "/reportes/productividad", modelo, sesion, redireccion);
    }

    @GetMapping("/reportes/consumo-merma")
    public String consumoMerma(Model modelo, HttpSession sesion, RedirectAttributes redireccion) {
        return generar("CONSUMO_MERMA", "/reportes/consumo-merma", modelo, sesion, redireccion);
    }

    @GetMapping("/reportes/eficiencia")
    public String eficiencia(Model modelo, HttpSession sesion, RedirectAttributes redireccion) {
        return generar("EFICIENCIA_IND", "/reportes/eficiencia", modelo, sesion, redireccion);
    }

    /** Historial de reportes generados. */
    @GetMapping("/reportes/historial")
    public String historial(Model modelo) {
        modelo.addAttribute("generados", reporteLN.historico(100));
        modelo.addAttribute("reportes", reporteLN.listarReportes());
        return "reportes/historial";
    }

    /** Descarga el reporte en PDF o Excel (Avance de Pedidos). */
    @GetMapping("/reportes/avance-pedidos/exportar")
    public void exportarAvance(@RequestParam(defaultValue = "PDF") String formato,
            HttpServletResponse respuesta, HttpSession sesion) throws java.io.IOException {
        exportar("AVANCE_PEDIDOS", formato, respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Productividad). */
    @GetMapping("/reportes/productividad/exportar")
    public void exportarProductividad(@RequestParam(defaultValue = "PDF") String formato,
            HttpServletResponse respuesta, HttpSession sesion) throws java.io.IOException {
        exportar("PRODUCTIVIDAD", formato, respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Consumo de Insumos y Merma). */
    @GetMapping("/reportes/consumo-merma/exportar")
    public void exportarConsumo(@RequestParam(defaultValue = "PDF") String formato,
            HttpServletResponse respuesta, HttpSession sesion) throws java.io.IOException {
        exportar("CONSUMO_MERMA", formato, respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Eficiencia Industrial). */
    @GetMapping("/reportes/eficiencia/exportar")
    public void exportarEficiencia(@RequestParam(defaultValue = "PDF") String formato,
            HttpServletResponse respuesta, HttpSession sesion) throws java.io.IOException {
        exportar("EFICIENCIA_IND", formato, respuesta, sesion);
    }

    private void exportar(String codigo, String formato, HttpServletResponse respuesta,
            HttpSession sesion) throws java.io.IOException {
        FiltrosReporte filtros = filtros(null, null, null, null, null);
        ResultadoReporte resultado = reporteLN.generar(codigo, formato, filtros,
                usuarioActual(sesion));
        respuesta.setContentType("PDF".equalsIgnoreCase(formato)
                ? "application/pdf" : "application/vnd.ms-excel");
        respuesta.setHeader("Content-Disposition",
                "attachment; filename=\"" + resultado.getNombreArchivo() + "\"");
        respuesta.getOutputStream().write(resultado.getContenido());
    }

    private String generar(String codigo, String ruta, Model modelo, HttpSession sesion,
            RedirectAttributes redireccion) {
        modelo.addAttribute("reportes", reporteLN.listarReportes());
        modelo.addAttribute("formatos", reporteLN.listarFormatos());
        modelo.addAttribute("clientes", clienteLN.listar(null));
        modelo.addAttribute("operarios", asignacionLN.operarios());
        modelo.addAttribute("maquinas", asignacionLN.maquinas());
        modelo.addAttribute("codigoReporte", codigo);
        modelo.addAttribute("rutaReporte", ruta);
        try {
            FiltrosReporte filtros = filtros(null, null, null, null, null);
            ResultadoReporte resultado = reporteLN.generar(codigo, "PANTALLA", filtros,
                    usuarioActual(sesion));
            modelo.addAttribute("resultado", resultado);
            modelo.addAttribute("columnas", resultado.getColumnas());
            modelo.addAttribute("filas", resultado.getFilas());
            modelo.addAttribute("indicadores", resultado.getIndicadores());
        } catch (ReglaNegocioException e) {
            LOG.warn("No se pudo generar el reporte {}: {}", codigo, e.getMessage());
            modelo.addAttribute("mensajeError", e.getMessage());
        }
        return "reportes/reporte";
    }

    private FiltrosReporte filtros(Integer idCliente, Integer idOperario, Integer idMaquina,
            LocalDate desde, LocalDate hasta) {
        FiltrosReporte filtros = new FiltrosReporte();
        filtros.setIdCliente(idCliente);
        filtros.setIdOperario(idOperario);
        filtros.setIdMaquina(idMaquina);
        filtros.setFechaDesde(desde);
        filtros.setFechaHasta(hasta);
        return filtros;
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
