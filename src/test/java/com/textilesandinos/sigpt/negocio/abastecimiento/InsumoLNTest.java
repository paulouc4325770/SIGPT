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
import com.textilesandinos.sigpt.datos.abastecimiento.ProveedorDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Pruebas del catalogo de insumos (EF-1001-5).
 *
 * <p>Verifican la regla RD5: todo insumo quimico o tinte exige su Hoja de
 * Seguridad antes de su uso.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsumoLNTest {

    @Mock
    private InsumoDAO insumoDAO;
    @Mock
    private ProveedorDAO proveedorDAO;
    @Mock
    private AuditoriaHelper auditoria;

    private InsumoLN insumoLN;

    @BeforeEach
    void setUp() {
        insumoLN = new InsumoLN(insumoDAO, proveedorDAO, auditoria);
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1);
        proveedor.setCodigo("PRV-003");
        proveedor.setRazonSocial("Quimicos Textiles Andinos");
        proveedor.setEstado("ACTIVO");
        when(proveedorDAO.buscarPorId(1)).thenReturn(Optional.of(proveedor));
        when(insumoDAO.siguienteCodigo()).thenReturn("INS-QUI-003");
    }

    private InsumoFormulario formulario(String tipo, String rutaMsds) {
        InsumoFormulario formulario = new InsumoFormulario();
        formulario.setIdProveedor(1);
        formulario.setNombre("Fijador quimico");
        formulario.setTipo(tipo);
        formulario.setUnidadMedida("KG");
        formulario.setStockMinimo(new BigDecimal("60"));
        formulario.setPrecioUnitario(new BigDecimal("15.70"));
        formulario.setRutaMsds(rutaMsds);
        return formulario;
    }

    @Test
    @DisplayName("RD5: un insumo quimico sin MSDS no se registra")
    void rechazaQuimicoSinMsds() {
        assertThatThrownBy(() -> insumoLN.registrar(formulario("QUIMICO", null), "jlogistica"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("MSDS")
                .hasMessageContaining("RD5");

        verify(insumoDAO, never()).insertar(any(), anyString());
    }

    @Test
    @DisplayName("RD5: un tinte sin MSDS tampoco se registra")
    void rechazaTinteSinMsds() {
        assertThatThrownBy(() -> insumoLN.registrar(formulario("TINTE", ""), "jlogistica"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("MSDS");
    }

    @Test
    @DisplayName("una fibra sin MSDS si se registra")
    void registraFibraSinMsds() {
        when(insumoDAO.insertar(any(), eq("jlogistica"))).thenReturn(9);

        var insumo = insumoLN.registrar(formulario("FIBRA", null), "jlogistica");

        assertThat(insumo.getIdInsumo()).isEqualTo(9);
        assertThat(insumo.getCodigo()).isEqualTo("INS-QUI-003");
        verify(auditoria).registrar(eq("jlogistica"), eq("ABASTECIMIENTO"),
                eq("REGISTRAR_INSUMO"), eq("insumo"), eq("9"), anyString());
    }

    @Test
    @DisplayName("un quimico con MSDS se registra")
    void registraQuimicoConMsds() {
        when(insumoDAO.insertar(any(), eq("jlogistica"))).thenReturn(10);

        var insumo = insumoLN.registrar(formulario("QUIMICO", "/msds/INS-QUI-003.pdf"), "jlogistica");

        assertThat(insumo.getIdInsumo()).isEqualTo(10);
        assertThat(insumo.getRutaMsds()).isEqualTo("/msds/INS-QUI-003.pdf");
    }

    @Test
    @DisplayName("no se registra un insumo de un proveedor inactivo")
    void rechazaProveedorInactivo() {
        Proveedor inactivo = new Proveedor();
        inactivo.setIdProveedor(1);
        inactivo.setEstado("INACTIVO");
        when(proveedorDAO.buscarPorId(1)).thenReturn(Optional.of(inactivo));

        assertThatThrownBy(() -> insumoLN.registrar(formulario("FIBRA", null), "jlogistica"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }
}
