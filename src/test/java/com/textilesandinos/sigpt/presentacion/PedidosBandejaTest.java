package com.textilesandinos.sigpt.presentacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
 * Filtros de la bandeja de pedidos.
 *
 * <p>Mismo defecto que tenia la pantalla de reportes: el controlador recibia
 * los criterios y filtraba la tabla, pero la pantalla volvia a pintarse con los
 * campos vacios, el semaforo mostraba los totales de la planta y los enlaces de
 * exportacion perdian los filtros.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PedidosBandejaTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("sin filtros se listan los tres pedidos y el semaforo los cuenta")
    void sinFiltrosListaTodo() throws Exception {
        String html = html("");

        assertThat(html).contains("PED-2026-001", "PED-2026-002", "PED-2026-003");
        assertThat(totalDelSemaforo(html)).isEqualTo(3);
    }

    @Test
    @DisplayName("el filtro por estado se conserva en el formulario")
    void filtraPorEstadoYPersisteLaSeleccion() throws Exception {
        String html = html("?estado=REGISTRADO");

        assertThat(html).containsPattern("<option value=\"REGISTRADO\"[^>]*selected");
        assertThat(totalDelSemaforo(html)).isEqualTo(3);

        String vacio = html("?estado=ANULADO");
        assertThat(vacio).doesNotContain("PED-2026-00");
        assertThat(totalDelSemaforo(vacio)).isZero();
        assertThat(vacio).contains("No se encontraron datos para los criterios seleccionados");
    }

    @Test
    @DisplayName("el filtro por cliente deja su pedido y el semaforo acompana")
    void filtraPorClienteYElSemaforoLoSigue() throws Exception {
        String html = html("?idCliente=1");

        assertThat(html).contains("PED-2026-001");
        assertThat(html).doesNotContain("PED-2026-002", "PED-2026-003");
        assertThat(html).containsPattern("<option value=\"1\"[^>]*selected");
        assertThat(totalDelSemaforo(html)).isEqualTo(1);
    }

    @Test
    @DisplayName("el rango de fechas de entrega se aplica y se conserva")
    void filtraPorRangoDeEntrega() throws Exception {
        String html = html("?desde=2026-12-10&hasta=2026-12-31");

        assertThat(html).contains("PED-2026-002", "PED-2026-003");
        assertThat(html).doesNotContain("PED-2026-001");
        assertThat(html).containsPattern("id=\"filtroDesde\"[^>]*value=\"2026-12-10\"");
        assertThat(html).containsPattern("id=\"filtroHasta\"[^>]*value=\"2026-12-31\"");
        assertThat(totalDelSemaforo(html)).isEqualTo(2);
    }

    @Test
    @DisplayName("los enlaces de exportacion conservan los criterios")
    void exportacionConservaLosFiltros() throws Exception {
        String html = html("?idCliente=1&desde=2026-12-01");

        assertThat(html).containsPattern("exportar\\?formato=CSV[^\"]*idCliente=1[^\"]*desde=2026-12-01");
        assertThat(html).containsPattern("exportar\\?formato=EXCEL[^\"]*idCliente=1[^\"]*desde=2026-12-01");
    }

    @Test
    @DisplayName("la exportacion aplica los mismos filtros que la pantalla")
    void exportacionAplicaFiltros() throws Exception {
        byte[] contenido = mockMvc.perform(get("/pedidos/bandeja/exportar")
                        .param("formato", "CSV")
                        .param("idCliente", "1")
                        .session(sesion()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        String csv = new String(contenido, StandardCharsets.UTF_8);
        assertThat(csv).contains("PED-2026-001");
        assertThat(csv).doesNotContain("PED-2026-002", "PED-2026-003");
    }

    @Test
    @DisplayName("\"Limpiar filtros\" solo aparece cuando hay filtros aplicados")
    void limpiarFiltrosSoloConFiltros() throws Exception {
        assertThat(html("")).doesNotContain("Limpiar filtros");
        assertThat(html("?idCliente=2")).contains("Limpiar filtros");
    }

    private String html(String consulta) throws Exception {
        return mockMvc.perform(get("/pedidos/bandeja" + consulta).session(sesion()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /** Valor del KPI "Total" del semaforo. */
    private int totalDelSemaforo(String html) {
        var match = java.util.regex.Pattern
                .compile("kpi-titulo\">Total</p>\\s*<p class=\"kpi-valor\"[^>]*>(\\d+)<")
                .matcher(html);
        assertThat(match.find()).as("el semaforo debe mostrar el total").isTrue();
        return Integer.parseInt(match.group(1));
    }

    /** Sesion del Jefe de Produccion con acceso a la bandeja. */
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
        opciones.add(opcion(8, "Nuevo Pedido", "/pedidos/nuevo"));

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
