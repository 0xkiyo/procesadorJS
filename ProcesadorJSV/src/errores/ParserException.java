package errores;

public class ParserException extends Exception {
    public enum Reason {
        CADENA, CODIGO_MUERTO, COMENTARIO, CONCATENACION_NO_IMPLEMENTADA, DECLARACION_INCOMPATIBLE, DEVUELVE_CADENA, EMPAREJA,
        FIRST_NO_COINCIDE, FUERA_DE_RANGO, FUNCION_NO_DECLARADA, ID, OP_LOGICO, TIPO_INCORRECTO, TIPOS_DIFERENTES, OTRO_SIMBOLO, VARIABLE_NO_DECLARADA
    }

    public ParserException(Reason reason, String message) {
        super(message);
    }
}
