package com.textilesandinos.sigpt.presentacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * Regresion de los filtros de los reportes gerenciales (EF-1001-8).
 *
 * <p>El defecto que cubren estas pruebas: el controlador ignoraba los parametros
 * de la consulta, de modo que el usuario elegia cliente, operario o fechas, la
 * pantalla volvia a pintarse sin los valores elegidos y el reporte salia igual
 * que sin filtrar. Ahora los filtros se aplican, se conservan en el formulario y
 * viajan a la exportacion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReportesFiltrosTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("el reporte muestra su titulo, no un encabezado vacio")
    void muestraTituloDelReporte() throws Exception {
        String html = html("/reportes/avance-pedidos", "");

        assertThat(html).containsPattern("<h2 class=\"titulo-pantalla\">[^<]+</h2>");
        assertThat(html).contains("Avance de Pedidos");
    }

    @Test
    @DisplayName("sin filtros se listan todos los pedidos")
    void sinFiltrosListaTodo() throws Exception {
        String html = html("/reportes/avance-pedidos", "");

        assertThat(html).contains("PED-2026-001", "PED-2026-002", "PED-2026-003");
    }

    @Test
    @DisplayName("el filtro por cliente se aplica y queda seleccionado en el formulario")
    void filtraPorClienteYPersisteLaSeleccion() throws Exception {
        String html = html("/reportes/avance-pedidos", "?idCliente=2");

        assertThat(html).contains("PED-2026-002");
        assertThat(html).doesNotContain("PED-2026-001", "PED-2026-003");
        assertThat(html).containsPattern("<option value=\"2\"[^>]*selected");
        assertThat(html).containsPattern("<option value=\"\"[^>]*>Todos</option>");
        assertThat(html).doesNotContainPattern("<option value=\"1\"[^>]*selected");
    }

    @Test
    @DisplayName("el rango de fechas se aplica sobre la fecha de entrega y se conserva")
    void filtraPorRangoDeFechas() throws Exception {
        String html = html("/reportes/avance-pedidos", "?desde=2026-12-10&hasta=2026-12-31");

        assertThat(html).contains("PED-2026-002", "PED-2026-003");
        assertThat(html).doesNotContain("PED-2026-001");
        assertThat(html).containsPattern("id=\"desdeFiltro\"[^>]*value=\"2026-12-10\"");
        assertThat(html).containsPattern("id=\"hastaFiltro\"[^>]*value=\"2026-12-31\"");
    }

    @Test
    @DisplayName("un periodo sin datos muestra el aviso y no inventa filas")
    void periodoSinDatos() throws Exception {
        String html = html("/reportes/avance-pedidos", "?desde=2030-01-01&hasta=2030-12-31");

        assertThat(html).contains("No se encontraron datos para los criterios seleccionados");
        assertThat(html).doesNotContain("PED-2026-00");
    }

    @Test
    @DisplayName("cada reporte muestra solo los filtros que aplica")
    void muestraSoloLosFiltrosQueAplica() throws Exception {
        String avance = html("/reportes/avance-pedidos", "");
        assertThat(avance).contains("id=\"idClienteFiltro\"");
        assertThat(avance).doesNotContain("id=\"idOperarioFiltro\"", "id=\"idMaquinaFiltro\"");

        String productividad = html("/reportes/productividad", "");
        assertThat(productividad).contains("id=\"idOperarioFiltro\"", "id=\"idMaquinaFiltro\"");
        assertThat(productividad).doesNotContain("id=\"idClienteFiltro\"");

        String consumo = html("/reportes/consumo-merma", "");
        assertThat(consumo).contains("id=\"desdeFiltro\"");
        assertThat(consumo).doesNotContain("id=\"idClienteFiltro\"", "id=\"idOperarioFiltro\"");
    }

    @Test
    @DisplayName("productividad filtra por operario y conserva la seleccion")
    void productividadFiltraPorOperario() throws Exception {
        String html = html("/reportes/productividad", "?idOperario=2");

        assertThat(html).contains("<td>OP-002</td>");
        assertThat(html).doesNotContain("<td>OP-001</td>");
        assertThat(html).containsPattern("<option value=\"2\"[^>]*selected");
    }

    @Test
    @DisplayName("consumo y merma toma la merma del periodo")
    void consumoMermaDelPeriodo() throws Exception {
        String html = html("/reportes/consumo-merma", "?desde=2026-11-12&hasta=2026-11-12");

        assertThat(html).contains("Merma del periodo");
        assertThat(html).contains("30.00 m");

        String vacio = html("/reportes/consumo-merma", "?desde=2020-01-01&hasta=2020-01-02");
        assertThat(vacio).contains("0.00 m");
        assertThat(vacio).doesNotContain("30.00 m");
    }

    @Test
    @DisplayName("eficiencia industrial filtra por fecha de inicio de la orden")
    void eficienciaFiltraPorFecha() throws Exception {
        String conDatos = html("/reportes/eficiencia", "?desde=2026-11-12&hasta=2026-11-12");
        assertThat(conDatos).contains("OP-2026-001");

        String sinDatos = html("/reportes/eficiencia", "?desde=2020-01-01&hasta=2020-01-02");
        assertThat(sinDatos).doesNotContain("OP-2026-001");
        assertThat(sinDatos).contains("No se encontraron datos");
    }

    @Test
    @DisplayName("los enlaces de exportacion conservan los filtros aplicados")
    void exportacionConservaLosFiltros() throws Exception {
        String html = html("/reportes/avance-pedidos", "?idCliente=2&desde=2026-12-01");

        assertThat(html).containsPattern("exportar\\?formato=PDF[^\"]*idCliente=2[^\"]*desde=2026-12-01");
        assertThat(html).containsPattern("exportar\\?formato=EXCEL[^\"]*idCliente=2[^\"]*desde=2026-12-01");
    }

    @Test
    @DisplayName("la exportacion aplica los mismos filtros que la pantalla")
    void exportacionAplicaFiltros() throws Exception {
        byte[] contenido = mockMvc.perform(get("/reportes/avance-pedidos/exportar")
                        .param("formato", "EXCEL")
                        .param("idCliente", "2")
                        .session(sesion()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("avance_pedidos.xls")))
                .andReturn().getResponse().getContentAsByteArray();

        String hoja = new String(contenido, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(hoja).contains("PED-2026-002");
        assertThat(hoja).doesNotContain("PED-2026-001", "PED-2026-003");
    }

    private String html(String ruta, String consulta) throws Exception {
        return mockMvc.perform(get(ruta + consulta).session(sesion()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    /** Sesion con permiso sobre las cuatro pantallas de reportes. */
    private MockHttpSession sesion() {
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(8);
        perfil.setCodPerfil("GER");
        perfil.setNombrePerfil("Gerencia General");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(8);
        usuario.setCodUsuario("gerencia");
        usuario.setNombres("Gerencia General");
        usuario.setArea("Gerencia General");
        usuario.setPerfil(perfil);

        List<Opcion> opciones = new ArrayList<>();
        opciones.add(opcion(40, "Avance de pedidos", "/reportes/avance-pedidos"));
        opciones.add(opcion(41, "Productividad", "/reportes/productividad"));
        opciones.add(opcion(42, "Consumo y merma", "/reportes/consumo-merma"));
        opciones.add(opcion(43, "Eficiencia industrial", "/reportes/eficiencia"));
        opciones.add(opcion(44, "Historial de reportes", "/reportes/historial"));

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
