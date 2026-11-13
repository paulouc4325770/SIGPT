package com.textilesandinos.sigpt.negocio.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.reportes.ReporteDAO;
import com.textilesandinos.sigpt.entidades.reportes.FormatoSalida;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGerencial;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.reportes.calculo.CalculoReporte;
import com.textilesandinos.sigpt.negocio.reportes.formato.EstrategiaFormato;
import com.textilesandinos.sigpt.negocio.reportes.formato.FormatoPantalla;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltroReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.FiltrosReporte;
import com.textilesandinos.sigpt.negocio.reportes.modelo.Indicador;
import com.textilesandinos.sigpt.negocio.reportes.modelo.ResultadoReporte;

/**
 * Pruebas del modulo de reportes (EF-1001-8).
 *
 * <p>Verifican el Template Method (el esqueleto de generacion), el Bridge
 * (el formato de salida) y la Strategy (el calculo de cada reporte).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReporteLNTest {

    @Mock
    private ReporteDAO reporteDAO;
    @Mock
    private AuditoriaHelper auditoria;
    @Mock
    private CalculoReporte calculo;

    private ReporteBase reporte;

    @BeforeEach
    void setUp() {
        ReporteGerencial catalogo = new ReporteGerencial();
        catalogo.setIdReporte(1);
        catalogo.setCodigo("AVANCE_PEDIDOS");
        catalogo.setNombre("Avance de Pedidos");
        when(reporteDAO.listarReportes()).thenReturn(List.of(catalogo));

        FormatoSalida formato = new FormatoSalida();
        formato.setIdFormato(1);
        formato.setCodigo("PANTALLA");
        formato.setNombre("Visualizacion en pantalla");
        when(reporteDAO.listarFormatos()).thenReturn(List.of(formato));

        when(calculo.columnas()).thenReturn(List.of("Codigo", "Cliente"));
        when(calculo.filas(any())).thenReturn(List.of(List.of("PED-2026-001", "Marca Andina")));
        when(calculo.indicadores(any())).thenReturn(
                List.of(new Indicador("Pedidos", "3", "Total")));
    }

    private ReporteLN fachada() {
        reporte = new ReporteAvancePedidos(reporteDAO, auditoria, calculo);
        return new ReporteLN(reporteDAO, new FabricaReportes(List.of(reporte)));
    }

    @Test
    @DisplayName("Template Method: generar arma indicadores, filas y contenido")
    void generaReporteCompleto() {
        ResultadoReporte resultado = fachada().generar("AVANCE_PEDIDOS", "PANTALLA",
                new FiltrosReporte(), "jperez");

        assertThat(resultado.getCodigo()).isEqualTo("AVANCE_PEDIDOS");
        assertThat(resultado.getColumnas()).containsExactly("Codigo", "Cliente");
        assertThat(resultado.getFilas()).hasSize(1);
        assertThat(resultado.getIndicadores()).hasSize(1);
        assertThat(resultado.getFormato()).isEqualTo("PANTALLA");
    }

    @Test
    @DisplayName("Bridge: el formato de salida se resuelve desde el catalogo")
    void resuelveFormatoDeSalida() {
        ResultadoReporte resultado = fachada().generar("AVANCE_PEDIDOS", "PANTALLA",
                new FiltrosReporte(), "jperez");

        assertThat(resultado.getNombreArchivo()).isEqualTo("avance_pedidos.html");
        verify(calculo).filas(any());
    }

    @Test
    @DisplayName("el reporte generado queda en el historial")
    void registraEnHistorial() {
        fachada().generar("AVANCE_PEDIDOS", "PANTALLA", new FiltrosReporte(), "jperez");

        verify(reporteDAO).registrarGenerado(any(ReporteGenerado.class));
        verify(auditoria).registrar(eq("jperez"), eq("REPORTES"), eq("GENERAR_REPORTE"),
                eq("reporte_gerencial"), eq("AVANCE_PEDIDOS"), anyString());
    }

    @Test
    @DisplayName("un formato no habilitado se rechaza con un mensaje claro")
    void rechazaFormatoNoHabilitado() {
        assertThatThrownBy(() -> fachada().generar("AVANCE_PEDIDOS", "XML", new FiltrosReporte(),
                "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("XML");

        verify(reporteDAO, never()).registrarGenerado(any());
    }

    @Test
    @DisplayName("un codigo de reporte desconocido se rechaza")
    void rechazaCodigoDesconocido() {
        assertThatThrownBy(() -> fachada().generar("INVENTARIO", "PANTALLA", new FiltrosReporte(),
                "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("INVENTARIO");
    }

    @Test
    @DisplayName("la estrategia aporta las columnas del reporte")
    void laEstrategiaAportaLasColumnas() {
        fachada();
        assertThat(reporte.calculo()).isSameAs(calculo);
        assertThat(reporte.calculo().columnas()).containsExactly("Codigo", "Cliente");
    }

    @Test
    @DisplayName("filtrosSoportados: la fachada publica solo los filtros de la estrategia")
    void filtrosSoportadosDelegaEnLaEstrategia() {
        when(calculo.filtrosSoportados()).thenReturn(
                List.of(FiltroReporte.CLIENTE, FiltroReporte.FECHAS));

        assertThat(fachada().filtrosSoportados("AVANCE_PEDIDOS"))
                .containsExactly(FiltroReporte.CLIENTE, FiltroReporte.FECHAS);
    }

    @Test
    @DisplayName("filtrosSoportados: sin declaracion explicita se ofrecen todos")
    void filtrosSoportadosSinDeclaracion() {
        when(calculo.filtrosSoportados()).thenReturn(null);

        assertThat(fachada().filtrosSoportados("AVANCE_PEDIDOS"))
                .containsExactly(FiltroReporte.values());
    }

    @Test
    @DisplayName("Bridge: el formato en pantalla no genera archivo")
    void elFormatoPantallaNoGeneraArchivo() {
        EstrategiaFormato formato = new FormatoPantalla();
        ResultadoReporte resultado = new ResultadoReporte();
        resultado.setColumnas(List.of("A"));
        resultado.setFilas(List.of(List.of("1")));

        assertThat(formato.escribir(resultado)).isEmpty();
        assertThat(formato.tipoContenido()).contains("text/html");
    }
}
