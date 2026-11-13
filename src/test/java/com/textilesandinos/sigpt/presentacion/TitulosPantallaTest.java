package com.textilesandinos.sigpt.presentacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * El titulo de cada pantalla sale de la opcion de menu de la ruta.
 *
 * <p>Regresion: solo la pantalla de inicio definia {@code titulo}; el resto
 * pintaba {@code <title>null - SIGPT</title>} y un encabezado vacio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TitulosPantallaTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("cada pantalla toma el nombre de su opcion de menu")
    void elTituloSaleDelMenu() throws Exception {
        comprobar("/pedidos/bandeja", "Bandeja de Pedidos");
        comprobar("/pedidos/clientes", "Bandeja de Clientes");
        comprobar("/abastecimiento/insumos", "Insumos");
        comprobar("/planta/asignacion", "Asignar Operarios y Maquinaria");
        comprobar("/reportes/historial", "Historial de Reportes");
    }

    private void comprobar(String ruta, String titulo) throws Exception {
        String html = mockMvc.perform(get(ruta).session(sesion()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("<title>" + titulo + " - SIGPT</title>");
        assertThat(html).contains("<h2 class=\"titulo-pantalla\">" + titulo + "</h2>");
        assertThat(html).doesNotContain("null - SIGPT");
    }

    private MockHttpSession sesion() {
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

        List<Opcion> opciones = new ArrayList<>();
        opciones.add(opcion(7, "Bandeja de Pedidos", "/pedidos/bandeja"));
        opciones.add(opcion(10, "Bandeja de Clientes", "/pedidos/clientes"));
        opciones.add(opcion(14, "Insumos", "/abastecimiento/insumos"));
        opciones.add(opcion(19, "Asignar Operarios y Maquinaria", "/planta/asignacion"));
        opciones.add(opcion(29, "Historial de Reportes", "/reportes/historial"));

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(SesionUsuario.ATRIBUTO_USUARIO, usuario);
        sesion.setAttribute(SesionUsuario.ATRIBUTO_OPCIONES, opciones);
        return sesion;
    }

    private Opcion opcion(int id, String nombre, String url) {
        Opcion opcion = new Opcion();
        opcion.setIdOpcion(id);
        opcion.setNombreOpcion(nombre);
        opcion.setUrlOpcion(url);
        opcion.setNroOrden(id);
        return opcion;
    }
}
