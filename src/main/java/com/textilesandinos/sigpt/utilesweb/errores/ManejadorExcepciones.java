package com.textilesandinos.sigpt.utilesweb.errores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de errores.
 *
 * <p>Equivale al {@code ExceptionFilterAtributes} del laboratorio: intercepta la
 * excepcion, la registra en el log y devuelve una pagina de error propia, sin
 * mostrar detalle tecnico al usuario (OWASP A05).
 */
@ControllerAdvice
public class ManejadorExcepciones {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorExcepciones.class);

    /**
     * No hay controlador para la ruta solicitada: es un 404, no un error del
     * sistema. Debe distinguirse del manejador generico.
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarNoEncontrado(Exception excepcion, Model modelo) {
        LOG.info("Recurso no encontrado: {}", excepcion.getMessage());
        modelo.addAttribute("codigoError", 404);
        modelo.addAttribute("mensajeError", "La pagina solicitada no existe.");
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String manejarErrorGenerico(Exception excepcion, Model modelo) {
        LOG.error("Error no controlado en la peticion", excepcion);
        modelo.addAttribute("codigoError", 500);
        modelo.addAttribute("mensajeError",
                "Ocurrio un error inesperado. Intente mas tarde o contacte al soporte tecnico.");
        return "error/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarDatoInvalido(IllegalArgumentException excepcion, Model modelo) {
        LOG.warn("Dato invalido: {}", excepcion.getMessage());
        modelo.addAttribute("codigoError", 400);
        modelo.addAttribute("mensajeError", excepcion.getMessage());
        return "error/error";
    }
}
