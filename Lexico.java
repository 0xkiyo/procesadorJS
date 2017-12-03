package lexico;

//Paquetes externos
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import error.*;
import tabla_simbolos.*;
import token.*;

public class Lexico {
    
    private File archivo = null;
    private FileReader fr = null;
    private char[] a;
    private BufferedWriter bw;
    public static Integer linea = 0;//Para recorrer las lineas
    public static int indice = 0;//Para recorrer el array
    public static Long digit = (long) 0;//Para detectar digitos
    public static String cadena = "";
    
    /** 
     * @funcion: lee ficheros
     * @param fichero 
	 */
	public void leeFicheros(File fichero){
		try {
        // Apertura del fichero e inicializacion de FileReader para leerlo
        this.archivo = fichero;
        this.fr = new FileReader(archivo);
        //Leer fichero
        this.a = new char [(int) archivo.length()];//Este Array nos almacena los caracteres
        this.fr.read(a); //añade el contenido al array
        String ruta = Lexico.miDir.getCanonicalPath()+"\\tokens.txt";
        File archivoTokens = new File(ruta);
        this.bw = new BufferedWriter(new FileWriter(archivoTokens));//leemos el fichero
		linea++;
		}catch (Exception e){
			System.out.println("Error al leer el fichero");
		}
	}

    /** 
    * @param: caracter a comprobar
    * @funcion: evalua si el caracter es un digito
    */
    private boolean isDigit(char caracter){
        return caracter > 47 && caracter < 58;
    }

    /** 
    * param: caracter a comprobar
    * function: evalua si el caracter es una letra
    */
    private boolean isLetter(char caracter){
        return (caracter > 64 && caracter < 91) || (caracter > 96 && caracter < 123);
    }

    /** 
    * param: array con el contenido para crear los tokens y tabla de simbolos
    * function: dependiendo de las condciones genera los tokens
    */
    public Token procS(char [] contenido ,TablaSimbolos tS){
        Token toReturn = null;
        if (indice == contenido.length) {
            toReturn = new Token("EOF", null);//genera el token eof
        } else if (contenido[indice] == '/') {
            indice++;
            procA(contenido);
        } else if (contenido[indice] == '\n') {
            linea++;
            indice++;//en este caso no se genera token
        } else if (contenido[indice] == ' ' || contenido[indice] == '\t') {//tabuladores o espacios
            indice++;
        } else if (contenido[indice] == '{') {
            indice++;
            toReturn = new Token("LLAVEABIERTA",null);//genera el token LLAVEABIERTA
        } else if (contenido[indice] == '{') {
            indice++;
            toReturn = new Token("LLAVECERRADA",null);//genera el token LLAVECERRADA
        } else if (isDigit(contenido[indice])) {//si es un digito
            digit = (long) 0;
            Integer num = Character.getNumericValue(contenido[indice]);//devuelve el valor entero segun la representacion Unicode
            digit = num.longValue();
            indice++;
            procD(contenido);//concatena todos los digitos del numero
            
            if (digit < Math.pow(2, 15)) {//resticcion maximo numero
                toReturn = new Token("INT",Long.toString(digit));//genera token (INT,Valor)
            } else {
                //Error fuera de rango
            }
        } else if (contenido[indice] == '\"') {//cadena
            cadena = "";
            indice++;
            procE(contenido);
            toReturn = new Token("CHARS",lexema);//genera token de cadena de caracteres
        } else if (contenido[indice] == '+') {
            indice++;
            toReturn = new Token("SUMA",null);//genera token suma
        } else if (contenido[indice] == '(') {
            indice++;
            toReturn = new Token("PARARENTABIERTO",null);//genera token PARARENTABIERTO
        } else if (contenido[indice] == ')') {
            indice++;
            toReturn = new Token("PARARENTCERRADO",null);//genera token PARARENTCERRADO
        } else if (contenido[indice] == '<') {
            indice++;
            toReturn = new Token("MENORQUE",null);//genera token MENORQUE
        } else if (contenido[indice] == '&') {
            indice++;
            if (contenido[indice] == '&') {
                toReturn = new Token("AND",null);//genera token AND
                indice++;
            } else {
                //Error se esperaba detectar el caracter &
            }
        } else if (contenido[indice] == '=') {
            indice++;
            toReturn = new Token("IGUAL",null);//genera token IGUAL
        } else if (contenido[indice] == ';') {
            indice++;
            toReturn = new Token("PUNTOYCOMA",null);
        } else if (contenido[indice] == ',') {
            indice++;
            toReturn = new Token("COMA",null);//genera token COMA
        } 
        /* Se descarta ya que este token no existe
        else if (isLetter(contenido[indice])) {
            cadena = Character.toString(contenido[indice]);//Devuelve un objeto String del caracter
            indice++;
            procG(contenido);

            Integer[] p = tS.buscaTS(cadena);//busca en la tabla de simbolos las palabras reservadas
            
            //comprobacion  identificadores
            if (p[0] == null) {
                tS.addTs(new Token("ID",cadena));//genera token ID
            } else if (p[0] != null && p[1] == 0 && Sintactico.flagDeclaracionLocal) {
                tS.addTs(new Token("ID",cadena));//genera token ID
            } else if (p[0] != null && (Sintactico.flagDeclaracionLocal || Sintactico.flagDeclaracionLocal)) {
                //Error ya ha sido declarado previamente esa variable o funcion
            } toReturn = new Token("ID",cadena);
        
        } */else if (contenido[indice] == 'i' ) {
            String cadena = "i";
            indice++;
            if(contenido[indice] == 'f' && indice < contenido.length) {
                toReturn = new Token("IF",null);//genera token IF
                indice++;
            } else  procJ(contenido,cadena += f);
        } else if (contenido[indice] == 'e') {
           String cadena = "e"; 
           indice++;
           procM(contenido,cadena);
        } else if (contenido[indice] == 'f') {
            String cadena = "f"; 
            indice++;
            procN(contenido,cadena);
        } else if (contenido[indice] == 'w') {
            String cadena = "w"; 
            indice++;
            procO(contenido,cadena);
        } else if (contenido[indice] == 'p') {
            String cadena = "p"; 
            indice++;
            procP(contenido,cadena);
        } else if (contenido[indice] == 'r') {
            String cadena = "r"; 
            indice++;
            procR(contenido,cadena);
        } else if (contenido[indice] == 'v') {
            String cadena = "v"; 
            indice++;
            procS(contenido,cadena);
        }  else {
            //Error gramatica en contenido[indiceBu]
        }
        return toReturn;
    }

    /** 
    * param: array con el contenido a comprobar
    * function: detecta comentarios
    */
    public void procA(char[] contenido) {
        if (contenido[indice] == '/') {
            indice++;
            procB(contenido);
        }
        else{
            //Error porque se esperaba /
        }
    }

    /** 
    * param: array con el contenido a comprobar
    * function: avanza en caso de no detecte retorno de carro y no este apuntado al final del array
    */
    public void procB(char [] contenido) {
        if (indice < contenido.length && contenido[indice] != '\r') {
            indice++;
            procB(contenido);
        }
    }

    /** 
    * param: array con el contenido a comprobar
    * function: detecta digitios y los concatena 
    */
    public void procD(char[] contenido){
        if (indice < contenido.length && isDigit(contenido[indice])) {
            digit = Character.getNumericValue(contenido[indice]) + digit*10;
            indice ++;
            procD(contenido);
        }
    }

    /** 
    * param: array con el contenido a comprobar
    * function: detecta letras y las concatena en una cadena
    */
    public void procE(char[] contenido) {
        if (indice < contenido.length && contenido[indice] != '\"' && contenido[indice] != '\n' && contenido[indice] != '\r') {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procE(contenido);
        } else if (indice < contenido.length && contenido[indice] == '\"') {
            indice++;
        } else {
            //Error salto de linea mientras se analizaba una cadena
        }
    }

    /**
     * param: array con el contenido a comprobar
     * function: detecta si los caracteres son de tipo letra o digito o _ para luego concatenarlos
     */
    //este metodo se usa para comprobar si los identificadores existen o en caso contrario crearlos
    /*public void procG(char[] contenido) {
        if (indice < contenido.length && (isDigit(caracter[indice]) || isLetter(caracter[indice]) || contenido[indice] == '_')) {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procG(contenido);
        }
    }*/

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: crear cadenas de caracteres
     */ 
    public void procJ(char[] contenido, String cadena) {
        if (indice < contenido.length && (isDigit(contenido[indice]) || isLetter(contenido[indice]))) {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procJ(contenido,cadena);
        } else if (indice == contenido.length) new Token("CHARS",cadena);//genera token (CHARS,LEXEMA)
    }
    
    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token else
     */ 
    public void procM(char[] contenido, String cadena) {
        if (indice < contenido.length && (contenido[indice] =='l')) {
            indice++;
            if (indice < contenido.length && (contenido[indice] =='s')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='e')) {
                     new Token("ELSE",null);//genera token ELSE
                    indice++;
                } else procJ(contenido,cadena += "e");
            } else procJ(contenido,cadena += "s");
        } else procJ(contenido,cadena += "l");
    }

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token function
     */ 
    public void procN(char[] contenido, String cadena) {
            if (indice < contenido.length && (contenido[indice] =='u')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='n')) {
                    indice++;
                    if (indice < contenido.length && (contenido[indice] =='c')) {
                        indice++;
                        if (indice < contenido.length && (contenido[indice] =='t')) {
                            indice++;
                            if (indice < contenido.length && (contenido[indice] =='i')) {
                                indice++;
                                if (indice < contenido.length && (contenido[indice] =='o')) {
                                    indice++;
                                    if (indice < contenido.length && (contenido[indice] =='n')) {
                                        new Token("FUNCTION",null);//genera token FUNCTION
                                        indice++;
                                    } else procJ(contenido,cadena += "n");
                                } else procJ(contenido,cadena += "o");
                            } else procJ(contenido,cadena += "i");
                        } else procJ(contenido,cadena += "t");
                    } else procJ(contenido,cadena += "c");
                } else procJ(contenido,cadena += "n");
            } else procJ(contenido,cadena += "u");
    }

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token write
     */ 
    public void procO(char[] contenido, String cadena) {
        if (indice < contenido.length && (contenido[indice] =='r')) {
            indice++;
            if (indice < contenido.length && (contenido[indice] =='i')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='t')) {
                    indice++;
                    if (indice < contenido.length && (contenido[indice] =='e')) {
                            new Token("WRITE",null);//genera token WRITE
                            indice++;
                        } else procJ(contenido,cadena += "e");
                    } else procJ(contenido,cadena += "t");
                } else procJ(contenido,cadena += "i");
            } else procJ(contenido,cadena += "r");
    }

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token prompt
     */ 
    public void procP(char[] contenido, String cadena) {
            if (indice < contenido.length && (contenido[indice] =='r')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='o')) {
                    indice++;
                    if (indice < contenido.length && (contenido[indice] =='m')) {
                        indice++;
                        if (indice < contenido.length && (contenido[indice] =='p')) {
                            indice++;
                            if (indice < contenido.length && (contenido[indice] =='t')) {
                                new Token("PROPMT",null);//genera token PROMPT
                                indice++;
                            } else procJ(contenido,cadena += "t");
                        } else procJ(contenido,cadena += "m");
                    } else procJ(contenido,cadena += "p");
                } else procJ(contenido,cadena += "o");
            } else procJ(contenido,cadena += "r");
    }

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token return
     */ 
    public void procR(char[] contenido, String cadena) {
            if (indice < contenido.length && (contenido[indice] =='e')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='t')) {
                    indice++;
                    if (indice < contenido.length && (contenido[indice] =='u')) {
                        indice++;
                        if (indice < contenido.length && (contenido[indice] =='r')) {
                            indice++;
                            if (indice < contenido.length && (contenido[indice] =='n')) {
                                new Token("RETURN",null);//genera token RETURN
                                indice++;
                            } else procJ(contenido,cadena +="n");
                        } else procJ(contenido,cadena +="r");
                    } else procJ(contenido,cadena +="u");
                } else procJ(contenido,cadena +="t");
            } else procJ(contenido,cadena +="e");
    }

    /**
     *  param: array de caracteres y cadena de caracteres
     *  function: generear token var
     */ 
    public void procS(char[] contenido, String cadena) {
            if (indice < contenido.length && (contenido[indice] =='a')) {
                indice++;
                if (indice < contenido.length && (contenido[indice] =='r')) {
                    toReturn = new Token("VAR",null);//genera token VAR
                    indice++;
                } else procJ(contenido,cadena += "r");
            } else procJ(contenido,cadena += "a");
    }

    //GETTERS 
    public static int getIndice() {
        return indice;
    }

    public static int getDigit() {
       return digit; 
    }

    public char[] getA() {
        return a;
    }

    public static String getCadena() {
        return cadena; 
     }
     
     public File getArchivo() {
         return archivo;
     }

     public BufferedWritter getBw() {
         return bw;
     }
     
     public void setBw(BufferedWriter bw) {
        this.bw = bw;
     } 

     public Token al(TablaSimbolos tabla_simbolos) {
       Token toReturn = null;
       while (toReturn == null) {
           toReturn = this.procS(this.getA(), tablaSimbolos);
       }
       this.bw.write(toReturn.toString());
       this.bw.newLine();
       return toReturn;
     }
}
