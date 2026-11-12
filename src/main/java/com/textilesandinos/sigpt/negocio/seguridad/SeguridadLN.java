package com.textilesandinos.sigpt.negocio.seguridad;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.seguridad.OpcionDAO;
import com.textilesandinos.sigpt.datos.seguridad.UsuarioDAO;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.utiles.seguridad.HashHelper;

/**
 * Fachada del modulo de seguridad (patron Fachada, Semana 12).
 *
 * <p>El controlador llama a un solo metodo y no conoce las clases internas. Aqui
 * viven las reglas de acceso: 3 intentos fallidos bloquean la cuenta y la
 * sesion expira por inactividad.
 */
@Service
public class SeguridadLN {

    private static final String MODULO = "seguridad";
    private static final String MENSAJE_CREDENCIALES = "Usuario o contrasena incorrectos";

    private final UsuarioDAO usuarioDAO;
    private final OpcionDAO opcionDAO;
    private final AuditoriaHelper auditoria;
    private final ParametrosNegocio parametros;

    public SeguridadLN(UsuarioDAO usuarioDAO, OpcionDAO opcionDAO, AuditoriaHelper auditoria,
            ParametrosNegocio parametros) {
        this.usuarioDAO = usuarioDAO;
        this.opcionDAO = opcionDAO;
        this.auditoria = auditoria;
        this.parametros = parametros;
    }

    /**
     * Valida las credenciales de un usuario.
     *
     * <p>Al机等 el inicio de sesion, carga el perfil y las opciones de menu que
     * corresponden a ese perfil.
     */
    public ResultadoAutenticacion autenticar(String codUsuario, String clave) {
        if (codUsuario == null || codUsuario.isBlank() || clave == null || clave.isBlank()) {
            return ResultadoAutenticacion.fallido(MENSAJE_CREDENCIALES);
        }

        var encontrado = usuarioDAO.buscarPorCodigo(codUsuario.trim());
        if (encontrado.isEmpty()) {
            auditoria.registrar(codUsuario, MODULO, "LOGIN_FALLIDO", "usuario", codUsuario,
                    "Usuario inexistente");
            return ResultadoAutenticacion.fallido(MENSAJE_CREDENCIALES);
        }

        Usuario usuario = encontrado.get();
        LocalDateTime ahora = LocalDateTime.now();

        if (!usuario.isEstado()) {
            return ResultadoAutenticacion.fallido("El usuario esta inactivo. Contacte al administrador.");
        }

        if (usuario.estaBloqueado(ahora)) {
            return ResultadoAutenticacion.bloqueado("La cuenta esta bloqueada temporalmente. "
                    + "Intentelo de nuevo mas tarde.");
        }

        if (!HashHelper.verificar(clave, usuario.getClaveHash())) {
            return registrarIntentoFallido(usuario, ahora);
        }

        // Credenciales correctas: se reinicia el contador y se registra el acceso.
        usuarioDAO.actualizarIntentos(usuario.getIdUsuario(), 0, null);
        usuarioDAO.registrarAcceso(usuario.getIdUsuario(), ahora);
        List<Opcion> opciones = opcionDAO.listarPorPerfil(usuario.getIdPerfil());
        auditoria.registrar(usuario.getCodUsuario(), MODULO, "LOGIN", "usuario",
                String.valueOf(usuario.getIdUsuario()), "Inicio de sesion correcto");
        return ResultadoAutenticacion.exitoso(usuario, opciones);
    }

    /**
     * Registra un intento fallido. Cuando se alcanza el maximo permitido, la
     * cuenta queda bloqueada temporalmente.
     */
    private ResultadoAutenticacion registrarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        int maximoIntentos = parametros.entero("INTENTOS_MAXIMOS_LOGIN");
        int intentos = usuario.getIntentosFallidos() + 1;

        if (intentos >= maximoIntentos) {
            int minutosBloqueo = parametros.entero("MINUTOS_BLOQUEO_CUENTA");
            LocalDateTime hasta = ahora.plusMinutes(minutosBloqueo);
            usuarioDAO.actualizarIntentos(usuario.getIdUsuario(), intentos, hasta);
            auditoria.registrar(usuario.getCodUsuario(), MODULO, "CUENTA_BLOQUEADA", "usuario",
                    String.valueOf(usuario.getIdUsuario()),
                    "Bloqueo por " + intentos + " intentos fallidos");
            return ResultadoAutenticacion.bloqueado("La cuenta fue bloqueada por " + intentos
                    + " intentos fallidos. Intentelo de nuevo en " + minutosBloqueo + " minutos.");
        }

        usuarioDAO.actualizarIntentos(usuario.getIdUsuario(), intentos, null);
        auditoria.registrar(usuario.getCodUsuario(), MODULO, "LOGIN_FALLIDO", "usuario",
                String.valueOf(usuario.getIdUsuario()), "Intento fallido numero " + intentos);
        return ResultadoAutenticacion.fallido(MENSAJE_CREDENCIALES);
    }

    /** Opciones de menu habilitadas para un perfil. */
    public List<Opcion> listarOpcionesDePerfil(int idPerfil) {
        return opcionDAO.listarPorPerfil(idPerfil);
    }

    /** Busca un usuario por su codigo, con su perfil. */
    public Usuario buscarUsuario(String codUsuario) {
        return usuarioDAO.buscarPorCodigo(codUsuario).orElse(null);
    }

    /** Lista de usuarios registrados. */
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listar();
    }

    /** Registra la salida de sesion de un usuario. */
    public void registrarCierreSesion(String codUsuario) {
        auditoria.registrar(codUsuario, MODULO, "LOGOUT", "usuario", null, "Cierre de sesion");
    }

    /** Genera el hash de una contrasena para almacenarla. */
    public String generarHash(String clave) {
        return HashHelper.hashear(clave);
    }
}
