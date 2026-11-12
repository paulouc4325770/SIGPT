package com.textilesandinos.sigpt.negocio.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.textilesandinos.sigpt.datos.pedidos.AdelantoDAO;
import com.textilesandinos.sigpt.datos.pedidos.CotizacionDAO;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.comun.TipoCambioLN;

/**
 * Pruebas de la cotizacion y el adelanto (EF-1001-3, pasos 6 y 7).
 *
 * <p>Verifican la vigencia de la cotizacion y la Regla R2: la produccion solo
 * se habilita con un adelanto verificado del 40 % del valor pactado.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CotizacionLNTest {

    @Mock
    private CotizacionDAO cotizacionDAO;
    @Mock
    private AdelantoDAO adelantoDAO;
    @Mock
    private PedidoDAO pedidoDAO;
    @Mock
    private OrdenProduccionDAO ordenDAO;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private TipoCambioLN tipoCambioLN;
    @Mock
    private AuditoriaHelper auditoria;

    private CotizacionLN cotizacionLN;

    @BeforeEach
    void setUp() {
        cotizacionLN = new CotizacionLN(cotizacionDAO, adelantoDAO, pedidoDAO, ordenDAO,
                parametros, tipoCambioLN, auditoria);
        when(parametros.entero("VIGENCIA_COTIZACION_DIAS")).thenReturn(15);
        when(parametros.texto("MONEDA_PREDETERMINADA")).thenReturn("USD");
        when(parametros.decimal("ADELANTO_MINIMO_PORCENTAJE")).thenReturn(new BigDecimal("40"));
    }

    private Pedido pedidoEn(String estado) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setCodPedido("PED-2026-0001");
        pedido.setEstado(estado);
        pedido.setMetros(new BigDecimal("3500"));
        return pedido;
    }

    private Cotizacion cotizacionVigente() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(1);
        cotizacion.setIdPedido(1);
        cotizacion.setMontoTotal(new BigDecimal("12500.00"));
        cotizacion.setMoneda("USD");
        cotizacion.setEstado("EMITIDA");
        cotizacion.setFechaEmision(LocalDate.now());
        cotizacion.setFechaVigencia(LocalDate.now().plusDays(15));
        cotizacion.setHorasCiclo(new BigDecimal("96"));
        return cotizacion;
    }

    @Test
    @DisplayName("emite la cotizacion con 15 dias de vigencia y pasa el pedido a Cotizado")
    void emiteCotizacion() {
        when(pedidoDAO.buscarPorId(1)).thenReturn(Optional.of(pedidoEn("REGISTRADO")));
        when(cotizacionDAO.insertar(any(), eq("jperez"))).thenReturn(1);

        CotizacionFormulario formulario = new CotizacionFormulario();
        formulario.setIdPedido(1);
        formulario.setMontoTotal(new BigDecimal("12500"));
        formulario.setFechaEmision(LocalDate.of(2026, 11, 12));

        ResultadoCotizacion resultado = cotizacionLN.emitir(formulario, "jperez");

        assertThat(resultado.getCotizacion().getFechaVigencia())
                .isEqualTo(LocalDate.of(2026, 11, 27));
        assertThat(resultado.getCotizacion().getMoneda()).isEqualTo("USD");
        verify(pedidoDAO).actualizarEstado(1, "COTIZADO", "jperez");
        verify(auditoria).registrar(eq("jperez"), eq("PEDIDOS"), eq("EMITIR_COTIZACION"),
                eq("cotizacion"), anyString(), anyString());
    }

    @Test
    @DisplayName("R2: un adelanto del 40 % habilita la produccion y emite la orden")
    void adelantoSuficienteHabilitaProduccion() {
        Cotizacion cotizacion = cotizacionVigente();
        when(cotizacionDAO.buscarPorId(1)).thenReturn(Optional.of(cotizacion));
        when(pedidoDAO.buscarPorId(1)).thenReturn(Optional.of(pedidoEn("COTIZADO")));
        when(adelantoDAO.insertar(any(), eq("jperez"))).thenReturn(1);
        when(adelantoDAO.sumaVerificada(1)).thenReturn(new BigDecimal("6000.00"));
        when(ordenDAO.siguienteCodigo()).thenReturn("OP-2026-0001");
        when(ordenDAO.insertar(any(), eq("jperez"))).thenReturn(1);

        AdelantoFormulario formulario = new AdelantoFormulario();
        formulario.setIdCotizacion(1);
        formulario.setMonto(new BigDecimal("6000"));
        formulario.setVerificado(true);

        ResultadoAdelanto resultado = cotizacionLN.registrarAdelanto(formulario, "jperez");

        assertThat(resultado.getPorcentajeCubierto()).isEqualByComparingTo("48.00");
        assertThat(resultado.isProduccionHabilitada()).isTrue();
        assertThat(resultado.getOrdenProduccion()).isNotNull();
        verify(pedidoDAO).actualizarEstado(1, "EN_PRODUCCION", "jperez");
        verify(ordenDAO).insertar(any(OrdenProduccion.class), eq("jperez"));
    }

    @Test
    @DisplayName("R2: un adelanto menor al 40 % no habilita la produccion")
    void adelantoInsuficienteNoHabilitaProduccion() {
        Cotizacion cotizacion = cotizacionVigente();
        when(cotizacionDAO.buscarPorId(1)).thenReturn(Optional.of(cotizacion));
        when(pedidoDAO.buscarPorId(1)).thenReturn(Optional.of(pedidoEn("COTIZADO")));
        when(adelantoDAO.insertar(any(), eq("jperez"))).thenReturn(1);
        when(adelantoDAO.sumaVerificada(1)).thenReturn(new BigDecimal("1000.00"));

        AdelantoFormulario formulario = new AdelantoFormulario();
        formulario.setIdCotizacion(1);
        formulario.setMonto(new BigDecimal("1000"));
        formulario.setVerificado(true);

        ResultadoAdelanto resultado = cotizacionLN.registrarAdelanto(formulario, "jperez");

        assertThat(resultado.isProduccionHabilitada()).isFalse();
        assertThat(resultado.getPorcentajeCubierto()).isEqualByComparingTo("8.00");
        verify(ordenDAO, never()).insertar(any(), anyString());
        verify(pedidoDAO, never()).actualizarEstado(1, "EN_PRODUCCION", "jperez");
    }

    @Test
    @DisplayName("no acepta adelantos sobre una cotizacion vencida")
    void rechazaAdelantoSobreCotizacionVencida() {
        Cotizacion cotizacion = cotizacionVigente();
        cotizacion.setFechaVigencia(LocalDate.now().minusDays(1));
        when(cotizacionDAO.buscarPorId(1)).thenReturn(Optional.of(cotizacion));

        AdelantoFormulario formulario = new AdelantoFormulario();
        formulario.setIdCotizacion(1);
        formulario.setMonto(new BigDecimal("6000"));

        assertThatThrownBy(() -> cotizacionLN.registrarAdelanto(formulario, "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vencio");

        verify(cotizacionDAO).actualizarEstado(1, "VENCIDA");
        verify(adelantoDAO, never()).insertar(any(), anyString());
    }

    @Test
    @DisplayName("no emite la cotizacion de un pedido anulado")
    void rechazaCotizarPedidoAnulado() {
        when(pedidoDAO.buscarPorId(1)).thenReturn(Optional.of(pedidoEn("ANULADO")));

        CotizacionFormulario formulario = new CotizacionFormulario();
        formulario.setIdPedido(1);
        formulario.setMontoTotal(new BigDecimal("12500"));

        assertThatThrownBy(() -> cotizacionLN.emitir(formulario, "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ANULADO");
    }
}
