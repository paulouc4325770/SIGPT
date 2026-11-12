package com.textilesandinos.sigpt.utilesweb.permisos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;

class MenuHelperTest {

    private Opcion opcion(Integer id, String nombre, String url, Integer padre) {
        Opcion opcion = new Opcion();
        opcion.setIdOpcion(id);
        opcion.setNombreOpcion(nombre);
        opcion.setUrlOpcion(url);
        opcion.setIdOpcionRef(padre);
        return opcion;
    }

    @Test
    @DisplayName("agrupa las hojas bajo su grupo")
    void agrupaOpcionesPorGrupo() {
        List<Opcion> opciones = List.of(
                opcion(1, "Pedidos", null, null),
                opcion(2, "Bandeja", "/pedidos/bandeja", 1),
                opcion(3, "Clientes", "/pedidos/clientes", 1),
                opcion(4, "Planta", null, null),
                opcion(5, "Avance", "/planta/avance", 4));

        var menu = MenuHelper.construirMenu(opciones);

        assertThat(menu).containsOnlyKeys("Pedidos", "Planta");
        assertThat(menu.get("Pedidos")).hasSize(2);
        assertThat(menu.get("Planta")).hasSize(1);
    }

    @Test
    @DisplayName("reconoce el acceso a una ruta permitida del perfil")
    void reconoceAccesoPermitido() {
        List<Opcion> opciones = List.of(
                opcion(1, "Pedidos", null, null),
                opcion(2, "Bandeja", "/pedidos/bandeja", 1));

        assertThat(MenuHelper.tieneAcceso(opciones, "/pedidos/bandeja")).isTrue();
    }

    @Test
    @DisplayName("niega el acceso a una ruta que no pertenece al perfil")
    void niegaAccesoNoPermitido() {
        List<Opcion> opciones = List.of(
                opcion(1, "Planta", null, null),
                opcion(2, "Avance", "/planta/avance", 1));

        assertThat(MenuHelper.tieneAcceso(opciones, "/pedidos/bandeja")).isFalse();
    }

    @Test
    @DisplayName("ignora la barra final y el query string al comparar rutas")
    void normalizaRutas() {
        List<Opcion> opciones = List.of(
                opcion(1, "Pedidos", null, null),
                opcion(2, "Bandeja", "/pedidos/bandeja", 1));

        assertThat(MenuHelper.tieneAcceso(opciones, "/pedidos/bandeja/")).isTrue();
        assertThat(MenuHelper.tieneAcceso(opciones, "/pedidos/bandeja?pagina=2")).isTrue();
    }

    @Test
    @DisplayName("un grupo de menu no concede acceso a ninguna ruta")
    void grupoNoConcedeAcceso() {
        List<Opcion> opciones = List.of(opcion(1, "Pedidos", null, null));

        assertThat(MenuHelper.tieneAcceso(opciones, "/pedidos")).isFalse();
    }
}
