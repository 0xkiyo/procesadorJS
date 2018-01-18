package errores;

/**
 * @author Rodrigo Rosado González
 */
public class FuncionNoDeclaradaException extends Exception {

    public FuncionNoDeclaradaException() {

    }

    public FuncionNoDeclaradaException(String message) {
        super(message);
    }
}
