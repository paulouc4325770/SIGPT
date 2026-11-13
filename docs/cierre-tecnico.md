# SIGPT — Cierre técnico del entregable 3

**Textiles Andinos S.A.C. — Sistema Integral de Gestión de Producción Textil**
UNMSM · Facultad de Ingeniería de Sistemas e Informática · FISI

Este documento cierra el tercer entregable: **abastecimiento (EF‑1001‑5)**, **reportes
gerenciales (EF‑1001‑8)** y el **cierre técnico** (PDF de la orden de compra, envío al
proveedor, pruebas de carga y revisión de seguridad).

---

## 1. Qué se implementó en este entregable

| Subconjunto | Pantalla | Ruta | Reglas |
|---|---|---|---|
| Proveedores | Padrón de proveedores | `/abastecimiento/proveedores` | Código homologado único |
| Insumos | Catálogo de insumos | `/abastecimiento/insumos` | **RD5** (MSDS obligatoria en químicos y tintes), **R‑Stock** (alerta de stock mínimo) |
| RFQ | Solicitudes de cotización | `/abastecimiento/rfq` | Matriz costo‑beneficio‑tiempo para adjudicar |
| Orden de compra | Emisión y seguimiento | `/abastecimiento/ordenes-compra` | PDF generado y enviado al proveedor (adaptador) |
| Kardex | Movimientos de insumos | `/abastecimiento/kardex` | **RD4** (varianza de consumo ≤ 15 %), stock siempre sincronizado |
| Reportes | Avance, Productividad, Consumo y merma, Eficiencia | `/reportes/…` | 4 reportes, 3 formatos de salida |
| Cierre | Parte diario, cierre de lote | `/planta/calidad` | **RD7**, **R‑Merma** |

### 1.1 Patrones de diseño aplicados

| Patrón | Dónde | Para qué |
|---|---|---|
| **State** | `negocio/pedidos/estados/` | Ciclo de vida del pedido: `Registrado → Cotizado → En Producción → Terminado`, con `Anulado` |
| **Strategy** | `negocio/reportes/calculo/` | El cálculo de cada reporte (4 estrategias intercambiables) |
| **Template Method** | `negocio/reportes/ReporteBase` | El esqueleto de generar un reporte: calcular → escribir → registrar en el historial |
| **Bridge** | `negocio/reportes/formato/` | La salida (pantalla, PDF, Excel) es independiente del tipo de reporte |
| **Adapter** | `utiles/notificacion/` | El envío de la orden de compra al proveedor: hoy se registra en el servidor; mañana puede ser SMTP sin tocar el dominio |
| **DAO** | `datos/**` | Acceso a datos con JDBC parametrizado, sin ORM (decisión D7) |
| **Singleton** | `negocio/comun/ParametrosNegocio` | Los umbrales de negocio se leen una vez de la tabla `parametro` |

---

## 2. Revisión de seguridad (OWASP Top 10 — 2021)

| # | Riesgo | Estado | Evidencia en el proyecto |
|---|---|---|---|
| A01 | Control de acceso roto | **Mitigado** | `InterceptorPermisos` valida **en cada petición** que la ruta sea una opción del perfil (o una subruta de ella). Verificado en vivo: el Operario recibe **403** en `/pedidos/bandeja` y el Supervisor en `/reportes/avance-pedidos` (CHG‑04: los reportes son del Jefe de Producción y de Gerencia) |
| A02 | Fallas criptográficas | **Mitigado** | Contraseñas con **PBKDF2** y sal por usuario (`HashHelper`). El secreto de la base de datos vive en `application-local.properties`, fuera de git; verificado que **no aparece en el `.jar`** ni en el repositorio |
| A03 | Inyección | **Mitigado** | El 100 % de las sentencias JDBC usa **parámetros enlazados**; no hay concatenación de valores en el SQL (el único `+` del código es formateo de un código correlativo, no SQL) |
| A04 | Diseño inseguro | **Mitigado** | Las reglas viven en la capa de negocio y los umbrales en la tabla `parametro` (nunca en el código). El ciclo de vida del pedido es un `State` con transiciones válidas: un pedido en producción **no** puede anularse |
| A05 | Configuración incorrecta | **Mitigado** | El manejador global de errores muestra una página propia **sin traza técnica**; las rutas inexistentes devuelven 404 y los datos de formulario inválidos devuelven 400 (no 500) |
| A06 | Componentes vulnerables | **Mitigado** | Java 17 + Spring Boot **3.5.16** (última versión de la línea 3, CHG‑01 anulado), MySQL Connector/J 9, OpenPDF 2.0.3 (MPL/LGPL, sin dependencias de iText proprietary) |
| A07 | Fallas de identificación y autenticación | **Mitigado** | PBKDF2, **bloqueo temporal de la cuenta** a los 3 intentos (15 min) y **cierre de sesión por inactividad** a los 15 min (`server.servlet.session.timeout=15m`) |
| A08 | Fallas de integridad | **Mitigado** | La construcción es reproducible desde el repositorio y la CI ejecuta `mvn clean verify`; el PDF de la orden se genera en el servidor, no se descarga de un tercero |
| A09 | Fallas de registro y supervisión | **Mitigado** | **Todas** las operaciones de negocio dejan traza en `registro_auditoria` (módulos `PEDIDOS`, `PLANTA`, `ABASTECIMIENTO`, `REPORTES`): usuario, fecha, entidad, id y detalle |
| A10 | SSRF | **No aplica** | El sistema no hace peticiones HTTP salientes. El adaptador de correo escribe en el servidor |

### 2.1 Bug de seguridad encontrado por las pruebas de carga

La prueba de carga discovery que el contenedor **reescribe la URL** con `;jsessionid=…`
cuando el cliente no envía la cookie de sesión (navegador con cookies deshabilitadas,
*r crawler*). El interceptor comparaba esa ruta literal con la opción de menú y
respondía **403** de forma inexplicable.

**Corrección:** `InterceptorPermisos.rutaEfectiva()` elimina los parámetros de ruta
(`;…`) antes de comparar, de modo que la comprobación de permisos mira la ruta real.

---

## 3. Pruebas de carga (RNF01: respuesta menor a 2 segundos)

### 3.1 Herramientas

| Herramienta | Uso | Dónde |
|---|---|---|
| **JMeter 5.6** | Plan de pruebas oficial: 20 usuarios concurrentes, 5 iteraciones, 6 pantallas, aserciones de código 200 y de duración | `docs/pruebas-carga/plan-carga-sigpt.jmx` |
| **Script propio (Python)** | Alternativa sin instalar nada: mide p50, p95 y máximo por pantalla | `scripts/prueba-carga.py` |

```bash
# JMeter
jmeter -n -t docs/pruebas-carga/plan-carga-sigpt.jmx -l resultados/carga.jtl -e -o resultados/informe

# Script propio
python scripts/prueba-carga.py --usuarios 20 --iteraciones 5
```

### 3.2 Resultados medidos (20 usuarios concurrentes, 5 iteraciones)

| Pantalla | Peticiones | p50 (ms) | p95 (ms) | máx (ms) | Errores |
|---|---|---|---|---|---|
| `/inicio` | 100 | 15 | 72 | 144 | 0 |
| `/planta/avance` | 100 | 17 | 59 | 267 | 0 |
| `/planta/asignacion` | 100 | 18 | 130 | 597 | 0 |
| `/planta/calidad` | 100 | 14 | 60 | 250 | 0 |
| `/pedidos/bandeja` | 100 | 14 | 61 | 92 | 0 |
| `/reportes/avance-pedidos` | 100 | 20 | 61 | 206 | 0 |
| **Total** | **600** | — | **200** | **597** | **0** |

**600 peticiones en 4,6 s (131 peticiones/s), p95 general 200 ms** frente al límite de
2 000 ms del RNF01: **se cumple con holgura** (el peor caso medido fue 597 ms).

El plan se ejecuta contra el servidor local con la base de datos MySQL 8.0.46 y el
pool de conexiones de HikariCP configurado en 10 conexiones.

---

## 4. PDF de la orden de compra y envío al proveedor

1. El Jefe de Logística emite la orden: se genera el **PDF con OpenPDF**, se guarda en
   `data/ordenes-compra/` y se registra la ruta en `orden_compra.ruta_pdf`.
2. El **adaptador** `NotificadorOrdenCompra` entrega el documento al proveedor. La
   implementación por defecto (`NotificadorArchivo`) deja el PDF y el mensaje (`.eml`)
   en `data/envios/`; el dominio no conoce el transporte, asi que sustituirlo por SMTP
   es entregar otra implementación de la misma interfaz.
3. Al **recibir** la mercaderia se registran los ingresos en el **kardex** y el stock
   de cada insumo queda actualizado en la misma transacción.

---

## 5. Instructivo de despliegue

```bash
# 1. Requisitos: Java 17, Maven 3.10, MySQL 8.0
java -version && mvn -version && mysql --version

# 2. Base de datos
mysql -uroot -p -e "CREATE DATABASE sigpt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p sigpt < src/main/resources/db/schema.sql
mysql -uroot -p sigpt < src/main/resources/db/seed.sql

# 3. Usuario de aplicacion (principio de minimo privilegio)
mysql -uroot -p -e "CREATE USER 'sigpt_app'@'localhost' IDENTIFIED BY '<clave>';
GRANT SELECT, INSERT, UPDATE, DELETE ON sigpt.* TO 'sigpt_app'@'localhost'; FLUSH PRIVILEGES;"

# 4. Secreto de conexion (FUERA del repositorio)
cat > application-local.properties <<'EOF'
spring.datasource.username=sigpt_app
spring.datasource.password=<clave>
EOF

# 5. Compilar y ejecutar
mvn clean verify
java -jar target/sigpt-1.0.0.jar
```

La aplicación queda en `http://localhost:8080`. Los datos de demostración se siembran
con `seed.sql`: 8 perfiles, 31 opciones de menú, 3 proveedores, 8 insumos, 3 pedidos,
cotizaciones, órdenes de producción, asignaciones y lotes.

**Antes de ponerlo en producción:** servir por HTTPS (el RNF de confidencialidad exige
que los datos no viajan en claro), cambiar la clave del usuario de base de datos y
configurar el envío real de correo.

---

## 6. Estado final del proyecto

| Entregable | Contenido | Estado |
|---|---|---|
| E1 | Base de datos, esqueleto, módulo de seguridad, vistas, diagramas | **Completo** |
| E2 | Pedidos (EF‑1001‑3), clientes (EF‑1001‑4) y planta (EF‑1001‑6, EF‑1001‑7) | **Completo** |
| E3 | Abastecimiento (EF‑1001‑5), reportes (EF‑1001‑8) y cierre técnico | **Completo** |

- **Pruebas automáticas:** 105, todas en verde (`mvn clean verify`).
- **Diagramas:** 20 imágenes (12 del modelo 4+1 + 8 del Modelo de Negocio) con fuente
  editable en Mermaid.
- **Reglas de negocio:** 14 reglas de dominio implementadas y verificadas con prueba
  unitaria o prueba en vivo.
- **Cobertura funcional:** los 7 casos de uso del enunciado están implementados; el
  despacho y la exportación de pedidos quedan fuera de alcance (CHG‑05) y la
  documentación formal de manuales queda para la fase siguiente.

### 6.1 Pendientes declarados

| # | Pendiente | Motivo |
|---|---|---|
| 1 | Envío real por SMTP | El adaptador está listo; falta el servidor de correo del cliente |
| 2 | HTTPS y certificados | Depende de la infraestructura de destino |
| 3 | Manuales de usuario y final | Fase de documentacion formal (fuera de la ventana de 3 semanas) |
| 4 | Notificaciones push de Mantenimiento | El enunciado lo menciona, pero Mantenimiento no es actor del sistema (§12.2, hallazgo 7) |
