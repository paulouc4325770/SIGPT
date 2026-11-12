package com.textilesandinos.sigpt.negocio.pedidos;

import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.pedidos.ClienteDAO;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Reglas de la cartera de clientes (EF-1001-4).
 *
 * <p>El RUC / Tax ID es la clave natural del cliente: no puede repetirse.
 */
@Service
public class ClienteLN {

    /** Mensaje de exito de la especificacion. */
    public static final String MENSAJE_REGISTRO = "Cliente registrado satisfactoriamente";

    /** Aviso de cliente duplicado (flujo excepcional 5.1 de la especificacion). */
    public static final String MENSAJE_DUPLICADO =
            "El RUC / Tax ID ya se encuentra registrado. No se permiten clientes duplicados.";

    private final ClienteDAO clienteDAO;
    private final AuditoriaHelper auditoria;

    public ClienteLN(ClienteDAO clienteDAO, AuditoriaHelper auditoria) {
        this.clienteDAO = clienteDAO;
        this.auditoria = auditoria;
    }

    /** Lista los clientes que coinciden con el criterio de busqueda. */
    public List<Cliente> listar(String busqueda) {
        return clienteDAO.listar(busqueda);
    }

    /** Busca un cliente por su identificador. */
    public Cliente buscar(int idCliente) {
        return clienteDAO.buscarPorId(idCliente)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));
    }

    /**
     * Registra o actualiza un cliente.
     *
     * @param esNuevo {@code true} si es un alta, {@code false} si es edicion
     */
    public Cliente guardar(Cliente cliente, boolean esNuevo, String codUsuario) {
        validar(cliente);
        Integer idExcluido = esNuevo ? null : cliente.getIdCliente();
        if (clienteDAO.existeRuc(cliente.getRucTaxId(), idExcluido)) {
            throw new ReglaNegocioException(MENSAJE_DUPLICADO);
        }
        if (esNuevo) {
            cliente.setCodigo(clienteDAO.siguienteCodigo());
            int id = clienteDAO.insertar(cliente, codUsuario);
            cliente.setIdCliente(id);
            auditoria.registrar(codUsuario, "PEDIDOS", "REGISTRAR_CLIENTE", "cliente",
                    String.valueOf(id), "Razon social: " + cliente.getRazonSocial());
        } else {
            clienteDAO.actualizar(cliente, codUsuario);
            auditoria.registrar(codUsuario, "PEDIDOS", "EDITAR_CLIENTE", "cliente",
                    String.valueOf(cliente.getIdCliente()),
                    "Razon social: " + cliente.getRazonSocial());
        }
        return cliente;
    }

    private void validar(Cliente cliente) {
        if (cliente == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (isVacio(cliente.getRazonSocial())) {
            throw new ReglaNegocioException("La Razon Social es obligatoria.");
        }
        if (isVacio(cliente.getRucTaxId())) {
            throw new ReglaNegocioException("El RUC / Tax ID es obligatorio.");
        }
        if (isVacio(cliente.getPais())) {
            throw new ReglaNegocioException("El Pais es obligatorio.");
        }
        if (cliente.getEstado() == null) {
            cliente.setEstado("ACTIVO");
        }
    }

    private boolean isVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
