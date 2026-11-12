package com.textilesandinos.sigpt.datos.comun;

import java.time.LocalDate;
import java.util.Optional;

import com.textilesandinos.sigpt.entidades.comun.TipoCambio;

/**
 * Acceso al tipo de cambio entre monedas.
 */
public interface TipoCambioDAO {

    /** Tipo de cambio vigente en la fecha indicada (la mas reciente que ya rige). */
    Optional<TipoCambio> buscarVigente(String monedaOrigen, String monedaDestino, LocalDate fecha);
}
