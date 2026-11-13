package com.textilesandinos.sigpt.negocio.planta;

import java.util.List;

import org.springframework.stereotype.Service;

import com.textilesandinos.sigpt.datos.comun.AuditoriaHelper;
import com.textilesandinos.sigpt.datos.planta.IncidenciaDAO;
import com.textilesandinos.sigpt.datos.planta.MaquinaDAO;
import com.textilesandinos.sigpt.entidades.planta.Incidencia;
import com.textilesandinos.sigpt.entidades.planta.Maquina;
import com.textilesandinos.sigpt.negocio.comun.ReglaNegocioException;

/**
 * Incidencias de maquinaria (flujo 3.1 de EF-1001-7).
 *
 * <p>Una averia deja la maquina en mantenimiento y se notifica al Supervisor.
 */
@Service
public class IncidenciaLN {

    private final IncidenciaDAO incidenciaDAO;
    private final MaquinaDAO maquinaDAO;
    private final AuditoriaHelper auditoria;

    public IncidenciaLN(IncidenciaDAO incidenciaDAO, MaquinaDAO maquinaDAO,
            AuditoriaHelper auditoria) {
        this.incidenciaDAO = incidenciaDAO;
        this.maquinaDAO = maquinaDAO;
        this.auditoria = auditoria;
    }

    /** Incidencias registradas. */
    public List<Incidencia> listar() {
        return incidenciaDAO.listarTodas();
    }

    /** Registra la incidencia y pasa la maquina a mantenimiento. */
    public void registrar(Incidencia incidencia, String codUsuario) {
        if (incidencia == null || incidencia.getDescripcion() == null
                || incidencia.getDescripcion().isBlank()) {
            throw new ReglaNegocioException("La descripcion de la incidencia es obligatoria.");
        }
        if (incidencia.getTipo() == null || incidencia.getTipo().isBlank()) {
            throw new ReglaNegocioException("Debe indicar el tipo de incidencia.");
        }
        incidencia.setEstado("ABIERTA");
        int id = incidenciaDAO.insertar(incidencia, codUsuario);

        if (incidencia.getIdMaquina() != null) {
            Maquina maquina = maquinaDAO.buscarPorId(incidencia.getIdMaquina()).orElse(null);
            if (maquina != null && !"MANTENIMIENTO".equals(maquina.getEstado())) {
                maquinaDAO.actualizarEstado(maquina.getIdMaquina(), "MANTENIMIENTO");
            }
        }
        auditoria.registrar(codUsuario, "PLANTA", "REGISTRAR_INCIDENCIA", "incidencia",
                String.valueOf(id), incidencia.getTipo() + ": " + incidencia.getDescripcion());
    }

    /** Cierra la incidencia. */
    public void cerrar(int idIncidencia, String codUsuario) {
        incidenciaDAO.actualizarEstado(idIncidencia, "CERRADA");
        auditoria.registrar(codUsuario, "PLANTA", "CERRAR_INCIDENCIA", "incidencia",
                String.valueOf(idIncidencia), "Incidencia cerrada");
    }
}
