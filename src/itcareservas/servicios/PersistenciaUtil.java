package itcareservas.servicios;

import itcareservas.excepciones.NoEncontradoException;
import itcareservas.modelo.Aula;
import itcareservas.modelo.Reserva;
import itcareservas.modelo.ReservaClase;
import itcareservas.modelo.ReservaEvento;
import itcareservas.modelo.ReservaPractica;
import itcareservasmodelo.enums.EstadoReserva;
import itcareservasmodelo.enums.TipoEvento;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaUtil {
    private static final String AULAS_FILE = "aulas.csv";
    private static final String RESERVAS_FILE = "reservas.csv";
    private GestorReservas gestor;

    public PersistenciaUtil(GestorReservas gestor) {
        this.gestor = gestor;
    }

    public void guardarAulas(List<Aula> aulas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(AULAS_FILE))) {
            aulas.forEach(aula -> pw.println(aula.toCSV()));
        } catch (IOException e) {
            System.err.println("Error al guardar aulas: " + e.getMessage());
        }
    }

    public List<Aula> cargarAulas() {
        List<Aula> aulas = new ArrayList<>();
        try {
            Files.lines(Paths.get(AULAS_FILE))
                 .map(line -> Aula.fromString(line))
                 .forEach(aulas::add);
        } catch (IOException e) {
            System.out.println("No se encontro archivo de aulas. Iniciando con lista vacia.");
        }
        return aulas;
    }

    public void guardarReservas(List<Reserva> reservas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RESERVAS_FILE))) {
            reservas.forEach(reserva -> pw.println(reserva.toCSV()));
        } catch (IOException e) {
            System.err.println("Error al guardar reservas: " + e.getMessage());
        }
    }

    public List<Reserva> cargarReservas() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            Files.lines(Paths.get(RESERVAS_FILE))
                 .forEach(line -> {
                     try {
                         reservas.add(reconstruirReserva(line));
                     } catch (NoEncontradoException ex) {
                         System.err.println("Error al cargar reserva (Aula no existe): " + ex.getMessage() + " Linea: " + line);
                     } catch (Exception ex) {
                         System.err.println("Error desconocido al cargar reserva. Linea: " + line);
                     }
                 });
        } catch (IOException e) {
            System.out.println("No se encontro archivo de reservas. Iniciando con lista vacia.");
        }
        return reservas;
    }
    
    private Reserva reconstruirReserva(String line) throws NoEncontradoException {
        String[] parts = line.split(",");
        String tipo = parts[0];
        String id = parts[1];
        Aula aula = gestor.buscarAulaPorId(parts[2]); 
        LocalDate fecha = LocalDate.parse(parts[3]);
        LocalTime hInicio = LocalTime.parse(parts[4]);
        LocalTime hFin = LocalTime.parse(parts[5]);
        String responsable = parts[6];
        EstadoReserva estado = EstadoReserva.valueOf(parts[7]);

        Reserva reserva = null;
        switch (tipo) {
            case "CLASE":
                reserva = new ReservaClase(id, aula, fecha, hInicio, hFin, responsable, parts[8], parts[9]);
                break;
            case "PRACTICA":
                reserva = new ReservaPractica(id, aula, fecha, hInicio, hFin, responsable, parts[8]);
                break;
            case "EVENTO":
                TipoEvento tipoEv = TipoEvento.valueOf(parts[8]);
                int aforo = Integer.parseInt(parts[9]);
                reserva = new ReservaEvento(id, aula, fecha, hInicio, hFin, responsable, tipoEv, aforo);
                break;
        }
        if (reserva != null) {
            reserva.setEstado(estado); 
        }
        return reserva;
    }
    
    public void exportarReporte(String nombreReporte, String contenido) {
        String filename = nombreReporte.replaceAll("\\s+", "_").toLowerCase() + "_" + LocalDate.now() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("--- REPORTE ITCA: " + nombreReporte.toUpperCase() + " ---");
            pw.println("Generado: " + LocalDate.now());
            pw.println("----------------------------------------");
            pw.println(contenido);
            System.out.println("Reporte exportado exitosamente a: " + filename);
        } catch (IOException e) {
            System.err.println("Error al exportar reporte: " + e.getMessage());
        }
    }
}