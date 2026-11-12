package com.textilesandinos.sigpt.negocio.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
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
import com.textilesandinos.sigpt.datos.seguridad.UsuarioDAO;
import com.textilesandinos.sigpt.entidades.seguridad.Opcion;
import com.textilesandinos.sigpt.entidades.seguridad.Usuario;
import com.textilesandinos.sigpt.negocio.comun.ParametrosNegocio;
import com.textilesandinos.sigpt.utiles.seguridad.HashHelper;

/**
 * Pruebas del modulo de seguridad: autenticacion, intentos fallidos y bloqueo
 * temporal de la cuenta.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeguridadLNTest {

    private static final String CLAVE = "Sigpt2026*";

    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private OpcionDAO opcionDAO;
    @Mock
    private AuditoriaHelper auditoria;
    @Mock
    private ParametrosNegocio parametros;

    private SeguridadLN seguridadLN;

    @BeforeEach
    void setUp() {
        seguridadLN = new SeguridadLN(usuarioDAO, opcionDAO, auditoria, parametros);
        when(parametros.entero("INTENTOS_MAXIMOS_LOGIN")).thenReturn(3);
        when(parametros.entero("MINUTOS_BLOQUEO_CUENTA")).thenReturn(15);
    }

    private Usuario usuarioCon(String claveHash, int intentos, LocalDateTime bloqueadoHasta) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCodUsuario("jperez");
        usuario.setClaveHash(claveHash);
        usuario.setNombres("Jorge Perez");
        usuario.setIdPerfil(1);
        usuario.setIntentosFallidos(intentos);
        usuario.setBloqueadoHasta(bloqueadoHasta);
        usuario.setEstado(true);
        return usuario;
    }

    @Test
    @DisplayName("con credenciales correctas autentica y carga las opciones del perfil")
    void autenticaConCredencialesCorrectas() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 0, null);
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));
        when(opcionDAO.listarPorPerfil(1)).thenReturn(List.of(new Opcion()));

        ResultadoAutenticacion resultado = seguridadLN.autenticar("jperez", CLAVE);

        assertThat(resultado.esExitoso()).isTrue();
        assertThat(resultado.getUsuario()).isSameAs(usuario);
        verify(usuarioDAO).registrarAcceso(eq(1), any(LocalDateTime.class));
        verify(usuarioDAO).actualizarIntentos(eq(1), eq(0), eq(null));
        verify(auditoria).registrar(eq("jperez"), eq("seguridad"), eq("LOGIN"), anyString(),
                anyString(), any());
    }

    @Test
    @DisplayName("con contrasena incorrecta aumenta el contador de intentos")
    void cuentaIntentoFallido() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 0, null);
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = seguridadLN.autenticar("jperez", "incorrecta");

        assertThat(resultado.esExitoso()).isFalse();
        assertThat(resultado.getMensaje()).isEqualTo("Usuario o contrasena incorrectos");
        verify(usuarioDAO).actualizarIntentos(eq(1), eq(1), eq(null));
    }

    @Test
    @DisplayName("alcanzado el maximo de intentos, la cuenta queda bloqueada")
    void bloqueaCuentaAlSuperarIntentos() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 2, null);
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = seguridadLN.autenticar("jperez", "incorrecta");

        assertThat(resultado.esExitoso()).isFalse();
        assertThat(resultado.isCuentaBloqueada()).isTrue();
        verify(usuarioDAO).actualizarIntentos(eq(1), eq(3), any(LocalDateTime.class));
        verify(auditoria).registrar(eq("jperez"), eq("seguridad"), eq("CUENTA_BLOQUEADA"), anyString(),
                anyString(), any());
    }

    @Test
    @DisplayName("una cuenta bloqueada no autentica aunque la contrasena sea correcta")
    void noAutenticaCuentaBloqueada() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 3,
                LocalDateTime.now().plusMinutes(10));
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = seguridadLN.autenticar("jperez", CLAVE);

        assertThat(resultado.esExitoso()).isFalse();
        assertThat(resultado.isCuentaBloqueada()).isTrue();
        verify(usuarioDAO, never()).registrarAcceso(anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("un usuario inexistente recibe el mismo mensaje que una contrasena incorrecta")
    void noRevelaQueUsuariosExisten() {
        when(usuarioDAO.buscarPorCodigo("fantasma")).thenReturn(Optional.empty());

        ResultadoAutenticacion resultado = seguridadLN.autenticar("fantasma", CLAVE);

        assertThat(resultado.esExitoso()).isFalse();
        assertThat(resultado.getMensaje()).isEqualTo("Usuario o contrasena incorrectos");
    }

    @Test
    @DisplayName("un usuario inactivo no puede autenticarse")
    void rechazaUsuarioInactivo() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 0, null);
        usuario.setEstado(false);
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = seguridadLN.autenticar("jperez", CLAVE);

        assertThat(resultado.esExitoso()).isFalse();
        assertThat(resultado.getMensaje()).contains("inactivo");
    }

    @Test
    @DisplayName("cada intento fallido deja traza en la auditoria")
    void registraAuditoriaDeIntentosFallidos() {
        Usuario usuario = usuarioCon(HashHelper.hashear(CLAVE), 0, null);
        when(usuarioDAO.buscarPorCodigo("jperez")).thenReturn(Optional.of(usuario));

        seguridadLN.autenticar("jperez", "incorrecta");

        verify(auditoria, times(1)).registrar(eq("jperez"), eq("seguridad"), eq("LOGIN_FALLIDO"),
                anyString(), anyString(), any());
    }
}
