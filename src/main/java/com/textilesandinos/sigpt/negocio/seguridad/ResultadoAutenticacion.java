package com.textilesandinos.sigpt.negocio.seguridad;

import java.util.List;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;

/**
 * Resultado del intento de inicio de sesion.
 *
 * <p>No se distingue entre "usuario inexistente" y "contrasena incorrecta": el
 * mensaje es el mismo en ambos casos, para no revelar que cuentas existen.
 */
public class ResultadoAutenticacion {

    private final boolean exitoso;
    private final String mensaje;
    private final Usuario usuario;
    private final List<Opcion> opciones;
    private final boolean cuentaBloqueada;

    private ResultadoAutenticacion(boolean exitoso, String mensaje, Usuario usuario,
            List<Opcion> opciones, boolean cuentaBloqueada) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.usuario = usuario;
        this.opciones = opciones;
        this.cuentaBloqueada = cuentaBloqueada;
    }

    public static ResultadoAutenticacion exitoso(Usuario usuario, List<Opcion> opciones) {
        return new ResultadoAutenticacion(true, null, usuario, opciones, false);
    }

    public static ResultadoAutenticacion fallido(String mensaje) {
        return new ResultadoAutenticacion(false, mensaje, null, List.of(), false);
    }

    public static ResultadoAutenticacion bloqueado(String mensaje) {
        return new ResultadoAutenticacion(false, mensaje, null, List.of(), true);
    }

    public boolean esExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<Opcion> getOpciones() {
        return opciones;
    }

    public boolean isCuentaBloqueada() {
        return cuentaBloqueada;
    }
}
