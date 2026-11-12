package com.textilesandinos.sigpt.presentacion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;

/**
 * Controlador auxiliar SOLO para pruebas: renderiza cada plantilla con un modelo
 * de referencia, de modo que un error de sintaxis o de expresiones rotas se
 * detecte en el build y no al abrir la pantalla en el navegador.
 *
 * <p>Vive en {@code src/test} y por tanto no forma parte de la aplicacion.
 */
@Controller
public class PlantillaDePruebasController {

    @GetMapping("/prueba-plantilla/{*nombre}")
    public String renderizar(@PathVariable String nombre, Model modelo) {
        atributos().forEach(modelo::addAttribute);
        return nombre;
    }

    private Map<String, Object> atributos() {
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(1);
        perfil.setCodPerfil("JPROD");
        perfil.setNombrePerfil("Jefe de Produccion");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCodUsuario("jperez");
        usuario.setNombres("Jorge Perez");
        usuario.setArea("Gerencia de Produccion");
        usuario.setPerfil(perfil);

        Opcion opcion = new Opcion();
        opcion.setIdOpcion(7);
        opcion.setNombreOpcion("Bandeja de Pedidos");
        opcion.setUrlOpcion("/pedidos/bandeja");
        opcion.setNroOrden(1);

        Map<String, Object> atributos = new LinkedHashMap<>();
        atributos.put("usuarioActual", usuario);
        atributos.put("menu", Map.of("Pedidos", List.of(opcion)));
        atributos.put("nombreAplicacion", "SIGPT");
        atributos.put("titulo", "Prueba de plantilla");
        atributos.put("tituloReporte", "Avance de Pedidos");
        atributos.put("filtroCodigo", "");
        atributos.put("busqueda", "");
        atributos.put("pedidos", List.of());
        atributos.put("clientes", List.of());
        atributos.put("proveedores", List.of());
        atributos.put("insumos", List.of());
        atributos.put("operarios", List.of());
        atributos.put("maquinas", List.of());
        atributos.put("turnos", List.of());
        atributos.put("ordenes", List.of());
        atributos.put("reportes", List.of());
        atributos.put("alertasStock", List.of());
        atributos.put("indicadores", List.of());
        atributos.put("filas", List.of());
        atributos.put("columnas", List.of("Columna"));
        atributos.put("semaforo", List.of());
        atributos.put("mostrarCliente", true);
        atributos.put("mostrarOperario", false);
        atributos.put("mostrarMaquina", false);
        atributos.put("fechaAsignacion", "19/07/2026");
        atributos.put("metrosMeta", 3000);
        atributos.put("metrosConsolidados", 1200);
        atributos.put("metrosMermaReprocesada", 60);
        atributos.put("porcentajeAvance", 40);
        atributos.put("varianzaPct", -18.5);

        atributos.put("pedido", new Pedido());
        atributos.put("cliente", new Cliente());
        atributos.put("insumo", new Insumo());
        atributos.put("asignacionForm", new Asignacion());
        atributos.put("avance", new RegistroAvance());

        return atributos;
    }
}
