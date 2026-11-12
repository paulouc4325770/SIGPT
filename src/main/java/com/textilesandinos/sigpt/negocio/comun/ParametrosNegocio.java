package com.textilesandinos.sigpt.negocio.comun;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.comun.ParametroDAO;
import com.textilesandinos.sigpt.entidades.comun.Parametro;

import jakarta.annotation.PostConstruct;

/**
 * Patron Singleton: una unica instancia con los valores de las reglas de negocio,
 * cargados desde la base de datos.
 *
 * <p>Spring administra una sola instancia de este componente (mismo criterio que
 * las clases LN y DAO: son instancia unica y las crea el framework). Los valores
 * se leen una vez al arrancar y quedan disponibles en memoria.
 *
 * <p>Los umbrales del sistema NO estan escritos en el codigo: viven en la tabla
 * {@code parametro}. Si un administrador cambia un valor, el sistema lo recoge al
 * reiniciar, sin recompilar.
 *
 * <p>Vive en la capa de negocio porque necesita leer de la base de datos, y la
 * capa de utilidades no puede depender de la capa de datos.
 */
@Component
public class ParametrosNegocio {

    private final ParametroDAO parametroDAO;
    private final Map<String, String> valores = new HashMap<>();

    public ParametrosNegocio(ParametroDAO parametroDAO) {
        this.parametroDAO = parametroDAO;
    }

    /** Carga los parametros al iniciar la aplicacion. */
    @PostConstruct
    public void cargar() {
        valores.clear();
        for (Parametro parametro : parametroDAO.listarActivos()) {
            valores.put(parametro.getCodigo(), parametro.getValor());
        }
    }

    /** Valor de un parametro como texto. */
    public String texto(String codigo) {
        String valor = valores.get(codigo);
        if (valor == null) {
            throw new ParametroNoEncontradoException(codigo);
        }
        return valor;
    }

    /** Valor de un parametro como entero. */
    public int entero(String codigo) {
        return Integer.parseInt(texto(codigo));
    }

    /** Valor de un parametro como decimal. */
    public BigDecimal decimal(String codigo) {
        return new BigDecimal(texto(codigo));
    }

    /** Valor de un parametro como booleano. */
    public boolean booleano(String codigo) {
        return Boolean.parseBoolean(texto(codigo));
    }

    /** Indica si el parametro existe. */
    public boolean existe(String codigo) {
        return valores.containsKey(codigo);
    }

    /** Todos los parametros cargados. */
    public Map<String, String> todos() {
        return Map.copyOf(valores);
    }

    /** Se lanza cuando se pide un parametro que no existe en la base de datos. */
    public static class ParametroNoEncontradoException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ParametroNoEncontradoException(String codigo) {
            super("No existe el parametro de negocio con codigo: " + codigo);
        }
    }
}
