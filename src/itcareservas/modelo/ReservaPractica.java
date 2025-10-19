package itcareservas.modelo;

import itcareservas.excepciones.ReglaNegocioException;
import itcareservasmodelo.enums.TipoAula;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaPractica extends Reserva {
    private String equipoNecesario;
    private static final long DURACION_MAX_HRS = 4;

    public ReservaPractica(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, 
                           LocalTime horaFin, String responsable, String equipoNecesario) {
        super(id, aula, fecha, horaInicio, horaFin, responsable);
        this.equipoNecesario = equipoNecesario;
    }

    public String getEquipoNecesario() { return equipoNecesario; }
    
    @Override
    public String obtenerDetalleTipo() {
        return "Practica (Equipo: " + equipoNecesario + ")";
    }

    @Override
    public boolean validarReglasNegocio() throws ReglaNegocioException {
        super.validarReglasNegocio();
        
        if (getAula().getTipo() != TipoAula.LABORATORIO) {
            throw new ReglaNegocioException("Reservas de Practica solo permitidas en LABORATORIO.");
        }
        
        if (calcularDuracionHoras() > DURACION_MAX_HRS) {
            throw new ReglaNegocioException("Reserva de Practica excede la duracion maxima de " 
                                            + DURACION_MAX_HRS + " horas.");
        }
        return true;
    }
    
    @Override
    public String toCSV() {
        return String.format("PRACTICA,%s,%s,%s,%s,%s,%s,%s,%s", 
                             getId(), getAula().getId(), getFecha(), getHoraInicio(), getHoraFin(), 
                             getResponsable(), getEstado(), equipoNecesario);
    }
}