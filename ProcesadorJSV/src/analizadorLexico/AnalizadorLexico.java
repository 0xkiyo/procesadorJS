package analizadorLexico;

import analizadorSintactico.AnalizadorSintactico;
import errores.ParserException;
import tablaSimbolos.TablaSimbolos;
import token.Token;

import java.io.*;


public class AnalizadorLexico {

    //FIXME language -> should be english
    //FIXME TOO MUCH STATE
    //FIXME TOO MUCH COUPLING

    public Integer linea = 0;

    private char[] a;
    private int indice;
    private Long digit = 0L;
    private String cadena = "";
    private final AnalizadorSintactico analizadorSintactico;
    private FileReader fr;
    private BufferedWriter bw;
    private String comentario = "";

    public AnalizadorLexico(AnalizadorSintactico analizadorSintactico) {
        this.analizadorSintactico = analizadorSintactico;
    }

    public void leerFichero(File fichero) {
        try {

            this.fr = new FileReader(fichero);

            this.a = new char[(int) fichero.length()];
            this.fr.read(a);

            String ruta = ".//impreso//tokens.txt";
            File archivoTokens = new File(ruta);
            this.bw = new BufferedWriter(new FileWriter(archivoTokens));
            linea++;

        } catch (Exception e) {
            System.err.println("Ha ocurrido un error leyendo el fichero");
        }
    }

    /*
     Metodo auxiliar que comprueba si el parametro caracter es un digito
     */
    private boolean isDigit(char caracter) {
        return caracter > 47 && caracter < 58;
    }

    /*
     Metodo auxiliar que comprueba si el parametro caracter es una letra (mirando tabla ASCII)
     */
    private boolean isLetter(char caracter) {
        return (caracter > 64 && caracter < 91) ||
                (caracter > 96 && caracter < 123);
    }

    private Token procS(char[] contenido, TablaSimbolos tS)
            throws ParserException {
        Token toReturn = null;
        if (indice == contenido.length) {
            //Final de archivo: genera token
            toReturn = new Token("EOF", null);
        } else if (contenido[indice] == '/') {
            //Comentarios: no genera token
            indice++;
            if (contenido[indice] == '=') {
                toReturn = new Token("ASIGDIV", null);
                indice++;
            } else {
                procA(contenido);
                tS.addTs(new Token("COMENTARIO", this.comentario));
                this.comentario = "";
            }
        } else if (contenido[indice] == '\n') {
            //No generamos token
            linea++;
            indice++;
        } else if (contenido[indice] == '\r') {
            //No generamos token
            indice++;
        } else if (contenido[indice] == ' ' || contenido[indice] == '\t') {
            //No generamos token
            indice++;
        } else if (contenido[indice] == ',') {
            //Coma: genera token
            indice++;
            toReturn = new Token("COMA", null);
        } else if (contenido[indice] == '{') {
            //Llave abierta: genera token
            indice++;
            toReturn = new Token("LLAVEABIERTA", null);
        } else if (contenido[indice] == '}') {
            //Llave cerrada: genera token
            indice++;
            toReturn = new Token("LLAVECERRADA", null);
        } else if (isDigit(contenido[indice])) {
            //Digito: genera token
            digit = 0L;
            Integer num = Character.getNumericValue(contenido[indice]);
            digit = num.longValue();
            indice++;
            procD(contenido);
            //Posible error en el digito introducido
            if (digit < Math.pow(2, 15)) {
                toReturn = new Token("ENTERA", Long.toString(digit));
            } else {
                throw new ParserException(ParserException.Reason.FUERA_DE_RANGO,
                        "Error en linea: " + linea.toString() + " El numero " +
                                Long.toString(digit) +
                                " sobrepasa el maximo representable");
            }
        } else if (contenido[indice] == '\"') {
            //Cadena: genera token
            cadena = "";
            indice++;
            procE(contenido);
            toReturn = new Token("CADENA", cadena);
        } else if (contenido[indice] == '+') {
            //Suma: genera token
            indice++;
            toReturn = new Token("SUMA", null);
        } else if (contenido[indice] == '(') {
            //Parentersis abierto: genera token
            indice++;
            toReturn = new Token("PARENTABIERTO", null);
        } else if (contenido[indice] == ')') {
            //Parentesis cerrado: genera token
            indice++;
            toReturn = new Token("PARENTCERRADO", null);
        } else if (contenido[indice] == '<') {
            //Menor que: genera token
            indice++;
            toReturn = new Token("MENORQUE", null);
        } else if (contenido[indice] == '&') {
            //Conjuncion: genera token
            indice++;
            procF(contenido);
            toReturn = new Token("CONJUNCION", null);
        } else if (contenido[indice] == '=') {
            //Asignacion: genera token
            indice++;
            toReturn = new Token("ASIG", null);
        } else if (isLetter(contenido[indice])) {
            //Son letras: genera token
            cadena = Character.toString(contenido[indice]);
            indice++;
            procG(contenido);

            Integer[] p = tS.buscaTS(cadena);
            //Comprobamos si son booleanas
            if (cadena.equals("true")) {
                toReturn = new Token("BOOL", cadena);
            } else if (cadena.equals("false")) {
                toReturn = new Token("BOOL", cadena);
            } else if (tS.isPR(cadena)) {
                //Palabra reservada: genera token
                toReturn = new Token("PR", cadena);
            } else {
                //Identificador: genera token
                if (p[0] == null &&
                        !analizadorSintactico.flagDeclaracionLocal) {
                    tS.addTs(new Token("ID", cadena));
                } else if (p[0] == null || p[1] == 0 && analizadorSintactico.flagDeclaracionLocal) {
                    tS.addTs(new Token("ID", cadena));//Se añade en la local
                } else if (p[1] == 1 && analizadorSintactico.flagDeclaracionLocal || p[1] == 0 && analizadorSintactico.flagDeclaracion) {
                    throw new ParserException(ParserException.Reason.DECLARACION_INCOMPATIBLE,
                            "Error en linea " + linea +
                                    ". La variable o funcion '" + cadena +
                                    "' ha sido declarada previamente.");
                }
                toReturn = new Token("ID", cadena);
            }
        } else if (contenido[indice] == ';') {
            //Punto y coma: genera token
            indice++;
            toReturn = new Token("PUNTCOM", null);
        } else {
            throw new ParserException(ParserException.Reason.OTRO_SIMBOLO,
                    "Error en linea: " + linea.toString() +
                            " Se ha encontrado un simbolo que no pertenece a la gramatica: " +
                            contenido[indice]);
        }
        return toReturn;
    }

    private void procA(char[] contenido) throws ParserException {
        if (contenido[indice] == '/') {
            indice++;
            procB(contenido);
        } else {
            throw new ParserException(ParserException.Reason.COMENTARIO,
                    "Error en linea: " + linea.toString() +
                            " Se esperaba detectar el simbolo '/'");
        }
    }

    private void procB(char[] contenido) {
        if (indice < contenido.length && contenido[indice] != '\r' &&
                contenido[indice] != '\n') {
            comentario += Character.toString(contenido[indice]);
            indice++;
            procB(contenido);
        }

    }

    private void procD(char[] contenido) {
        if (indice < contenido.length && isDigit(contenido[indice])) {
            digit = Character.getNumericValue(contenido[indice]) + digit * 10;
            indice++;
            procD(contenido);
        }
    }

    private void procE(char[] contenido) throws ParserException {
        if (indice < contenido.length && contenido[indice] != '\"' &&
                contenido[indice] != '\n' && contenido[indice] != '\r') {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procE(contenido);
        } else if (indice < contenido.length && contenido[indice] == '\"') {
            indice++;
        } else {
            throw new ParserException(ParserException.Reason.CADENA, "Error en linea: " + linea.toString() +
                    " Se ha encontrado un salto de linea mientras se analizaba una cadena");
        }
    }

    private void procF(char[] contenido) throws ParserException {
        if (contenido[indice] == '&') {
            indice++;
        } else {
            throw new ParserException(ParserException.Reason.OP_LOGICO, "Error en linea: " + linea.toString() +
                    " Se esperaba detectar o '&'");
        }
    }

    private void procG(char[] contenido) {
        if (indice < contenido.length &&
                (isDigit(contenido[indice]) || isLetter(contenido[indice]) ||
                        contenido[indice] == '_')) {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procG(contenido);
        }
    }

    public FileReader getFr() {
        return fr;
    }

    private char[] getA() {
        return a;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    public Token al(TablaSimbolos tablaSimbolos)
            throws ParserException, IOException {
        Token toReturn = null;

        while (toReturn == null) {
            toReturn = this.procS(this.getA(), tablaSimbolos);
        }
        this.bw.write(toReturn.toString());
        this.bw.newLine();
        return toReturn;

    }
}