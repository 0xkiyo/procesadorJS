package errores;

public class ParserException extends RuntimeException {

    private Reason reason;

    public enum Reason {
        CADENA, COMENTARIO, CONCATENACION_NO_IMPLEMENTADA, DECLARACION_INCOMPATIBLE, DEVUELVE_CADENA, EMPAREJA,
        FIRST_NO_COINCIDE, FUERA_DE_RANGO, FUNCION_NO_DECLARADA, ID, OP_LOGICO, TIPO_INCORRECTO, TIPOS_DIFERENTES, OTRO_SIMBOLO
    }

    public ParserException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
