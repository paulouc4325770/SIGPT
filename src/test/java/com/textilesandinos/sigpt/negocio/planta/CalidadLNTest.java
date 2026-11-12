package com.textilesandinos.sigpt.negocio.planta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.InspeccionCalidadDAO;
import com.textilesandinos.sigpt.datos.planta.LoteDAO;
import com.textilesandinos.sigpt.datos.planta.ParteDiarioDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.planta.Lote;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Pruebas del control de calidad (EF-1001-7).
 *
 * <p>Verifican la Regla RD7 (la calidad se judgega contra la ficha tecnica) y la
 * Regla R-Merma (un lote no conforme no avanza el progreso del pedido).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalidadLNTest {

    @Mock
    private LoteDAO loteDAO;
    @Mock
    private InspeccionCalidadDAO inspeccionDAO;
    @Mock
    private RegistroAvanceDAO avanceDAO;
    @Mock
    private AsignacionDAO asignacionDAO;
    @Mock
    private OrdenProduccionDAO ordenDAO;
    @Mock
    private PedidoDAO pedidoDAO;
    @Mock
    private ParteDiarioDAO parteDiarioDAO;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private AuditoriaHelper auditoria;

    private CalidadLN calidadLN;

    @BeforeEach
    void setUp() {
        calidadLN = new CalidadLN(loteDAO, inspeccionDAO, avanceDAO, asignacionDAO, ordenDAO,
                pedidoDAO, parteDiarioDAO, parametros, auditoria);
        when(parametros.decimal("TOLERANCIA_COLOR_DEFAULT")).thenReturn(new BigDecimal("2.00"));

        Lote lote = new Lote();
        lote.setIdLote(1);
        lote.setCodigo("LOTE-2026-0001");
        lote.setIdOrdenProduccion(1);
        lote.setIdAsignacion(1);
        lote.setMetros(new BigDecimal("300"));
        lote.setEstado("PENDIENTE_INSPECCION");
        when(loteDAO.buscarPorId(1)).thenReturn(Optional.of(lote));

        RegistroAvance avance = new RegistroAvance();
        avance.setIdAvance(10);
        avance.setEstadoCalidad("PENDIENTE");
        when(avanceDAO.listarPorAsignacion(1)).thenReturn(List.of(avance));

        OrdenProduccion orden = new OrdenProduccion();
        orden.setIdOrden(1);
        orden.setCodOrden("OP-2026-0001");
        orden.setIdPedido(1);
        orden.setMetrosTotales(new BigDecimal("1000"));
        orden.setMetrosConsolidados(new BigDecimal("0.00"));
        orden.setEstado("EN_PRODUCCION");
        when(ordenDAO.buscarPorId(1)).thenReturn(Optional.of(orden));

        Pedido pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setCodPedido("PED-2026-0001");
        pedido.setEstado("EN_PRODUCCION");
        pedido.setMetros(new BigDecimal("1000"));
        pedido.setMetrosConsolidados(new BigDecimal("0.00"));
        when(pedidoDAO.buscarPorId(1)).thenReturn(Optional.of(pedido));

        FichaTecnica ficha = new FichaTecnica();
        ficha.setIdPedido(1);
        ficha.setToleranciaColor(new BigDecimal("2.00"));
        when(pedidoDAO.buscarFichaTecnica(1)).thenReturn(Optional.of(ficha));

        when(inspeccionDAO.insertar(any(), eq("supervisor"))).thenReturn(30);
    }

    private InspeccionFormulario formulario(BigDecimal varianza) {
        InspeccionFormulario formulario = new InspeccionFormulario();
        formulario.setIdLote(1);
        formulario.setVarianzaColorPct(varianza);
        formulario.setFecha(LocalDate.now());
        return formulario;
    }

    @Test
    @DisplayName("RD7: un lote dentro de la tolerancia de la ficha tecnica es conforme")
    void loteConformeConsolidaElProgreso() {
        ResultadoCalidad resultado = calidadLN.inspeccionar(formulario(new BigDecimal("1.50")),
                "supervisor");

        assertThat(resultado.isConforme()).isTrue();
        assertThat(resultado.getToleranciaAplicada()).isEqualByComparingTo("2.00");
        assertThat(resultado.getPorcentajeAvanceOrden()).isEqualByComparingTo("30.00");
        verify(loteDAO).actualizarEstado(1, "CONFORME");
        verify(avanceDAO).actualizarEstadoCalidad(10, "CONFORME");
        verify(ordenDAO).actualizarMetrosConsolidados(1, new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("RD7 y R-Merma: un lote que excede la tolerancia se registra como Merma y no avanza")
    void loteNoConformeSeRegistraComoMerma() {
        ResultadoCalidad resultado = calidadLN.inspeccionar(formulario(new BigDecimal("3.20")),
                "supervisor");

        assertThat(resultado.isConforme()).isFalse();
        assertThat(resultado.getMensaje()).contains("Merma");
        verify(loteDAO).actualizarEstado(1, "MERMA");
        verify(avanceDAO).actualizarEstadoCalidad(10, "NO_CONFORME");
        verify(ordenDAO, never()).actualizarMetrosConsolidados(anyInt(), any());
        verify(pedidoDAO, never()).actualizarMetrosConsolidados(anyInt(), any());
    }

    @Test
    @DisplayName("no reinspecciona un lote ya inspeccionado")
    void rechazaReinspeccionDeLote() {
        Lote lote = new Lote();
        lote.setIdLote(1);
        lote.setEstado("CONFORME");
        when(loteDAO.buscarPorId(1)).thenReturn(Optional.of(lote));

        assertThatThrownBy(() -> calidadLN.inspeccionar(formulario(new BigDecimal("1")), "supervisor"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya fue inspeccionado");
    }

    @Test
    @DisplayName("el parte diario solo se genera cuando la orden alcanza el 100 %")
    void parteDiarioRequiereCienPorCiento() {
        assertThatThrownBy(() -> calidadLN.generarParteDiario(1, LocalDate.now(), "supervisor"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("100 %");

        verify(parteDiarioDAO, never()).guardar(any(), anyString());
    }
}
