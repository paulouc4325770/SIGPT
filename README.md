# SIGPT — Sistema Integral de Gestión de Producción Textil

**Textiles Andinos S.A.C.** · UNMSM – FISI · Grupo N.º 4

Sistema web para la gestión integral de pedidos textiles, el abastecimiento de
materias primas y el control de tareas de planta. Reemplaza los registros
manuales (cuadernos físicos y hojas de cálculo) por una única fuente de
información auditable.

---

## 1. Stack tecnológico

| Elemento | Tecnología | Requisito que lo origina |
|---|---|---|
| Lenguaje | Java 17 | RDS1 |
| Framework | Spring Boot **3.5.16** (MVC + inyección de dependencias) | RDS1 / RNF05 |
| Vistas | Thymeleaf + Bootstrap 5.3.3 (incluido localmente, sin CDN) | D5 |
| Base de datos | MySQL 8.0 (InnoDB, utf8mb4) | RDS1 / RNF06 |
| Acceso a datos | JDBC puro con SQL parametrizado, sin ORM | D7 |
| Construcción | Maven 3.9+ | RDS1 |
| Pruebas | JUnit 5 + Mockito + AssertJ | RDS6 |
| Integración continua | GitHub Actions | RDS3 |
| Versionado | Git + Pull Requests | RDS3 / RDS4 |

> **Spring Boot 3.5.16** es la última versión publicada de la línea 3.x. Se
> mantiene esa línea porque RDS1 fija "Spring Boot 3.x" y evita la
> unfamiliaridad con la API de la rama 4. El CHG-01 propuesto en el documento de
> arquitectura queda por lo tanto **anulado**.

---

## 2. Estructura del proyecto

Monolito modular en **3 niveles y 5 módulos** (D4 del documento de arquitectura):

```
src/main/java/com/textilesandinos/sigpt/
├── presentacion/        02 FrontEnd — controladores MVC y vistas
│   ├── seguridad/       login, inicio, errores
│   ├── pedidos/         pedido, cliente, cotizaciones
│   ├── abastecimiento/  proveedores, insumos, RFQ, orden de compra, kardex
│   ├── planta/          asignación, avance, calidad, incidencias
│   ├── reportes/        los 4 reportes gerenciales
│   └── comun/           atributos globales de vista
├── negocio/             01 BackEnd — lógica de negocio (clases LN)
│   ├── seguridad/       SeguridadLN: autenticación, intentos, bloqueo
│   ├── pedidos/         PedidoLN, ClienteLN
│   ├── abastecimiento/  ProveedorLN, InsumoLN, KardexLN
│   ├── planta/          AsignacionLN, AvanceLN, CalidadLN
│   ├── reportes/        ReporteGerencial + factorías
│   └── comun/           ParametrosNegocio (Singleton de reglas)
├── datos/               01 BackEnd — acceso a datos (DAO + JDBC)
│   ├── comun/           DaoBase, ParametroDAO, AuditoriaHelper
│   ├── seguridad/       UsuarioDAO, PerfilDAO, OpcionDAO, RegistroAuditoriaDAO
│   └── …
├── entidades/           03 Transversal — clases del dominio
│   ├── seguridad/  pedidos/  abastecimiento/  planta/  reportes/  comun/
├── utiles/              03 Transversal — hash, log, correo, exportación
└── utilesweb/           03 Transversal — sesión, permisos, errores, binders
```

**Reglas de dependencia (estrictas):**

```
presentacion → negocio → datos (solo interfaces)
entidades, utiles, utilesweb → no dependen de nadie
```

La capa `presentacion` **no** accede a la capa de datos: ninguna pantalla se
saltarse una regla de negocio.

---

## 3. Requisitos para ejecutarlo

| Requisito | Versión |
|---|---|
| JDK | 17 o superior |
| Maven | 3.9 o superior |
| MySQL Server | 8.0 |

### 3.1 Crear la base de datos

```sql
CREATE DATABASE sigpt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sigpt_app'@'localhost' IDENTIFIED BY '<su-clave>';
GRANT ALL PRIVILEGES ON sigpt.* TO 'sigpt_app'@'localhost';
FLUSH PRIVILEGES;
```

### 3.2 Aplicar el esquema y los datos de prueba

```bash
mysql -u root -p sigpt < src/main/resources/db/schema.sql
mysql -u root -p sigpt < src/main/resources/db/seed.sql
```

`schema.sql` es idempotente: borra y vuelve a crear todas las tablas.

### 3.3 Configurar la conexión local

Copiar y editar `application-local.properties` (ya está en `.gitignore`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sigpt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&characterEncoding=UTF-8
spring.datasource.username=sigpt_app
spring.datasource.password=<su-clave>
```

Alternativa: variables de entorno `SIGPT_DB_HOST`, `SIGPT_DB_PORT`,
`SIGPT_DB_NAME`, `SIGPT_DB_USER`, `SIGPT_DB_PASSWORD`.

> El archivo local se carga con `spring.config.import=optional:file:./application-local.properties`.
> **Nunca** se versiona el secreto (RDS5 / OWASP A02).

### 3.4 Compilar y ejecutar

```bash
mvn clean verify          # compila y corre todas las pruebas
mvn spring-boot:run       # levanta la app en http://localhost:8080
```

O bien:

```bash
java -jar target/sigpt-1.0.0.jar
```

---

## 4. Usuarios de prueba

Todos usan la contraseña `Sigpt2026*`.

| Usuario | Perfil | Accesos principales |
|---|---|---|
| `jperez` | Jefe de Producción | Pedidos, clientes, cotizaciones, reportes |
| `jlogistica` | Jefe de Logística | Proveedores, insumos, RFQ, órdenes de compra, kardex |
| `supervisor` | Supervisor de Tareas | Asignación, avance, calidad, incidencias, parte diario |
| `operario` | Operario | Registrar avance |
| `almacen` | Encargado de Almacén | Insumos, kardex |
| `cliente` | Cliente | Consultar estado de pedido |
| `proveedor` | Proveedor | RFQ, órdenes de compra |
| `gerencia` | Gerencia General | Reportes (solo lectura) |

> El menú de cada perfil se arma **desde la base de datos** (`opcion` +
> `perfil_opcion`), no está cocido en el código. El filtro de permisos valida
> el acceso en **cada petición**: ocultar la opción del menú no basta.

---

## 5. Reglas de negocio dónde viven

| Regla | Ubicación | Parámetro que la controla |
|---|---|---|
| RD1 · pedido mínimo 1 000 m | `PedidoLN.registrar` | `PEDIDO_MINIMO_METROS` |
| RD2 · adelanto mínimo 40 % | `PedidoLN` (transición a En Producción) | `ADELANTO_MINIMO_PORCENTAJE` |
| RD3 · máximo 16 h en turnos consecutivos | `AsignacionLN.registrar` | `JORNADA_MAXIMA_HORAS` |
| RD4 · varianza de consumo > 15 % | `KardexLN.registrarSalida` | `VARIANZA_MAXIMA_CONSUMO_PORCENTAJE` |
| RD5 · MSDS obligatorio | `InsumoLN.registrar` | — |
| RD6 · trazabilidad del lote | `KardexLN` / `CalidadLN` | — |
| RD7 · varianza de color | `CalidadLN.registrarInspeccion` | `TOLERANCIA_COLOR_DEFAULT` |
| R‑Varianza · avance < 15 % del teórico | `AvanceLN.registrar` | `VARIANZA_MAXIMA_AVANCE_PORCENTAJE` |
| R‑Stock · alerta de stock mínimo | `KardexLN.registrarMovimiento` | — |
| 3 intentos y bloqueo de 15 min | `SeguridadLN.autenticar` | `INTENTOS_MAXIMOS_LOGIN`, `MINUTOS_BLOQUEO_CUENTA` |

**Los umbrales no están en el código**: viven en la tabla `parametro` y se cargan
al arrancar en el Singleton `ParametrosNegocio`. Un administrador puede
cambiarlos sin recompilar.

---

## 6. Seguridad implementada

- Contraseñas con **PBKDF2-HMAC-SHA256**, 210 000 iteraciones y salt aleatorio por
  usuario. La clave no es reversible y nunca se almacena en claro.
- Sesión HTTP con **cierre por inactividad de 15 minutos**.
- Menú y permisos **por perfil**, leídos de la base de datos.
- Filtro que valida el acceso en **cada petición**.
- **SQL siempre parametrizado**; nunca se concatena texto.
- **Auditoría** de toda operación crítica con código de usuario y marca de tiempo.
- Páginas de error propias sin detalle técnico al usuario.
- Secretos fuera del repositorio.

---

## 7. Documentación

| Documento | Ubicación |
|---|---|
| Documento de arquitectura (vistas 4+1) | `D:\Diseño\SIGPT-Documento-de-Arquitectura.md` |
| Decisiones de diseño D1–D10 | `D:\Diseño\SIGPT_Decisiones_de_Diseno.md` |
| Diagramas | `docs/diagramas/` |
| Esquema de base de datos | `src/main/resources/db/schema.sql` |
| Datos de prueba | `src/main/resources/db/seed.sql` |

---

## 8. Estado del proyecto

| Entregable | Contenido | Estado |
|---|---|---|
| E1 | Base de datos, esqueleto de código, módulo de seguridad, diagramas | En curso |
| E2 | Pedidos, clientes y planta | Pendiente |
| E3 | Abastecimiento, reportes y cierre técnico | Pendiente |

---

## 9. Licencia

Proyecto académico de coursework. Textiles Andinos S.A.C. y la compañía son
ficticios.
