package com.textilesandinos.sigpt.negocio.comun;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.TipoCambioDAO;
import com.textilesandinos.sigpt.entidades.comun.TipoCambio;

/**
 * Cambio de moneda entre el precio pactado (USD) y la contabilidad interna (PEN).
 *
 * <p>Textiles Andinos exporta: la cotizacion se emite en dolares, pero la
 * operacion interna necesita el equivalente en soles.
 */
@Service
public class TipoCambioLN {

    private final TipoCambioDAO tipoCambioDAO;

    public TipoCambioLN(TipoCambioDAO tipoCambioDAO) {
        this.tipoCambioDAO = tipoCambioDAO;
    }

    /**
     * Convierte un monto de una moneda a otra usando el tipo de cambio vigente.
     *
     * @throws com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException
     *         si no hay tipo de cambio registrado para esa fecha
     */
    public BigDecimal convertir(BigDecimal monto, String monedaOrigen, String monedaDestino,
            LocalDate fecha) {
        if (monto == null) {
            return BigDecimal.ZERO;
        }
        if (monedaOrigen.equals(monedaDestino)) {
            return monto;
        }
        TipoCambio tipoCambio = tipoCambioDAO.buscarVigente(monedaOrigen, monedaDestino, fecha)
                .orElse(null);
        if (tipoCambio != null) {
            return monto.multiply(tipoCambio.getValor()).setScale(2, RoundingMode.HALF_UP);
        }
        // Si no hay el par directo se usa el inverso: 1 USD = 1 / (1 PEN en USD) PEN.
        TipoCambio inverso = tipoCambioDAO.buscarVigente(monedaDestino, monedaOrigen, fecha)
                .orElse(null);
        if (inverso != null && inverso.getValor().compareTo(BigDecimal.ZERO) != 0) {
            return monto.divide(inverso.getValor(), 2, RoundingMode.HALF_UP);
        }
        throw new ReglaNegocioException("No hay tipo de cambio vigente de " + monedaOrigen + " a "
                + monedaDestino + " en la fecha " + fecha + ".");
    }

    /** Tipo de cambio vigente, o {@code null} si no hay registrado. */
    public TipoCambio vigente(String monedaOrigen, String monedaDestino, LocalDate fecha) {
        return tipoCambioDAO.buscarVigente(monedaOrigen, monedaDestino, fecha).orElse(null);
    }
}
