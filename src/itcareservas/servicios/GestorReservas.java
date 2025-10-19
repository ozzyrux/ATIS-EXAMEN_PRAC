package itcareservas.servicios;

import itcareservas.excepciones.ConflictoHorarioException;
import itcareservas.excepciones.NoEncontradoException;
import itcareservas.excepciones.ReglaNegocioException;
import itcareservas.modelo.Aula;
import itcareservas.modelo.Reserva;
import itcareservasmodelo.enums.EstadoReserva;
import itcareservasmodelo.enums.TipoAula;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestorReservas {
    private List<Aula> aulas;
    private List<Reserva> reservas;

    public GestorReservas() {
        this.aulas = new ArrayList<>();
        this.reservas = new ArrayList<>();
    }

    public void registrarAula(Aula aula) {
        if (aulas.stream().anyMatch(a -> a.getId().equals(aula.getId()))) {
            System.out.println("Error: Aula con ID " + aula.getId() + " ya existe.");
            return;
        }
        aulas.add(aula);
    }
    
    public Aula buscarAulaPorId(String id) throws NoEncontradoException {
        return aulas.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("Aula no encontrada con ID: " + id));
    }

    public List<Aula> listarAulas() {
        return aulas;
    }


    public void modificarAula(String id, String nuevoNombre, Integer nuevaCapacidad, TipoAula nuevoTipo) 
            throws NoEncontradoException, ReglaNegocioException {
        Aula aula = buscarAulaPorId(id);
        
        
        boolean tieneReservasActivas = reservas.stream()
                .anyMatch(r -> r.getAula().getId().equals(id) && r.getEstado() == EstadoReserva.ACTIVA);
        
        if (tieneReservasActivas) {
            throw new ReglaNegocioException("No se puede modificar el aula " + id + 
                                          " porque tiene reservas activas. Cancele las reservas primero.");
        }
        
       
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            aula.setNombre(nuevoNombre);
        }
        
        if (nuevaCapacidad != null && nuevaCapacidad > 0) {
            aula.setCapacidad(nuevaCapacidad);
        } else if (nuevaCapacidad != null) {
            throw new ReglaNegocioException("La capacidad debe ser mayor a 0.");
        }
           
    }
    
    
    public void eliminarAula(String id) throws NoEncontradoException, ReglaNegocioException {
        Aula aula = buscarAulaPorId(id);
        
        
        boolean tieneReservas = reservas.stream()
                .anyMatch(r -> r.getAula().getId().equals(id));
        
        if (tieneReservas) {
            throw new ReglaNegocioException("No se puede eliminar el aula " + id + 
                                          " porque tiene reservas asociadas. Elimine las reservas primero.");
        }
        
        aulas.remove(aula);
    }
    
    
    public List<Reserva> buscarReservasPorAula(String aulaId) {
        return reservas.stream()
                .filter(r -> r.getAula().getId().equals(aulaId))
                .collect(Collectors.toList());
    }

  
    private void validarSolapamiento(Reserva nuevaReserva) throws ConflictoHorarioException {
        
        List<Reserva> conflictos = reservas.stream()
            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA) 
            .filter(r -> r.getAula().getId().equals(nuevaReserva.getAula().getId()))
            .filter(r -> r.getFecha().equals(nuevaReserva.getFecha()))
            .filter(r -> {
                return nuevaReserva.getHoraInicio().isBefore(r.getHoraFin()) &&
                       nuevaReserva.getHoraFin().isAfter(r.getHoraInicio());
            })
            .collect(Collectors.toList());

        if (!conflictos.isEmpty()) {
            Reserva conflicto = conflictos.get(0);
            throw new ConflictoHorarioException(
                "Conflicto de horario con reserva " + conflicto.getId() + 
                " de " + conflicto.getHoraInicio() + " a " + conflicto.getHoraFin()
            );
        }
    }


    public void registrarReserva(Reserva reserva) throws ConflictoHorarioException, ReglaNegocioException {
        reserva.validarReglasNegocio();
        validarSolapamiento(reserva);
        
        reservas.add(reserva);
    }

    public List<Reserva> listarReservas(String sortBy) {
        Comparator<Reserva> comparator;
        switch (sortBy.toLowerCase()) {
            case "fecha":
                comparator = Comparator.comparing(Reserva::getFecha)
                                       .thenComparing(Reserva::getHoraInicio);
                break;
            case "responsable":
                comparator = Comparator.comparing(Reserva::getResponsable);
                break;
            case "aula":
                comparator = Comparator.comparing((Reserva r) -> r.getAula().getId());
                break;
            default:
                comparator = Comparator.comparing(Reserva::getId);
        }
        return reservas.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    public List<Reserva> buscarPorResponsable(String texto) {
        return reservas.stream()
                .filter(r -> r.getResponsable().toLowerCase().contains(texto.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void modificarReserva(String id, LocalDate nuevaFecha, LocalTime nuevaInicio, LocalTime nuevaFin, String nuevaAulaId) 
        throws NoEncontradoException, ConflictoHorarioException, ReglaNegocioException {
        
        Reserva reserva = reservas.stream()
            .filter(r -> r.getId().equals(id) && r.getEstado() == EstadoReserva.ACTIVA)
            .findFirst()
            .orElseThrow(() -> new NoEncontradoException("Reserva ACTIVA no encontrada con ID: " + id));

        Aula oldAula = reserva.getAula();
        LocalDate oldFecha = reserva.getFecha();
        LocalTime oldInicio = reserva.getHoraInicio();
        LocalTime oldFin = reserva.getHoraFin();
        
        try {
            if (nuevaAulaId != null && !nuevaAulaId.isEmpty() && !nuevaAulaId.equals(oldAula.getId())) {
                reserva.setAula(buscarAulaPorId(nuevaAulaId));
            }
            
            if (nuevaFecha != null) reserva.setFecha(nuevaFecha);
            if (nuevaInicio != null) reserva.setHoraInicio(nuevaInicio);
            if (nuevaFin != null) reserva.setHoraFin(nuevaFin);

            reserva.validarReglasNegocio(); 
            validarSolapamiento(reserva); 
            
        } catch (Exception e) {
            reserva.setAula(oldAula);
            reserva.setFecha(oldFecha);
            reserva.setHoraInicio(oldInicio);
            reserva.setHoraFin(oldFin);
            throw e;
        }
    }

    public void cancelarReserva(String id) throws NoEncontradoException {
         Reserva reserva = reservas.stream()
            .filter(r -> r.getId().equals(id) && r.getEstado() == EstadoReserva.ACTIVA)
            .findFirst()
            .orElseThrow(() -> new NoEncontradoException("Reserva ACTIVA no encontrada con ID: " + id));
            
        reserva.setEstado(EstadoReserva.CANCELADA);
    }
    
    public void eliminarReserva(String id) throws NoEncontradoException {
        Reserva reserva = reservas.stream()
            .filter(r -> r.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NoEncontradoException("Reserva no encontrada con ID: " + id));
            
        reservas.remove(reserva);
    }
    
    //  REPORTES 
    
    // 1. Top 3 aulas con más horas reservadas
    public Map<String, Long> reporteTopAulasPorHoras() {
        return reservas.stream()
            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
            .collect(Collectors.groupingBy(
                r -> r.getAula().getNombre(),
                Collectors.summingLong(Reserva::calcularDuracionHoras) 
            ))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (e1, e2) -> e1, 
                java.util.LinkedHashMap::new
            ));
    }
    
    // 2. Ocupación por tipo de aula
    public Map<TipoAula, Long> reporteHorasPorTipoAula() {
        return reservas.stream()
            .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
            .collect(Collectors.groupingBy(
                r -> r.getAula().getTipo(),
                Collectors.summingLong(Reserva::calcularDuracionHoras)
            ));
    }
    
    // 3. Distribución por tipo de reserva
    public Map<String, Long> reporteDistribucionPorTipo() {
        return reservas.stream()
            .collect(Collectors.groupingBy(
                r -> r.getClass().getSimpleName(),
                Collectors.counting()
            ));
    }

    // Getters para persistencia
    public List<Aula> getAulas() { return aulas; }
    public List<Reserva> getReservas() { return reservas; }
}