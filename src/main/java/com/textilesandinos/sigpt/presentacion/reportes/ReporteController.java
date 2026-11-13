package com.textilesandinos.sigpt.presentacion.reportes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.pedidos.ClienteLN;
import com.textilesandinos.sigpt.negocio.planta.AsignacionLN;
import com.textilesandinos.sigpt.negocio.reportes.ReporteLN;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltroReporte;
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
 *
 * <p>Los filtros del formulario llegan como parametros de la consulta, se
 * aplican al calculo, se devuelven a la pantalla para que el formulario
 * conserve lo elegido y viajan tambien a los enlaces de exportacion. Cada
 * reporte declara que filtros usa ({@link FiltroReporte}) y la pantalla muestra
 * solo esos: ningun control queda sin efecto.
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
    public String avancePedidos(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo, HttpSession sesion) {
        return generar("AVANCE_PEDIDOS", "/reportes/avance-pedidos",
                filtros(idCliente, idOperario, idMaquina, desde, hasta), modelo, sesion);
    }

    @GetMapping("/reportes/productividad")
    public String productividad(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo, HttpSession sesion) {
        return generar("PRODUCTIVIDAD", "/reportes/productividad",
                filtros(idCliente, idOperario, idMaquina, desde, hasta), modelo, sesion);
    }

    @GetMapping("/reportes/consumo-merma")
    public String consumoMerma(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo, HttpSession sesion) {
        return generar("CONSUMO_MERMA", "/reportes/consumo-merma",
                filtros(idCliente, idOperario, idMaquina, desde, hasta), modelo, sesion);
    }

    @GetMapping("/reportes/eficiencia")
    public String eficiencia(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo, HttpSession sesion) {
        return generar("EFICIENCIA_IND", "/reportes/eficiencia",
                filtros(idCliente, idOperario, idMaquina, desde, hasta), modelo, sesion);
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
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse respuesta, HttpSession sesion) throws IOException {
        exportar("AVANCE_PEDIDOS", formato,
                filtros(idCliente, idOperario, idMaquina, desde, hasta), respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Productividad). */
    @GetMapping("/reportes/productividad/exportar")
    public void exportarProductividad(@RequestParam(defaultValue = "PDF") String formato,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse respuesta, HttpSession sesion) throws IOException {
        exportar("PRODUCTIVIDAD", formato,
                filtros(idCliente, idOperario, idMaquina, desde, hasta), respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Consumo de Insumos y Merma). */
    @GetMapping("/reportes/consumo-merma/exportar")
    public void exportarConsumo(@RequestParam(defaultValue = "PDF") String formato,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse respuesta, HttpSession sesion) throws IOException {
        exportar("CONSUMO_MERMA", formato,
                filtros(idCliente, idOperario, idMaquina, desde, hasta), respuesta, sesion);
    }

    /** Descarga el reporte en PDF o Excel (Eficiencia Industrial). */
    @GetMapping("/reportes/eficiencia/exportar")
    public void exportarEficiencia(@RequestParam(defaultValue = "PDF") String formato,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idOperario,
            @RequestParam(required = false) Integer idMaquina,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            HttpServletResponse respuesta, HttpSession sesion) throws IOException {
        exportar("EFICIENCIA_IND", formato,
                filtros(idCliente, idOperario, idMaquina, desde, hasta), respuesta, sesion);
    }

    private void exportar(String codigo, String formato, FiltrosReporte filtros,
            HttpServletResponse respuesta, HttpSession sesion) throws IOException {
        ResultadoReporte resultado = reporteLN.generar(codigo, formato, filtros,
                usuarioActual(sesion));
        respuesta.setContentType("PDF".equalsIgnoreCase(formato)
                ? "application/pdf" : "application/vnd.ms-excel");
        respuesta.setHeader("Content-Disposition",
                "attachment; filename=\"" + resultado.getNombreArchivo() + "\"");
        respuesta.getOutputStream().write(resultado.getContenido());
    }

    private String generar(String codigo, String ruta, FiltrosReporte filtros, Model modelo,
            HttpSession sesion) {
        modelo.addAttribute("reportes", reporteLN.listarReportes());
        modelo.addAttribute("formatos", reporteLN.listarFormatos());
        modelo.addAttribute("clientes", clienteLN.listar(null));
        modelo.addAttribute("operarios", asignacionLN.operarios());
        modelo.addAttribute("maquinas", asignacionLN.maquinas());
        modelo.addAttribute("codigoReporte", codigo);
        modelo.addAttribute("rutaReporte", ruta);
        modelo.addAttribute("tituloReporte", reporteLN.titulo(codigo));
        modelo.addAttribute("filtros", filtros);

        List<FiltroReporte> disponibles = reporteLN.filtrosSoportados(codigo);
        modelo.addAttribute("verFiltroCliente", disponibles.contains(FiltroReporte.CLIENTE));
        modelo.addAttribute("verFiltroOperario", disponibles.contains(FiltroReporte.OPERARIO));
        modelo.addAttribute("verFiltroMaquina", disponibles.contains(FiltroReporte.MAQUINA));
        modelo.addAttribute("verFiltroFechas", disponibles.contains(FiltroReporte.FECHAS));

        String consulta = filtros.comoConsulta();
        modelo.addAttribute("urlExportarPdf", ruta + "/exportar?formato=PDF" + consulta);
        modelo.addAttribute("urlExportarExcel", ruta + "/exportar?formato=EXCEL" + consulta);

        try {
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
