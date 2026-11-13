package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.abastecimiento.ProveedorDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Reglas del padron de proveedores (EF-1001-5).
 */
@Service
public class ProveedorLN {

    private final ProveedorDAO proveedorDAO;
    private final AuditoriaHelper auditoria;

    public ProveedorLN(ProveedorDAO proveedorDAO, AuditoriaHelper auditoria) {
        this.proveedorDAO = proveedorDAO;
        this.auditoria = auditoria;
    }

    /** Lista los proveedores que coinciden con el criterio de busqueda. */
    public List<Proveedor> listar(String busqueda) {
        return proveedorDAO.listar(busqueda);
    }

    /** Busca un proveedor por su identificador. */
    public Proveedor buscar(int idProveedor) {
        return proveedorDAO.buscarPorId(idProveedor)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
    }

    /** Registra un proveedor homologado. */
    public Proveedor registrar(ProveedorFormulario formulario, String codUsuario) {
        validar(formulario);
        if (formulario.getCodigo() != null && !formulario.getCodigo().isBlank()
                && proveedorDAO.existeCodigo(formulario.getCodigo())) {
            throw new ReglaNegocioException("El codigo de proveedor "
                    + formulario.getCodigo() + " ya esta registrado.");
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setCodigo(formulario.getCodigo() == null || formulario.getCodigo().isBlank()
                ? proveedorDAO.siguienteCodigo() : formulario.getCodigo());
        proveedor.setRazonSocial(formulario.getRazonSocial());
        proveedor.setRuc(formulario.getRuc());
        proveedor.setTipo(formulario.getTipo());
        proveedor.setContacto(formulario.getContacto());
        proveedor.setCorreo(formulario.getCorreo());
        proveedor.setTelefono(formulario.getTelefono());
        proveedor.setEstado(formulario.getEstado() == null ? "ACTIVO" : formulario.getEstado());

        int idProveedor = proveedorDAO.insertar(proveedor, codUsuario);
        proveedor.setIdProveedor(idProveedor);
        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "REGISTRAR_PROVEEDOR", "proveedor",
                String.valueOf(idProveedor), proveedor.getRazonSocial());
        return proveedor;
    }

    private void validar(ProveedorFormulario formulario) {
        if (formulario == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (formulario.getRazonSocial() == null || formulario.getRazonSocial().isBlank()) {
            throw new ReglaNegocioException("La Razon Social del proveedor es obligatoria.");
        }
        if (formulario.getTipo() == null || formulario.getTipo().isBlank()) {
            throw new ReglaNegocioException("El tipo del proveedor es obligatorio.");
        }
        if (!List.of("AGRICOLA", "GANADERA", "QUIMICA", "MIXTA").contains(formulario.getTipo())) {
            throw new ReglaNegocioException("El tipo " + formulario.getTipo()
                    + " no es valido. Use AGRICOLA, GANADERA, QUIMICA o MIXTA.");
        }
    }
}
