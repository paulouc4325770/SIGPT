package com.textilesandinos.sigpt.datos.abastecimiento;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;

/**
 * Acceso al padron de proveedores.
 */
public interface ProveedorDAO {

    /** Lista los proveedores que coinciden con el criterio de busqueda. */
    List<Proveedor> listar(String busqueda);

    /** Busca un proveedor por su identificador. */
    Optional<Proveedor> buscarPorId(int idProveedor);

    /** Busca un proveedor por su codigo homologado. */
    Optional<Proveedor> buscarPorCodigo(String codigo);

    /** Indica si el codigo de proveedor ya esta registrado. */
    boolean existeCodigo(String codigo);

    /** Inserta un proveedor y devuelve su identificador. */
    int insertar(Proveedor proveedor, String codUsuario);

    /** Actualiza los datos de un proveedor. */
    int actualizar(Proveedor proveedor, String codUsuario);

    /** Genera el siguiente codigo de proveedor (PRV-NNN). */
    String siguienteCodigo();
}
