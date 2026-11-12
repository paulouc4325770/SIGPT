package com.textilesandinos.sigpt.datos.comun;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * Base comun de los DAO: acceso JDBC con sentencias parametrizadas, apertura y
 * cierre de recursos y control de transacciones.
 *
 * <p>Se mantiene JDBC puro, sin ORM (decision D7): los DAO solo hacen altas,
 * cambios y consultas; las reglas de negocio viven en la capa de negocio.
 */
public abstract class DaoBase {

    protected final DataSource origenDatos;

    protected DaoBase(DataSource origenDatos) {
        this.origenDatos = origenDatos;
    }

    /** Ejecuta una consulta que devuelve como maximo una fila. */
    protected <T> Optional<T> consultarUno(String sql, List<ParametroSql> parametros,
            Mapeador<T> mapeador) {
        List<T> filas = consultarLista(sql, parametros, mapeador);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }

    /** Ejecuta una consulta que devuelve varias filas. */
    protected <T> List<T> consultarLista(String sql, List<ParametroSql> parametros,
            Mapeador<T> mapeador) {
        List<T> entidades = new ArrayList<>();
        try (Connection conexion = origenDatos.getConnection();
                PreparedStatement sentencia = preparar(conexion, sql, parametros);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                entidades.add(mapeador.mapear(resultado));
            }
            return entidades;
        } catch (SQLException e) {
            throw new ExcepcionDatos("Error al ejecutar la consulta: " + sql, e);
        }
    }

    /** Ejecuta una sentencia de insercion, actualizacion o borrado. */
    protected int ejecutar(String sql, List<ParametroSql> parametros) {
        try (Connection conexion = origenDatos.getConnection();
                PreparedStatement sentencia = preparar(conexion, sql, parametros)) {
            return sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new ExcepcionDatos("Error al ejecutar la sentencia: " + sql, e);
        }
    }

    /**
     * Genera el siguiente codigo correlativo de una entidad.
     *
     * <p>El formato es el que usa la base de datos: {@code prefijo + numero con
     * la cantidad de digitos indicada}. El numero se calcula sobre el mayor
     * correlativo existente y se verifica que el codigo resultante este libre,
     * de modo que una serie con formatos mixtos no genere colisiones.
     *
     * @param sqlUltimo consulta que devuelve el ultimo correlativo en la columna
     *                  {@code ultimo}
     * @param prefijo  prefijo del codigo, por ejemplo {@code PED-2026-}
     * @param digitos  cantidad de digitos del correlativo
     * @param yaExiste comprueba si un codigo concreto ya esta en uso
     */
    protected String siguienteCodigo(String sqlUltimo, String prefijo, int digitos,
            java.util.function.Predicate<String> yaExiste) {
        int ultimo = consultarUno(sqlUltimo,
                List.of(ParametroSql.texto("prefijo", prefijo + "%")),
                resultado -> resultado.getInt("ultimo")).orElse(0);
        for (int numero = ultimo + 1; numero <= ultimo + 1000; numero++) {
            String codigo = prefijo + String.format("%0" + digitos + "d", numero);
            if (!yaExiste.test(codigo)) {
                return codigo;
            }
        }
        throw new ExcepcionDatos("No se pudo generar un codigo correlativo con el prefijo "
                + prefijo);
    }

    /**
     * Ejecuta una operacion dentro de una transaccion: se confirma completa o se
     * revierte completa.
     */
    protected <T> T enTransaccion(OperacionTransaccional<T> operacion) {
        try (Connection conexion = origenDatos.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (Exception e) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }
                throw new ExcepcionDatos("La transaccion fue revertida", e);
            }
        } catch (SQLException e) {
            throw new ExcepcionDatos("No se pudo abrir la conexion para la transaccion", e);
        }
    }

    /** Prepara una sentencia con sus parametros enlazados. */
    protected static PreparedStatement preparar(Connection conexion, String sql,
            List<ParametroSql> parametros) throws SQLException {
        PreparedStatement sentencia = conexion.prepareStatement(sql);
        if (parametros != null) {
            int indice = 1;
            for (ParametroSql parametro : parametros) {
                if (parametro.tipo() == Types.NULL) {
                    sentencia.setObject(indice++, parametro.valor());
                } else {
                    sentencia.setObject(indice++, parametro.valor(), parametro.tipo());
                }
            }
        }
        return sentencia;
    }

    /** Operacion a ejecutar dentro de una transaccion. */
    @FunctionalInterface
    public interface OperacionTransaccional<T> {

        T ejecutar(Connection conexion) throws SQLException;
    }
}
