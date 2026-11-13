package com.textilesandinos.sigpt.datos.abastecimiento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;

/**
 * Acceso al catalogo de insumos y a su stock.
 */
public interface InsumoDAO {

    /** Lista los insumos que coinciden con el criterio de busqueda. */
    List<Insumo> listar(String busqueda);

    /** Lista todos los insumos. */
    List<Insumo> listarTodos();

    /** Lista los insumos cuyo stock esta en o por debajo del minimo (R-Stock). */
    List<Insumo> listarEnStockMinimo();

    /** Busca un insumo por su identificador. */
    Optional<Insumo> buscarPorId(int idInsumo);

    /** Busca un insumo por su codigo. */
    Optional<Insumo> buscarPorCodigo(String codigo);

    /** Inserta un insumo y devuelve su identificador. */
    int insertar(Insumo insumo, String codUsuario);

    /** Actualiza los datos de un insumo. */
    int actualizar(Insumo insumo, String codUsuario);

    /** Actualiza el stock actual del insumo. */
    int actualizarStock(int idInsumo, BigDecimal stockActual);

    /** Genera el siguiente codigo de insumo. */
    String siguienteCodigo();
}
