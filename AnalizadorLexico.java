package analizadorLexico;

import errores.CadenaException;
import errores.ComentarioException;
import errores.FueraDeRangoException;
import errores.IdException;
import errores.OpLogicoException;
import errores.OtroSimboloException;
import analizadorSintactico.AnalizadorSintactico;
import errores.DeclaracionIncompatibleException;

import java.io.*;
import tablaSimbolos.TablaSimbolos;
import token.Token;

/**
*
* @author 
*/
public class AnalizadorLexico {

   public static int indice = 0;
   public static Long digit = 0L;
   public static String cadena = "";
   public static Integer linea = 0;
   private File archivo = null;
   private FileReader fr = null;
   private BufferedWriter bw; //Esto lo usamos para escribir en el fichero (se inicializa en leerFichero)

   private char[] a;

   public void leerFichero(File fichero) {
       try {
    	// Apertura del fichero e inicializacion de FileReader para leerlo
           this.archivo = fichero;
           this.fr = new FileReader(archivo);
           // Lectura del fichero
           this.a = new char[(int) archivo.length()]; //Este array almacena todos los caracteres
           this.fr.read(a); // mete el contenido en el array
           //Ruta del fichero de tokens
//           String ruta = "C:\\Users\\Jaimegon\\Desktop\\Universidad\\5 semestre\\PDL\\tokens.txt";
           String ruta = AnalizadorSintactico.miDir.getCanonicalPath()+"\\tokens.txt";
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
       return (caracter > 64 && caracter < 91) || (caracter > 96 && caracter < 123);
   }

   public Token procS(char[] contenido, TablaSimbolos tS) throws ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, DeclaracionIncompatibleException {
       Token toReturn = null;
       if (indice == contenido.length) {
           toReturn = new Token("EOF", null);
       } else if (contenido[indice] == '/') {
           indice++;
           procA(contenido);
//           System.out.println("Comentario detectado");
       } else if (contenido[indice] == '\n') {//SALTO DE LINEA
           linea++;
           indice++;//no se genera token

       } else if (contenido[indice] == '\r') {//CARRY
           indice++;
           toReturn = new Token("CR", null);
       } else if (contenido[indice] == ' ' || contenido[indice] == '\t') {
           indice++;
//           System.out.println("Detectado delimitador");

       } else if (contenido[indice] == '{') {
           indice++;
           toReturn = new Token("LLAVEABIERTA", null);
       } else if (contenido[indice] == '}') {
           indice++;
           toReturn = new Token("LLAVECERRADA", null);
       } else if (isDigit(contenido[indice])) {
           digit = 0L;
           Integer num = Character.getNumericValue(contenido[indice]);
           digit = num.longValue();
           indice++;
//           System.out.println("Detectado primer digito");
           procD(contenido);
           if (digit < Math.pow(2, 15)) {
               toReturn = new Token("NUM", Long.toString(digit));
           } else {
               throw new FueraDeRangoException("Error en linea: " + linea.toString() + " El numero " + Long.toString(digit) + " sobrepasa el maximo representable");
           }
       } else if (contenido[indice] == '\"') {
           cadena = "";
           indice++;
           procE(contenido);
           toReturn = new Token("CADENA", cadena);
       } else if (contenido[indice] == '+') {
           indice++;
           toReturn = new Token("SUMA", null);
       } else if (contenido[indice] == '-') {
           indice++;
           toReturn = new Token("RESTA", null);
       } else if (contenido[indice] == '(') {
           indice++;
           toReturn = new Token("PARENTABIERTO", null);
       } else if (contenido[indice] == ')') {
           indice++;
           toReturn = new Token("PARENTCERRADO", null);
       } else if (contenido[indice] == '<') {
           indice++;
           toReturn = new Token("MENORQUE", null);
       } else if (contenido[indice] == '>') {
           indice++;
           toReturn = new Token("MAYORQUE", null);
       }else if (contenido[indice] == '&') {
    	   indice++;
    	   if(contenido[indice] == '='){
    		   toReturn = new Token("ASIGLOGICO", null);
    		   indice++;
    	   }
    	   else{
           procF(contenido);
           toReturn = new Token("CONJUNCION", null);
    	   }
       }else if (contenido[indice] == '|') {
           indice++;
           procI(contenido);
           toReturn = new Token("DISYUNCION", null);
       } else if (contenido[indice] == '=') {
           indice++;
           toReturn = new Token("ASIG", null);
       } else if (isLetter(contenido[indice])) {
           cadena = Character.toString(contenido[indice]);
           indice++;
           procG(contenido);

           Integer[] p = tS.buscaTS(cadena);
           String CadenaTrue = new String("true");
           String CadenaFalse = new String("false");
           if (cadena.equals(CadenaTrue)){
        	   toReturn = new Token("BOOL", cadena);
           }
           else if (cadena.equals(CadenaFalse)){
        	   toReturn = new Token("BOOL", cadena);
           }
           else if (tS.isPR(cadena)) {//En la accion semantica del lexico se pasa la posicion.
               toReturn = new Token("PR", cadena);
           } else {
               if (p[0] == null) {//AQUI FALTA COMPROBAR SI ESTAMOS EN ZONA DE DECLARACION
                   tS.addTs(new Token("ID", cadena));
               }else if(p[0] != null && p[1] == 0 && AnalizadorSintactico.flagDeclaracionLocal){
                   tS.addTs(new Token("ID", cadena));//Se añade en la local
               }else if(p[0] != null && (AnalizadorSintactico.flagDeclaracion || AnalizadorSintactico.flagDeclaracionLocal)){
                   throw new DeclaracionIncompatibleException("Error en linea "+AnalizadorLexico.linea+". La variable o funcion '"+cadena+"' ha sido declarada previamente.");
               }
               toReturn = new Token("ID", cadena);
           }
       } else if (contenido[indice] == ';') {
           indice++;
           toReturn = new Token("PUNTCOM", null);
       } else {
           throw new OtroSimboloException("Error en linea: " + linea.toString() + " Se ha encontrado un simbolo que no pertenece a la gramatica: " + contenido[indice]);
       }
       return toReturn;
   }

   public void procA(char[] contenido) throws ComentarioException {
       if (contenido[indice] == '/') {
           indice++;
           procB(contenido);
       } else {
           throw new ComentarioException("Error en linea: " + linea.toString() + " Se esperaba detectar el simbolo '/'");
       }
   }

   public void procB(char[] contenido) {
       if (indice < contenido.length && contenido[indice] != '\r') {
           indice++;
           procB(contenido);
       }

   }

   public void procD(char[] contenido) {
       if (indice < contenido.length && isDigit(contenido[indice])) {
           digit = Character.getNumericValue(contenido[indice]) + digit * 10;
           indice++;
           procD(contenido);
       }
   }

   public void procE(char[] contenido) throws CadenaException {
       if (indice < contenido.length && contenido[indice] != '\"' && contenido[indice] != '\n' && contenido[indice] != '\r') {
           cadena += Character.toString(contenido[indice]);
           indice++;
           procE(contenido);
       } else if (indice < contenido.length && contenido[indice] == '\"') {
           indice++;
       } else {
           throw new CadenaException("Error en linea: " + linea.toString() + " Se ha encontrado un salto de linea mientras se analizaba una cadena");
       }
   }

   public void procF(char[] contenido) throws OpLogicoException {
       if (contenido[indice] == '&' || contenido[indice] == '=') {
           indice++;
       } else {
           throw new OpLogicoException("Error en linea: " + linea.toString() + " Se esperaba detectar o '&' o '='");
       }
   }

   public void procG(char[] contenido) throws IdException {
       if (indice < contenido.length && (isDigit(contenido[indice]) || isLetter(contenido[indice]) || contenido[indice] == '_')) {
           cadena += Character.toString(contenido[indice]);
           indice++;
           procG(contenido);
       }
   }

   public void procH(char[] contenido) {
       if (indice < contenido.length && isLetter(contenido[indice])) {
           cadena += Character.toString(contenido[indice]);
           indice++;
           procH(contenido);
       }
   }
   
   public void procI(char[] contenido) throws OpLogicoException {
       if (contenido[indice] == '|') {
           indice++;
       } else {
           throw new OpLogicoException("Error en linea: " + linea.toString() + " Se esperaba detectar otro simbolo '|'");
       }
   }

   public static int getIndice() {
       return indice;
   }

   public static Long getDigit() {
       return digit;
   }

   public static String getCadena() {
       return cadena;
   }

   public File getArchivo() {
       return archivo;
   }

   public FileReader getFr() {
       return fr;
   }

   public char[] getA() {
       return a;
   }

   public BufferedWriter getBw() {
       return bw;
   }

   public void setBw(BufferedWriter bw) {
       this.bw = bw;
   }

   public Token al(TablaSimbolos tablaSimbolos) throws ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException {
       Token toReturn = null;

       while (toReturn == null) {
           toReturn = this.procS(this.getA(), tablaSimbolos);
       }
       //Estas lineas son para escribir en el fichero de volcado de los tokens
       this.bw.write(toReturn.toString());
       this.bw.newLine();
       return toReturn;

   }
}

