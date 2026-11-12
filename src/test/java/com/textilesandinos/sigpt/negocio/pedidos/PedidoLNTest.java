package com.textilesandinos.sigpt.negocio.pedidos;

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
import com.textilesandinos.sigpt.datos.pedidos.ClienteDAO;
import com.textilesandinos.sigpt.datos.pedidos.CotizacionDAO;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.pedidos.PedidoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.negocio.planta.CapacidadProduccionLN;

/**
 * Pruebas de las reglas del pedido (EF-1001-3).
 *
 * <p>Verifican la Regla R1 (metraje minimo), la anulacion segun el ciclo de vida
 * y la auditoria de las operaciones.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoLNTest {

    @Mock
    private PedidoDAO pedidoDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private CotizacionDAO cotizacionDAO;
    @Mock
    private AdelantoDAO adelantoDAO;
    @Mock
    private OrdenProduccionDAO ordenDAO;
    @Mock
    private CapacidadProduccionLN capacidad;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private AuditoriaHelper auditoria;

    private PedidoLN pedidoLN;

    @BeforeEach
    void setUp() {
        pedidoLN = new PedidoLN(pedidoDAO, clienteDAO, cotizacionDAO, adelantoDAO, ordenDAO,
                capacidad, parametros, auditoria);
        when(parametros.decimal("PEDIDO_MINIMO_METROS")).thenReturn(new BigDecimal("1000"));
        when(parametros.decimal("TOLERANCIA_COLOR_DEFAULT")).thenReturn(new BigDecimal("2.00"));
    }

    private Cliente clienteActivo() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setRazonSocial("Marca Andina");
        cliente.setEstado("ACTIVO");
        return cliente;
    }

    private PedidoFormulario formulario(BigDecimal metros) {
        PedidoFormulario formulario = new PedidoFormulario();
        formulario.setIdCliente(1);
        formulario.setTipoHilado("Peinado 30/1");
        formulario.setGramajeM2(new BigDecimal("180"));
        formulario.setColorPantone("19-4023 TCX");
        formulario.setComposicion("100% Algodon Pima");
        formulario.setMetros(metros);
        formulario.setFechaEntrega(LocalDate.now().plusDays(30));
        return formulario;
    }

    @Test
    @DisplayName("R1: no acepta un pedido con menos de 1,000 metros")
    void rechazaPedidoPorDebajoDelMetrajeMinimo() {
        when(clienteDAO.buscarPorId(1)).thenReturn(Optional.of(clienteActivo()));

        assertThatThrownBy(() -> pedidoLN.registrar(formulario(new BigDecimal("800")), "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("1,000 metros")
                .hasMessageContaining("R1");

        verify(pedidoDAO, never()).insertarConFicha(any(), any(), anyString());
    }

    @Test
    @DisplayName("R1: acepta un pedido con el metraje minimo y lo registra con su ficha")
    void registraPedidoValido() {
        when(clienteDAO.buscarPorId(1)).thenReturn(Optional.of(clienteActivo()));
        when(capacidad.esViable(any(), any())).thenReturn(true);
        when(pedidoDAO.siguienteCodigo()).thenReturn("PED-2026-0004");
        when(pedidoDAO.insertarConFicha(any(), any(), eq("jperez"))).thenReturn(4);

        int idPedido = pedidoLN.registrar(formulario(new BigDecimal("3500")), "jperez");

        assertThat(idPedido).isEqualTo(4);
        verify(auditoria).registrar(eq("jperez"), eq("PEDIDOS"), eq("REGISTRAR_PEDIDO"),
                eq("pedido"), eq("4"), anyString());
    }

    @Test
    @DisplayName("no registra un pedido de un cliente inactivo")
    void rechazaClienteInactivo() {
        Cliente cliente = clienteActivo();
        cliente.setEstado("INACTIVO");
        when(clienteDAO.buscarPorId(1)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> pedidoLN.registrar(formulario(new BigDecimal("3500")), "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    @DisplayName("no registra un pedido que no es viable para el periodo solicitado")
    void rechazaPedidoNoViable() {
        when(clienteDAO.buscarPorId(1)).thenReturn(Optional.of(clienteActivo()));
        when(capacidad.esViable(any(), any())).thenReturn(false);
        when(capacidad.capacidadDiariaMetros()).thenReturn(new BigDecimal("1680"));
        when(capacidad.diasDisponibles(any())).thenReturn(10);

        assertThatThrownBy(() -> pedidoLN.registrar(formulario(new BigDecimal("50000")), "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no es viable");
    }

    @Test
    @DisplayName("anula un pedido registrado y deja la traza de auditoria")
    void anulaPedidoRegistrado() {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(7);
        pedido.setCodPedido("PED-2026-0007");
        pedido.setEstado("REGISTRADO");
        when(pedidoDAO.buscarPorId(7)).thenReturn(Optional.of(pedido));

        pedidoLN.anular(7, "Cliente cancelo", "jperez");

        verify(pedidoDAO).actualizarEstado(7, "ANULADO", "jperez");
        verify(auditoria).registrar(eq("jperez"), eq("PEDIDOS"), eq("ANULAR_PEDIDO"),
                eq("pedido"), eq("7"), anyString());
    }

    @Test
    @DisplayName("no anula un pedido que ya esta en produccion")
    void noAnulaPedidoEnProduccion() {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(8);
        pedido.setCodPedido("PED-2026-0008");
        pedido.setEstado("EN_PRODUCCION");
        when(pedidoDAO.buscarPorId(8)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoLN.anular(8, "motivo", "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no puede anularse");

        verify(pedidoDAO, never()).actualizarEstado(anyInt(), anyString(), anyString());
    }
}
