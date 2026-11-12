package com.textilesandinos.sigpt.datos.comun;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.textilesandinos.sigpt.datos.seguridad.RegistroAuditoriaDAO;
import com.textilesandinos.sigpt.entidades.seguridad.RegistroAuditoria;

/**
 * Punto unico de registro de auditoria (RC8).
 *
 * <p>Todos los DAO llaman a este componente dentro de su misma transaccion, de
 * modo que la traza de auditoria se confirma o se revierte junto con la
 * operacion de negocio.
 */
@Component
public class AuditoriaHelper {

    private final RegistroAuditoriaDAO registroAuditoriaDAO;

    public AuditoriaHelper(RegistroAuditoriaDAO registroAuditoriaDAO) {
        this.registroAuditoriaDAO = registroAuditoriaDAO;
    }

    /** Registra una operacion del modulo indicado. */
    public void registrar(String codUsuario, String modulo, String operacion, String entidad,
            String idEntidad, String detalle) {
        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setCodUsuario(codUsuario == null || codUsuario.isBlank() ? "sistema" : codUsuario);
        registro.setFechaHora(LocalDateTime.now());
        registro.setModulo(modulo);
        registro.setOperacion(operacion);
        registro.setEntidad(entidad);
        registro.setIdEntidad(idEntidad);
        registro.setDetalle(detalle);
        registroAuditoriaDAO.registrar(registro);
    }
}
