package com.textilesandinos.sigpt.datos.comun;

/**
 * Excepcion de la capa de datos. Envuelve los errores de JDBC para que la capa
 * de negocio no dependa de {@link java.sql.SQLException}.
 */
public class ExcepcionDatos extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcepcionDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ExcepcionDatos(String mensaje) {
        super(mensaje);
    }
}
