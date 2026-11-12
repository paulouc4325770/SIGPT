package com.textilesandinos.sigpt.utiles.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HashHelperTest {

    @Test
    @DisplayName("una contrasena correcta verifica contra su hash")
    void verificaClaveCorrecta() {
        String hash = HashHelper.hashear("Sigpt2026*");
        assertThat(HashHelper.verificar("Sigpt2026*", hash)).isTrue();
    }

    @Test
    @DisplayName("una contrasena incorrecta no verifica")
    void rechazaClaveIncorrecta() {
        String hash = HashHelper.hashear("Sigpt2026*");
        assertThat(HashHelper.verificar("otra-clave", hash)).isFalse();
    }

    @Test
    @DisplayName("el hash no contiene la contrasena en claro")
    void noGuardaClaveEnClaro() {
        String hash = HashHelper.hashear("Sigpt2026*");
        assertThat(hash).doesNotContain("Sigpt2026*");
        assertThat(hash).contains(":").hasSizeGreaterThan(40);
    }

    @Test
    @DisplayName("dos usuarios con la misma contrasena producen hashes distintos (salt aleatorio)")
    void generaSaltDistintoPorUsuario() {
        String hash1 = HashHelper.hashear("misma-clave");
        String hash2 = HashHelper.hashear("misma-clave");
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(HashHelper.verificar("misma-clave", hash1)).isTrue();
        assertThat(HashHelper.verificar("misma-clave", hash2)).isTrue();
    }

    @Test
    @DisplayName("un hash con formato invalido no revienta la verificacion")
    void toleraHashInvalido() {
        assertThat(HashHelper.verificar("clave", "no-es-un-hash")).isFalse();
        assertThat(HashHelper.verificar("clave", null)).isFalse();
        assertThat(HashHelper.verificar(null, "210000:abc:def")).isFalse();
    }

    @Test
    @DisplayName("no se puede hashear una contrasena vacia")
    void rechazaClaveVacia() {
        assertThatThrownBy(() -> HashHelper.hashear(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
