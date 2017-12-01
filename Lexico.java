package lexico;

//Paquetes internos
import lexico.*;
import error.*;
import tabla_simbolos.*;
import token.*;

//Paquetes externos
import java.io.*;

public class Lexico {
    
    private File archivo = null;
    private FileReader fr = null;
    private BufferedWriter bw;
    public static Integer linea = 0;//Para recorrer las lineas
    public static int indice = 0;//Para recorrer el array
	/*
	 * param: fichero a leer
     * funcion: lee ficheros
	 */
	public void leeFicheros(){
		try {
        // Apertura del fichero e inicializacion de FileReader para leerlo
        this.archivo;
        this.fr = new FileReader(archivo);
        //Leer fichero
        this.a = new char [(int) archivo.length()];//Este Array nos almacena los caracteres
        this.fr.read(a); //añade el contenido al array
        String ruta = Lexico.miDir.getCanonicalPath()+"\\tokens.txt";
        File archivoTokens = new File(ruta);
        this.bw = new BufferedWriter(new FileWriter(archivoTokens));
		linea++;
		}catch (Exception e){
			System.out.println("Error al leer el fichero");
		}
	}

    /*
    * param: caracter a comprobar
    * funcion: evalua si el caracter es un digito
    */
    private boolean isDigit(char caracter){
        return caracter > 47 && caracter < 58;
    }

    /*
    * param: caracter a comprobar
    * function: evalua si el caracter es una letra
    */
    private boolean isLetter(char caracter){
        return (caracter > 64 && caracter < 91) || (caracter > 96 && caracter < 123);
    }

    /*
    * param: array con el contenido para crear los tokens y tabla de simbolos
    * function: dependiendo de las condciones genera los tokens
    */
    public Token procS(char [] contenido ,TablaSimbolos ts){
        Token toReturn = null;
        if (indice == contenido.length) {
            toReturn = new Token("EOF", null);
        } else if (contenido[indice] == '/') {
            indice++;
            procA(contenido);
        } else if (contenido[indice] == '\n') {
            linea++;
            indice++;//en este caso no se genera token
        } else if (contenido[indice] == '\r') {
            indice++;
            toReturn = new Token("CR",null);/////////Aqui estoy
        }
    }

    /*
    * param: array con el contenido a comprobar
    * function: detecta comentarios
    */
    public void procA(char[] contenido) {
        if (contenido[indice] == '/') {
            indice++;
            procB(contenido);
        }
        else{
            //Aquí va una saldida de error
        }
    }

    /*
    * param: array con el contenido a comprobar
    * function: detecta saltos de linea
    */
    public void procB(char [] contenido) {
        if (indice < contenido.length && contenido[indice] != '\r') {
            indice++;
            procB(contenido);
        }
    }
}
