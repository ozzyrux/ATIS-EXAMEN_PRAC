/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package itcareservas.excepciones;

/**
 *
 * @author bryan
 */
public class ConflictoHorarioException extends Exception{
    public ConflictoHorarioException(String mensaje){
        super(mensaje);
    }
}
