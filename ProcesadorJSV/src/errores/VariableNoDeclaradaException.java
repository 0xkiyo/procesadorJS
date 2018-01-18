package errores;

/**
 * @author Rodrigo Rosado González
 */
public class VariableNoDeclaradaException extends Exception {

    public VariableNoDeclaradaException() {

    }

    public VariableNoDeclaradaException(String message) {
        super(message);
    }
}
