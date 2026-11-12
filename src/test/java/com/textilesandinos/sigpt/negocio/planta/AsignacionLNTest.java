package com.textilesandinos.sigpt.negocio.planta;

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

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.OrdenProduccionDAO;
import com.textilesandinos.sigpt.datos.planta.AsignacionDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.datos.planta.OperarioDAO;
import com.textilesandinos.sigpt.datos.planta.TurnoDAO;
import com.textilesandinos.sigpt.entidades.pedidos.OrdenProduccion;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.entidades.planta.Operario;
import com.textilesandinos.sigpt.entidades.planta.Turno;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Pruebas de la asignacion de operarios y maquinaria (EF-1001-6).
 *
 * <p>Verifican la Regla R-Turno (RD3): ningun operario puede recibir turnos
 * consecutivos que superen las 16 horas.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsignacionLNTest {

    @Mock
    private AsignacionDAO asignacionDAO;
    @Mock
    private OperarioDAO operarioDAO;
    @Mock
    private MaquinaDAO maquinaDAO;
    @Mock
    private TurnoDAO turnoDAO;
    @Mock
    private OrdenProduccionDAO ordenDAO;
    @Mock
    private ParametrosNegocio parametros;
    @Mock
    private AuditoriaHelper auditoria;

    private AsignacionLN asignacionLN;

    @BeforeEach
    void setUp() {
        asignacionLN = new AsignacionLN(asignacionDAO, operarioDAO, maquinaDAO, turnoDAO, ordenDAO,
                parametros, auditoria);
        when(parametros.decimal("JORNADA_MAXIMA_HORAS")).thenReturn(new BigDecimal("16"));

        when(operarioDAO.buscarPorId(1)).thenReturn(Optional.of(operario()));
        when(maquinaDAO.buscarPorId(1)).thenReturn(Optional.of(maquina("LIBRE")));
        when(turnoDAO.buscarPorId(1)).thenReturn(Optional.of(turno()));
        when(ordenDAO.buscarPorId(1)).thenReturn(Optional.of(orden()));
        when(asignacionDAO.listarPorMaquinaYFecha(any(Integer.class), any(LocalDate.class)))
                .thenReturn(List.of());
    }

    private Operario operario() {
        Operario operario = new Operario();
        operario.setIdOperario(1);
        operario.setCodigo("OP-001");
        operario.setNombres("Ana Quispe");
        operario.setEstado("ACTIVO");
        return operario;
    }

    private Maquina maquina(String estado) {
        Maquina maquina = new Maquina();
        maquina.setIdMaquina(1);
        maquina.setCodigo("TC-001");
        maquina.setNombre("Telar circular 1");
        maquina.setEstado(estado);
        maquina.setVelocidadTeoricaMtsHora(new BigDecimal("45"));
        maquina.setHorasPorTurno(new BigDecimal("8"));
        return maquina;
    }

    private Turno turno() {
        Turno turno = new Turno();
        turno.setIdTurno(1);
        turno.setNombre("Manana");
        turno.setHoras(new BigDecimal("8"));
        return turno;
    }

    private OrdenProduccion orden() {
        OrdenProduccion orden = new OrdenProduccion();
        orden.setIdOrden(1);
        orden.setCodOrden("OP-2026-0001");
        orden.setEstado("EN_PRODUCCION");
        orden.setMetrosTotales(new BigDecimal("3500"));
        return orden;
    }

    private AsignacionFormulario formulario() {
        AsignacionFormulario formulario = new AsignacionFormulario();
        formulario.setIdOperario(1);
        formulario.setIdMaquina(1);
        formulario.setIdTurno(1);
        formulario.setIdOrdenProduccion(1);
        formulario.setFecha(LocalDate.now());
        return formulario;
    }

    @Test
    @DisplayName("R-Turno: bloquea la asignacion cuando el operario superaria las 16 horas")
    void bloqueaAsignacionQueSuperaLasDieciseisHoras() {
        // El operario ya tiene dos turnos (16 h); un tercero lo llevaria a 24 h.
        when(asignacionDAO.horasAsignadas(1, LocalDate.now())).thenReturn(new BigDecimal("16.00"));

        assertThatThrownBy(() -> asignacionLN.asignar(formulario(), "supervisor"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("16")
                .hasMessageContaining("R-Turno");

        verify(asignacionDAO, never()).insertar(any(), anyString());
    }

    @Test
    @DisplayName("R-Turno: permite el segundo turno (16 horas exactas)")
    void permiteSegundoTurnoDeOchoHoras() {
        when(asignacionDAO.horasAsignadas(1, LocalDate.now())).thenReturn(new BigDecimal("8.00"));
        when(asignacionDAO.insertar(any(), eq("supervisor"))).thenReturn(5);

        var asignacion = asignacionLN.asignar(formulario(), "supervisor");

        assertThat(asignacion.getIdAsignacion()).isEqualTo(5);
        assertThat(asignacion.getHorasAsignadas()).isEqualByComparingTo("8.00");
        verify(maquinaDAO).actualizarEstado(1, "OCUPADA");
        verify(auditoria).registrar(eq("supervisor"), eq("PLANTA"), eq("ASIGNAR_OPERARIO"),
                eq("asignacion"), eq("5"), anyString());
    }

    @Test
    @DisplayName("no asigna una maquina que esta en mantenimiento")
    void rechazaMaquinaEnMantenimiento() {
        when(maquinaDAO.buscarPorId(1)).thenReturn(Optional.of(maquina("MANTENIMIENTO")));

        assertThatThrownBy(() -> asignacionLN.asignar(formulario(), "supervisor"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("mantenimiento");
    }

    @Test
    @DisplayName("no asigna una maquina que ya esta ocupada en el mismo turno")
    void rechazaMaquinaOcupadaEnElTurno() {
        var existente = new com.textilesandinos.sigpt.entidades.planta.Asignacion();
        existente.setIdAsignacion(3);
        existente.setIdMaquina(1);
        existente.setIdTurno(1);
        existente.setEstado("ASIGNADA");
        when(asignacionDAO.listarPorMaquinaYFecha(1, LocalDate.now()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> asignacionLN.asignar(formulario(), "supervisor"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya esta ocupada");
    }
}
