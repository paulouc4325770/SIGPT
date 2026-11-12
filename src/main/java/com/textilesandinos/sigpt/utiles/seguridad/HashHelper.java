package com.textilesandinos.sigpt.utiles.seguridad;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contrasenas con PBKDF2-HMAC-SHA256 (algoritmo estandar de la JDK).
 *
 * <p>Contraste deliberado con el laboratorio, que usaba Rfc2898DeriveBytes con la
 * llave incrustada en el codigo (clave reversible con la llave en el codigo).
 * Aqui la contrasena no es reversible: solo se puede volver a verificar.
 *
 * <p>Formato almacenado: {@code iteraciones:base64(salt):base64(hash)}.
 * El salt es aleatorio y distinto por usuario, de modo que dos usuarios con la
 * misma contrasena no producen el mismo hash.
 *
 * <p>Referencia: OWASP Password Storage Cheat Sheet (PBKDF2-HMAC-SHA256,
 * 210 000 iteraciones, 32 bytes de clave derivada).
 */
public final class HashHelper {

    /** Iteraciones PBKDF2. */
    public static final int ITERACIONES = 210_000;

    /** Longitud de la clave derivada en bits. */
    private static final int LONGITUD_CLAVE_BITS = 256;

    /** Longitud del salt en bytes. */
    private static final int LONGITUD_SALT_BYTES = 16;

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private HashHelper() {
    }

    /**
     * Genera el hash de una contrasena con un salt aleatorio nuevo.
     *
     * @param clave contrasena en texto claro
     * @return cadena con el formato {@code iteraciones:salt:hash}
     */
    public static String hashear(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser nula ni vacia");
        }
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(clave, salt, ITERACIONES);
        return ITERACIONES
                + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifica una contrasena contra el hash almacenado.
     *
     * @param clave           contrasena ingresada por el usuario
     * @param hashAlmacenado  valor leido de la base de datos
     * @return {@code true} si coinciden
     */
    public static boolean verificar(String clave, String hashAlmacenado) {
        if (clave == null || hashAlmacenado == null) {
            return false;
        }
        String[] partes = hashAlmacenado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] obtenido = derivar(clave, salt, iteraciones);
            return comparacionConstante(esperado, obtenido);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] salt, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(clave.toCharArray(), salt, iteraciones,
                LONGITUD_CLAVE_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo derivar la clave con " + ALGORITMO, e);
        } finally {
            especificacion.clearPassword();
        }
    }

    /** Compara dos arreglos sin filtrar informacion por tiempo de ejecucion. */
    private static boolean comparacionConstante(byte[] esperado, byte[] obtenido) {
        if (esperado.length != obtenido.length) {
            return false;
        }
        int diferencia = 0;
        for (int i = 0; i < esperado.length; i++) {
            diferencia |= esperado[i] ^ obtenido[i];
        }
        return diferencia == 0;
    }
}
