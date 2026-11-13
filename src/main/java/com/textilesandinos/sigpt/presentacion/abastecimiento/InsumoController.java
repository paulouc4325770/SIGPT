package com.textilesandinos.sigpt.presentacion.abastecimiento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.abastecimiento.InsumoFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.InsumoLN;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorLN;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

import jakarta.servlet.http.HttpSession;

/**
 * Catalogo de insumos (EF-1001-5), con la alerta de stock minimo (R-Stock) y
 * el adjunto de la Hoja de Seguridad (RD5).
 */
@Controller
public class InsumoController {

    private final InsumoLN insumoLN;
    private final ProveedorLN proveedorLN;
    private final String carpetaMsds;

    public InsumoController(InsumoLN insumoLN, ProveedorLN proveedorLN,
            @Value("${sigpt.msds.carpeta:./data/msds}") String carpetaMsds) {
        this.insumoLN = insumoLN;
        this.proveedorLN = proveedorLN;
        this.carpetaMsds = carpetaMsds;
    }

    /** Catalogo de insumos. */
    @GetMapping("/abastecimiento/insumos")
    public String bandeja(@RequestParam(required = false) String busqueda, Model modelo) {
        List<Insumo> insumos = insumoLN.listar(busqueda);
        List<Proveedor> proveedores = proveedorLN.listar(null);
        modelo.addAttribute("insumos", insumos);
        modelo.addAttribute("proveedores", proveedores);
        modelo.addAttribute("insumo", new InsumoFormulario());
        modelo.addAttribute("alertasStock", insumoLN.alertasStockMinimo());
        modelo.addAttribute("busqueda", busqueda == null ? "" : busqueda);
        if (insumos.isEmpty()) {
            modelo.addAttribute("mensajeSinDatos",
                    "No se encontraron datos para los criterios seleccionados.");
        }
        return "abastecimiento/insumos";
    }

    /** Registra el insumo, con su MSDS si corresponde. */
    @PostMapping("/abastecimiento/insumos/guardar")
    public String guardar(InsumoFormulario formulario,
            @RequestParam(required = false) MultipartFile archivoMsds,
            HttpSession sesion, Model modelo, RedirectAttributes redireccion) {
        try {
            formulario.setRutaMsds(guardarMsds(formulario, archivoMsds));
            insumoLN.registrar(formulario, usuarioActual(sesion));
            redireccion.addFlashAttribute("mensajeExito", "Insumo registrado satisfactoriamente");
            return "redirect:/abastecimiento/insumos";
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("mensajeError", e.getMessage());
            modelo.addAttribute("insumos", insumoLN.listar(null));
            modelo.addAttribute("proveedores", proveedorLN.listar(null));
            modelo.addAttribute("alertasStock", insumoLN.alertasStockMinimo());
            modelo.addAttribute("insumo", formulario);
            return "abastecimiento/insumos";
        } catch (IOException e) {
            redireccion.addFlashAttribute("mensajeError",
                    "No se pudo guardar el archivo MSDS: " + e.getMessage());
            return "redirect:/abastecimiento/insumos";
        }
    }

    /** Guarda la Hoja de Seguridad en el servidor y devuelve su ruta. */
    private String guardarMsds(InsumoFormulario formulario, MultipartFile archivo)
            throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return formulario.getRutaMsds();
        }
        Path carpeta = Path.of(carpetaMsds);
        Files.createDirectories(carpeta);
        String nombre = (formulario.getTipo() == null ? "INS" : formulario.getTipo()) + "-"
                + System.currentTimeMillis() + "-" + archivo.getOriginalFilename();
        Path destino = carpeta.resolve(nombre);
        Files.write(destino, archivo.getBytes());
        return destino.toString();
    }

    private String usuarioActual(HttpSession sesion) {
        var usuario = SesionUsuario.usuario(sesion);
        return usuario == null ? "sistema" : usuario.getCodUsuario();
    }
}
