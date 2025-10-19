package itcareservas.app;

import itcareservas.servicios.*;
import itcareservas.excepciones.ConflictoHorarioException;
import itcareservas.excepciones.NoEncontradoException;
import itcareservas.excepciones.ReglaNegocioException;
import itcareservas.modelo.Aula;
import itcareservas.modelo.Reserva;
import itcareservas.modelo.ReservaClase;
import itcareservas.modelo.ReservaEvento;
import itcareservas.modelo.ReservaPractica;
import itcareservasmodelo.enums.TipoAula;
import itcareservasmodelo.enums.TipoEvento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GestorReservasApp {
    private GestorReservas gestor;
    private Scanner scanner;
    private PersistenciaUtil persistencia;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public GestorReservasApp() {
        this.gestor = new GestorReservas();
        this.persistencia = new PersistenciaUtil(gestor);
        
        // Cargar datos al inicio
        this.gestor.getAulas().addAll(persistencia.cargarAulas());
        this.gestor.getReservas().addAll(persistencia.cargarReservas());
        
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        new GestorReservasApp().run();
    }

    public void run() {
        int opcion;
        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1: menuAulas(); break;
                    case 2: menuReservas(); break;
                    case 3: menuReportes(); break;
                    case 0: 
                        persistencia.guardarAulas(gestor.getAulas());
                        persistencia.guardarReservas(gestor.getReservas());
                        System.out.println("Datos guardados. Gracias por usar el sistema!");
                        break;
                    default: System.out.println("Opcion no valida.");
                }
            } catch (Exception e) {
                System.err.println("Ocurrio un error inesperado: " + e.getMessage());
            }
        } while (opcion != 0);
        
        scanner.close();
    }
    
    private void mostrarMenuPrincipal() {
        System.out.println("\n=================================");
        System.out.println("  GESTOR DE RESERVAS DE AULAS ITCA-FEPADE ");
        System.out.println("=================================");
        System.out.println("1. Gestion de Aulas");
        System.out.println("2. Gestion de Reservas");
        System.out.println("3. Reportes");
        System.out.println("0. Salir y Guardar Datos");
    }

    private int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.err.println("Error: Por favor, ingrese un numero entero valido.");
            }
        }
    }
    
    private String leerTipoAula(String prompt) {
        while (true) {
            System.out.print(prompt + " (TEORICA, LABORATORIO, AUDITORIO): ");
            String input = scanner.nextLine().toUpperCase().trim();
            try {
                TipoAula.valueOf(input);
                return input;
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Tipo de aula no valido.");
            }
        }
    }
    
    private LocalDate leerFecha(String prompt) {
        while (true) {
            System.out.print(prompt + " (DD/MM/YYYY): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null; 
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de fecha incorrecto.");
            }
        }
    }

    private LocalTime leerHora(String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:MM): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            try {
                return LocalTime.parse(input, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de hora incorrecto.");
            }
        }
    }
    
    // GESTION DE AULAS 
    private void menuAulas() {
        System.out.println("\n--- GESTION DE AULAS ---");
        System.out.println("1. Registrar Aula");
        System.out.println("2. Listar Aulas");
        System.out.println("3. Modificar Aula");
        System.out.println("4. Eliminar Aula");
        System.out.print("Opcion: ");
        
        switch (leerEntero("")) {
            case 1: registrarAula(); break;
            case 2: listarAulas(); break;
            case 3: modificarAula(); break;
            case 4: eliminarAula(); break;
            default: System.out.println("Opcion no valida.");
        }
    }
    
    private void registrarAula() {
        System.out.println("\n--- REGISTRO DE AULA ---");
        System.out.print("ID del Aula: ");
        String id = scanner.nextLine().toUpperCase().trim();
        System.out.print("Nombre del Aula: ");
        String nombre = scanner.nextLine().trim();
        int capacidad = leerEntero("Capacidad: ");
        TipoAula tipo = TipoAula.valueOf(leerTipoAula("Tipo de Aula: "));
        
        Aula aula = new Aula(id, nombre, capacidad, tipo);
        gestor.registrarAula(aula);
        System.out.println("Aula registrada: " + aula.getNombre());
    }
    
    private void listarAulas() {
        if (gestor.listarAulas().isEmpty()) {
            System.out.println("No hay aulas registradas.");
            return;
        }
        System.out.println("\n--- LISTADO DE AULAS ---");
        gestor.listarAulas().forEach(System.out::println);
    }
    
    private void modificarAula() {
        System.out.println("\n--- MODIFICAR AULA ---");
        System.out.print("ID del aula a modificar: ");
        String id = scanner.nextLine().toUpperCase().trim();
        
        try {
           
            Aula aulaActual = gestor.buscarAulaPorId(id);
            System.out.println("Aula actual: " + aulaActual);
            
            System.out.print("Nuevo nombre (dejar vacío para no cambiar): ");
            String nuevoNombre = scanner.nextLine().trim();
            
            System.out.print("Nueva capacidad (0 para no cambiar): ");
            int nuevaCapacidad = leerEnteroOpcional();
            
            System.out.print("Nuevo tipo (TEORICA, LABORATORIO, AUDITORIO - dejar vacío para no cambiar): ");
            String tipoStr = scanner.nextLine().trim();
            TipoAula nuevoTipo = null;
            if (!tipoStr.isEmpty()) {
                try {
                    nuevoTipo = TipoAula.valueOf(tipoStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: Tipo de aula no valido.");
                    return;
                }
            }
            
            gestor.modificarAula(id, 
                nuevoNombre.isEmpty() ? null : nuevoNombre,
                nuevaCapacidad == 0 ? null : nuevaCapacidad,
                nuevoTipo);
                
            System.out.println("Aula modificada exitosamente.");
            
        } catch (NoEncontradoException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ReglaNegocioException e) {
            System.err.println("Error de regla de negocio: " + e.getMessage());
        }
    }
    
    private void eliminarAula() {
        System.out.println("\n--- ELIMINAR AULA ---");
        System.out.print("ID del aula a eliminar: ");
        String id = scanner.nextLine().toUpperCase().trim();
        
        try {
          
            Aula aula = gestor.buscarAulaPorId(id);
            System.out.println("Aula a eliminar: " + aula);
            
            List<Reserva> reservasAula = gestor.buscarReservasPorAula(id);
            if (!reservasAula.isEmpty()) {
                System.out.println("\nADVERTENCIA: Esta aula tiene " + reservasAula.size() + " reservas asociadas:");
                reservasAula.forEach(r -> System.out.println("  - " + r.getId() + " (" + r.getEstado() + ")"));
            }
            
            System.out.print("¿Está seguro de que desea eliminar esta aula? (s/n): ");
            String confirmacion = scanner.nextLine().trim();
            
            if (confirmacion.equalsIgnoreCase("s")) {
                gestor.eliminarAula(id);
                System.out.println("Aula eliminada exitosamente.");
            } else {
                System.out.println("Eliminacion cancelada.");
            }
            
        } catch (NoEncontradoException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ReglaNegocioException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Puede usar la opcion de gestion de reservas para eliminar las reservas primero.");
        }
    }
    
    private int leerEnteroOpcional() {
        while (true) {
            System.out.print(": ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.err.println("Error: Por favor, ingrese un numero entero valido o deje vacio.");
            }
        }
    }
    
    //GESTION DE RESERVA
    private void menuReservas() {
        System.out.println("\n--- GESTION DE RESERVAS ---");
        System.out.println("1. Registrar Nueva Reserva");
        System.out.println("2. Listar Reservas");
        System.out.println("3. Buscar Reserva por Responsable (Filtro)");
        System.out.println("4. Modificar Reserva (Fecha/Hora/Aula)");
        System.out.println("5. Cancelar Reserva");
        System.out.print("Opcion: ");
        
        switch (leerEntero("")) {
            case 1: menuRegistrarReserva(); break;
            case 2: listarReservas(); break;
            case 3: buscarPorResponsable(); break;
            case 4: modificarReserva(); break;
            case 5: cancelarReserva(); break;
            default: System.out.println("Opcion no valida.");
        }
    }
    
    private void menuRegistrarReserva() {
        System.out.println("\n--- TIPO DE RESERVA ---");
        System.out.println("1. Clase");
        System.out.println("2. Practica");
        System.out.println("3. Evento");
        System.out.print("Seleccione tipo: ");
        int tipo = leerEntero("");

        try {
            Reserva reserva = crearReservaBase(tipo);
            if (reserva == null) return;

            gestor.registrarReserva(reserva);
            System.out.println("Reserva registrada exitosamente. ID: " + reserva.getId());

        } catch (NoEncontradoException | ConflictoHorarioException | ReglaNegocioException e) {
            System.err.println("Error de Registro: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("Error: Ocurrio un problema al ingresar los datos: " + e.getMessage());
        }
    }

    private Reserva crearReservaBase(int tipo) throws NoEncontradoException {
        System.out.println("\n--- DATOS BASICOS ---");
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Aula aula;
        
        while(true) {
            System.out.print("ID del Aula a reservar: ");
            String idAula = scanner.nextLine().toUpperCase().trim();
            try {
                aula = gestor.buscarAulaPorId(idAula);
                break;
            } catch (NoEncontradoException e) {
                System.err.println(e.getMessage());
            }
        }
        
        LocalDate fecha = leerFecha("Fecha de Reserva");
        LocalTime horaInicio = leerHora("Hora Inicio");
        LocalTime horaFin = leerHora("Hora Fin");
        System.out.print("Responsable: ");
        String responsable = scanner.nextLine().trim();
        
        switch (tipo) {
            case 1:
                System.out.print("Materia: "); 
                String materia = scanner.nextLine().trim();
                System.out.print("Grupo: "); 
                String grupo = scanner.nextLine().trim();
                return new ReservaClase(id, aula, fecha, horaInicio, horaFin, responsable, materia, grupo);
            case 2:
                System.out.print("Equipo Necesario: "); 
                String equipo = scanner.nextLine().trim();
                return new ReservaPractica(id, aula, fecha, horaInicio, horaFin, responsable, equipo);
            case 3:
                System.out.print("Tipo de Evento (CONFERENCIA, TALLER, REUNION): ");
                TipoEvento tipoEv = TipoEvento.valueOf(scanner.nextLine().toUpperCase().trim());
                int aforo = leerEntero("Aforo Esperado: ");
                return new ReservaEvento(id, aula, fecha, horaInicio, horaFin, responsable, tipoEv, aforo);
            default:
                System.out.println("Tipo de reserva no valido.");
                return null;
        }
    }

    private void listarReservas() {
        if (gestor.getReservas().isEmpty()) {
            System.out.println("No hay reservas registradas.");
            return;
        }
        System.out.println("\n--- LISTADO DE RESERVAS ---");
        System.out.print("Ordenar por (ID, fecha, responsable, aula): ");
        String sortBy = scanner.nextLine().trim();
        
        AtomicInteger index = new AtomicInteger(1);
        gestor.listarReservas(sortBy).forEach(r -> {
            System.out.printf("%d. [ID: %s] %s | Aula: %s | %s %s-%s | Resp: %s | Estado: %s\n",
                index.getAndIncrement(), r.getId(), r.obtenerDetalleTipo(), r.getAula().getId(), 
                r.getFecha().format(DATE_FORMAT), r.getHoraInicio(), r.getHoraFin(), 
                r.getResponsable(), r.getEstado());
        });
    }
    
    private void buscarPorResponsable() {
        System.out.print("Ingrese texto a buscar en Responsable: ");
        String texto = scanner.nextLine().trim();
        
        if (texto.isEmpty()) {
            System.out.println("Debe ingresar un texto para buscar.");
            return;
        }
        
        List<Reserva> resultados = gestor.buscarPorResponsable(texto);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron reservas para el responsable: " + texto);
            return;
        }
        
        System.out.println("\n--- RESULTADOS DE BUSQUEDA ---");
        resultados.forEach(r -> {
            System.out.printf("[ID: %s] %s | Aula: %s | Fecha: %s | Responsable: %s\n",
                r.getId(), r.obtenerDetalleTipo(), r.getAula().getId(), 
                r.getFecha().format(DATE_FORMAT), r.getResponsable());
        });
    }

    private void modificarReserva() {
        System.out.print("ID de la reserva a modificar (ACTIVA): ");
        String id = scanner.nextLine().toUpperCase().trim();
        try {
            LocalDate nuevaFecha = leerFecha("Nueva Fecha");
            LocalTime nuevaInicio = leerHora("Nueva Hora Inicio");
            LocalTime nuevaFin = leerHora("Nueva Hora Fin");
            System.out.print("Nuevo ID de Aula (dejar vacio para no cambiar): ");
            String nuevaAulaId = scanner.nextLine().toUpperCase().trim();
            
            gestor.modificarReserva(id, nuevaFecha, nuevaInicio, nuevaFin, 
                                    nuevaAulaId.isEmpty() ? null : nuevaAulaId);
            System.out.println("Reserva modificada exitosamente.");
            
        } catch (NoEncontradoException | ConflictoHorarioException | ReglaNegocioException e) {
            System.err.println("Error de Modificacion: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("Error: Ocurrio un problema al ingresar los datos: " + e.getMessage());
        }
    }
    
    private void cancelarReserva() {
        System.out.print("ID de la reserva a CANCELAR: ");
        String id = scanner.nextLine().toUpperCase().trim();
        try {
            gestor.cancelarReserva(id);
            System.out.println("Reserva " + id + " CANCELADA.");
        } catch (NoEncontradoException e) {
            System.err.println(e.getMessage());
        }
    }
    
    // REPORTES 
    private void menuReportes() {
        System.out.println("\n--- REPORTES ---");
        System.out.println("1. Top 3 Aulas con mas Horas Reservadas");
        System.out.println("2. Ocupacion por Tipo de Aula (Horas)");
        System.out.println("3. Distribucion por Tipo de Reserva (Conteo)");
        System.out.print("Opcion: ");
        int opcion = leerEntero("");

        String reporteNombre;
        String reporteContenido;
        
        switch (opcion) {
            case 1:
                reporteNombre = "Top 3 Aulas por Horas";
                reporteContenido = gestor.reporteTopAulasPorHoras().entrySet().stream()
                    .map(e -> String.format("- %s: %d horas", e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n"));
                break;
            case 2:
                reporteNombre = "Ocupacion por Tipo de Aula";
                reporteContenido = gestor.reporteHorasPorTipoAula().entrySet().stream()
                    .map(e -> String.format("- %s: %d horas", e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n"));
                break;
            case 3:
                reporteNombre = "Distribucion por Tipo de Reserva";
                reporteContenido = gestor.reporteDistribucionPorTipo().entrySet().stream()
                    .map(e -> String.format("- %s: %d reservas", e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n"));
                break;
            default:
                System.out.println("Opcion no valida.");
                return;
        }

        System.out.println("\n--- Resultado del Reporte: " + reporteNombre + " ---");
        System.out.println(reporteContenido);
        
        System.out.print("Desea exportar este reporte a archivo de texto? (s/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            persistencia.exportarReporte(reporteNombre, reporteContenido);
        }
    }
}