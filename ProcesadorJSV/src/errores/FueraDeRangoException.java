package errores;

/**
 * @author Rodrigo Rosado González
 */
public class FueraDeRangoException extends Exception {

    public FueraDeRangoException() {

    }

    public FueraDeRangoException(String message) {
        super(message);
    }
}
