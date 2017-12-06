package error;

public class FueraDeRangoException extends Exception {

    public FueraDeRangoException() {

    }

    public FueraDeRangoException(String message) {
        super(message);
    }
}
