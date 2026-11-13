# SIGPT — Decisiones de diseño (propuesta)

**Sistema:** Sistema Integral de Gestión de Producción Textil (SIGPT) — Textiles Andinos S.A.C.
**Base:** Modelo de Negocio, Especificación de Requerimientos (Semana 15) y Modelado final de Ing. de Requisitos.
**Criterio:** cada decisión se apoya en un requisito de esos documentos y en un tema visto en Diseño de Software (se indica la semana). No se usa nada que no esté en el curso, en el laboratorio de n-capas o en cursos previos (POO, BD).

> Cuando el PDF de Semana 15 y el "Modelado final" no coinciden, tomo el Modelado final porque está más completo (trae entero el caso de uso de abastecimiento y los requisitos de dominio con criterio de aceptación). Las diferencias están listadas en la sección 8.

---

## 1. Resumen de decisiones

| # | Tema | Decisión |
|---|------|----------|
| D1 | Estilo arquitectónico | Monolito modular en capas (multinivel, 3 niveles), cliente-servidor web |
| D2 | Infraestructura | On-premise, dentro de la LAN de la planta |
| D3 | Lenguaje y plataforma | Java 17 + Spring Boot (solo la parte web MVC) + MySQL 8 + Maven |
| D4 | Organización del código | Los 3 niveles y capas del laboratorio; dentro de cada capa, un paquete por módulo de negocio |
| D5 | Presentación | MVC con vistas generadas en el servidor; dos familias de pantallas (oficina y planta táctil) |
| D6 | Lógica de negocio | Clases de negocio (LN) como fachada de cada módulo; ahí viven las reglas RD1–RD8 |
| D7 | Acceso a datos | Clases DAO con JDBC y SQL parametrizado, detrás de interfaces |
| D8 | Seguridad | Login propio por sesión; menú y permisos por perfil leídos de la BD |
| D9 | Patrones | Fachada, Template Method, Estrategia, Factoría, Adapter, Singleton; State y Observer como segunda prioridad |
| D10 | Proceso | Scrum con sprints de 2 semanas; documento de arquitectura con vistas 4+1 |

---

## 2. Qué condiciona el diseño

**Alcance funcional:** 7 casos de uso del sistema (EF-1001-2 a EF-1001-8): Login, Pedido Textil, Clientes, Proveedores e Insumos, Asignar Operarios y Maquinaria, Avance y Control de Calidad, Reportes Gerenciales. 8 actores. Fuera de alcance: contabilidad, planillas y SUNAT.

**Requisitos que más pesan en la arquitectura** (Semana 3: la arquitectura se decide por los requisitos no funcionales):

| Requisito | Qué obliga a decidir |
|-----------|----------------------|
| Restricción: intranet cerrada, acceso externo solo por VPN; proteger clientes y fórmulas de tintes (RNF02) | On-premise; nada en nube pública salvo el respaldo |
| RNF03: disponible los 3 turnos, tolerancia a fallos en terminales | Terminales sin estado propio: todo se guarda en el servidor, cualquier terminal sirve |
| RNF01: respuesta < 2 s en consultas de planta | Pocas capas físicas, consultas simples con índices, sin llamadas entre servicios |
| RNF04: táctil minimalista para operarios + web responsiva para oficina | Dos familias de vistas sobre la misma lógica |
| RNF05: modular, que permita agregar módulos | Módulos por proceso de negocio con alta cohesión y bajo acoplamiento |
| RNF06 / RDS1: Windows en terminales, servidor Linux, MySQL 8, Java 17 | Aplicación web (el terminal solo necesita navegador) |
| RNF08 + RD6: auditoría y trazabilidad de insumo a despacho | Una sola BD relacional con llaves foráneas y transacciones |
| RDS6: pruebas unitarias con JUnit | La lógica de negocio debe poder probarse sin BD → interfaces e inyección |

Prioridad MoSCoW de los propios documentos: RNF01, RNF02, RNF03 y RNF07 son obligatorios; el resto, deseables. Si hay conflicto, mandan esos cuatro.

---

## 3. Arquitectura

### 3.1 Estilo: monolito modular en capas (D1)

Una sola aplicación web desplegable, organizada en capas (presentación, negocio, datos) y dividida por dentro en módulos.

| Alternativa (Semana 3) | Por qué no |
|------------------------|-----------|
| Microservicios | Exigen descubrimiento de servicios, balanceo, trazas distribuidas y transacciones distribuidas. Somos un equipo chico, hay una sola planta y RD6 pide trazabilidad completa, que es mucho más simple con una sola BD y transacciones locales. La diapositiva misma lo dice: complejidad alta en gestión y orquestación. |
| Basada en eventos | Sirve para sistemas distribuidos y asíncronos. Aquí las alertas (stock mínimo, varianza) se resuelven dentro de la misma aplicación con un patrón de diseño, sin colas ni mensajería. |
| Monolito sin capas | Es lo más rápido al inicio, pero mezcla pantalla, reglas y SQL, y eso choca con RNF05 (mantenibilidad) y con cohesión/acoplamiento de Semana 1. |

Lo que se gana: simplicidad de desarrollo, prueba y despliegue (un solo artefacto), y comunicación eficiente entre componentes, que ayuda con RNF01. La desventaja conocida del monolito (hay que redesplegar todo ante un cambio) es aceptable para una sola planta; la división en módulos deja abierta una separación futura.

Se combina con **cliente-servidor**: el cliente es el navegador de la PC de oficina o del terminal en modo kiosko; el servidor concentra lógica y datos. Combinar estilos es válido según Semana 3.

### 3.2 Infraestructura on-premise (D2)

El Modelo de Negocio restringe el sistema a una intranet cerrada, con acceso externo solo por VPN, y ya presupuesta servidor propio, sala técnica y UPS. La nube aportaría elasticidad que una planta con usuarios fijos no necesita, y sacaría de la empresa la cartera de clientes y las fórmulas de tintes. Único uso externo: la copia diaria de respaldo (RNF07), que el documento ya permite guardar en nube o servidor secundario.

### 3.3 Niveles y capas (D4)

Se respeta la estructura del laboratorio "Menús y Seguridad en capas" (3 niveles, n capas):

```
02 FrontEnd     presentacion   controladores MVC + vistas
01 BackEnd      negocio        clases LN: reglas, estados, reportes
                datos          interfaces DAO + implementación JDBC
03 Transversal  entidades      clases del dominio
                utiles         hash de claves, log, correo, exportación
                utilesweb      filtros de sesión y permisos, armado del menú
```

**Reglas de dependencia**

- presentacion → negocio → datos. Todas pueden ver entidades y utiles.
- presentacion **no** ve datos. En el laboratorio la capa web sí podía ver AccesoDatos; aquí se prohíbe porque en Semana 3 cada capa habla solo con la inmediatamente inferior, y así ninguna pantalla puede saltarse una regla de negocio.
- negocio depende de las **interfaces** DAO, no de la implementación (DIP, Semana 6).
- entidades y utiles no dependen de nadie.

### 3.4 Módulos (D4)

Dentro de cada capa hay un paquete por módulo. La división sigue los procesos de negocio (Semana 1: los módulos se definen por la lógica del negocio), que son los tres casos de uso del negocio más dos transversales:

| Módulo | Casos de uso | Proceso de negocio |
|--------|--------------|--------------------|
| seguridad | EF-1001-2 | Transversal |
| pedidos | EF-1001-3, EF-1001-4 | Atención al Cliente |
| abastecimiento | EF-1001-5 | Gestión de Materias Primas |
| planta | EF-1001-6, EF-1001-7 | Gestión de Tareas de Operarios |
| reportes | EF-1001-8 | Transversal (solo lectura) |

Un módulo usa a otro únicamente a través de su clase LN (por ejemplo, planta le pregunta a pedidos por las órdenes "En Producción"); nunca toca sus tablas ni sus DAO. Eso da la independencia funcional de Semana 1 y permite que cada integrante trabaje un módulo en paralelo.

### 3.5 Vista física

| Nodo | Contenido |
|------|-----------|
| Servidor de aplicación (Linux Ubuntu) | La aplicación SIGPT (un solo .jar) |
| Servidor de BD (Linux) | MySQL 8. Puede ser la misma máquina al inicio y separarse después |
| PC de oficina (Windows 10/11) | Navegador: Jefes, Logística, Gerencia |
| Terminal táctil de planta (Windows, kiosko, WiFi) | Navegador limitado al SIGPT: Operario, Supervisor, Almacén |
| Usuarios externos (Cliente, Proveedor, Gerencia fuera de planta) | Navegador a través de la VPN |
| Respaldo externo | Copia diaria de BD y configuración |

---

## 4. Plataforma tecnológica (D3)

### 4.1 Matriz de evaluación

Semana 3 pide elegir la plataforma con una matriz de criterios ponderados y dejar la razón documentada. Los dos candidatos reales son el stack que ya fijan los requisitos y el stack del laboratorio.

| Criterio | Peso | A: Java 17 + Spring Boot + MySQL | B: C# ASP.NET MVC + SQL Server (laboratorio) |
|----------|------|:--:|:--:|
| Cumple los requisitos ya aprobados (RNF05, RNF06, RDS1) | 30 % | 5 | 1 |
| Coincide con lo practicado en el curso | 25 % | 3 | 5 |
| Experiencia del equipo | 20 % | 4 | 3 |
| Costo y encaje con servidor Linux on-premise | 15 % | 5 | 2 |
| Madurez y soporte | 10 % | 5 | 3 |
| **Total ponderado** | 100 % | **4.30** | **2.75** |

Notas sobre los puntajes:

- B saca 1 en requisitos porque obliga a cambiar RNF05, RNF06 y RDS1 por el proceso de gestión de cambios (EF-1001-15), y ASP.NET MVC como en el laboratorio corre sobre Windows, no sobre el servidor Linux pedido.
- A saca 3 y no 1 en "curso" porque los ejemplos de patrones de las clases (Adapter, Template Method, Factoría, Singleton, Prototipo) están en Java y porque la arquitectura n-capas no depende del lenguaje (ver 4.3).
- "Experiencia del equipo" es el único criterio que no sale de los documentos: el Modelado dice que el equipo tiene experiencia en Java, pero ajusten ese puntaje a su realidad.

**Decisión: opción A.** Lo que la haría cambiar: que el profesor exija entregar en .NET como el laboratorio. En ese caso el resto de este documento se mantiene igual, solo cambia la tabla 4.2 y hay que registrar el cambio de RNF05/RNF06/RDS1.

### 4.2 Stack

| Elemento | Elección | Justificación |
|----------|----------|---------------|
| Lenguaje | Java 17 | Lo fija RDS1; es orientado a objetos y es el lenguaje de los ejemplos de patrones del curso |
| Framework web | Spring Boot, usando solo MVC e inyección de dependencias | Lo fija RNF05. Hace el papel de ASP.NET MVC del laboratorio |
| Vistas | Plantillas HTML en el servidor (Thymeleaf) + Bootstrap | Equivale a las vistas Razor del laboratorio; no se necesita un frontend separado. Se puede reutilizar la plantilla AdminLTE que ya se usó |
| Base de datos | MySQL 8 (InnoDB) | Lo fija RNF06; transacciones y llaves foráneas para RD6 |
| Acceso a datos | JDBC con SQL parametrizado dentro de los DAO | Igual que las clases DA del laboratorio; solo requiere SQL de BD. Sin ORM |
| Construcción | Maven | RDS1 |
| Pruebas | JUnit 5 | RDS6 |
| Versiones | Git + GitHub con Pull Request | RDS3, RDS4 |
| Modelado | StarUML (UML 2.5) | Estándar del Modelo de Negocio |

**Versión de Spring Boot:** RDS1 dice 3.x, pero esa rama dejó de recibir parches gratuitos el 30/06/2026 ([anuncio de Spring](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/)). Conviene usar la 4.x vigente, que sigue aceptando Java 17, y registrarlo como cambio menor de RDS1.

### 4.3 Lo aprendido en el laboratorio se traslada directo

| Laboratorio (.NET) | SIGPT (Java) |
|--------------------|--------------|
| Solución con 01 BackEnd / 02 FrontEnd / 03 Transversal | Paquetes negocio+datos / presentacion / entidades+utiles+utilesweb |
| Controlador (UsuarioController) | Controlador MVC |
| Vista Razor + _Layout | Plantilla HTML + plantilla base |
| UsuarioLN | UsuarioLN |
| UsuarioDA + LlenarEntidad | UsuarioDAO (interfaz) + implementación JDBC |
| Tablas Usuario y Opcion, MenuHelper | Lo mismo, más Perfil |
| VariablesWeb (usuario y opciones en sesión) | Usuario y opciones en la sesión HTTP |
| Filtros y páginas de error 500/404 | Interceptor de sesión/permisos y páginas de error |
| log4net | Log estándar de la plataforma |
| web.config | Archivo de propiedades |

**Diferencias deliberadas con el laboratorio**

1. La capa web no ve la capa de datos (3.3).
2. LN no hace `new UsuarioDA()`: recibe la interfaz por constructor. Es el mismo caso del ejemplo de DIP de Semana 6 (`new EmailService()`).
3. La clave se guarda con hash de un solo sentido, no con cifrado reversible con la llave en el código.
4. La cadena de conexión y otras claves van fuera del repositorio (RDS5).
5. El menú se filtra por perfil; en el laboratorio se listaban todas las opciones.

---

## 5. Diseño detallado

### 5.1 Clases del dominio por módulo

Salen de las listas de entidades de los tres casos de uso del negocio.

| Módulo | Clases |
|--------|--------|
| seguridad | Usuario, Perfil, Opcion, RegistroAuditoria |
| pedidos | Cliente, Pedido, FichaTecnica, Cotizacion, Adelanto, OrdenProduccion |
| abastecimiento | Proveedor, Insumo, SolicitudCotizacion (RFQ), CotizacionProveedor, OrdenCompra, MovimientoKardex |
| planta | Operario, Maquina, Turno, Asignacion, RegistroAvance, Lote, InspeccionCalidad, Incidencia |
| reportes | ReporteGerencial y sus cuatro tipos, FormatoSalida |

Decisiones de modelado (Semana 5 y repaso de POO):

- **Pedido compone a FichaTecnica**: la ficha no existe sin su pedido (composición).
- **Pedido y OrdenProduccion son clases distintas**: la orden interna nace recién cuando el pedido pasa a "En Producción" (RD2).
- **Usuario no es Operario**: Usuario es la cuenta de acceso; Operario es el recurso de planta que se asigna a turnos. Se asocian, no se heredan.
- **Los perfiles son datos, no subclases**: no hay `JefeProduccion extends Usuario`. Los ocho perfiles se diferencian por sus opciones de menú (tabla), no por comportamiento. Evita herencia que no cumpliría Liskov y permite crear un perfil sin tocar código.
- **Maquina con atributo tipo (telar/teñidora) y estado**, sin subclases mientras no tengan comportamiento distinto.
- **Asignacion** es la clase que une Operario, Maquina, Turno, fecha y OrdenProduccion, con su meta de metros.
- **Turno, estado de máquina y resultado de calidad** son enumeraciones (valores fijos en los requisitos).

### 5.2 Dónde vive cada regla de negocio (D6)

Todas en la capa de negocio. La pantalla solo adelanta la validación para dar respuesta rápida; la BD solo protege integridad.

| Regla | Clase responsable | Comportamiento |
|-------|-------------------|----------------|
| RD1: mínimo 1 000 m | PedidoLN al registrar | No guarda y avisa |
| RD2: adelanto ≥ 40 % | Transición Cotizado → En Producción del Pedido | Sin adelanto verificado no hay orden interna |
| Cotización vence si no llega el adelanto (plazo configurable; el documento pone 15 días como ejemplo) | PedidoLN, revisión diaria | El pedido no avanza |
| RD3: no más de 16 h seguidas | AsignacionLN | Bloquea la asignación y dice por qué |
| Máquina ocupada o en mantenimiento | AsignacionLN | Rechaza y sugiere la de respaldo |
| Varianza > 15 % | AvanceLN al registrar avance | Genera alerta, no bloquea |
| Lote no conforme | AvanceLN al inspeccionar | Lote queda como Merma; el progreso no sube |
| RD5: MSDS en químicos y tintes | InsumoLN | No registra sin el archivo |
| Stock mínimo | KardexLN en cada movimiento | Genera alerta visual |
| 3 intentos fallidos, sesión de 15 min | SeguridadLN / filtro web | Bloqueo temporal, cierre por inactividad |
| RNF08: usuario y hora en todo registro | Un solo punto en la capa de datos | Columnas de auditoría + tabla histórica |

Los números (1 000, 40, 15, 16…) no se escriben en el código: van en una tabla de parámetros (ver Singleton).

### 5.3 Patrones de diseño (D9)

Solo donde hay un problema real que el patrón resuelve; un patrón puesto a la fuerza agrega clases sin beneficio.

**Prioridad 1 — vistos en clase**

| Patrón | Dónde | Problema que resuelve |
|--------|-------|-----------------------|
| **Capas + MVC** (Semana 3, laboratorio) | Toda la aplicación | Separa pantalla, reglas y datos |
| **Fachada** (Semana 12) | Cada clase LN es la puerta de entrada de su módulo. `PedidoLN.registrar()` coordina validación, viabilidad, generación de código, guardado y auditoría | El controlador hace una sola llamada y no conoce las clases internas. Es el uso que da la clase: la fachada como punto de entrada de cada subsistema al dividir en capas |
| **Template Method** (Semana 13) | `ReporteGerencial.generar()`: validar filtros → consultar → calcular indicadores → armar tablero → registrar auditoría. Las cuatro subclases (Avance de Pedidos, Productividad, Consumo y Merma, Eficiencia Industrial) solo redefinen consultar y calcular | El flujo de EF-1001-8 es idéntico para los cuatro reportes; se escribe una vez y no se duplica |
| **Estrategia** (Semana 13) | `FormatoSalida` con Pantalla, PDF y Excel. Se usa en reportes y en la exportación de la bandeja de pedidos | El usuario elige el formato en ejecución; agregar uno nuevo es una clase más, sin `if` por formato |
| **Factoría** (Semanas 11 y 13) | `FabricaReportes.crear(tipo)` y `FabricaFormatos.crear(formato)`, como el `RelojFactory` de la práctica | El controlador pide "el reporte de productividad" sin conocer la clase concreta |
| **Adapter** (Semana 12) | `INotificador` implementado por un adaptador de la librería de correo (envío de la Orden de Compra). Lo mismo para las librerías de PDF y Excel, y a futuro para SUNAT o contabilidad | El negocio usa una interfaz propia; cambiar de librería o sumar una integración no lo toca |
| **Singleton** (Semana 11) | `ParametrosNegocio`: una sola instancia con los valores de las reglas, cargada desde la BD | Todos leen los mismos valores desde un único punto. Las clases LN y DAO también son instancia única (las crea el framework). Ojo: nada que sea de un usuario va en un singleton; eso va en la sesión |

Reportes × formatos forman además la estructura del **Bridge** de Semana 12 (la abstracción Reporte y la implementación Formato varían por separado): 4 reportes y 3 formatos dan 7 clases en vez de 12.

**Prioridad 2 — del catálogo GoF del curso, si el tiempo alcanza**

| Patrón | Dónde | Alternativa mínima |
|--------|-------|--------------------|
| **State** | Ciclo de vida del Pedido: Registrado, Cotizado, En Producción, Terminado, Despachado, Anulado. Lo que se puede hacer depende del estado | Enumeración `EstadoPedido` y un solo método en Pedido que valida las transiciones permitidas |
| **Observer** | Alertas: stock mínimo, varianza, avería, cotización vencida. El negocio avisa y se suscriben el tablero, el correo y la auditoría | Llamar directo a un `ServicioAlertas` desde cada LN |

En ambos casos primero va el diagrama de estados del Pedido (Semana 7, que usa justo un Pedido como ejemplo).

**Evaluados y descartados**

| Patrón | Motivo |
|--------|--------|
| Factoría Abstracta | No hay familias de objetos intercambiables (una sola plataforma, una sola BD) |
| Prototipo | Solo serviría si entra al alcance copiar fichas técnicas del portafolio de la empresa (flujo alterno 4.1 del negocio). Hoy no hay caso de uso |
| Singleton hecho a mano para la conexión | El pool de conexiones ya lo administra la plataforma |

### 5.4 SOLID aplicado (Semana 6)

| Principio | Cómo se cumple en SIGPT |
|-----------|-------------------------|
| **SRP** | Controlador: atender la petición. LN: reglas. DAO: SQL. Entidad: datos y validaciones propias. En reportes se separa calcular, dar formato y auditar: el mismo caso de `InformeDeVentas` de la clase |
| **OCP** | Reporte nuevo, formato nuevo o canal de notificación nuevo = clase nueva. No se modifica lo que ya está probado |
| **LSP** | Herencia solo en la jerarquía de reportes, donde cualquier subclase puede reemplazar a `ReporteGerencial`. Perfiles y tipos de máquina no se modelan con herencia |
| **ISP** | Interfaces chicas. Ejemplo: `ConsultaPedido` (solo lectura) para Cliente y Gerencia, separada de la interfaz completa del Jefe de Producción. Un DAO por entidad principal, no uno gigante |
| **DIP** | LN depende de `PedidoDAO`, `INotificador`, `FormatoSalida`; la implementación llega por constructor. Así se prueba la regla del 40 % con un DAO falso, sin BD |

### 5.5 Datos (D7)

- Modelo relacional normalizado; una tabla por entidad de 5.1. Llaves foráneas en toda la cadena insumo → kardex → lote → orden → pedido → despacho (RD6).
- Los DAO usan sentencias parametrizadas, nunca SQL armado concatenando texto (RDS5, inyección SQL).
- Procedimientos almacenados no: el laboratorio los usa, pero aquí las reglas quedan en un solo lugar (capa de negocio) y los DAO solo hacen altas, cambios y consultas.
- Transacción por operación de negocio, controlada desde LN (ejemplo: registrar ingreso al Kardex y actualizar stock, o todo o nada).
- Columnas de auditoría en cada tabla y tabla histórica (RNF08).
- Archivos MSDS en carpeta del servidor; en la BD solo la ruta.
- Para RNF01: índices en llaves foráneas, estado y fecha; bandejas paginadas; reportes siempre con rango de fechas.
- Tablas de seguridad: Usuario, Perfil, Opcion y Perfil–Opcion. Es la tabla Opcion del laboratorio más el perfil, que la Matriz de Accesos (Anexo 2) pide para armar el menú.

### 5.6 Seguridad (D8)

- Autenticación propia con sesión, como en el laboratorio. No se usa un framework de seguridad aparte porque no se ha visto y oculta justo la parte que hay que diseñar.
- Al iniciar sesión se cargan en la sesión el usuario, su perfil y sus opciones; con eso se arma el menú (EF-1001-2).
- Ocultar la opción del menú no basta: un filtro revisa en **cada petición** que haya sesión y que la dirección pedida esté entre las opciones del perfil.
- Clave con hash usando una librería conocida; nunca un algoritmo propio.
- Perfiles de solo lectura (Gerencia, Cliente) reciben interfaces de solo consulta (ISP).
- Errores: páginas propias y registro en log, sin mostrar detalles técnicos al usuario.
- Lo de red (VPN, kiosko, antivirus) es de infraestructura; la aplicación no lo implementa.

### 5.7 Interfaz de usuario (D5, Semana 9)

Dos plantillas base sobre los mismos controladores y la misma lógica (igual que el laboratorio tiene `_Layout` y `_LayoutLogin`):

| | Oficina | Planta táctil |
|--|---------|---------------|
| Usuarios | Jefes, Logística, Gerencia, Cliente, Proveedor | Operario, Supervisor, Almacén |
| Diseño | Menú lateral, bandejas con búsqueda, formularios completos, tableros | Botones grandes, un dato por pantalla, teclado numérico, 3 toques o menos |
| Casos de uso | EF-1001-3, 4, 5, 8 | EF-1001-6, 7 |

Heurísticas de Nielsen que se aplican de forma concreta:

- **Visibilidad del estado**: barra de progreso del pedido, semáforo en el tablero, confirmación tras cada registro.
- **Lenguaje del usuario**: lote, telar, merma, turno. Nada de términos técnicos.
- **Prevención de errores**: listas para turno y máquina, selector de fecha, aviso inmediato si el metraje es menor a 1 000 m.
- **Reconocer antes que recordar**: el operario ve solo su orden asignada, no escribe códigos.
- **Consistencia**: botones siempre en el mismo orden (Cancelar, Grabar), mismos colores para los mismos estados.
- **Mensajes de error útiles**: los que ya define la especificación, con qué falta y cómo corregirlo.

Antes de construir: validar los prototipos con supervisores usando wireframes de baja fidelidad (es la recomendación 1 del documento de requisitos y el diseño centrado en el usuario de Semana 9).

---

## 6. Lo que no se usa, a propósito

| Descartado | Razón |
|------------|-------|
| Microservicios, colas de mensajes, contenedores | Complejidad operativa que el problema no pide |
| Frontend separado (SPA) y API REST | Con vistas en el servidor alcanza. La API tipo Web API del laboratorio queda para cuando haya integraciones (SUNAT, contabilidad) |
| ORM | Agrega conceptos no vistos; JDBC con DAO es lo que ya se practicó |
| Framework de seguridad completo | Ver 5.6 |
| Modo sin conexión en terminales | Fuera de alcance. Contingencia: el registro manual en paralelo que ya prevé EF-1001-10 |
| Nube | Restricción de intranet cerrada |

---

## 7. Documentación y orden de construcción (D10)

**Documento de arquitectura** con las vistas de Semana 3:

| Vista | Diagramas |
|-------|-----------|
| Escenarios | Casos de uso del sistema y prototipos (ya existen) |
| Lógica | Diagrama de paquetes (capas y módulos), diagrama de clases por módulo |
| Procesos | Secuencia de Registrar Pedido, Asignar Operarios y Registrar Avance; estados del Pedido |
| Implementación | Diagrama de componentes |
| Física | Diagrama de despliegue (3.5) |
| Decisiones | Este documento |

**Orden sugerido** (Scrum, sprints de 2 semanas, índice de Semana 9):

1. Arquitectura base + seguridad (login, perfiles, menú): es el laboratorio llevado a Java.
2. Clientes y Pedido con sus estados.
3. Asignación y Avance/Calidad: el núcleo operativo que los requisitos piden priorizar; necesita que exista un pedido "En Producción".
4. Abastecimiento y Kardex.
5. Reportes y exportación.

---

## 8. Pendientes por confirmar

**Con el profesor**

1. Si el proyecto debe entregarse en .NET como el laboratorio (cambia solo 4.2).
2. Si hay que implementar los 7 casos de uso o un subconjunto.

**Inconsistencias en los documentos base** (conviene cerrarlas antes de diagramar)

| # | Hallazgo | Propuesta |
|---|----------|-----------|
| 1 | El código EF-1001-5 se usa para "Gestionar Proveedores e Insumos", "Registrar Insumos" y "Editar Cliente" | Renumerar; Editar Cliente va dentro de Gestionar Clientes |
| 2 | En el PDF, EF-1001-5 solo desarrolla "Registrar Insumos"; el Modelado final tiene el flujo completo (RFQ, orden de compra, recepción) | Tomar el del Modelado final |
| 3 | "Consultar Estado de Pedido" está en la Matriz de Accesos pero no tiene especificación | Especificarlo o tratarlo como consulta de solo lectura dentro de pedidos |
| 4 | Reportes: la matriz le da acceso al Supervisor, pero EF-1001-8 solo nombra a Jefe de Producción y Gerencia | Definir cuál manda |
| 5 | Bajo el 15 % hay dos reglas distintas: varianza del avance contra la velocidad teórica del telar (EF-1001-7) y consumo de insumos contra el teórico (RD4) | Separarlas en dos reglas con nombre propio |
| 6 | El pedido "genera el código de Orden de Producción" al registrarse, pero la orden interna solo puede emitirse con el 40 % | Código de pedido al registrar; orden interna al pasar a En Producción (ya asumido en 5.1) |
| 7 | La avería "notifica a Mantenimiento", pero no hay actor ni caso de uso de mantenimiento (el propio Modelado lo deja como recomendación) | Por ahora solo registrar la incidencia y cambiar el estado de la máquina |
| 8 | El despacho lo hace el Encargado de Almacén, pero no hay flujo que lleve el pedido a "Despachado" | Agregar el paso o dejarlo fuera del alcance de forma explícita |
| 9 | RDS1 fija Spring Boot 3.x, ya sin soporte gratuito | Cambio menor a 4.x (ver 4.2) |
