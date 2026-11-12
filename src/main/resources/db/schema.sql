-- ============================================================================
--  SIGPT - Sistema Integral de Gestion de Produccion Textil
--  Textiles Andinos S.A.C.  |  UNMSM - FISI  |  Grupo N. 4
--
--  Esquema relacional normalizado - MySQL 8.0 (InnoDB) - utf8mb4
--  Derivado del ERD del Documento de Arquitectura (vista de datos, seccion 5).
--
--  Convenciones:
--    * Llave primaria id_<entidad>
--    * Llaves foraneas con la tabla de mayor nivel arriba
--    * Columnas de auditoria en todas las tablas (RC8): usuario_registro + fecha_registro
--    * Los umbrales de negocio NO viven aqui: viven en la tabla `parametro`
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reporte_generado;
DROP TABLE IF EXISTS formato_salida;
DROP TABLE IF EXISTS reporte_gerencial;
DROP TABLE IF EXISTS parte_diario_produccion;
DROP TABLE IF EXISTS incidencia;
DROP TABLE IF EXISTS inspeccion_calidad;
DROP TABLE IF EXISTS lote_insumo;
DROP TABLE IF EXISTS lote;
DROP TABLE IF EXISTS registro_avance;
DROP TABLE IF EXISTS asignacion;
DROP TABLE IF EXISTS turno;
DROP TABLE IF EXISTS maquina;
DROP TABLE IF EXISTS operario;
DROP TABLE IF EXISTS movimiento_kardex;
DROP TABLE IF EXISTS orden_compra_detalle;
DROP TABLE IF EXISTS orden_compra;
DROP TABLE IF EXISTS cotizacion_proveedor;
DROP TABLE IF EXISTS solicitud_cotizacion;
DROP TABLE IF EXISTS insumo;
DROP TABLE IF EXISTS proveedor;
DROP TABLE IF EXISTS orden_produccion_insumo;
DROP TABLE IF EXISTS orden_produccion;
DROP TABLE IF EXISTS adelanto;
DROP TABLE IF EXISTS cotizacion;
DROP TABLE IF EXISTS ficha_tecnica;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS registro_auditoria;
DROP TABLE IF EXISTS perfil_opcion;
DROP TABLE IF EXISTS opcion;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS perfil;
DROP TABLE IF EXISTS tipo_cambio;
DROP TABLE IF EXISTS moneda;
DROP TABLE IF EXISTS parametro;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
--  MODULO: SEGURIDAD
-- ============================================================================

CREATE TABLE perfil (
  id_perfil      INT AUTO_INCREMENT PRIMARY KEY,
  cod_perfil     VARCHAR(20)  NOT NULL,
  nombre_perfil  VARCHAR(60)  NOT NULL,
  descripcion    VARCHAR(200) NULL,
  estado         BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_perfil_cod UNIQUE (cod_perfil)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE usuario (
  id_usuario        INT AUTO_INCREMENT PRIMARY KEY,
  cod_usuario       VARCHAR(30)  NOT NULL,
  clave_hash        VARCHAR(100) NOT NULL COMMENT 'Hash de un solo sentido con salt (BCrypt/Argon2). Nunca texto plano.',
  nombres           VARCHAR(120) NOT NULL,
  area              VARCHAR(60)  NULL COMMENT 'Descripcion del area (salida del servicio Login, EF-1001-2)',
  id_perfil         INT          NOT NULL,
  intentos_fallidos TINYINT      NOT NULL DEFAULT 0,
  bloqueado_hasta   DATETIME     NULL,
  ultimo_acceso     DATETIME     NULL,
  estado            BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro  VARCHAR(30)  NOT NULL DEFAULT 'sistema',
  fecha_registro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT uk_usuario_cod UNIQUE (cod_usuario),
  CONSTRAINT fk_usuario_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil),
  CONSTRAINT ck_usuario_intentos CHECK (intentos_fallidos >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_usuario_perfil ON usuario (id_perfil);
CREATE INDEX idx_usuario_estado ON usuario (estado);

CREATE TABLE opcion (
  id_opcion     INT AUTO_INCREMENT PRIMARY KEY,
  nombre_opcion VARCHAR(80)  NOT NULL,
  url_opcion    VARCHAR(200) NULL COMMENT 'Area/Controlador/Accion. NULL si es solo un grupo de menu.',
  ruta_imagen   VARCHAR(200) NULL,
  nro_orden     INT          NOT NULL DEFAULT 0,
  id_opcion_ref INT          NULL COMMENT 'Opcion padre: jerarquia del menu',
  estado        BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_opcion_padre FOREIGN KEY (id_opcion_ref) REFERENCES opcion (id_opcion)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_opcion_padre_orden ON opcion (id_opcion_ref, nro_orden);

CREATE TABLE perfil_opcion (
  id_perfil INT NOT NULL,
  id_opcion INT NOT NULL,
  estado    BOOLEAN     NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_perfil, id_opcion),
  CONSTRAINT fk_perfil_opcion_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil),
  CONSTRAINT fk_perfil_opcion_opcion FOREIGN KEY (id_opcion) REFERENCES opcion (id_opcion)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_perfil_opcion_opcion ON perfil_opcion (id_opcion);

CREATE TABLE registro_auditoria (
  id_registro BIGINT AUTO_INCREMENT PRIMARY KEY,
  cod_usuario VARCHAR(30) NOT NULL COMMENT 'Codigo del usuario. "sistema" para operaciones automaticas (sin FK a proposito)',
  fecha_hora   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modulo       VARCHAR(40) NOT NULL,
  operacion    VARCHAR(40) NOT NULL,
  entidad      VARCHAR(40) NOT NULL,
  id_entidad   VARCHAR(60) NULL,
  detalle      TEXT        NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_auditoria_usuario_fecha ON registro_auditoria (cod_usuario, fecha_hora);
CREATE INDEX idx_auditoria_entidad ON registro_auditoria (entidad, id_entidad);

-- ============================================================================
--  MODULO: PEDIDOS
-- ============================================================================

CREATE TABLE cliente (
  id_cliente    INT AUTO_INCREMENT PRIMARY KEY,
  codigo        VARCHAR(20)  NOT NULL,
  razon_social  VARCHAR(150) NOT NULL,
  ruc_tax_id    VARCHAR(20)  NOT NULL COMMENT 'Unico: el sistema no permite clientes duplicados',
  pais          VARCHAR(60)  NOT NULL,
  contacto      VARCHAR(120) NULL,
  correo        VARCHAR(120) NULL,
  telefono      VARCHAR(40)  NULL,
  estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT uk_cliente_codigo UNIQUE (codigo),
  CONSTRAINT uk_cliente_ruc UNIQUE (ruc_tax_id),
  CONSTRAINT ck_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE pedido (
  id_pedido     INT AUTO_INCREMENT PRIMARY KEY,
  cod_pedido    VARCHAR(20)   NOT NULL,
  id_cliente    INT           NOT NULL,
  metros        DECIMAL(12,2) NOT NULL,
  fecha_entrega DATE          NOT NULL,
  estado        VARCHAR(20)   NOT NULL DEFAULT 'REGISTRADO',
  metros_consolidados DECIMAL(12,2) NOT NULL DEFAULT 0.00
                 COMMENT 'Metros ya aprobados por control de calidad (consolidado del avance)',
  fecha_registro_operativa DATE NULL COMMENT 'Fecha en que el pedido paso a En Produccion',
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT uk_pedido_codigo UNIQUE (cod_pedido),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
  CONSTRAINT ck_pedido_estado CHECK (estado IN
    ('REGISTRADO', 'COTIZADO', 'EN_PRODUCCION', 'TERMINADO', 'DESPACHADO', 'ANULADO')),
  CONSTRAINT ck_pedido_metros CHECK (metros > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_pedido_cliente ON pedido (id_cliente);
CREATE INDEX idx_pedido_estado_fecha ON pedido (estado, fecha_entrega);

CREATE TABLE ficha_tecnica (
  id_ficha          INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido         INT           NOT NULL COMMENT 'Composicion: la ficha tecnica no existe sin su pedido',
  tipo_hilado       VARCHAR(80)   NOT NULL,
  gramaje_m2        DECIMAL(8,2)  NOT NULL,
  color_pantone     VARCHAR(40)   NOT NULL,
  composicion       VARCHAR(150)  NOT NULL,
  metros            DECIMAL(12,2) NOT NULL,
  tolerancia_color  DECIMAL(8,2)  NOT NULL DEFAULT 2.00
                    COMMENT 'Tolerancia de varianza de color en % (criterio de RD7)',
  observaciones     VARCHAR(300)  NULL,
  usuario_registro  VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_ficha_pedido UNIQUE (id_pedido),
  CONSTRAINT fk_ficha_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE cotizacion (
  id_cotizacion   INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido       INT            NOT NULL,
  monto_total     DECIMAL(14,2)  NOT NULL COMMENT 'Valor pactado: base del 40% de RD2',
  moneda          VARCHAR(5)     NOT NULL DEFAULT 'USD',
  costo_insumos   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  horas_ciclo     DECIMAL(8,2)   NOT NULL DEFAULT 0.00 COMMENT 'Tiempo de ciclo estimado',
  fecha_emision   DATE           NOT NULL,
  fecha_vigencia  DATE           NOT NULL COMMENT 'Vigencia de la oferta (15 dias por parametro)',
  estado          VARCHAR(20)    NOT NULL DEFAULT 'EMITIDA',
  usuario_registro VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT fk_cotizacion_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido),
  CONSTRAINT ck_cotizacion_estado CHECK (estado IN ('EMITIDA', 'ACEPTADA', 'VENCIDA', 'RECHAZADA', 'ANULADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_cotizacion_pedido ON cotizacion (id_pedido);
CREATE INDEX idx_cotizacion_vigencia ON cotizacion (fecha_vigencia, estado);

CREATE TABLE adelanto (
  id_adelanto     INT AUTO_INCREMENT PRIMARY KEY,
  id_cotizacion   INT           NOT NULL,
  monto           DECIMAL(14,2) NOT NULL,
  moneda          VARCHAR(5)    NOT NULL DEFAULT 'USD',
  fecha_adelanto  DATE          NOT NULL COMMENT 'Fecha en que se registro el adelanto',
  verificado      BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Verificado por el Jefe de Produccion',
  referencia      VARCHAR(80)   NULL COMMENT 'Letra de credito o transferencia',
  usuario_registro VARCHAR(30)  NOT NULL DEFAULT 'sistema',
  fecha_registro  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_adelanto_cotizacion FOREIGN KEY (id_cotizacion) REFERENCES cotizacion (id_cotizacion)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_adelanto_cotizacion ON adelanto (id_cotizacion);

CREATE TABLE orden_produccion (
  id_orden              INT AUTO_INCREMENT PRIMARY KEY,
  cod_orden             VARCHAR(20)   NOT NULL,
  id_pedido             INT           NOT NULL,
  metros_totales        DECIMAL(12,2) NOT NULL,
  metros_consolidados   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  horas_maquina_previstas DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT 'Base del reporte de Eficiencia Industrial',
  horas_hombre_previstas DECIMAL(8,2) NOT NULL DEFAULT 0.00,
  fecha_inicio           DATE          NULL,
  fecha_fin              DATE          NULL,
  estado                VARCHAR(20)   NOT NULL DEFAULT 'EN_PRODUCCION',
  usuario_registro      VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion  VARCHAR(30)   NULL,
  fecha_modificacion    DATETIME      NULL,
  CONSTRAINT uk_orden_codigo UNIQUE (cod_orden),
  CONSTRAINT fk_orden_pedido FOREIGN KEY (id_pedido) REFERENCES pedido (id_pedido),
  CONSTRAINT ck_orden_estado CHECK (estado IN ('EN_PRODUCCION', 'TERMINADO', 'ANULADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_orden_pedido ON orden_produccion (id_pedido);

-- ============================================================================
--  MODULO: ABASTECIMIENTO
-- ============================================================================

CREATE TABLE proveedor (
  id_proveedor  INT AUTO_INCREMENT PRIMARY KEY,
  codigo        VARCHAR(20)  NOT NULL COMMENT 'Codigo de proveedor homologado',
  razon_social  VARCHAR(150) NOT NULL,
  ruc           VARCHAR(20)  NULL,
  tipo          VARCHAR(40)  NOT NULL COMMENT 'Agricola, Ganadera (cooperativas alpaqueras) o Quimica',
  contacto      VARCHAR(120) NULL,
  correo        VARCHAR(120) NULL,
  telefono      VARCHAR(40)  NULL,
  estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT uk_proveedor_codigo UNIQUE (codigo),
  CONSTRAINT ck_proveedor_estado CHECK (estado IN ('ACTIVO', 'INACTIVO')),
  CONSTRAINT ck_proveedor_tipo CHECK (tipo IN ('AGRICOLA', 'GANADERA', 'QUIMICA', 'MIXTA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE insumo (
  id_insumo       INT AUTO_INCREMENT PRIMARY KEY,
  codigo          VARCHAR(30)   NOT NULL,
  id_proveedor    INT           NOT NULL,
  nombre          VARCHAR(150)  NOT NULL,
  tipo            VARCHAR(30)   NOT NULL COMMENT 'FIBRA, TINTE, QUIMICO, CAJA',
  unidad_medida   VARCHAR(20)   NOT NULL DEFAULT 'KG',
  stock_minimo    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Umbral de alerta de stock (R-Stock)',
  stock_actual    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Mantenido en la misma transaccion del kardex',
  precio_unitario DECIMAL(12,4) NOT NULL DEFAULT 0.00,
  ruta_msds       VARCHAR(300)  NULL COMMENT 'Ruta del archivo MSDS en el servidor. Obligatorio para QUIMICO y TINTE (RD5)',
  estado          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
  usuario_registro VARCHAR(30)  NOT NULL DEFAULT 'sistema',
  fecha_registro  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion VARCHAR(30) NULL,
  fecha_modificacion   DATETIME    NULL,
  CONSTRAINT uk_insumo_codigo UNIQUE (codigo),
  CONSTRAINT fk_insumo_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
  CONSTRAINT ck_insumo_tipo CHECK (tipo IN ('FIBRA', 'TINTE', 'QUIMICO', 'CAJA')),
  CONSTRAINT ck_insumo_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_insumo_proveedor ON insumo (id_proveedor);
CREATE INDEX idx_insumo_stock ON insumo (stock_actual, stock_minimo);

CREATE TABLE solicitud_cotizacion (
  id_solicitud        INT AUTO_INCREMENT PRIMARY KEY,
  codigo              VARCHAR(20)  NOT NULL,
  id_orden_produccion INT           NOT NULL,
  fecha_emision       DATE          NOT NULL,
  fecha_cierre        DATE          NULL,
  estado              VARCHAR(20)   NOT NULL DEFAULT 'EMITIDA',
  observaciones       VARCHAR(300)  NULL,
  usuario_registro    VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_solicitud_codigo UNIQUE (codigo),
  CONSTRAINT fk_solicitud_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT ck_solicitud_estado CHECK (estado IN ('EMITIDA', 'CERRADA', 'CANCELADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_solicitud_orden ON solicitud_cotizacion (id_orden_produccion);

CREATE TABLE cotizacion_proveedor (
  id_cotizacion_proveedor INT AUTO_INCREMENT PRIMARY KEY,
  id_solicitud         INT            NOT NULL,
  id_proveedor         INT            NOT NULL,
  precio_unitario      DECIMAL(12,4)  NOT NULL,
  unidad               VARCHAR(20)    NOT NULL DEFAULT 'KG',
  tiempo_entrega_dias  INT            NOT NULL DEFAULT 0 COMMENT 'Criterio de la matriz costo-beneficio-tiempo',
  certificaciones      VARCHAR(200)   NULL,
  observaciones        VARCHAR(300)   NULL,
  estado               VARCHAR(20)    NOT NULL DEFAULT 'RECIBIDA',
  usuario_registro     VARCHAR(30)    NOT NULL DEFAULT 'sistema',
  fecha_registro       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_cotprov_sol_prov UNIQUE (id_solicitud, id_proveedor),
  CONSTRAINT fk_cotprov_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud_cotizacion (id_solicitud),
  CONSTRAINT fk_cotprov_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
  CONSTRAINT ck_cotprov_estado CHECK (estado IN ('RECIBIDA', 'ADJUDICADA', 'DESCARTADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_cotprov_proveedor ON cotizacion_proveedor (id_proveedor);

CREATE TABLE orden_compra (
  id_orden_compra         INT AUTO_INCREMENT PRIMARY KEY,
  codigo                  VARCHAR(20)   NOT NULL,
  id_proveedor            INT           NOT NULL,
  id_solicitud            INT           NULL,
  id_cotizacion_proveedor INT           NULL,
  monto_total             DECIMAL(14,2) NOT NULL,
  moneda                  VARCHAR(5)    NOT NULL DEFAULT 'USD',
  fecha_emision           DATE          NOT NULL,
  fecha_entrega_esperada  DATE          NULL,
  ruta_pdf                VARCHAR(300)  NULL COMMENT 'PDF generado y enviado al proveedor (Adapter de correo)',
  estado                  VARCHAR(20)   NOT NULL DEFAULT 'EMITIDA',
  observaciones           VARCHAR(300)  NULL,
  usuario_registro        VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion    VARCHAR(30)   NULL,
  fecha_modificacion      DATETIME      NULL,
  CONSTRAINT uk_oc_codigo UNIQUE (codigo),
  CONSTRAINT fk_oc_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor),
  CONSTRAINT fk_oc_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud_cotizacion (id_solicitud),
  CONSTRAINT fk_oc_cotprov FOREIGN KEY (id_cotizacion_proveedor) REFERENCES cotizacion_proveedor (id_cotizacion_proveedor),
  CONSTRAINT ck_oc_estado CHECK (estado IN ('EMITIDA', 'CONFIRMADA', 'RECIBIDA', 'ANULADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_oc_proveedor ON orden_compra (id_proveedor);

CREATE TABLE orden_compra_detalle (
  id_orden_compra   INT           NOT NULL,
  id_insumo         INT           NOT NULL,
  cantidad          DECIMAL(12,2) NOT NULL,
  precio_unitario   DECIMAL(12,4) NOT NULL,
  usuario_registro  VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden_compra, id_insumo),
  CONSTRAINT fk_ocdet_oc FOREIGN KEY (id_orden_compra) REFERENCES orden_compra (id_orden_compra) ON DELETE CASCADE,
  CONSTRAINT fk_ocdet_insumo FOREIGN KEY (id_insumo) REFERENCES insumo (id_insumo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE movimiento_kardex (
  id_movimiento     INT AUTO_INCREMENT PRIMARY KEY,
  id_insumo         INT            NOT NULL,
  tipo_movimiento   VARCHAR(20)    NOT NULL COMMENT 'INGRESO, SALIDA, AJUSTE',
  cantidad          DECIMAL(12,2)  NOT NULL,
  precio_unitario   DECIMAL(12,4)  NOT NULL DEFAULT 0.00,
  id_orden_compra   INT            NULL,
  id_orden_produccion INT          NULL,
  fecha             DATE           NOT NULL,
  observaciones     VARCHAR(300)   NULL,
  usuario_registro  VARCHAR(30)    NOT NULL DEFAULT 'sistema',
  fecha_registro    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_kardex_insumo FOREIGN KEY (id_insumo) REFERENCES insumo (id_insumo),
  CONSTRAINT fk_kardex_oc FOREIGN KEY (id_orden_compra) REFERENCES orden_compra (id_orden_compra),
  CONSTRAINT fk_kardex_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT ck_kardex_tipo CHECK (tipo_movimiento IN ('INGRESO', 'SALIDA', 'AJUSTE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_kardex_insumo_fecha ON movimiento_kardex (id_insumo, fecha);
CREATE INDEX idx_kardex_orden ON movimiento_kardex (id_orden_produccion);

-- Plan teorico de consumo por orden de produccion: base del 15% de RD4.
CREATE TABLE orden_produccion_insumo (
  id_orden_produccion INT            NOT NULL,
  id_insumo           INT            NOT NULL,
  cantidad_teorica    DECIMAL(12,2)  NOT NULL COMMENT 'Consumo teorico planificado (base de RD4)',
  usuario_registro    VARCHAR(30)    NOT NULL DEFAULT 'sistema',
  fecha_registro      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_orden_produccion, id_insumo),
  CONSTRAINT fk_opins_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden) ON DELETE CASCADE,
  CONSTRAINT fk_opins_insumo FOREIGN KEY (id_insumo) REFERENCES insumo (id_insumo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
--  MODULO: PLANTA
-- ============================================================================

CREATE TABLE operario (
  id_operario   INT AUTO_INCREMENT PRIMARY KEY,
  codigo        VARCHAR(20)  NOT NULL,
  nombres       VARCHAR(120) NOT NULL,
  especialidad  VARCHAR(40)  NOT NULL COMMENT 'Telar, TeÃ±idor',
  id_usuario    INT          NULL COMMENT 'Asociacion, NO herencia: el usuario es la cuenta de acceso',
  estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_operario_codigo UNIQUE (codigo),
  CONSTRAINT fk_operario_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_operario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE maquina (
  id_maquina                INT AUTO_INCREMENT PRIMARY KEY,
  codigo                    VARCHAR(20)   NOT NULL,
  nombre                    VARCHAR(120)  NOT NULL,
  tipo                      VARCHAR(30)   NOT NULL COMMENT 'Telar circular, Telar rectilineo, Urdidora, Tintoreria',
  estado                    VARCHAR(20)   NOT NULL DEFAULT 'LIBRE' COMMENT 'libre, ocupada, en mantenimiento, back-up',
  velocidad_teorica_mts_hora DECIMAL(10,2) NOT NULL DEFAULT 0.00
                            COMMENT 'Velocidad teorica del telar: base de R-Varianza (>15%)',
  horas_por_turno           DECIMAL(5,2)  NOT NULL DEFAULT 8.00,
  observaciones             VARCHAR(300)   NULL,
  usuario_registro          VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_modificacion      VARCHAR(30)   NULL,
  fecha_modificacion        DATETIME      NULL,
  CONSTRAINT uk_maquina_codigo UNIQUE (codigo),
  CONSTRAINT ck_maquina_estado CHECK (estado IN ('LIBRE', 'OCUPADA', 'MANTENIMIENTO', 'BACKUP')),
  CONSTRAINT ck_maquina_tipo CHECK (tipo IN ('TELAR_CIRCULAR', 'TELAR_RECTILINEO', 'URDIDORA', 'TINTORERIA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE turno (
  id_turno    INT AUTO_INCREMENT PRIMARY KEY,
  codigo      VARCHAR(20)  NOT NULL COMMENT 'MANANA, TARDE, AMANECIDA',
  nombre      VARCHAR(40)  NOT NULL,
  hora_inicio TIME         NOT NULL,
  hora_fin    TIME         NOT NULL,
  horas       DECIMAL(5,2) NOT NULL,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_turno_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE asignacion (
  id_asignacion      INT AUTO_INCREMENT PRIMARY KEY,
  id_operario        INT           NOT NULL,
  id_maquina         INT           NOT NULL,
  id_turno           INT           NOT NULL,
  fecha              DATE          NOT NULL,
  id_orden_produccion INT          NOT NULL,
  meta_metros        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  horas_asignadas    DECIMAL(5,2)  NOT NULL DEFAULT 8.00
                     COMMENT 'Se acumula para validar las 16 h consecutivas de RD3',
  estado             VARCHAR(20)   NOT NULL DEFAULT 'ASIGNADA',
  usuario_registro   VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_asig_operario FOREIGN KEY (id_operario) REFERENCES operario (id_operario),
  CONSTRAINT fk_asig_maquina FOREIGN KEY (id_maquina) REFERENCES maquina (id_maquina),
  CONSTRAINT fk_asig_turno FOREIGN KEY (id_turno) REFERENCES turno (id_turno),
  CONSTRAINT fk_asig_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT ck_asig_estado CHECK (estado IN ('ASIGNADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_asig_orden ON asignacion (id_orden_produccion);
CREATE INDEX idx_asig_operario_fecha ON asignacion (id_operario, fecha);
CREATE INDEX idx_asig_maquina_fecha ON asignacion (id_maquina, fecha);

CREATE TABLE registro_avance (
  id_avance           INT AUTO_INCREMENT PRIMARY KEY,
  id_asignacion       INT           NOT NULL,
  id_orden_produccion INT           NOT NULL,
  id_operario         INT           NOT NULL,
  id_maquina          INT           NOT NULL,
  id_turno            INT           NOT NULL,
  fecha               DATE          NOT NULL,
  metros_producidos   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  metros_merma        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  varianza_pct        DECIMAL(8,2)  NOT NULL DEFAULT 0.00
                      COMMENT '(avance - teorico) / teorico. R-Varianza: alerta si < -15%',
  estado_calidad      VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, CONFORME, NO_CONFORME',
  observacion         VARCHAR(300)  NULL,
  usuario_registro    VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_avance_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion (id_asignacion),
  CONSTRAINT fk_avance_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT fk_avance_operario FOREIGN KEY (id_operario) REFERENCES operario (id_operario),
  CONSTRAINT fk_avance_maquina FOREIGN KEY (id_maquina) REFERENCES maquina (id_maquina),
  CONSTRAINT fk_avance_turno FOREIGN KEY (id_turno) REFERENCES turno (id_turno),
  CONSTRAINT ck_avance_calidad CHECK (estado_calidad IN ('PENDIENTE', 'CONFORME', 'NO_CONFORME'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_avance_orden_fecha ON registro_avance (id_orden_produccion, fecha);
CREATE INDEX idx_avance_asignacion ON registro_avance (id_asignacion);

CREATE TABLE lote (
  id_lote             INT AUTO_INCREMENT PRIMARY KEY,
  codigo              VARCHAR(30)   NOT NULL,
  id_orden_produccion INT           NOT NULL,
  id_asignacion       INT           NOT NULL,
  metros              DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE_INSPECCION' COMMENT 'PENDIENTE_INSPECCION, CONFORME, MERMA',
  usuario_registro    VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_lote_codigo UNIQUE (codigo),
  CONSTRAINT fk_lote_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT fk_lote_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion (id_asignacion),
  CONSTRAINT ck_lote_estado CHECK (estado IN ('PENDIENTE_INSPECCION', 'CONFORME', 'MERMA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_lote_orden ON lote (id_orden_produccion);

-- Trazabilidad RD6: un lote consume N movimientos de kardex.
CREATE TABLE lote_insumo (
  id_lote             INT           NOT NULL,
  id_movimiento_kardex INT          NOT NULL,
  cantidad_consumida  DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_lote, id_movimiento_kardex),
  CONSTRAINT fk_loteins_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote) ON DELETE CASCADE,
  CONSTRAINT fk_loteins_kardex FOREIGN KEY (id_movimiento_kardex) REFERENCES movimiento_kardex (id_movimiento)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE inspeccion_calidad (
  id_inspeccion       INT AUTO_INCREMENT PRIMARY KEY,
  id_lote             INT           NOT NULL,
  id_avance           INT           NULL,
  fecha               DATE          NOT NULL,
  varianza_color_pct  DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
  tolerancia_aplicada DECIMAL(8,2)  NOT NULL DEFAULT 2.00 COMMENT 'Tolerancia tomada de la ficha tecnica (RD7)',
  resultado           VARCHAR(20)   NOT NULL COMMENT 'CONFORME, NO_CONFORME',
  observacion         VARCHAR(300)  NULL,
  usuario_registro    VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inspeccion_lote FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
  CONSTRAINT fk_inspeccion_avance FOREIGN KEY (id_avance) REFERENCES registro_avance (id_avance),
  CONSTRAINT ck_inspeccion_resultado CHECK (resultado IN ('CONFORME', 'NO_CONFORME'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_inspeccion_lote ON inspeccion_calidad (id_lote);

CREATE TABLE incidencia (
  id_incidencia   INT AUTO_INCREMENT PRIMARY KEY,
  id_avance       INT           NULL,
  id_maquina      INT           NULL,
  tipo            VARCHAR(30)   NOT NULL COMMENT 'AVERIA, PARADA, FALLA_TELAR, OTRO',
  descripcion     VARCHAR(300)  NOT NULL,
  estado          VARCHAR(20)   NOT NULL DEFAULT 'ABIERTA' COMMENT 'ABIERTA, EN_ATENCION, CERRADA',
  usuario_registro VARCHAR(30)  NOT NULL DEFAULT 'sistema',
  fecha_registro  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_incidencia_avance FOREIGN KEY (id_avance) REFERENCES registro_avance (id_avance),
  CONSTRAINT fk_incidencia_maquina FOREIGN KEY (id_maquina) REFERENCES maquina (id_maquina),
  CONSTRAINT ck_incidencia_estado CHECK (estado IN ('ABIERTA', 'EN_ATENCION', 'CERRADA'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE parte_diario_produccion (
  id_parte             INT AUTO_INCREMENT PRIMARY KEY,
  id_orden_produccion  INT           NOT NULL,
  fecha                DATE          NOT NULL,
  horas_hombre         DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
  horas_maquina        DECIMAL(8,2)  NOT NULL DEFAULT 0.00,
  metros               DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  observaciones        VARCHAR(300)  NULL,
  usuario_registro     VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_parte_orden FOREIGN KEY (id_orden_produccion) REFERENCES orden_produccion (id_orden),
  CONSTRAINT uk_parte_orden_fecha UNIQUE (id_orden_produccion, fecha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
--  MODULO: REPORTES
-- ============================================================================

CREATE TABLE reporte_gerencial (
  id_reporte   INT AUTO_INCREMENT PRIMARY KEY,
  codigo       VARCHAR(30)  NOT NULL,
  nombre       VARCHAR(120) NOT NULL,
  descripcion  VARCHAR(300) NULL,
  estado       BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_reporte_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE formato_salida (
  id_formato   INT AUTO_INCREMENT PRIMARY KEY,
  codigo       VARCHAR(30)  NOT NULL COMMENT 'PANTALLA, PDF, EXCEL',
  nombre       VARCHAR(120) NOT NULL,
  clase_implementacion VARCHAR(200) NULL COMMENT 'Clase de la estrategia de formato',
  estado       BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_formato_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE reporte_generado (
  id_reporte_generado BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_reporte          INT          NOT NULL,
  cod_usuario         VARCHAR(30)  NOT NULL,
  filtros             TEXT         NULL COMMENT 'Filtros aplicados, en JSON',
  formato_salida      VARCHAR(30)  NULL,
  fecha_generacion    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_repg_reporte FOREIGN KEY (id_reporte) REFERENCES reporte_gerencial (id_reporte),
  CONSTRAINT fk_repg_usuario FOREIGN KEY (cod_usuario) REFERENCES usuario (cod_usuario)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
CREATE INDEX idx_repg_usuario_fecha ON reporte_generado (cod_usuario, fecha_generacion);

-- ============================================================================
--  CATALOGOS Y PARAMETROS (transversal)
-- ============================================================================

CREATE TABLE moneda (
  id_moneda   INT AUTO_INCREMENT PRIMARY KEY,
  codigo      VARCHAR(5)   NOT NULL COMMENT 'Codigo ISO 4217 (PEN, USD)',
  nombre      VARCHAR(60)  NOT NULL,
  simbolo     VARCHAR(5)   NULL,
  estado      BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_moneda_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE tipo_cambio (
  id_tipo_cambio   INT AUTO_INCREMENT PRIMARY KEY,
  moneda_origen    VARCHAR(5)    NOT NULL DEFAULT 'PEN',
  moneda_destino   VARCHAR(5)    NOT NULL DEFAULT 'USD',
  valor            DECIMAL(14,6) NOT NULL,
  fecha_vigencia   DATE          NOT NULL,
  estado           BOOLEAN       NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30)   NOT NULL DEFAULT 'sistema',
  fecha_registro   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_tipocambio UNIQUE (moneda_origen, moneda_destino, fecha_vigencia),
  CONSTRAINT fk_tipocambio_origen FOREIGN KEY (moneda_origen) REFERENCES moneda (codigo),
  CONSTRAINT fk_tipocambio_destino FOREIGN KEY (moneda_destino) REFERENCES moneda (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE parametro (
  id_parametro  INT AUTO_INCREMENT PRIMARY KEY,
  codigo        VARCHAR(60)  NOT NULL,
  nombre        VARCHAR(120) NOT NULL,
  valor         VARCHAR(120) NOT NULL,
  tipo_dato     VARCHAR(20)  NOT NULL DEFAULT 'TEXTO' COMMENT 'TEXTO, ENTERO, DECIMAL, BOOLEANO',
  descripcion   VARCHAR(300) NULL,
  estado        BOOLEAN      NOT NULL DEFAULT TRUE,
  usuario_registro VARCHAR(30) NOT NULL DEFAULT 'sistema',
  fecha_registro TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_parametro_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
