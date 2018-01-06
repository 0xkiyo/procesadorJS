package analizadorLexico;

import errores.*;
import analizadorSintactico.AnalizadorSintactico;
import java.io.*;
import tablaSimbolos.TablaSimbolos;
import token.Token;


public class AnalizadorLexico {

   public static int indice = 0;
   public static Long digit = 0L; 
   public static String cadena = "";
   public static Integer linea = 0;
   private File archivo = null;
   private FileReader fr = null;
   private BufferedWriter bw; 
   private String comentario = "";

   private char[] a;

   public void leerFichero(File fichero) {
       try {
    	
           this.archivo = fichero;
           this.fr = new FileReader(archivo);
         
           this.a = new char[(int) archivo.length()]; 
           this.fr.read(a); 
           
           String ruta = AnalizadorSintactico.miDir.getCanonicalPath()+"//impreso//tokens.txt";
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
           //Final de archivo: genera token
           toReturn = new Token("EOF", null);
       } else if (contenido[indice] == '/') {
           //Comentarios: no genera token
           indice++;
           if (contenido[indice] == '=') {
               toReturn = new Token("ASIGDIV", null);
               indice++;
           }
           else {
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
            toReturn = new Token("COMA",null);
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
               throw new FueraDeRangoException("Error en linea: " + linea.toString() + " El numero " + Long.toString(digit) + " sobrepasa el maximo representable");
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
       }else if (contenido[indice] == '=') {
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
           if (cadena.equals("true")){
        	   toReturn = new Token("BOOL", cadena);
           }
           else if (cadena.equals("false")){
        	   toReturn = new Token("BOOL", cadena);
           }
           else if (tS.isPR(cadena)) {
               //Palabra reservada: genera token
               toReturn = new Token("PR", cadena);
           } else {
<<<<<<< HEAD
        	   
        	   //Identificador: genera token
               if (p[0] == null) {
            	       tS.addTs(new Token("ID", cadena));
               }else if(p[0] != null && p[1] == 0 && AnalizadorSintactico.flagDeclaracionLocal){
=======
               //Identificador: genera token
               if (p[0] == null && !AnalizadorSintactico.flagDeclaracionLocal) {
                   tS.addTs(new Token("ID", cadena));
                 }else if ((p[0] == null && AnalizadorSintactico.flagDeclaracionLocal) || (p[0] != null && p[1] == 0 && AnalizadorSintactico.flagDeclaracionLocal)) {
>>>>>>> 9bde06a8021b8dc559d88bff1309f4ffac667314
                   tS.addTs(new Token("ID", cadena));//Se añade en la local
                 }
               else if(p[0] != null && ((p[1] == 1 && AnalizadorSintactico.flagDeclaracionLocal) || (p[1] == 0 && AnalizadorSintactico.flagDeclaracion))){
                   throw new DeclaracionIncompatibleException("Error en linea "+AnalizadorLexico.linea+". La variable o funcion '"+cadena+"' ha sido declarada previamente.");
               } else if(p[0] != null && AnalizadorSintactico.flagDeclaracionLocal==false) {
            	   	   throw new DeclaracionIncompatibleException("Error en linea "+AnalizadorLexico.linea+". La variable o funcion '"+cadena+"' ha sido declarada previamente.");
               }
               toReturn = new Token("ID", cadena);
           }
       } else if (contenido[indice] == ';') {
           //Punto y coma: genera token
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
       if (indice < contenido.length && contenido[indice] != '\r' && contenido[indice] != '\n') {
           comentario += Character.toString(contenido[indice]);
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
       if (contenido[indice] == '&') {
           indice++;
       } else {
           throw new OpLogicoException("Error en linea: " + linea.toString() + " Se esperaba detectar '&'");
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
       this.bw.write(toReturn.toString());
       this.bw.newLine();
       return toReturn;

   }
}

