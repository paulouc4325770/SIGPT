package com.textilesandinos.sigpt.presentacion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.utilesweb.sesion.SesionUsuario;

/**
 * Renderiza cada plantilla Thymeleaf en el stack web real.
 *
 * <p>Su proposito es detectar errores de sintaxis o de expresiones rotas en las
 * vistas, que de otro modo solo aparecerian al abrir la pantalla en el navegador.
 *
 * <p>La peticion se hace con una sesion autenticada a la que se le concede el
 * permiso de la ruta probada: asi el filtro de permisos tambien queda ejercitado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PruebasPlantillasTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "la plantilla {0} se renderiza sin errores")
    @ValueSource(strings = {
            "seguridad/login",
            "inicio",
            "error/error",
            "pedidos/bandeja",
            "pedidos/formulario",
            "pedidos/clientes",
            "abastecimiento/insumos",
            "planta/asignacion",
            "planta/avance",
            "reportes/formulario"
    })
    void renderizaPlantilla(String nombre) throws Exception {
        mockMvc.perform(get("/prueba-plantilla/" + nombre).session(sesionConPermiso(nombre)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @DisplayName("la pantalla de inicio monta el menu lateral con las opciones del perfil")
    void layoutMontaMenuLateral() throws Exception {
        mockMvc.perform(get("/prueba-plantilla/inicio").session(sesionConPermiso("inicio")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("sidebar-menu")))
                .andExpect(content().string(Matchers.containsString("Bandeja de Pedidos")));
    }

    @Test
    @DisplayName("la pantalla de login no monta el menu lateral")
    void loginNoExponeMenu() throws Exception {
        mockMvc.perform(get("/seguridad/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("sidebar-menu"))));
    }

    @Test
    @DisplayName("la pantalla de login muestra el formulario de acceso")
    void loginMuestraFormulario() throws Exception {
        mockMvc.perform(get("/seguridad/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("codUsuario")))
                .andExpect(content().string(Matchers.containsString("Ingresar")));
    }

    @Test
    @DisplayName("sin sesion iniciada, el acceso a una pantalla protegida redirige al login")
    void pantallaProtegidaExigeSesion() throws Exception {
        mockMvc.perform(get("/pedidos/bandeja"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Bandeja de Pedidos"))));
    }

    /** Sesion autenticada con permiso para la ruta que se va a probar. */
    private MockHttpSession sesionConPermiso(String nombre) {
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

        Opcion permiso = new Opcion();
        permiso.setIdOpcion(99);
        permiso.setNombreOpcion("Ruta de prueba");
        permiso.setUrlOpcion("/prueba-plantilla/" + nombre);

        List<Opcion> opciones = new ArrayList<>(List.of(opcion, permiso));

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUsuario.ATRIBUTO_USUARIO, usuario);
        sesion.setAttribute(SesionUsuario.ATRIBUTO_OPCIONES, opciones);
        return sesion;
    }
}
