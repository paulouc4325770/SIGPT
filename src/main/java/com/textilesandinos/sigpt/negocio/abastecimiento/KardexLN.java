package com.textilesandinos.sigpt.negocio.abastecimiento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.abastecimiento.InsumoDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.MovimientoKardexDAO;
import com.textilesandinos.sigpt.datos.abastecimiento.OrdenProduccionInsumoDAO;
import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenProduccionInsumo;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Kardex de insumos: ingresos, salidas y ajustes (EF-1001-5).
 *
 * <p>El stock del insumo se actualiza en la misma transaccion que el
 * movimiento, de modo que nunca queda desincronizado. La regla RD4 compara el
 * consumo real de una orden de produccion con el consumo teorico planificado y
 * avisa cuando la varianza supera el 15 %.
 */
@Service
public class KardexLN {

    private final MovimientoKardexDAO kardexDAO;
    private final InsumoDAO insumoDAO;
    private final OrdenProduccionInsumoDAO consumoTeoricoDAO;
    private final ParametrosNegocio parametros;
    private final AuditoriaHelper auditoria;

    public KardexLN(MovimientoKardexDAO kardexDAO, InsumoDAO insumoDAO,
            OrdenProduccionInsumoDAO consumoTeoricoDAO, ParametrosNegocio parametros,
            AuditoriaHelper auditoria) {
        this.kardexDAO = kardexDAO;
        this.insumoDAO = insumoDAO;
        this.consumoTeoricoDAO = consumoTeoricoDAO;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    /** Lista todos los movimientos del kardex. */
    public List<MovimientoKardex> listarMovimientos() {
        return kardexDAO.listarTodos();
    }

    /** Movimientos de un insumo. */
    public List<MovimientoKardex> listarPorInsumo(int idInsumo) {
        return kardexDAO.listarPorInsumo(idInsumo);
    }

    /**
     * Registra un movimiento de kardex.
     *
     * @return el movimiento registrado
     */
    public MovimientoKardex registrar(KardexFormulario formulario, String codUsuario) {
        if (formulario == null || formulario.getIdInsumo() == null) {
            throw new ReglaNegocioException("Debe seleccionar el insumo del movimiento.");
        }
        if (formulario.getCantidad() == null
                || formulario.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad del movimiento debe ser mayor que cero.");
        }
        if (!List.of("INGRESO", "SALIDA", "AJUSTE").contains(formulario.getTipoMovimiento())) {
            throw new ReglaNegocioException("El tipo de movimiento " + formulario.getTipoMovimiento()
                    + " no es valido. Use INGRESO, SALIDA o AJUSTE.");
        }
        Insumo insumo = insumoDAO.buscarPorId(formulario.getIdInsumo())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se encontraron datos para los criterios seleccionados."));

        if ("SALIDA".equals(formulario.getTipoMovimiento())) {
            BigDecimal stock = insumo.getStockActual() == null ? BigDecimal.ZERO
                    : insumo.getStockActual();
            if (stock.compareTo(formulario.getCantidad()) < 0) {
                throw new ReglaNegocioException("Stock insuficiente de " + insumo.getNombre()
                        + ": hay " + stock.toPlainString() + " " + insumo.getUnidadMedida()
                        + " y se solicitan " + formulario.getCantidad().toPlainString() + ".");
            }
        }

        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setIdInsumo(formulario.getIdInsumo());
        movimiento.setTipoMovimiento(formulario.getTipoMovimiento());
        movimiento.setCantidad(formulario.getCantidad());
        movimiento.setPrecioUnitario(formulario.getPrecioUnitario() == null ? BigDecimal.ZERO
                : formulario.getPrecioUnitario());
        movimiento.setIdOrdenCompra(formulario.getIdOrdenCompra());
        movimiento.setIdOrdenProduccion(formulario.getIdOrdenProduccion());
        movimiento.setFecha(formulario.getFecha() == null ? LocalDate.now() : formulario.getFecha());
        movimiento.setObservaciones(formulario.getObservaciones());

        int idMovimiento = kardexDAO.registrar(movimiento, codUsuario);
        movimiento.setIdMovimiento(idMovimiento);

        auditoria.registrar(codUsuario, "ABASTECIMIENTO", "KARDEX_" + formulario.getTipoMovimiento(),
                "movimiento_kardex", String.valueOf(idMovimiento), insumo.getNombre() + " | "
                        + formulario.getCantidad().toPlainString() + " "
                        + insumo.getUnidadMedida());
        return movimiento;
    }

    /**
     * Varianza del consumo de una orden de produccion frente a lo teorico
     * (regla RD4).
     *
     * @param idOrdenProduccion orden a la que se le calcula la varianza
     * @return la varianza en porcentaje; {@code null} si no hay consumo teorico
     */
    public BigDecimal varianzaConsumo(int idOrdenProduccion) {
        List<OrdenProduccionInsumo> teoricos =
                consumoTeoricoDAO.listarPorOrden(idOrdenProduccion);
        if (teoricos.isEmpty()) {
            return null;
        }
        BigDecimal limite = parametros.decimal("VARIANZA_MAXIMA_CONSUMO_PORCENTAJE");
        BigDecimal mayorVarianza = null;
        for (OrdenProduccionInsumo teorico : teoricos) {
            BigDecimal real = BigDecimal.ZERO;
            for (MovimientoKardex movimiento : kardexDAO.listarPorOrden(idOrdenProduccion)) {
                if (movimiento.getIdInsumo().equals(teorico.getIdInsumo())
                        && "SALIDA".equals(movimiento.getTipoMovimiento())) {
                    real = real.add(movimiento.getCantidad());
                }
            }
            BigDecimal teoricoCantidad = teorico.getCantidadTeorica() == null ? BigDecimal.ZERO
                    : teorico.getCantidadTeorica();
            if (teoricoCantidad.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal varianza = real.subtract(teoricoCantidad)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(teoricoCantidad, 2, RoundingMode.HALF_UP);
            if (mayorVarianza == null || varianza.compareTo(mayorVarianza) > 0) {
                mayorVarianza = varianza;
            }
            if (varianza.compareTo(limite) > 0) {
                return varianza;
            }
        }
        return mayorVarianza;
    }

    /** Indica si la varianza de consumo supera el umbral de la regla RD4. */
    public boolean alertaVarianzaConsumo(int idOrdenProduccion) {
        BigDecimal varianza = varianzaConsumo(idOrdenProduccion);
        if (varianza == null) {
            return false;
        }
        return varianza.compareTo(parametros.decimal("VARIANZA_MAXIMA_CONSUMO_PORCENTAJE")) > 0;
    }
}
