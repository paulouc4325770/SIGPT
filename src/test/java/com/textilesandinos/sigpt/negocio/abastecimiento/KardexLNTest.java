package com.textilesandinos.sigpt.negocio.abastecimiento;

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

import com.textilesandinos.sigpt.datos.abastecimiento.InsumoDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.MovimientoKardexDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.OrdenProduccionInsumoDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenProduccionInsumo;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Pruebas del kardex (EF-1001-5).
 *
 * <p>Verifican el control de stock y la regla RD4: la varianza del consumo
 * frente al consumo teorico no puede superar el 15 %.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KardexLNTest {

    @Mock
    private MovimientoKardexDAO kardexDAO;
    @Mock
    private InsumoDAO insumoDAO;
    @Mock
    private OrdenProduccionInsumoDAO consumoTeoricoDAO;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private AuditoriaHelper auditoria;

    private KardexLN kardexLN;

    @BeforeEach
    void setUp() {
        kardexLN = new KardexLN(kardexDAO, insumoDAO, consumoTeoricoDAO, parametros, auditoria);
        when(parametros.decimal("VARIANZA_MAXIMA_CONSUMO_PORCENTAJE"))
                .thenReturn(new BigDecimal("15"));
        when(kardexDAO.registrar(any(), anyString())).thenReturn(1);
    }

    private Insumo insumo(BigDecimal stock) {
        Insumo insumo = new Insumo();
        insumo.setIdInsumo(1);
        insumo.setCodigo("INS-FIB-001");
        insumo.setNombre("Lana de alpaca");
        insumo.setUnidadMedida("KG");
        insumo.setStockActual(stock);
        return insumo;
    }

    private KardexFormulario formulario(String tipo, BigDecimal cantidad) {
        KardexFormulario formulario = new KardexFormulario();
        formulario.setIdInsumo(1);
        formulario.setTipoMovimiento(tipo);
        formulario.setCantidad(cantidad);
        formulario.setFecha(LocalDate.now());
        return formulario;
    }

    @Test
    @DisplayName("registra un ingreso y deja la traza de auditoria")
    void registraIngreso() {
        when(insumoDAO.buscarPorId(1)).thenReturn(Optional.of(insumo(new BigDecimal("100"))));

        MovimientoKardex movimiento = kardexLN.registrar(formulario("INGRESO", new BigDecimal("50")),
                "jlogistica");

        assertThat(movimiento.getIdMovimiento()).isEqualTo(1);
        verify(kardexDAO).registrar(any(), eq("jlogistica"));
        verify(auditoria).registrar(eq("jlogistica"), eq("ABASTECIMIENTO"),
                eq("KARDEX_INGRESO"), eq("movimiento_kardex"), eq("1"), anyString());
    }

    @Test
    @DisplayName("no permite una salida mayor que el stock disponible")
    void rechazaSalidaSinStock() {
        when(insumoDAO.buscarPorId(1)).thenReturn(Optional.of(insumo(new BigDecimal("30"))));

        assertThatThrownBy(() -> kardexLN.registrar(formulario("SALIDA", new BigDecimal("50")),
                "jlogistica"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(kardexDAO, never()).registrar(any(), anyString());
    }

    @Test
    @DisplayName("RD4: avisa cuando el consumo supera en mas del 15 % al teorico")
    void alertaVarianzaDeConsumo() {
        OrdenProduccionInsumo teorico = new OrdenProduccionInsumo();
        teorico.setIdOrdenProduccion(1);
        teorico.setIdInsumo(1);
        teorico.setCantidadTeorica(new BigDecimal("100"));
        when(consumoTeoricoDAO.listarPorOrden(1)).thenReturn(List.of(teorico));

        MovimientoKardex salida = new MovimientoKardex();
        salida.setIdInsumo(1);
        salida.setTipoMovimiento("SALIDA");
        salida.setCantidad(new BigDecimal("120"));
        when(kardexDAO.listarPorOrden(1)).thenReturn(List.of(salida));

        assertThat(kardexLN.varianzaConsumo(1)).isEqualByComparingTo("20.00");
        assertThat(kardexLN.alertaVarianzaConsumo(1)).isTrue();
    }

    @Test
    @DisplayName("RD4: no avisa cuando el consumo esta dentro del 15 %")
    void noAlertaVarianzaDentroDelUmbral() {
        OrdenProduccionInsumo teorico = new OrdenProduccionInsumo();
        teorico.setIdOrdenProduccion(1);
        teorico.setIdInsumo(1);
        teorico.setCantidadTeorica(new BigDecimal("100"));
        when(consumoTeoricoDAO.listarPorOrden(1)).thenReturn(List.of(teorico));

        MovimientoKardex salida = new MovimientoKardex();
        salida.setIdInsumo(1);
        salida.setTipoMovimiento("SALIDA");
        salida.setCantidad(new BigDecimal("110"));
        when(kardexDAO.listarPorOrden(1)).thenReturn(List.of(salida));

        assertThat(kardexLN.varianzaConsumo(1)).isEqualByComparingTo("10.00");
        assertThat(kardexLN.alertaVarianzaConsumo(1)).isFalse();
    }
}
