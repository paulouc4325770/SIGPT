package com.textilesandinos.sigpt.datos.pedidos;

import java.util.List;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.pedidos.Cliente;

/**
 * Acceso a la cartera de clientes.
 */
public interface ClienteDAO {

    /** Lista los clientes activos e inactivos; {@code busqueda} filtra por razon social o RUC. */
    List<Cliente> listar(String busqueda);

    /** Busca un cliente por su identificador. */
    Optional<Cliente> buscarPorId(int idCliente);

    /** Busca un cliente por su RUC / Tax ID. */
    Optional<Cliente> buscarPorRuc(String rucTaxId);

    /**
     * Indica si el RUC ya esta registrado.
     *
     * @param idClienteExcluido cliente que se esta editando, para no compararse consigo mismo
     */
    boolean existeRuc(String rucTaxId, Integer idClienteExcluido);

    /** Inserta un cliente y devuelve su identificador. */
    int insertar(Cliente cliente, String codUsuario);

    /** Actualiza los datos de un cliente. */
    int actualizar(Cliente cliente, String codUsuario);

    /** Genera el siguiente codigo de cliente (CLI-NNN). */
    String siguienteCodigo();

    /** Cuenta los clientes registrados. */
    int contar();
}
