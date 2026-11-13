package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.abastecimiento.InsumoDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.ProveedorDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Reglas del catalogo de insumos y proveedores (EF-1001-5).
 *
 * <p>Aplica la regla RD5 (todo insumo quimico requiere su Hoja de Seguridad
 * antes de su uso) y la regla R-MSDS (todo quimico o tinte debe ingresar
 * acompanado de su MSDS). La regla R-Stock se materializa en la alerta de
 * stock minimo que muestra la pantalla de insumos.
 */
@Service
public class InsumoLN {

    /** Tipos de insumo que exigen Hoja de Seguridad de Datos (RD5 / R-MSDS). */
    public static final List<String> TIPOS_CON_MSDS = List.of("QUIMICO", "TINTE");

    private final InsumoDAO insumoDAO;
    private final ProveedorDAO proveedorDAO;
    private final AuditoriaHelper auditoria;

    public InsumoLN(InsumoDAO insumoDAO, ProveedorDAO proveedorDAO, AuditoriaHelper auditoria) {
        this.insumoDAO = insumoDAO;
        this.proveedorDAO = proveedorDAO;
        this.auditoria = auditoria;
    }

    /** Lista los insumos que coinciden con el criterio de busqueda. */
    public List<Insumo> listar(String busqueda) {
        return insumoDAO.listar(busqueda);
    }

    /** Lista los insumos cuyo stock esta en el minimo (alerta R-Stock). */
    public List<Insumo> alertasStockMinimo() {
        return insumoDAO.listarEnStockMinimo();
    }

    /** Busca un insumo por su identificador. */
    public Insumo buscar(int idInsumo) {
        return insumoDAO.buscarPorId(idInsumo)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
    }

    /**
     * Registra un insumo.
     *
     * <p>Un quimico o un tinte no puede registrarse sin su Hoja de Seguridad
     * (RD5): el sistema lo rechaza y explica el motivo.
     */
    public Insumo registrar(InsumoFormulario formulario, String codUsuario) {
        validar(formulario);

        Proveedor proveedor = proveedorDAO.buscarPorId(formulario.getIdProveedor())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El proveedor seleccionado no esta registrado."));
        if (!"ACTIVO".equals(proveedor.getEstado())) {
            throw new ReglaNegocioException("El proveedor " + proveedor.getCodigo()
                    + " esta inactivo: no admite insumos nuevos.");
        }

        if (TIPOS_CON_MSDS.contains(formulario.getTipo())
                && (formulario.getRutaMsds() == null || formulario.getRutaMsds().isBlank())) {
            throw new ReglaNegocioException("Todo insumo " + formulario.getTipo()
                    + " requiere su Hoja de Seguridad (MSDS) antes de su uso (Regla RD5).",
                    "RD5");
        }

        Insumo insumo = new Insumo();
        insumo.setCodigo(insumoDAO.siguienteCodigo());
        insumo.setIdProveedor(formulario.getIdProveedor());
        insumo.setNombre(formulario.getNombre());
        insumo.setTipo(formulario.getTipo());
        insumo.setUnidadMedida(formulario.getUnidadMedida() == null ? "KG"
                : formulario.getUnidadMedida());
        insumo.setStockMinimo(formulario.getStockMinimo() == null ? BigDecimal.ZERO
                : formulario.getStockMinimo());
        insumo.setStockActual(BigDecimal.ZERO);
        insumo.setPrecioUnitario(formulario.getPrecioUnitario() == null ? BigDecimal.ZERO
                : formulario.getPrecioUnitario());
        insumo.setRutaMsds(formulario.getRutaMsds());
        insumo.setEstado(formulario.getEstado() == null ? "ACTIVO" : formulario.getEstado());

        int idInsumo = insumoDAO.insertar(insumo, codUsuario);
        insumo.setIdInsumo(idInsumo);

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "REGISTRAR_INSUMO", "insumo",
                String.valueOf(idInsumo), insumo.getNombre() + " | " + insumo.getTipo());
        return insumo;
    }

    /** Actualiza los datos de un insumo (incluida su MSDS). */
    public void actualizar(Insumo insumo, String codUsuario) {
        if (TIPOS_CON_MSDS.contains(insumo.getTipo())
                && (insumo.getRutaMsds() == null || insumo.getRutaMsds().isBlank())) {
            throw new ReglaNegocioException("Todo insumo " + insumo.getTipo()
                    + " requiere su Hoja de Seguridad (MSDS) antes de su uso (Regla RD5).",
                    "RD5");
        }
        insumoDAO.actualizar(insumo, codUsuario);
        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "EDITAR_INSUMO", "insumo",
                String.valueOf(insumo.getIdInsumo()), insumo.getNombre());
    }

    private void validar(InsumoFormulario formulario) {
        if (formulario == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (formulario.getIdProveedor() == null) {
            throw new ReglaNegocioException("Debe seleccionar el proveedor del insumo.");
        }
        if (formulario.getNombre() == null || formulario.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del insumo es obligatorio.");
        }
        if (formulario.getTipo() == null || formulario.getTipo().isBlank()) {
            throw new ReglaNegocioException("El tipo de insumo es obligatorio.");
        }
        if (!List.of("FIBRA", "TINTE", "QUIMICO", "CAJA").contains(formulario.getTipo())) {
            throw new ReglaNegocioException("El tipo " + formulario.getTipo()
                    + " no es valido. Use FIBRA, TINTE, QUIMICO o CAJA.");
        }
    }

    /** Optional del insumo, para uso interno. */
    public Optional<Insumo> opcional(int idInsumo) {
        return insumoDAO.buscarPorId(idInsumo);
    }
}
