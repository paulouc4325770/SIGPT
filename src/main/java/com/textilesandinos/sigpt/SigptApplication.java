package com.textilesandinos.sigpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion SIGPT.
 *
 * <p>Estructura en 3 niveles y 5 modulos (D4 del Documento de Arquitectura):
 * <ul>
 *   <li>{@code presentacion} - controladores MVC y vistas Thymeleaf</li>
 *   <li>{@code negocio} - clases LN, fachada de cada modulo, con las reglas RD1-RD8</li>
 *   <li>{@code datos} - interfaces DAO e implementaciones JDBC</li>
 *   <li>{@code entidades} - clases del dominio (nombres en español, RDS4)</li>
 *   <li>{@code utiles} / {@code utilesweb} - hash, log, correo, exportacion, filtros de sesion</li>
 * </ul>
 */
@SpringBootApplication
public class SigptApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigptApplication.class, args);
    }
}
