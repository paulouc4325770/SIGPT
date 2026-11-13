package com.textilesandinos.sigpt.negocio.reportes.modelo;

/**
 * Filtros que un reporte sabe aplicar sobre sus datos.
 *
 * <p>No todos los reportes admiten los mismos criterios: el avance de pedidos se
 * filtra por cliente y por fecha de entrega, la productividad por operario,
 * maquina y fecha de la asignacion, y los reportes de consumo y eficiencia solo
 * por fecha. La pantalla muestra unicamente los filtros que el reporte usa, para
 * que ningun control quede sin efecto.
 */
public enum FiltroReporte {
    CLIENTE,
    OPERARIO,
    MAQUINA,
    FECHAS
}
