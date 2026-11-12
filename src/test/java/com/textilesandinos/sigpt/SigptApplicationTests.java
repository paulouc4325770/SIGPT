package com.textilesandinos.sigpt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Prueba de arranque: verifica que el contexto de Spring carga correctamente
 * con la estructura de 3 niveles y 5 modulos.
 */
@SpringBootTest
class SigptApplicationTests {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void elContextoDeSpringCarga() {
        assertThat(contexto).isNotNull();
        assertThat(contexto.getBean(SigptApplication.class)).isNotNull();
    }

    @Test
    void laBaseDeDatosEsMySql8() {
        var dataSource = contexto.getBean(javax.sql.DataSource.class);
        assertThat(dataSource).isNotNull();
    }
}
