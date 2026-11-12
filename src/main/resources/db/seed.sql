-- ============================================================================
--  SIGPT - Datos semilla (seed)
--
--  Perfiles, usuarios de prueba, menu jerarquico por perfil, parametros de
--  negocio, catalogos y datos de demostracion.
--
--  IMPORTANTE
--  Las contrasenas se almacenan como hash PBKDF2-HMAC-SHA256 con salt por
--  usuario, 210 000 iteraciones y 32 bytes de clave (OWASP).
--  Formato: iteraciones:base64(salt):base64(hash)
--
--  Contrasena de todos los usuarios de prueba:  Sigpt2026*
--  (se documenta en el README; es solo para demostracion del curso)
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM reporte_generado;
DELETE FROM formato_salida;
DELETE FROM reporte_gerencial;
DELETE FROM parte_diario_produccion;
DELETE FROM incidencia;
DELETE FROM inspeccion_calidad;
DELETE FROM lote_insumo;
DELETE FROM lote;
DELETE FROM registro_avance;
DELETE FROM asignacion;
DELETE FROM turno;
DELETE FROM maquina;
DELETE FROM operario;
DELETE FROM movimiento_kardex;
DELETE FROM orden_compra_detalle;
DELETE FROM orden_compra;
DELETE FROM cotizacion_proveedor;
DELETE FROM solicitud_cotizacion;
DELETE FROM insumo;
DELETE FROM proveedor;
DELETE FROM orden_produccion_insumo;
DELETE FROM orden_produccion;
DELETE FROM adelanto;
DELETE FROM cotizacion;
DELETE FROM ficha_tecnica;
DELETE FROM pedido;
DELETE FROM cliente;
DELETE FROM registro_auditoria;
DELETE FROM perfil_opcion;
DELETE FROM opcion;
DELETE FROM usuario;
DELETE FROM perfil;
DELETE FROM tipo_cambio;
DELETE FROM moneda;
DELETE FROM parametro;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
--  MONEDAS Y TIPO DE CAMBIO (CHG-02)
-- ---------------------------------------------------------------------------
INSERT INTO moneda (codigo, nombre, simbolo) VALUES
  ('PEN', 'Sol peruano', 'S/'),
  ('USD', 'Dolar estadounidense', '$');

INSERT INTO tipo_cambio (moneda_origen, moneda_destino, valor, fecha_vigencia) VALUES
  ('PEN', 'USD', 0.26850000, CURDATE()),
  ('PEN', 'USD', 0.27100000, DATE_ADD(CURDATE(), INTERVAL 30 DAY));

-- ---------------------------------------------------------------------------
--  PARAMETROS DE NEGOCIO
--  Los umbrales NO estan en el codigo: se leen de aqui (Singleton ParametrosNegocio)
-- ---------------------------------------------------------------------------
INSERT INTO parametro (codigo, nombre, valor, tipo_dato, descripcion) VALUES
  ('PEDIDO_MINIMO_METROS', 'Metraje minimo de pedido', '1000', 'DECIMAL', 'RD1: pedido minimo de manufactura y tenido'),
  ('ADELANTO_MINIMO_PORCENTAJE', 'Adelanto minimo para iniciar produccion', '40', 'DECIMAL', 'RD2: porcentaje minimo del valor pactado'),
  ('VIGENCIA_COTIZACION_DIAS', 'Vigencia de la cotizacion', '15', 'ENTERO', 'La cotizacion expira si no hay adelanto en este plazo'),
  ('JORNADA_MAXIMA_HORAS', 'Jornada maxima en turnos consecutivos', '16', 'DECIMAL', 'RD3: maximo de horas continuas por operario'),
  ('VARIANZA_MAXIMA_AVANCE_PORCENTAJE', 'Varianza maxima de avance', '15', 'DECIMAL', 'R-Varianza: varianza de metros frente a la velocidad teorica del telar'),
  ('VARIANZA_MAXIMA_CONSUMO_PORCENTAJE', 'Varianza maxima de consumo', '15', 'DECIMAL', 'RD4: varianza de consumo de insumos frente al consumo teorico'),
  ('INTENTOS_MAXIMOS_LOGIN', 'Intentos maximos de inicio de sesion', '3', 'ENTERO', 'Tras N intentos fallidos la cuenta se bloquea temporalmente'),
  ('MINUTOS_BLOQUEO_CUENTA', 'Minutos de bloqueo de una cuenta', '15', 'ENTERO', 'Duracion del bloqueo temporal tras superar los intentos maximos'),
  ('SESION_MINUTOS_INACTIVIDAD', 'Minutos de inactividad para cerrar la sesion', '15', 'ENTERO', 'La sesion expira automaticamente por inactividad'),
  ('DISPONIBILIDAD_OBJETIVO_PORCENTAJE', 'Disponibilidad objetivo en horario de operacion', '99.5', 'DECIMAL', 'RC3'),
  ('RNF01_TIEMPO_RESPUESTA_SEGUNDOS', 'Tiempo de respuesta maximo en consultas de planta', '2', 'ENTERO', 'RC1'),
  ('MONEDA_PREDETERMINADA', 'Moneda predeterminada de cotizacion', 'USD', 'TEXTO', 'Textiles Andinos exporta; la cotizacion se emite en USD'),
  ('TOLERANCIA_COLOR_DEFAULT', 'Tolerancia de varianza de color por defecto', '2.00', 'DECIMAL', 'RD7: se usa si la ficha tecnica no define tolerancia propia');

-- ---------------------------------------------------------------------------
--  PERFILES (los 8 actores del sistema)
-- ---------------------------------------------------------------------------
INSERT INTO perfil (id_perfil, cod_perfil, nombre_perfil, descripcion) VALUES
  (1, 'JPROD',      'Jefe de Produccion',              'Registra y aprueba pedidos, evalua viabilidad, genera ordenes de produccion'),
  (2, 'JLOG',       'Jefe de Logistica de Operaciones','Gestiona proveedores, RFQ, ordenes de compra y stock de insumos'),
  (3, 'SUPERV',     'Supervisor de Tareas',            'Asigna operarios y maquinas por lote y turno; valida calidad y aprueba avance'),
  (4, 'OPERARIO',   'Operario (Telar / Teñidor)',      'Registra metros producidos por turno y reporta mermas o incidencias'),
  (5, 'ALMACEN',    'Encargado de Almacen / Despacho', 'Registra ingreso al Kardex y despacho de lotes terminados'),
  (6, 'CLIENTE',    'Cliente',                         'Envia ficha tecnica y consulta el estado de avance'),
  (7, 'PROVEEDOR',  'Proveedor',                       'Recibe solicitudes de cotizacion y ordenes de compra'),
  (8, 'GERENCIA',   'Gerencia General',                'Consulta reportes consolidados en modo solo lectura');

-- ---------------------------------------------------------------------------
--  USUARIOS DE PRUEBA  (clave: Sigpt2026*)
-- ---------------------------------------------------------------------------
INSERT INTO usuario (id_usuario, cod_usuario, clave_hash, nombres, area, id_perfil) VALUES
  (1, 'jperez',    '210000:4k8vL8S52ernM70lim4TtQ==:Vzvzte2F768dkjuCeuIIBCdIfyoHN1KjyVlyBUHj2Qo=', 'Jorge Perez Quispe',    'Gerencia de Produccion',    1),
  (2, 'jlogistica','210000:7VaZTXIgPi0eLvJ2vuD84Q==:3PEM0I77NK65s8gNht06eOIGwTJOksMAlj1UOJIJJMc=', 'Lucia Ramos Vega',       'Gerencia de Logistica',     2),
  (3, 'supervisor','210000:3QFLacLmePj3a1APLV2pGQ==:XIa2fOaa7whIS8ENTZ3KHUiOCEQLTiFlC0WINwCWRq8=', 'Marco Huaman Torres',   'Piso de Planta',            3),
  (4, 'operario',  '210000:1U8H5TO8HH6JqL5UAy7RVQ==:c3Z5vDd2ZausbAwpfsxrT7VQQbN1U3QtE5cutTqAa9M=', 'Ana Quispe Mamani',     'Piso de Planta',            4),
  (5, 'almacen',   '210000:PPPjrHPZsH7ai/boHrGBYw==:no4p7mnYcnlksAREvA3P9+PGHhP/myrENOusN1+XjFU=', 'Carlos Flores Ruiz',    'Almacen Central',           5),
  (6, 'cliente',   '210000:Y2Lz+G2uOwo/TWgNzvzALw==:/YqlToBTIH6jDHngq0/RNkKoeFKdUos/v8sQZoAo7lU=', 'Maria Cotton Corp',      'Cliente Externo',           6),
  (7, 'proveedor', '210000:oAQi3YeQPqo5jRpKopJgKA==:SkMolumogVdz1wM+tBJuIgMsQC3xyvasQXVluXt9YmE=', 'Cooperativa Alpaca Sur','Proveedor Externo',         7),
  (8, 'gerencia',  '210000:pucsds40kgtbomrOrs1jOQ==:t6t6zDXPEUIrpEj5eezUStoaf6I1Bgf1FRbisdAiGbg=', 'Elena Rojas Medina',    'Gerencia General',          8);

-- ---------------------------------------------------------------------------
--  MENU: ARBOL DE OPCIONES  (grupo -> opcion)
--  Los grupos tienen url_opcion NULL; las hojas llevan Area/Controlador/Accion
-- ---------------------------------------------------------------------------
INSERT INTO opcion (id_opcion, nombre_opcion, url_opcion, ruta_imagen, nro_orden, id_opcion_ref) VALUES
  (30, 'General',         NULL,                        'fas fa-home',           0,  NULL),
  (31, 'Inicio',          '/inicio',                   'fas fa-house',          1,  30),
  (1,  'Seguridad',        NULL,                        'fas fa-shield-alt',      1,  NULL),
  (2,  'Iniciar Sesion',   '/seguridad/login',          'fas fa-sign-in-alt',     1,  1),
  (3,  'Usuarios',         '/seguridad/usuarios',       'fas fa-users',           2,  1),
  (4,  'Perfiles',         '/seguridad/perfiles',       'fas fa-user-tag',        3,  1),
  (5,  'Opciones de Menu', '/seguridad/opciones',       'fas fa-list-ul',         4,  1),
  (6,  'Pedidos',          NULL,                        'fas fa-file-invoice',    2,  NULL),
  (7,  'Bandeja de Pedidos','/pedidos/bandeja',          'fas fa-inbox',           1,  6),
  (8,  'Nuevo Pedido',     '/pedidos/nuevo',            'fas fa-plus-circle',     2,  6),
  (9,  'Cotizaciones',     '/pedidos/cotizaciones',     'fas fa-file-invoice-dollar', 3, 6),
  (10, 'Bandeja de Clientes','/pedidos/clientes',        'fas fa-address-book',    4,  6),
  (11, 'Consultar Estado de Pedido', '/pedidos/estado',   'fas fa-search',          5,  6),
  (12, 'Abastecimiento',   NULL,                        'fas fa-boxes',           3,  NULL),
  (13, 'Proveedores',      '/abastecimiento/proveedores','fas fa-industry',       1,  12),
  (14, 'Insumos',          '/abastecimiento/insumos',   'fas fa-cubes',           2,  12),
  (15, 'Solicitudes de Cotizacion', '/abastecimiento/rfq','fas fa-envelope-open-text', 3, 12),
  (16, 'Ordenes de Compra','/abastecimiento/ordenes-compra','fas fa-shopping-cart', 4, 12),
  (17, 'Kardex',           '/abastecimiento/kardex',    'fas fa-clipboard-list',  5,  12),
  (18, 'Planta',           NULL,                        'fas fa-industry',        4,  NULL),
  (19, 'Asignar Operarios y Maquinaria', '/planta/asignacion','fas fa-users-cog',    1,  18),
  (20, 'Registrar Avance', '/planta/avance',            'fas fa-chart-line',      2,  18),
  (21, 'Control de Calidad','/planta/calidad',          'fas fa-check-double',    3,  18),
  (22, 'Incidencias',      '/planta/incidencias',       'fas fa-exclamation-triangle', 4, 18),
  (23, 'Parte Diario de Produccion', '/planta/parte-diario','fas fa-clipboard-check', 5, 18),
  (24, 'Reportes',         NULL,                        'fas fa-chart-pie',       5,  NULL),
  (25, 'Avance de Pedidos','/reportes/avance-pedidos',  'fas fa-chart-bar',       1,  24),
  (26, 'Productividad',    '/reportes/productividad',   'fas fa-chart-line',      2,  24),
  (27, 'Consumo de Insumos y Merma','/reportes/consumo-merma','fas fa-chart-area', 3, 24),
  (28, 'Eficiencia Industrial','/reportes/eficiencia',  'fas fa-chart-pie',       4,  24),
  (29, 'Historial de Reportes','/reportes/historial',    'fas fa-history',         5,  24);

-- ---------------------------------------------------------------------------
--  PERFIL - OPCION  (Matriz de Accesos, Anexo 2 de la Especificacion)
--  CHG-04: "Generar Reportes Gerenciales" queda en Jefe de Produccion y
--  Gerencia General (EF-1001-8). El Supervisor NO accede a reportes.
-- ---------------------------------------------------------------------------
INSERT INTO perfil_opcion (id_perfil, id_opcion) VALUES
  -- Inicio: disponible para todos los perfiles
  (1, 30), (1, 31), (2, 30), (2, 31), (3, 30), (3, 31), (4, 30), (4, 31),
  (5, 30), (5, 31), (6, 30), (6, 31), (7, 30), (7, 31), (8, 30), (8, 31),
  -- Jefe de Produccion
  (1, 1), (1, 3), (1, 4), (1, 5),
  (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
  (1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29),
  -- Jefe de Logistica
  (2, 1), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17),
  -- Supervisor de Tareas
  (3, 1), (3, 18), (3, 19), (3, 20), (3, 21), (3, 22), (3, 23),
  -- Operario: el grupo "Planta" tambien se asigna, no solo sus hojas
  (4, 1), (4, 18), (4, 20),
  -- Encargado de Almacen
  (5, 1), (5, 12), (5, 14), (5, 17),
  -- Cliente
  (6, 1), (6, 6), (6, 11),
  -- Proveedor
  (7, 1), (7, 12), (7, 15), (7, 16),
  -- Gerencia General
  (8, 1), (8, 24), (8, 25), (8, 26), (8, 27), (8, 28), (8, 29);

-- ---------------------------------------------------------------------------
--  CATALOGOS DE REPORTES (EF-1001-8) Y FORMATOS (Estrategia)
-- ---------------------------------------------------------------------------
INSERT INTO reporte_gerencial (id_reporte, codigo, nombre, descripcion) VALUES
  (1, 'AVANCE_PEDIDOS',  'Avance de Pedidos',        'Por estado, por cliente y por periodo, con tablero de control semaforizado'),
  (2, 'PRODUCTIVIDAD',   'Productividad',             'Por operario, maquina y turno (metros/hora y horas-hombre)'),
  (3, 'CONSUMO_MERMA',   'Consumo de Insumos y Merma','Basado en el Kardex valorizado'),
  (4, 'EFICIENCIA_IND',  'Eficiencia Industrial',     'Contraste de horas-hombre y horas-maquina frente a la cotizacion original');

INSERT INTO formato_salida (id_formato, codigo, nombre, clase_implementacion) VALUES
  (1, 'PANTALLA', 'Visualizacion en pantalla', 'com.textilesandinos.sigpt.negocio.reportes.formato.FormatoPantalla'),
  (2, 'PDF',      'Documento PDF',              'com.textilesandinos.sigpt.negocio.reportes.formato.FormatoPdf'),
  (3, 'EXCEL',    'Hoja de calculo Excel',      'com.textilesandinos.sigpt.negocio.reportes.formato.FormatoExcel');

-- ---------------------------------------------------------------------------
--  TURNOS (mañana, tarde, amanecida)
-- ---------------------------------------------------------------------------
INSERT INTO turno (id_turno, codigo, nombre, hora_inicio, hora_fin, horas) VALUES
  (1, 'MANANA',    'Manana',    '06:00:00', '14:00:00', 8.00),
  (2, 'TARDE',     'Tarde',     '14:00:00', '22:00:00', 8.00),
  (3, 'AMANECIDA', 'Amanecida', '22:00:00', '06:00:00', 8.00);

-- ---------------------------------------------------------------------------
--  PROVEEDORES
-- ---------------------------------------------------------------------------
INSERT INTO proveedor (id_proveedor, codigo, razon_social, ruc, tipo, contacto, correo, telefono) VALUES
  (1, 'PRV-001', 'Cooperativa Alpaca Sur',  '20501234567', 'GANADERA',  'Rosa Mamani',   'ventas@alpacasur.pe',  '51-984112233'),
  (2, 'PRV-002', 'Agricola Pima del Valle', '20507654321', 'AGRICOLA',  'Jorge Salazar', 'compras@pimavalle.pe', '51-984445566'),
  (3, 'PRV-003', 'Quimicos Textiles Andinos','20509876543','QUIMICA',   'Luis Cordova',  'ventas@qtandinos.pe',  '51-984778899');

-- ---------------------------------------------------------------------------
--  INSUMOS  (los quimicos y tintes exigen MSDS: RD5)
-- ---------------------------------------------------------------------------
INSERT INTO insumo (id_insumo, codigo, id_proveedor, nombre, tipo, unidad_medida, stock_minimo, stock_actual, precio_unitario, ruta_msds) VALUES
  (1,  'INS-FIB-001', 1, 'Lana de alpaca en fardo',      'FIBRA',   'KG',   500.00, 1200.00, 18.5000, NULL),
  (2,  'INS-FIB-002', 2, 'Algodon pima fibra corta',    'FIBRA',   'KG',   800.00, 2400.00,  6.8000, NULL),
  (3,  'INS-FIB-003', 2, 'Hilo de urdimbre 30/1',       'FIBRA',   'KG',   300.00,  650.00,  9.2000, NULL),
  (4,  'INS-TIN-001', 3, 'Colorante anilina azul 2B',   'TINTE',   'KG',    50.00,  120.00, 24.0000, '/msds/INS-TIN-001.pdf'),
  (5,  'INS-TIN-002', 3, 'Colorante reactivo negro',    'TINTE',   'KG',    40.00,   95.00, 21.5000, '/msds/INS-TIN-002.pdf'),
  (6,  'INS-QUI-001', 3, 'Fijador quimico',             'QUIMICO', 'KG',    60.00,   80.00, 15.7000, '/msds/INS-QUI-001.pdf'),
  (7,  'INS-QUI-002', 3, 'Suavizante textil',           'QUIMICO', 'L',    100.00,  250.00,  7.3000, '/msds/INS-QUI-002.pdf'),
  (8,  'INS-CAJ-001', 2, 'Caja para rollo de tela',     'CAJA',    'UND',  200.00,  450.00,  2.9000, NULL);

-- ---------------------------------------------------------------------------
--  MAQUINAS  (velocidad teorica: base de R-Varianza)
-- ---------------------------------------------------------------------------
INSERT INTO maquina (id_maquina, codigo, nombre, tipo, estado, velocidad_teorica_mts_hora, horas_por_turno) VALUES
  (1, 'TC-001', 'Telar circular 1',    'TELAR_CIRCULAR', 'LIBRE',   45.00, 8.00),
  (2, 'TC-002', 'Telar circular 2',    'TELAR_CIRCULAR', 'LIBRE',   45.00, 8.00),
  (3, 'TC-003', 'Telar rectilineo 1',  'TELAR_RECTILINEO','LIBRE',  60.00, 8.00),
  (4, 'TC-004', 'Telar rectilineo 2',  'TELAR_RECTILINEO','LIBRE',  60.00, 8.00),
  (5, 'UR-001', 'Urdidora 1',          'URDIDORA',       'LIBRE',   80.00, 8.00),
  (6, 'TN-001', 'Tintoreria 1',        'TINTORERIA',     'LIBRE',   35.00, 8.00),
  (7, 'TC-005', 'Telar circular 3',    'TELAR_CIRCULAR', 'BACKUP',  45.00, 8.00),
  (8, 'TC-006', 'Telar circular 4',    'TELAR_CIRCULAR', 'MANTENIMIENTO', 45.00, 8.00);

-- ---------------------------------------------------------------------------
--  OPERARIOS  (asociados a un usuario de acceso, no heredados)
-- ---------------------------------------------------------------------------
INSERT INTO operario (id_operario, codigo, nombres, especialidad, id_usuario) VALUES
  (1, 'OP-001', 'Ana Quispe Mamani',   'TELAR',   4),
  (2, 'OP-002', 'Pedro Rojas Huaman',  'TELAR',   NULL),
  (3, 'OP-003', 'Silvia Marin Castillo','TELAR',   NULL),
  (4, 'OP-004', 'Diego Alvarez Paz',   'TENIDOR', NULL),
  (5, 'OP-005', 'Rosa Yupanqui Diaz',  'TENIDOR', NULL);

-- ---------------------------------------------------------------------------
--  CLIENTES
-- ---------------------------------------------------------------------------
INSERT INTO cliente (id_cliente, codigo, razon_social, ruc_tax_id, pais, contacto, correo, telefono) VALUES
  (1, 'CLI-001', 'Marca Corporativa Andina S.A.C.', '20100011122', 'Peru',     'Claudia Nores',   'compras@marcaandina.pe', '51-211-1100'),
  (2, 'CLI-002', 'Diseños Internacionales Ltda.',    '20500033344', 'España',   'Javier Solana',  'textil@disenosintl.es',  '34-911-2233'),
  (3, 'CLI-003', 'Retail Fashion Group Inc.',       'US-88-22110','Estados Unidos','Nancy Brooks','sourcing@retailfg.com','1-212-4455');

-- ---------------------------------------------------------------------------
--  PEDIDOS Y FICHA TECNICA (compone)
-- ---------------------------------------------------------------------------
INSERT INTO pedido (id_pedido, cod_pedido, id_cliente, metros, fecha_entrega, estado) VALUES
  (1, 'PED-2026-001', 1, 3500.00, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'REGISTRADO'),
  (2, 'PED-2026-002', 2, 12000.00, DATE_ADD(CURDATE(), INTERVAL 40 DAY), 'REGISTRADO'),
  (3, 'PED-2026-003', 3,  800.00, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'REGISTRADO');

INSERT INTO ficha_tecnica (id_ficha, id_pedido, tipo_hilado, gramaje_m2, color_pantone, composicion, metros, tolerancia_color) VALUES
  (1, 1, 'Peinado 30/1',  180.00, '19-4023 TCX', '100% Algodon Pima',      3500.00, 2.00),
  (2, 2, 'Peinado 40/1',  220.00, '19-4050 TCX', '70% Alpaca / 30% Pima', 12000.00, 1.50),
  (3, 3, 'Open end 20/1', 140.00, '19-4006 TCX', '100% Algodon Pima',       800.00, 2.00);

-- ---------------------------------------------------------------------------
--  COTIZACION, ADELANTO Y ORDEN DE PRODUCCION
--  PED-2026-002 ya tiene el 50% de adelanto verificado: cumple RD2 y su
--  orden interna esta habilitada. PED-2026-001 esta cotizada sin adelanto.
-- ---------------------------------------------------------------------------
INSERT INTO cotizacion (id_cotizacion, id_pedido, monto_total, moneda, costo_insumos, horas_ciclo, fecha_emision, fecha_vigencia, estado) VALUES
  (1, 1, 18500.00, 'USD', 11200.00, 120.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'EMITIDA'),
  (2, 2, 52000.00, 'USD', 30000.00, 260.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'ACEPTADA'),
  (3, 3,  4200.00, 'USD',  2500.00,  40.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'EMITIDA');

INSERT INTO adelanto (id_adelanto, id_cotizacion, monto, moneda, fecha_adelanto, verificado, referencia) VALUES
  (1, 2, 26000.00, 'USD', CURDATE(), TRUE, 'LC-00045-2026');

INSERT INTO orden_produccion (id_orden, cod_orden, id_pedido, metros_totales, metros_consolidados, horas_maquina_previstas, horas_hombre_previstas, fecha_inicio, estado) VALUES
  (1, 'OP-2026-001', 2, 12000.00, 1200.00, 260.00, 240.00, CURDATE(), 'EN_PRODUCCION');

-- Plan teorico de consumo de la orden de produccion (base de RD4)
INSERT INTO orden_produccion_insumo (id_orden_produccion, id_insumo, cantidad_teorica) VALUES
  (1, 2, 1500.00),
  (1, 3, 800.00),
  (1, 4, 60.00),
  (1, 6, 90.00);

-- ---------------------------------------------------------------------------
--  ASIGNACION, AVANCE, LOTE E INSPECCION  (estado de planta para la demo)
-- ---------------------------------------------------------------------------
INSERT INTO asignacion (id_asignacion, id_operario, id_maquina, id_turno, fecha, id_orden_produccion, meta_metros, horas_asignadas, estado) VALUES
  (1, 1, 1, 2, CURDATE(), 1, 600.00, 8.00, 'EN_CURSO'),
  (2, 2, 3, 2, CURDATE(), 1, 480.00, 8.00, 'EN_CURSO');

INSERT INTO registro_avance (id_avance, id_asignacion, id_orden_produccion, id_operario, id_maquina, id_turno, fecha, metros_producidos, metros_merma, varianza_pct, estado_calidad, observacion) VALUES
  (1, 1, 1, 1, 1, 2, CURDATE(), 210.00, 20.00, -6.67, 'CONFORME', 'Trama estable, sin manchas'),
  (2, 2, 1, 2, 3, 2, CURDATE(), 180.00, 10.00, -12.50, 'PENDIENTE', 'Pendiente de inspeccion del Supervisor');

INSERT INTO lote (id_lote, codigo, id_orden_produccion, id_asignacion, metros, estado) VALUES
  (1, 'LOTE-2026-001', 1, 1, 210.00, 'CONFORME'),
  (2, 'LOTE-2026-002', 1, 2, 180.00, 'PENDIENTE_INSPECCION');

INSERT INTO inspeccion_calidad (id_inspeccion, id_lote, id_avance, fecha, varianza_color_pct, tolerancia_aplicada, resultado, observacion) VALUES
  (1, 1, 1, CURDATE(), 1.20, 1.50, 'CONFORME', 'Color dentro de la tolerancia de la ficha tecnica');

-- Ingreso de insumos al Kardex (recepcion de almacen)
INSERT INTO movimiento_kardex (id_movimiento, id_insumo, tipo_movimiento, cantidad, precio_unitario, id_orden_compra, id_orden_produccion, fecha, observaciones) VALUES
  (1, 2, 'INGRESO', 2400.00, 6.8000, NULL, NULL, CURDATE(), 'Ingreso inicial de almacen'),
  (2, 4, 'INGRESO', 120.00, 24.0000, NULL, NULL, CURDATE(), 'Ingreso inicial de almacen'),
  (3, 2, 'SALIDA',  150.00, 6.8000, NULL, 1, CURDATE(), 'Consumo para la orden OP-2026-001');

INSERT INTO lote_insumo (id_lote, id_movimiento_kardex, cantidad_consumida) VALUES
  (1, 3, 25.00);

-- ---------------------------------------------------------------------------
--  AUDITORIA INICIAL
-- ---------------------------------------------------------------------------
INSERT INTO registro_auditoria (cod_usuario, fecha_hora, modulo, operacion, entidad, id_entidad, detalle) VALUES
  ('sistema', NOW(), 'seguridad', 'CARGA_INICIAL', 'perfil', NULL, 'Carga de la semilla de datos del SIGPT');
