package com.textilesandinos.sigpt.negocio.planta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.IncidenciaDAO;
import com.textilesandinos.sigpt.datos.planta.LoteDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.RegistroAvanceDAO;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;

/**
 * Pruebas del registro de avance (EF-1001-7).
 *
 * <p>Verifican la Regla R-Varianza: si el avance diario presenta una varianza
 * mayor al 15 % respecto de la velocidad teorica del telar, el sistema avisa
 * de baja productividad o parada no reportada.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvanceLNTest {

    @Mock
    private RegistroAvanceDAO avanceDAO;
    @Mock
    private AsignacionDAO asignacionDAO;
    @Mock
    private LoteDAO loteDAO;
    @Mock
    private MaquinaDAO maquinaDAO;
    @Mock
    private IncidenciaDAO incidenciaDAO;
    @Mock
    private OrdenProduccionDAO ordenDAO;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private AuditoriaHelper auditoria;

    private AvanceLN avanceLN;

    @BeforeEach
    void setUp() {
        avanceLN = new AvanceLN(avanceDAO, asignacionDAO, loteDAO, maquinaDAO, incidenciaDAO,
                ordenDAO, parametros, auditoria);
        when(parametros.decimal("VARIANZA_MAXIMA_AVANCE_PORCENTAJE"))
                .thenReturn(new BigDecimal("15"));

        Asignacion asignacion = new Asignacion();
        asignacion.setIdAsignacion(1);
        asignacion.setIdOrdenProduccion(1);
        asignacion.setIdOperario(1);
        asignacion.setIdMaquina(1);
        asignacion.setIdTurno(1);
        asignacion.setEstado("ASIGNADA");
        when(asignacionDAO.buscarPorId(1)).thenReturn(Optional.of(asignacion));

        Maquina maquina = new Maquina();
        maquina.setIdMaquina(1);
        maquina.setVelocidadTeoricaMtsHora(new BigDecimal("45"));
        maquina.setHorasPorTurno(new BigDecimal("8"));
        when(maquinaDAO.buscarPorId(1)).thenReturn(Optional.of(maquina));

        when(avanceDAO.insertar(any(), eq("op001"))).thenReturn(10);
        when(loteDAO.siguienteCodigo()).thenReturn("LOTE-2026-0001");
        when(loteDAO.insertar(any(), eq("op001"))).thenReturn(20);
    }

    private AvanceFormulario formulario(BigDecimal metros) {
        AvanceFormulario formulario = new AvanceFormulario();
        formulario.setIdAsignacion(1);
        formulario.setFecha(LocalDate.now());
        formulario.setMetrosProducidos(metros);
        return formulario;
    }

    @Test
    @DisplayName("R-Varianza: un avance por debajo del 15 % de la velocidad teorica genera alerta")
    void alertaPorBajaProductividad() {
        ResultadoAvance resultado = avanceLN.registrar(formulario(new BigDecimal("300")), "op001");

        // Teorico del turno: 45 m/h x 8 h = 360 m. 300 m es -16.67 %.
        assertThat(resultado.getMetrosTeoricos()).isEqualByComparingTo("360");
        assertThat(resultado.getVarianzaPct()).isEqualByComparingTo("-16.67");
        assertThat(resultado.isAlertaVarianza()).isTrue();
        assertThat(resultado.getMensajeAlerta()).contains("Baja Productividad");
    }

    @Test
    @DisplayName("R-Varianza: un avance dentro del umbral no genera alerta")
    void noAlertaConAvanceDentroDelUmbral() {
        ResultadoAvance resultado = avanceLN.registrar(formulario(new BigDecimal("340")), "op001");

        assertThat(resultado.getVarianzaPct()).isEqualByComparingTo("-5.56");
        assertThat(resultado.isAlertaVarianza()).isFalse();
    }

    @Test
    @DisplayName("R-Merma: el avance genera un lote pendiente de inspeccion")
    void generaLotePendienteDeInspeccion() {
        ResultadoAvance resultado = avanceLN.registrar(formulario(new BigDecimal("340")), "op001");

        assertThat(resultado.getIdAvance()).isEqualTo(10);
        assertThat(resultado.getIdLote()).isEqualTo(20);
        verify(loteDAO).insertar(any(), eq("op001"));
        verify(auditoria).registrar(eq("op001"), eq("PLANTA"), eq("REGISTRAR_AVANCE"),
                eq("registro_avance"), eq("10"), any());
    }

    @Test
    @DisplayName("registra la incidencia de maquinaria cuando el operario la informa")
    void registraIncidencia() {
        AvanceFormulario formulario = formulario(new BigDecimal("340"));
        formulario.setTipoIncidencia("AVERIA");
        formulario.setDescripcionIncidencia("El telar se detuvo");

        avanceLN.registrar(formulario, "op001");

        verify(incidenciaDAO).insertar(any(), eq("op001"));
    }
}
