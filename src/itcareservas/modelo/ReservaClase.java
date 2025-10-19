package itcareservas.modelo;

import itcareservas.excepciones.ReglaNegocioException;
import itcareservasmodelo.enums.TipoAula;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaClase extends Reserva {
    private String materia;
    private String grupo;
    private static final long DURACION_MAX_HRS = 3;

    public ReservaClase(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, 
                        LocalTime horaFin, String responsable, String materia, String grupo) {
        super(id, aula, fecha, horaInicio, horaFin, responsable);
        this.materia = materia;
        this.grupo = grupo;
    }

    public String getMateria() { return materia; }
    public String getGrupo() { return grupo; }
    
    @Override
    public String obtenerDetalleTipo() {
        return "Clase: " + materia + " (" + grupo + ")";
    }

    @Override
    public boolean validarReglasNegocio() throws ReglaNegocioException {
        super.validarReglasNegocio(); 
        
        TipoAula tipoAula = getAula().getTipo();
        
        if (tipoAula == TipoAula.AUDITORIO) {
            throw new ReglaNegocioException("Reservas de Clase no permitidas en AUDITORIO.");
        }
        
        if (calcularDuracionHoras() > DURACION_MAX_HRS) {
            throw new ReglaNegocioException("Reserva de Clase excede la duracion maxima de " 
                                            + DURACION_MAX_HRS + " horas.");
        }
        return true;
    }
    
    @Override
    public String toCSV() {
        return String.format("CLASE,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
                             getId(), getAula().getId(), getFecha(), getHoraInicio(), getHoraFin(), 
                             getResponsable(), getEstado(), materia, grupo);
    }
}