package com.textilesandinos.sigpt.negocio.seguridad;

import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.seguridad.OpcionDAO;
import com.textilesandinos.sigpt.datos.seguridad.PerfilDAO;
import com.textilesandinos.sigpt.datos.seguridad.UsuarioDAO;
import com.textilesandinos.sigpt.entidades.seguridad.MatrizAcceso;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utiles.seguridad.HashHelper;

/**
 * Administracion del modulo de seguridad: usuarios, perfiles y matriz de
 * accesos.
 *
 * <p>Las contrasenas se guardan siempre cifradas con PBKDF2 y sal por usuario;
 * el texto plano no se escribe jamas en la base de datos.
 */
@Service
public class SeguridadAdminLN {

    private final UsuarioDAO usuarioDAO;
    private final PerfilDAO perfilDAO;
    private final OpcionDAO opcionDAO;
    private final AuditoriaHelper auditoria;

    public SeguridadAdminLN(UsuarioDAO usuarioDAO, PerfilDAO perfilDAO, OpcionDAO opcionDAO,
            AuditoriaHelper auditoria) {
        this.usuarioDAO = usuarioDAO;
        this.perfilDAO = perfilDAO;
        this.opcionDAO = opcionDAO;
        this.auditoria = auditoria;
    }

    /** Usuarios registrados. */
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listar();
    }

    /** Perfiles del sistema. */
    public List<Perfil> listarPerfiles() {
        return perfilDAO.listar();
    }

    /** Opciones del menu. */
    public List<Opcion> listarOpciones() {
        return opcionDAO.listarTodas();
    }

    /** Matriz de accesos: que perfil tiene asignada cada opcion. */
    public List<MatrizAcceso> listarMatrizAccesos() {
        return opcionDAO.listarMatriz();
    }

    /** Registra un usuario nuevo con su perfil. */
    public Usuario registrarUsuario(Usuario usuario, String clave, String codUsuario) {
        validarUsuario(usuario);
        if (clave == null || clave.isBlank() || clave.length() < 8) {
            throw new ReglaNegocioException("La clave debe tener al menos 8 caracteres.");
        }
        if (usuarioDAO.existePorCodigo(usuario.getCodUsuario())) {
            throw new ReglaNegocioException("El usuario " + usuario.getCodUsuario()
                    + " ya esta registrado.");
        }
        usuario.setClaveHash(HashHelper.hashear(clave));
        usuario.setEstado(true);
        int idUsuario = usuarioDAO.insertar(usuario);
        usuario.setIdUsuario(idUsuario);
        auditoria.registrar(codUsuario, "seguridad", "REGISTRAR_USUARIO", "usuario",
                String.valueOf(idUsuario), "Codigo " + usuario.getCodUsuario()
                        + " | Perfil " + usuario.getIdPerfil());
        return usuario;
    }

    /** Actualiza los datos del usuario (sin tocar la clave). */
    public void actualizarUsuario(Usuario usuario, String codUsuario) {
        validarUsuario(usuario);
        usuarioDAO.actualizar(usuario);
        auditoria.registrar(codUsuario, "seguridad", "EDITAR_USUARIO", "usuario",
                String.valueOf(usuario.getIdUsuario()), "Codigo " + usuario.getCodUsuario());
    }

    /** Genera una nueva clave para el usuario. */
    public void resetearClave(int idUsuario, String codUsuario, String clave) {
        if (clave == null || clave.isBlank() || clave.length() < 8) {
            throw new ReglaNegocioException("La clave debe tener al menos 8 caracteres.");
        }
        usuarioDAO.actualizarClave(idUsuario, HashHelper.hashear(clave));
        auditoria.registrar(codUsuario, "seguridad", "RESETEAR_CLAVE", "usuario",
                String.valueOf(idUsuario), "Se genero una clave nueva");
    }

    /** Registra un perfil nuevo. */
    public Perfil registrarPerfil(Perfil perfil, String codUsuario) {
        if (perfil.getNombrePerfil() == null || perfil.getNombrePerfil().isBlank()) {
            throw new ReglaNegocioException("El nombre del perfil es obligatorio.");
        }
        if (perfil.getCodPerfil() == null || perfil.getCodPerfil().isBlank()) {
            throw new ReglaNegocioException("El codigo del perfil es obligatorio.");
        }
        perfil.setEstado(true);
        int idPerfil = perfilDAO.insertar(perfil);
        perfil.setIdPerfil(idPerfil);
        auditoria.registrar(codUsuario, "seguridad", "REGISTRAR_PERFIL", "perfil",
                String.valueOf(idPerfil), perfil.getNombrePerfil());
        return perfil;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new ReglaNegocioException("No se encontraron datos para los criterios "
                    + "seleccionados.");
        }
        if (usuario.getCodUsuario() == null || usuario.getCodUsuario().isBlank()) {
            throw new ReglaNegocioException("El codigo de usuario es obligatorio.");
        }
        if (usuario.getNombres() == null || usuario.getNombres().isBlank()) {
            throw new ReglaNegocioException("El nombre del usuario es obligatorio.");
        }
        if (usuario.getIdPerfil() == null || usuario.getIdPerfil() == 0) {
            throw new ReglaNegocioException("Debe seleccionar el perfil del usuario.");
        }
    }
}
