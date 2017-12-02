package lexico;

//Paquetes internos
import lexico.*;
import sun.security.pkcs.ContentInfo;
import error.*;
import jdk.nashorn.internal.parser.TokenStream;
import tabla_simbolos.*;
import token.*;

//Paquetes externos
import java.io.*;

import analizadorLexico.AnalizadorLexico;

public class Lexico {
    
    private File archivo = null;
    private FileReader fr = null;
    private BufferedWriter bw;
    public static Integer linea = 0;//Para recorrer las lineas
    public static int indice = 0;//Para recorrer el array
    public static Long digit = (long) 0;//Para detectar digitos
    
    /** 
	 * param: fichero a leer
     * funcion: lee ficheros
	 */
	public void leeFicheros(){
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
    * param: caracter a comprobar
    * funcion: evalua si el caracter es un digito
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
            cadena = 'i';
            indice++;
            if(contenido[indice] == 'f' && indice < contenido.length) {
                toReturn = new Token("IF",null);//genera token IF
                indice++;
            }
            else {
                procJ(contenido,cadena);
            }
        } else if (contenido[indice] == 't') {
            cadena = 't';
            indice++;
           procK(cadena);
        }
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
    public void procG(char[] contenido) {
        if (indice < contenido.length && (isDigit(caracter[indice]) || isLetter(caracter[indice]) || contenido[indice] == '_')) {
            cadena += Character.toString(contenido[indice]);
            indice++;
            procG(contenido);
        }
    }

    

}
