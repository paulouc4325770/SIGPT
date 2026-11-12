package com.textilesandinos.sigpt.negocio.pedidos.estados;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patron State del ciclo de vida del pedido (EF-1001-3).
 */
class FabricaEstadoPedidoTest {

    @Test
    @DisplayName("el ciclo completo del pedido es valido")
    void cicloCompletoDelPedido() {
        assertThat(FabricaEstadoPedido.puedeTransitar("REGISTRADO", "COTIZADO")).isTrue();
        assertThat(FabricaEstadoPedido.puedeTransitar("COTIZADO", "EN_PRODUCCION")).isTrue();
        assertThat(FabricaEstadoPedido.puedeTransitar("EN_PRODUCCION", "TERMINADO")).isTrue();
    }

    @Test
    @DisplayName("un pedido registrado puede anularse")
    void anulacionDesdeRegistrado() {
        assertThat(FabricaEstadoPedido.puedeTransitar("REGISTRADO", "ANULADO")).isTrue();
        assertThat(FabricaEstadoPedido.puedeTransitar("COTIZADO", "ANULADO")).isTrue();
    }

    @Test
    @DisplayName("un pedido en produccion no se anula ni retrocede")
    void noSeAnulaEnProduccion() {
        assertThat(FabricaEstadoPedido.puedeTransitar("EN_PRODUCCION", "ANULADO")).isFalse();
        assertThat(FabricaEstadoPedido.puedeTransitar("EN_PRODUCCION", "COTIZADO")).isFalse();
        assertThat(FabricaEstadoPedido.puedeTransitar("EN_PRODUCCION", "REGISTRADO")).isFalse();
    }

    @Test
    @DisplayName("los estados finales no admiten salidas")
    void estadosFinales() {
        assertThat(FabricaEstadoPedido.de("TERMINADO").esTerminal()).isTrue();
        assertThat(FabricaEstadoPedido.de("ANULADO").esTerminal()).isTrue();
        assertThat(FabricaEstadoPedido.de("TERMINADO").puedeTransitarA(
                FabricaEstadoPedido.de("ANULADO"))).isFalse();
    }

    @Test
    @DisplayName("reconoce el estado por su codigo y rechaza los desconocidos")
    void reconoceEstados() {
        assertThat(FabricaEstadoPedido.de("COTIZADO").nombre()).isEqualTo("Cotizado");
        assertThatThrownBy(() -> FabricaEstadoPedido.de("INVENTADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVENTADO");
    }
}
