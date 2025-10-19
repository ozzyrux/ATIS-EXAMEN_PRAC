package itcareservas.modelo;

import itcareservas.excepciones.ReglaNegocioException;

public interface Validable {
    boolean validarReglasNegocio() throws ReglaNegocioException;
}