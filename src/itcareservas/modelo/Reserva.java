package itcareservas.modelo;

import itcareservas.excepciones.ReglaNegocioException;
import itcareservasmodelo.enums.EstadoReserva;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.io.Serializable;

public abstract class Reserva implements Validable, Serializable {
    private String id;
    private Aula aula;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String responsable;
    private EstadoReserva estado;

    public Reserva(String id, Aula aula, LocalDate fecha, LocalTime horaInicio, 
                   LocalTime horaFin, String responsable) {
        this.id = id;
        this.aula = aula;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.responsable = responsable;
        this.estado = EstadoReserva.ACTIVA;
    }

    public String getId() { return id; }
    public Aula getAula() { return aula; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public String getResponsable() { return responsable; }
    public EstadoReserva getEstado() { return estado; }

    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public void setAula(Aula aula) { this.aula = aula; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public abstract String obtenerDetalleTipo();
    
    public long calcularDuracionHoras() {
        return Duration.between(horaInicio, horaFin).toHours();
    }
    
    @Override
    public boolean validarReglasNegocio() throws ReglaNegocioException {
        if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        if (calcularDuracionHoras() <= 0) {
            throw new ReglaNegocioException("La duracion de la reserva debe ser de al menos una hora.");
        }
        return true;
    }
    
    public abstract String toCSV();
}