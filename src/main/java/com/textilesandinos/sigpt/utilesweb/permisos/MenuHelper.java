package com.textilesandinos.sigpt.utilesweb.permisos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.textilesandinos.sigpt.entidades.seguridad.Opcion;

/**
 * Construye el menu por perfil a partir de la lista plana de opciones que
 * devuelve la base de datos.
 *
 * <p>Equivale al {@code MenuHelper} del laboratorio, que armaba el arbol con
 * {@code TagBuilder} de forma recursiva. Aqui el arbol se arma en Java y la
 * plantilla Thymeleaf solo lo recorre.
 */
public final class MenuHelper {

    private MenuHelper() {
    }

    /**
     * Agrupa las opciones en un arbol: grupos de primer nivel con sus hojas.
     *
     * @param opciones lista plana de opciones del perfil
     * @return mapa de grupo a sus subopciones, en el orden definido en la base
     */
    public static Map<String, List<Opcion>> construirMenu(List<Opcion> opciones) {
        Map<String, List<Opcion>> menu = new LinkedHashMap<>();
        Map<Integer, String> nombreGrupo = new LinkedHashMap<>();

        for (Opcion opcion : opciones) {
            if (opcion.getIdOpcionRef() == null) {
                nombreGrupo.put(opcion.getIdOpcion(), opcion.getNombreOpcion());
                menu.put(opcion.getNombreOpcion(), new ArrayList<>());
            }
        }

        for (Opcion opcion : opciones) {
            if (opcion.getIdOpcionRef() != null) {
                String grupo = nombreGrupo.get(opcion.getIdOpcionRef());
                if (grupo != null) {
                    menu.get(grupo).add(opcion);
                }
            }
        }

        return menu;
    }

    /**
     * Indica si una ruta solicitada esta permitida para el perfil del usuario.
     *
     * <p>Es la comprobacion que hace el interceptor en cada peticion: ocultar la
     * opcion del menu no es suficiente, hay que validar el acceso al recurso.
     *
     * <p>Se acepta la ruta exacta de la opcion y tambien sus subrutas, porque
     * las acciones de una pantalla (guardar, consultar, anular, exportar) viven
     * bajo la URL de esa pantalla: {@code /pedidos/bandeja/anular} es una accion
     * de la opcion {@code /pedidos/bandeja}. Una subruta nunca habilita a otra
     * opcion, porque cada opcion se compara por separado.
     *
     * @param opciones  opciones del perfil
     * @param ruta      ruta solicitada, por ejemplo {@code /pedidos/bandeja}
     * @return {@code true} si el perfil tiene esa opcion
     */
    public static boolean tieneAcceso(List<Opcion> opciones, String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return false;
        }
        String normalizada = normalizar(ruta);
        for (Opcion opcion : opciones) {
            if (opcion.esGrupo()) {
                continue;
            }
            String url = normalizar(opcion.getUrlOpcion());
            if (url == null || url.isEmpty()) {
                continue;
            }
            if (normalizada.equals(url)) {
                return true;
            }
            if (normalizada.startsWith(url + "/")) {
                return true;
            }
        }
        return false;
    }

    /** Normaliza una ruta para comparar sin barras finales ni query string. */
    private static String normalizar(String ruta) {
        if (ruta == null) {
            return "";
        }
        String resultado = ruta.trim();
        int interrogacion = resultado.indexOf('?');
        if (interrogacion >= 0) {
            resultado = resultado.substring(0, interrogacion);
        }
        if (resultado.length() > 1 && resultado.endsWith("/")) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado;
    }
}
