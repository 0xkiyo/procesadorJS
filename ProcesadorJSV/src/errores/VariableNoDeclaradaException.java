/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package errores;

/**
 *
 * @author Rodrigo Rosado Gonz�lez
 */
public class VariableNoDeclaradaException extends Exception{
    
    public VariableNoDeclaradaException(){
        
    }
    
    public VariableNoDeclaradaException(String message){
        super(message);
    }
}
