package com.textilesandinos.sigpt.utilesweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.textilesandinos.sigpt.utilesweb.permisos.InterceptorPermisos;

/**
 * Configuracion web: registra el filtro de permisos en todas las rutas.
 *
 * <p>El propio interceptor decide que rutas son publicas, de modo que anadir una
 * pantalla nueva no obliga a tocar esta clase.
 */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final InterceptorPermisos interceptorPermisos;

    public ConfiguracionWeb(InterceptorPermisos interceptorPermisos) {
        this.interceptorPermisos = interceptorPermisos;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(interceptorPermisos).addPathPatterns("/**");
    }
}
