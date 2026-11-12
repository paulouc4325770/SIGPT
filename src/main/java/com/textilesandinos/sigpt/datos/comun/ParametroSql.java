package com.textilesandinos.sigpt.datos.comun;

/**
 * Parametro de una sentencia SQL parametrizada. Evita concatenar texto en las
 * consultas (RDS5 / OWASP A03: inyeccion SQL).
 *
 * @param nombre   nombre del marcador de la sentencia
 * @param valor    valor a enviar
 * @param tipo     tipo JDBC del valor
 */
public record ParametroSql(String nombre, Object valor, int tipo) {

    public static ParametroSql de(String nombre, Object valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.NULL);
    }

    public static ParametroSql entero(String nombre, int valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.INTEGER);
    }

    public static ParametroSql largo(String nombre, long valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.BIGINT);
    }

    public static ParametroSql decimal(String nombre, java.math.BigDecimal valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.DECIMAL);
    }

    public static ParametroSql texto(String nombre, String valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.VARCHAR);
    }

    public static ParametroSql fecha(String nombre, java.sql.Date valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.DATE);
    }

    public static ParametroSql fechaHora(String nombre, java.sql.Timestamp valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.TIMESTAMP);
    }

    public static ParametroSql booleano(String nombre, boolean valor) {
        return new ParametroSql(nombre, valor, java.sql.Types.BOOLEAN);
    }
}
