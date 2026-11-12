package com.textilesandinos.sigpt.negocio.comun;

/**
 * Error de una regla de negocio.
 *
 * <p>La lanza la capa de negocio cuando una operacion no cumple una regla de
 * dominio (RD1, RD2, RD3, RD7, R-Varianza, R-Merma...). El mensaje es el texto
 * que ve el usuario, tal como aparece en la especificacion, y el presentador lo
 * muestra sin corregirlo.
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String regla;

    public ReglaNegocioException(String mensaje) {
        this(mensaje, null);
    }

    public ReglaNegocioException(String mensaje, String regla) {
        super(mensaje);
        this.regla = regla;
    }

    /** Codigo de la regla incumplida (RD1, RD2...), si se conoce. */
    public String getRegla() {
        return regla;
    }
}
