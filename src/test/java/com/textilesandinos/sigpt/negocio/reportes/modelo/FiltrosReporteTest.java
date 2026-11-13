package com.textilesandinos.sigpt.negocio.reportes.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del objeto de filtros: rango de fechas y cadena de consulta que se
 * comparte con los enlaces de exportacion.
 */
class FiltrosReporteTest {

    @Test
    @DisplayName("sin fechas, cualquier fecha cae en el rango")
    void sinRangoAceptaTodo() {
        FiltrosReporte filtros = new FiltrosReporte();

        assertThat(filtros.enRango(LocalDate.of(2026, 11, 12))).isTrue();
        assertThat(filtros.enRango(null)).isTrue();
    }

    @Test
    @DisplayName("el rango es inclusivo por ambos extremos")
    void rangoInclusivo() {
        FiltrosReporte filtros = new FiltrosReporte();
        filtros.setFechaDesde(LocalDate.of(2026, 11, 1));
        filtros.setFechaHasta(LocalDate.of(2026, 11, 30));

        assertThat(filtros.enRango(LocalDate.of(2026, 11, 1))).isTrue();
        assertThat(filtros.enRango(LocalDate.of(2026, 11, 30))).isTrue();
        assertThat(filtros.enRango(LocalDate.of(2026, 10, 31))).isFalse();
        assertThat(filtros.enRango(LocalDate.of(2026, 12, 1))).isFalse();
    }

    @Test
    @DisplayName("una fecha nula no entra cuando hay un rango elegido")
    void fechaNulaQuedaFueraConRango() {
        FiltrosReporte filtros = new FiltrosReporte();
        filtros.setFechaDesde(LocalDate.of(2026, 1, 1));

        assertThat(filtros.enRango(null)).isFalse();
    }

    @Test
    @DisplayName("la cadena de consulta solo incluye los filtros elegidos")
    void cadenaDeConsulta() {
        FiltrosReporte filtros = new FiltrosReporte();
        assertThat(filtros.comoConsulta()).isEmpty();
        assertThat(filtros.hayFiltros()).isFalse();

        filtros.setIdCliente(2);
        filtros.setFechaDesde(LocalDate.of(2026, 12, 1));

        assertThat(filtros.comoConsulta()).isEqualTo("&idCliente=2&desde=2026-12-01");
        assertThat(filtros.hayFiltros()).isTrue();
    }

    @Test
    @DisplayName("el resumen del historial indica los filtros usados")
    void resumenParaElHistorial() {
        FiltrosReporte filtros = new FiltrosReporte();
        assertThat(filtros.resumir()).contains("cliente=todos", "operario=todos",
                "maquina=todas", "desde=inicio", "hasta=hoy");

        filtros.setIdOperario(3);
        filtros.setFechaHasta(LocalDate.of(2026, 11, 30));

        assertThat(filtros.resumir()).contains("operario=3", "hasta=2026-11-30");
    }
}
