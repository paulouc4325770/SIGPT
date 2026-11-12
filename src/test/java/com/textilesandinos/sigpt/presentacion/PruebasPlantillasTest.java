package com.textilesandinos.sigpt.presentacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
            "pedidos/consulta",
            "pedidos/cotizaciones",
            "abastecimiento/insumos",
            "planta/asignacion",
            "planta/avance",
            "planta/calidad",
            "reportes/formulario"
    })
    void renderizaPlantilla(String nombre) throws Exception {
        mockMvc.perform(get("/prueba-plantilla/" + nombre).session(sesionConPermiso(nombre)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    /**
     * Cada pantalla debe pintarse una sola vez.
     *
     * <p>Regresion: el fragmento {@code contenido} era hermano del elemento
     * reemplazado por el layout, de modo que Thymeleaf lo insertaba dentro del
     * layout y ademas lo repintaba en su posicion original: todas las pantallas
     * salian duplicadas.
     */
    @ParameterizedTest(name = "la plantilla {0} no se repite el contenido")
    @CsvSource({
            "seguridad/login,        Ingresar",
            "inicio,                 titulo-pantalla",
            "error/error,            codigo-error",
            "pedidos/bandeja,        titulo-pantalla",
            "pedidos/formulario,     titulo-pantalla",
            "pedidos/clientes,       titulo-pantalla",
            "pedidos/consulta,       titulo-pantalla",
            "pedidos/cotizaciones,   titulo-pantalla",
            "abastecimiento/insumos, titulo-pantalla",
            "planta/asignacion,      titulo-pantalla",
            "planta/avance,          titulo-pantalla",
            "planta/calidad,         titulo-pantalla",
            "reportes/formulario,    titulo-pantalla"
    })
    void renderizaPlantillaUnaSolaVez(String nombre, String marcador) throws Exception {
        String html = mockMvc.perform(get("/prueba-plantilla/" + nombre)
                        .session(sesionConPermiso(nombre)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains(marcador);
        assertThat(contar(html, marcador))
                .as("la plantilla %s debe contener el marcador '%s' una sola vez", nombre, marcador)
                .isEqualTo(1);
    }

    @Test
    @DisplayName("el menu lateral se monta una sola vez en la pagina")
    void menuLateralNoSeRepite() throws Exception {
        String html = mockMvc.perform(get("/prueba-plantilla/inicio")
                        .session(sesionConPermiso("inicio")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(contar(html, "sidebar-menu")).isEqualTo(1);
        assertThat(contar(html, "contenedor-principal")).isEqualTo(1);
    }

    /** Cuenta apariciones no solapadas de un texto. */
    private int contar(String texto, String fragmento) {
        int total = 0;
        int indice = texto.indexOf(fragmento);
        while (indice >= 0) {
            total++;
            indice = texto.indexOf(fragmento, indice + fragmento.length());
        }
        return total;
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
