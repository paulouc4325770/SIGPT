package com.textilesandinos.sigpt.negocio.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.seguridad.OpcionDAO;
import com.textilesandinos.sigpt.datos.seguridad.PerfilDAO;
import com.textilesandinos.sigpt.datos.seguridad.UsuarioDAO;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;
import com.textilesandinos.sigpt.utiles.seguridad.HashHelper;

/**
 * Pruebas de la administracion del modulo de seguridad.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeguridadAdminLNTest {

    private static final String CLAVE = "Sigpt2026*";

    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private PerfilDAO perfilDAO;
    @Mock
    private OpcionDAO opcionDAO;
    @Mock
    private AuditoriaHelper auditoria;

    private SeguridadAdminLN seguridadAdminLN;

    @BeforeEach
    void setUp() {
        seguridadAdminLN = new SeguridadAdminLN(usuarioDAO, perfilDAO, opcionDAO, auditoria);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setCodUsuario("nuevo");
        usuario.setNombres("Nuevo Usuario");
        usuario.setArea("Produccion");
        usuario.setIdPerfil(1);
        return usuario;
    }

    @Test
    @DisplayName("registra el usuario con la clave cifrada y salt por usuario")
    void registraUsuarioConClaveCifrada() {
        when(usuarioDAO.insertar(any())).thenReturn(9);

        Usuario usuario = seguridadAdminLN.registrarUsuario(usuario(), CLAVE, "jperez");

        assertThat(usuario.getClaveHash()).isNotEqualTo(CLAVE);
        assertThat(HashHelper.verificar(CLAVE, usuario.getClaveHash())).isTrue();
        assertThat(usuario.isEstado()).isTrue();
        verify(auditoria).registrar(eq("jperez"), eq("seguridad"), eq("REGISTRAR_USUARIO"),
                eq("usuario"), eq("9"), anyString());
    }

    @Test
    @DisplayName("no registra un usuario con un codigo repetido")
    void rechazaUsuarioDuplicado() {
        when(usuarioDAO.existePorCodigo("nuevo")).thenReturn(true);

        assertThatThrownBy(() -> seguridadAdminLN.registrarUsuario(usuario(), CLAVE, "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya esta registrado");

        verify(usuarioDAO, never()).insertar(any());
    }

    @Test
    @DisplayName("exige una clave de al menos 8 caracteres")
    void exigeClaveLarga() {
        assertThatThrownBy(() -> seguridadAdminLN.registrarUsuario(usuario(), "corta", "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("8 caracteres");
    }

    @Test
    @DisplayName("al resetear la clave se guarda cifrada")
    void reseteaClave() {
        seguridadAdminLN.resetearClave(9, "jperez", "NuevaClave2026");

        verify(usuarioDAO).actualizarClave(eq(9), anyString());
    }

    @Test
    @DisplayName("exige perfil y nombre al registrar un usuario")
    void validaDatosDelUsuario() {
        Usuario sinPerfil = usuario();
        sinPerfil.setIdPerfil(0);
        assertThatThrownBy(() -> seguridadAdminLN.registrarUsuario(sinPerfil, CLAVE, "jperez"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("perfil");
    }
}
