package com.textilesandinos.sigpt.utilesweb.presentacion;

import org.springframework.stereotype.Component;

/**
 * Colores de las etiquetas de estado de las pantallas.
 *
 * <p>Se resuelve desde las plantillas con {@code ${@clases.badge(estado)}}.
 * Centralizar el color aqui evita repetir un ternario largo en cada celda de
 * cada tabla y mantiene las vistas libres de logica de presentacion.
 */
@Component
public class Clases {

    /** Devuelve la clase CSS de la etiqueta segun el estado recibido. */
    public String badge(String estado) {
        if (estado == null) {
            return "bg-secondary";
        }
        return switch (estado) {
            case "EN_PRODUCCION", "ASIGNADA", "EN_CURSO" -> "bg-primary";
            case "TERMINADO", "CONFORME", "ACTIVO", "ACEPTADA" -> "bg-success";
            case "ANULADO", "MERMA", "NO_CONFORME", "INACTIVO", "RECHAZADA" -> "bg-danger";
            case "COTIZADO", "EMITIDA", "PENDIENTE_INSPECCION", "PENDIENTE" -> "bg-info";
            case "VENCIDA", "MANTENIMIENTO", "BACKUP", "CANCELADA", "ABIERTA" -> "bg-warning";
            default -> "bg-secondary";
        };
    }

    /** Devuelve la clase CSS de un avance segun su estado de calidad. */
    public String badgeCalidad(String estadoCalidad) {
        if (estadoCalidad == null) {
            return "bg-secondary";
        }
        return switch (estadoCalidad) {
            case "CONFORME" -> "bg-success";
            case "NO_CONFORME" -> "bg-danger";
            case "PENDIENTE" -> "bg-info";
            default -> "bg-secondary";
        };
    }
}
