package com.textilesandinos.sigpt.datos.pedidos;

import java.math.BigDecimal;
import java.util.List;

import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;

/**
 * Acceso a los adelantos de las cotizaciones.
 */
public interface AdelantoDAO {

    /** Inserta un adelanto y devuelve su identificador. */
    int insertar(Adelanto adelanto, String codUsuario);

    /** Lista los adelantos de una cotizacion. */
    List<Adelanto> listarPorCotizacion(int idCotizacion);

    /** Suma de los adelantos ya verificados de una cotizacion. */
    BigDecimal sumaVerificada(int idCotizacion);
}
