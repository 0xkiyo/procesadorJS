package error;

public class VariableNoDeclaradaException extends Exception{
    
    public VariableNoDeclaradaException(){
        
    }
    
    public VariableNoDeclaradaException(String message){
        super(message);
    }
}
