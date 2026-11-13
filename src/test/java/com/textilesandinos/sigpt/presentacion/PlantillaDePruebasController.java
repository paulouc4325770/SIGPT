package com.textilesandinos.sigpt.presentacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.textilesandinos.sigpt.entidades.abastecimiento.CotizacionProveedor;
import com.textilesandinos.sigpt.entidades.abastecimiento.Insumo;
import com.textilesandinos.sigpt.entidades.abastecimiento.MovimientoKardex;
import com.textilesandinos.sigpt.entidades.abastecimiento.OrdenCompra;
import com.textilesandinos.sigpt.entidades.abastecimiento.Proveedor;
import com.textilesandinos.sigpt.entidades.abastecimiento.SolicitudCotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.Adelanto;
import com.textilesandinos.sigpt.entidades.pedidos.Cliente;
import com.textilesandinos.sigpt.entidades.pedidos.Cotizacion;
import com.textilesandinos.sigpt.entidades.pedidos.FichaTecnica;
import com.textilesandinos.sigpt.entidades.pedidos.Pedido;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoDetalle;
import com.textilesandinos.sigpt.entidades.pedidos.PedidoFila;
import com.textilesandinos.sigpt.entidades.pedidos.ResumenPedidos;
import com.textilesandinos.sigpt.entidades.planta.Asignacion;
import com.textilesandinos.sigpt.entidades.planta.Lote;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.entidades.planta.Operario;
import com.textilesandinos.sigpt.entidades.planta.OrdenFila;
import com.textilesandinos.sigpt.entidades.planta.ParteDiarioProduccion;
import com.textilesandinos.sigpt.entidades.planta.RegistroAvance;
import com.textilesandinos.sigpt.entidades.planta.Turno;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGenerado;
import com.textilesandinos.sigpt.entidades.reportes.ReporteGerencial;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Perfil;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.abastecimiento.CotizacionProveedorFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.InsumoFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.KardexFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.OrdenCompraFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.ProveedorFormulario;
import com.textilesandinos.sigpt.negocio.abastecimiento.SolicitudCotizacionFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.AdelantoFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.CotizacionFormulario;
import com.textilesandinos.sigpt.negocio.pedidos.PedidoFormulario;
import com.textilesandinos.sigpt.negocio.planta.AsignacionFormulario;
import com.textilesandinos.sigpt.negocio.planta.AvanceFormulario;
import com.textilesandinos.sigpt.negocio.planta.InspeccionFormulario;

/**
 * Controlador auxiliar SOLO para pruebas: renderiza cada plantilla con un modelo
 * de referencia, de modo que un error de sintaxis o de expresiones rotas se
 * detecte en el build y no al abrir la pantalla en el navegador.
 *
 * <p>Vive en {@code src/test} y por tanto no forma parte de la aplicacion.
 */
@Controller
public class PlantillaDePruebasController {

    @GetMapping("/prueba-plantilla/{*nombre}")
    public String renderizar(@PathVariable String nombre, Model modelo) {
        String plantilla = nombre.startsWith("/") ? nombre.substring(1) : nombre;
        Map<String, Object> atributos = atributos();
        Pedido entidad = (Pedido) atributos.get("pedidoEntidad");
        atributos.remove("pedidoEntidad");
        // El formulario de pedido trabaja con un PedidoFormulario; la ficha del
        // pedido, con la entidad Pedido.
        atributos.put("pedido", "pedidos/formulario".equals(plantilla)
                ? new PedidoFormulario()
                : entidad);
        atributos.forEach(modelo::addAttribute);
        return plantilla;
    }

    private Map<String, Object> atributos() {
        Map<String, Object> atributos = new LinkedHashMap<>();

        // --- seguridad ---
        Perfil perfil = new Perfil();
        perfil.setIdPerfil(1);
        perfil.setCodPerfil("JPROD");
        perfil.setNombrePerfil("Jefe de Produccion");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCodUsuario("jperez");
        usuario.setNombres("Jorge Perez");
        usuario.setArea("Gerencia de Produccion");
        usuario.setPerfil(perfil);

        Opcion opcion = new Opcion();
        opcion.setIdOpcion(7);
        opcion.setNombreOpcion("Bandeja de Pedidos");
        opcion.setUrlOpcion("/pedidos/bandeja");
        opcion.setNroOrden(1);

        atributos.put("usuarioActual", usuario);
        atributos.put("menu", Map.of("Pedidos", List.of(opcion)));
        atributos.put("nombreAplicacion", "SIGPT");
        atributos.put("titulo", "Prueba de plantilla");
        atributos.put("tituloReporte", "Avance de Pedidos");
        atributos.put("mensajeError", null);
        atributos.put("mensajeExito", null);
        atributos.put("mensajeAlerta", null);
        atributos.put("mensajeSinDatos", null);

        // --- pedidos ---
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setCodigo("CLI-001");
        cliente.setRazonSocial("Marca Corporativa Andina S.A.C.");
        cliente.setRucTaxId("20100011122");
        cliente.setPais("Peru");
        cliente.setEstado("ACTIVO");

        Pedido pedido = new Pedido();
        pedido.setIdPedido(1);
        pedido.setCodPedido("PED-2026-001");
        pedido.setIdCliente(1);
        pedido.setMetros(new BigDecimal("3500.00"));
        pedido.setMetrosConsolidados(new BigDecimal("1200.00"));
        pedido.setFechaEntrega(LocalDate.of(2026, 12, 7));
        pedido.setEstado("EN_PRODUCCION");

        FichaTecnica ficha = new FichaTecnica();
        ficha.setIdFicha(1);
        ficha.setIdPedido(1);
        ficha.setTipoHilado("Peinado 30/1");
        ficha.setGramajeM2(new BigDecimal("180.00"));
        ficha.setColorPantone("19-4023 TCX");
        ficha.setComposicion("100% Algodon Pima");
        ficha.setMetros(new BigDecimal("3500.00"));
        ficha.setToleranciaColor(new BigDecimal("2.00"));

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(1);
        cotizacion.setIdPedido(1);
        cotizacion.setMontoTotal(new BigDecimal("12500.00"));
        cotizacion.setMoneda("USD");
        cotizacion.setCostoInsumos(new BigDecimal("7000.00"));
        cotizacion.setHorasCiclo(new BigDecimal("96.00"));
        cotizacion.setFechaEmision(LocalDate.of(2026, 11, 12));
        cotizacion.setFechaVigencia(LocalDate.of(2026, 11, 27));
        cotizacion.setEstado("EMITIDA");

        Adelanto adelanto = new Adelanto();
        adelanto.setIdAdelanto(1);
        adelanto.setIdCotizacion(1);
        adelanto.setMonto(new BigDecimal("6000.00"));
        adelanto.setMoneda("USD");
        adelanto.setFechaAdelanto(LocalDate.of(2026, 11, 13));
        adelanto.setVerificado(true);
        adelanto.setReferencia("LC-00145");

        PedidoDetalle detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setCliente(cliente);
        detalle.setFichaTecnica(ficha);
        detalle.setCotizaciones(List.of(cotizacion));
        detalle.setAdelantos(List.of(adelanto));
        detalle.setMontoPactado(new BigDecimal("12500.00"));
        detalle.setAdelantoVerificado(new BigDecimal("6000.00"));
        detalle.setPorcentajeAdelanto(new BigDecimal("48.00"));

        PedidoFila fila = new PedidoFila();
        fila.setIdPedido(1);
        fila.setCodPedido("PED-2026-001");
        fila.setIdCliente(1);
        fila.setNombreCliente("Marca Corporativa Andina S.A.C.");
        fila.setTipoHilado("Peinado 30/1");
        fila.setGramajeM2(new BigDecimal("180.00"));
        fila.setColorPantone("19-4023 TCX");
        fila.setMetros(new BigDecimal("3500.00"));
        fila.setMetrosConsolidados(new BigDecimal("1200.00"));
        fila.setFechaEntrega(LocalDate.of(2026, 12, 7));
        fila.setEstado("EN_PRODUCCION");

        ResumenPedidos resumen = new ResumenPedidos();
        resumen.setTotal(3);
        resumen.setRegistrados(1);
        resumen.setCotizados(1);
        resumen.setEnProduccion(1);

        atributos.put("pedidos", List.of(fila));
        atributos.put("resumen", resumen);
        atributos.put("clientes", List.of(cliente));
        atributos.put("cliente", cliente);
        atributos.put("filtroCodigo", "");
        atributos.put("filtroEstado", "");
        atributos.put("busqueda", "");
        atributos.put("pedidoEntidad", pedido);
        atributos.put("detalle", detalle);
        atributos.put("ficha", ficha);
        atributos.put("cotizaciones", List.of(cotizacion));
        atributos.put("cotizacion", new CotizacionFormulario());
        atributos.put("adelanto", new AdelantoFormulario());

        // --- planta ---
        OrdenFila orden = new OrdenFila();
        orden.setIdOrden(1);
        orden.setCodOrden("OP-2026-0001");
        orden.setIdPedido(1);
        orden.setCodPedido("PED-2026-001");
        orden.setNombreCliente("Marca Corporativa Andina S.A.C.");
        orden.setMetrosTotales(new BigDecimal("3500.00"));
        orden.setMetrosConsolidados(new BigDecimal("1200.00"));
        orden.setEstado("EN_PRODUCCION");

        Operario operario = new Operario();
        operario.setIdOperario(1);
        operario.setCodigo("OP-001");
        operario.setNombres("Ana Quispe Mamani");
        operario.setEspecialidad("TELAR");
        operario.setEstado("ACTIVO");

        Maquina maquina = new Maquina();
        maquina.setIdMaquina(1);
        maquina.setCodigo("TC-001");
        maquina.setNombre("Telar circular 1");
        maquina.setTipo("TELAR_CIRCULAR");
        maquina.setEstado("LIBRE");
        maquina.setVelocidadTeoricaMtsHora(new BigDecimal("45.00"));
        maquina.setHorasPorTurno(new BigDecimal("8.00"));

        Turno turno = new Turno();
        turno.setIdTurno(1);
        turno.setCodigo("MANANA");
        turno.setNombre("Manana");
        turno.setHoraInicio(LocalTime.of(6, 0));
        turno.setHoraFin(LocalTime.of(14, 0));
        turno.setHoras(new BigDecimal("8.00"));

        Asignacion asignacion = new Asignacion();
        asignacion.setIdAsignacion(1);
        asignacion.setIdOperario(1);
        asignacion.setIdMaquina(1);
        asignacion.setIdTurno(1);
        asignacion.setFecha(LocalDate.of(2026, 11, 12));
        asignacion.setIdOrdenProduccion(1);
        asignacion.setMetaMetros(new BigDecimal("3000.00"));
        asignacion.setHorasAsignadas(new BigDecimal("8.00"));
        asignacion.setEstado("ASIGNADA");

        RegistroAvance avance = new RegistroAvance();
        avance.setIdAvance(1);
        avance.setIdAsignacion(1);
        avance.setIdOrdenProduccion(1);
        avance.setFecha(LocalDate.of(2026, 11, 12));
        avance.setMetrosProducidos(new BigDecimal("300.00"));
        avance.setMetrosMerma(new BigDecimal("12.00"));
        avance.setVarianzaPct(new BigDecimal("-16.67"));
        avance.setEstadoCalidad("PENDIENTE");

        Lote lote = new Lote();
        lote.setIdLote(1);
        lote.setCodigo("LOTE-2026-0001");
        lote.setIdOrdenProduccion(1);
        lote.setIdAsignacion(1);
        lote.setMetros(new BigDecimal("300.00"));
        lote.setEstado("PENDIENTE_INSPECCION");

        ParteDiarioProduccion parte = new ParteDiarioProduccion();
        parte.setIdParte(1);
        parte.setIdOrdenProduccion(1);
        parte.setFecha(LocalDate.of(2026, 11, 12));
        parte.setHorasHombre(new BigDecimal("24.00"));
        parte.setHorasMaquina(new BigDecimal("24.00"));
        parte.setMetros(new BigDecimal("3500.00"));
        parte.setObservaciones("Cierre de lote");

        atributos.put("ordenes", List.of(orden));
        atributos.put("ordenSeleccionada", orden);
        atributos.put("asignaciones", List.of(asignacion));
        atributos.put("asignacion", asignacion);
        atributos.put("operarios", List.of(operario));
        atributos.put("maquinas", List.of(maquina));
        atributos.put("turnos", List.of(turno));
        atributos.put("asignacionForm", new AsignacionFormulario());
        atributos.put("avance", new AvanceFormulario());
        atributos.put("avances", List.of(avance));
        atributos.put("lotes", List.of(lote));
        atributos.put("lotesOrden", List.of(lote));
        atributos.put("inspeccion", new InspeccionFormulario());
        atributos.put("partes", List.of(parte));
        atributos.put("fechaAsignacion", LocalDate.of(2026, 11, 12));
        atributos.put("fechaInspeccion", LocalDate.of(2026, 11, 12));
        atributos.put("codOperario", "OP-001");
        atributos.put("codMaquina", "TC-001");
        atributos.put("nombreTurno", "Manana");
        atributos.put("metrosMeta", new BigDecimal("3000.00"));
        atributos.put("metrosConsolidados", new BigDecimal("1200.00"));
        atributos.put("porcentajeAvance", 34);
        atributos.put("varianzaPct", new BigDecimal("-16.67"));

        // --- abastecimiento ---
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1);
        proveedor.setCodigo("PRV-001");
        proveedor.setRazonSocial("Cooperativa Alpaca Sur");
        proveedor.setRuc("20111122233");
        proveedor.setTipo("GANADERA");
        proveedor.setEstado("ACTIVO");

        Insumo insumo = new Insumo();
        insumo.setIdInsumo(1);
        insumo.setCodigo("INS-FIB-001");
        insumo.setIdProveedor(1);
        insumo.setNombre("Lana de alpaca en fardo");
        insumo.setTipo("FIBRA");
        insumo.setUnidadMedida("KG");
        insumo.setStockMinimo(new BigDecimal("500.00"));
        insumo.setStockActual(new BigDecimal("1200.00"));
        insumo.setPrecioUnitario(new BigDecimal("18.5000"));
        insumo.setEstado("ACTIVO");

        SolicitudCotizacion solicitud = new SolicitudCotizacion();
        solicitud.setIdSolicitud(1);
        solicitud.setCodigo("RFQ-2026-001");
        solicitud.setIdOrdenProduccion(1);
        solicitud.setFechaEmision(LocalDate.of(2026, 11, 12));
        solicitud.setEstado("EMITIDA");

        CotizacionProveedor cotizacionProveedor = new CotizacionProveedor();
        cotizacionProveedor.setIdCotizacionProveedor(1);
        cotizacionProveedor.setIdSolicitud(1);
        cotizacionProveedor.setIdProveedor(1);
        cotizacionProveedor.setPrecioUnitario(new BigDecimal("18.5000"));
        cotizacionProveedor.setUnidad("KG");
        cotizacionProveedor.setTiempoEntregaDias(7);
        cotizacionProveedor.setEstado("RECIBIDA");

        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setIdOrdenCompra(1);
        ordenCompra.setCodigo("OC-2026-001");
        ordenCompra.setIdProveedor(1);
        ordenCompra.setMontoTotal(new BigDecimal("12500.00"));
        ordenCompra.setMoneda("USD");
        ordenCompra.setFechaEmision(LocalDate.of(2026, 11, 12));
        ordenCompra.setFechaEntregaEsperada(LocalDate.of(2026, 11, 20));
        ordenCompra.setEstado("EMITIDA");

        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setIdMovimiento(1);
        movimiento.setIdInsumo(1);
        movimiento.setTipoMovimiento("INGRESO");
        movimiento.setCantidad(new BigDecimal("120.00"));
        movimiento.setPrecioUnitario(new BigDecimal("18.5000"));
        movimiento.setFecha(LocalDate.of(2026, 11, 12));

        atributos.put("proveedores", List.of(proveedor));
        atributos.put("proveedor", new ProveedorFormulario());
        atributos.put("insumos", List.of(insumo));
        atributos.put("insumo", new InsumoFormulario());
        atributos.put("alertasStock", List.of());
        atributos.put("solicitudes", List.of(solicitud));
        atributos.put("solicitudForm", new SolicitudCotizacionFormulario());
        atributos.put("cotizacionForm", new CotizacionProveedorFormulario());
        atributos.put("cotizacionesProveedor", List.of(cotizacionProveedor));
        atributos.put("solicitudSeleccionada", 1);
        atributos.put("ordenesCompra", List.of(ordenCompra));
        atributos.put("ordenForm", new OrdenCompraFormulario());
        atributos.put("movimientos", List.of(movimiento));
        atributos.put("kardexForm", new KardexFormulario());

        // --- reportes ---
        ReporteGerencial reporte = new ReporteGerencial();
        reporte.setIdReporte(1);
        reporte.setCodigo("AVANCE_PEDIDOS");
        reporte.setNombre("Avance de Pedidos");

        ReporteGenerado generado = new ReporteGenerado();
        generado.setIdReporteGenerado(1L);
        generado.setIdReporte(1);
        generado.setCodUsuario("jperez");
        generado.setFiltros("cliente=todos");
        generado.setFormatoSalida("PANTALLA");
        generado.setFechaGeneracion(LocalDateTime.of(2026, 11, 12, 10, 30));

        atributos.put("reportes", List.of(reporte));
        atributos.put("generados", List.of(generado));
        atributos.put("codigoReporte", "AVANCE_PEDIDOS");
        atributos.put("rutaReporte", "/reportes/avance-pedidos");
        atributos.put("formatos", List.of());
        atributos.put("columnas", List.of("Codigo", "Cliente", "Metros"));
        atributos.put("filas", List.of(List.of("PED-2026-001", "Marca Andina", "3500")));
        atributos.put("indicadores", List.of());

        return atributos;
    }
}
