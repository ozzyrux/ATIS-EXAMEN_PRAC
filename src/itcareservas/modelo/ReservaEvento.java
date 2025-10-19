package itcareservas.modelo;

import itcareservas.excepciones.ReglaNegocioException;
import itcareservasmodelo.enums.TipoAula;
import itcareservasmodelo.enums.TipoEvento;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaEvento extends Reserva {
    private TipoEvento tipoEvento;
    private int aforoEsperado;
    private static final long DURACION_MAX_HRS = 8;

    public ReservaEvento(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, 
                         LocalTime horaFin, String responsable, TipoEvento tipoEvento, int aforoEsperado) {
        super(id, aula, fecha, horaInicio, horaFin, responsable);
        this.tipoEvento = tipoEvento;
        this.aforoEsperado = aforoEsperado;
    }

    public TipoEvento getTipoEvento() { return tipoEvento; }
    public int getAforoEsperado() { return aforoEsperado; }
    
    @Override
    public String obtenerDetalleTipo() {
        return "Evento: " + tipoEvento.name() + " (Aforo: " + aforoEsperado + ")";
    }

    @Override
    public boolean validarReglasNegocio() throws ReglaNegocioException {
        super.validarReglasNegocio();
        
        TipoAula tipoAula = getAula().getTipo();
        
        if (tipoEvento == TipoEvento.TALLER && (tipoAula != TipoAula.LABORATORIO && tipoAula != TipoAula.TEORICA)) {
             throw new ReglaNegocioException("TALLER solo permitido en LABORATORIO o TEORICA.");
        }
        
        if (calcularDuracionHoras() > DURACION_MAX_HRS) {
            throw new ReglaNegocioException("Reserva de Evento excede la duracion maxima de " 
                                            + DURACION_MAX_HRS + " horas.");
        }
        
        if (aforoEsperado > getAula().getCapacidad()) {
             throw new ReglaNegocioException("Aforo esperado (" + aforoEsperado + 
                                             ") excede la capacidad del aula (" + getAula().getCapacidad() + ").");
        }
        return true;
    }
    
    @Override
    public String toCSV() {
        return String.format("EVENTO,%s,%s,%s,%s,%s,%s,%s,%s,%d", 
                             getId(), getAula().getId(), getFecha(), getHoraInicio(), getHoraFin(), 
                             getResponsable(), getEstado(), tipoEvento, aforoEsperado);
    }
}