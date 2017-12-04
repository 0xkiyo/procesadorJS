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
			String ruta = Sintactico.miDir.getCanonicalPath()+"\\tokens.txt";
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
				toReturn = new Token("NUM",Long.toString(digit));//genera token (NUM,Valor)
			} else {
				//Error fuera de rango
			}
		} else if (contenido[indice] == '\"') {
			cadena = "";
			indice++;
			procE(contenido);
			toReturn = new Token("",cadena);//genera token (CHARS,LEXEMA)
		} else if (contenido[indice] == '+') {
			indice++;
			toReturn = new Token("SUMA",null);//genera token SUMA
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
		else if (isLetter(contenido[indice])) {
			cadena = Character.toString(contenido[indice]);//Devuelve un objeto String del caracter
			indice++;
			procG(contenido);
			toReturn = new Token("ID",cadena);//genera token (ID,LEXEMA)
		}else if (contenido[indice] == 'i' ) {
			cadena = "i";
			indice++;
			if(contenido[indice] == 'f') {
				cadena += Character.toString(contenido[indice]);
				indice++;
				toReturn = new Token("IF",null);//genera token IF
			} else if (contenido[indice] == 'n') {
				cadena += Character.toString(contenido[indice]);
				indice++;
				procQ(contenido);
				toReturn = new Token("INT",null);//genera token INT
			} else  procG(contenido);
		} else if (contenido[indice] == 'e') {
			cadena = "e"; 
			indice++;
			procM(contenido);
			new Token("ELSE",null);//genera token ELSE
		} else if (contenido[indice] == 'f') {
			cadena = "f"; 
			indice++;
			procN(contenido);
			new Token("FUNCTION",null);//genera token FUNCTION
		} else if (contenido[indice] == 'w') {
			cadena = "w"; 
			indice++;
			procO(contenido);
			new Token("WRITE",null);//genera token WRITE
		} else if (contenido[indice] == 'p') {
			cadena = "p"; 
			indice++;
			procP(contenido);
			new Token("PROMPT",null);//genera token PROMPT
		} else if (contenido[indice] == 'r') {
			cadena = "r"; 
			indice++;
			procR(contenido);
			new Token("RETURN",null);//genera token RETURN
		} else if (contenido[indice] == 'v') {
			cadena = "v"; 
			indice++;
			procU(contenido);
			new Token("VAR",null);//genera token VAR
		} else if (contenido[indice] == 'c') {
			cadena = "c"; 
			indice++;
			procT(contenido);
			new Token("CHARS",null);//genera token CHARS
		} else {
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
	public void procG(char[] contenido) {
		if (indice < contenido.length && (isDigit(contenido[indice]) || isLetter(contenido[indice]) || contenido[indice] == '_')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			procG(contenido);
		}
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token else
	 */ 
	public void procM(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='l')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='s')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='e')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
				} else procG(contenido);
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token function
	 */ 
	public void procN(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='u')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='n')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='c')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
					if (indice < contenido.length && (contenido[indice] =='t')) {
						cadena += Character.toString(contenido[indice]);
						indice++;
						if (indice < contenido.length && (contenido[indice] =='i')) {
							cadena += Character.toString(contenido[indice]);
							indice++;
							if (indice < contenido.length && (contenido[indice] =='o')) {
								cadena += Character.toString(contenido[indice]);
								indice++;
								if (indice < contenido.length && (contenido[indice] =='n')) {
									cadena += Character.toString(contenido[indice]);
									indice++;
								} else procG(contenido);
							} else procG(contenido);
						} else procG(contenido);
					} else procG(contenido);
				} else procG(contenido);
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token write
	 */ 
	public void procO(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='r')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='i')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='t')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
					if (indice < contenido.length && (contenido[indice] =='e')) {
						cadena += Character.toString(contenido[indice]);
						indice++;
					} else procG(contenido);
				} else procG(contenido);
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token prompt
	 */ 
	public void procP(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='r')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='o')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='m')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
					if (indice < contenido.length && (contenido[indice] =='p')) {
						cadena += Character.toString(contenido[indice]);
						indice++;
						if (indice < contenido.length && (contenido[indice] =='t')) {
							cadena += Character.toString(contenido[indice]);
							indice++;
						} else procG(contenido);
					} else procG(contenido);
				} else procG(contenido);
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token return
	 */ 
	public void procR(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='e')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='t')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='u')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
					if (indice < contenido.length && (contenido[indice] =='r')) {
						cadena += Character.toString(contenido[indice]);
						indice++;
						if (indice < contenido.length && (contenido[indice] =='n')) {
							cadena += Character.toString(contenido[indice]);
							indice++;
						} else procG(contenido);
					} else procG(contenido);
				} else procG(contenido);
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token var
	 */ 
	public void procU(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='a')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			if (indice < contenido.length && (contenido[indice] =='r')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
			} else procG(contenido);
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token int
	 */ 
	public void procQ(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='t')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
		} else procG(contenido);
	}

	/**
	 *  param: array de caracteres y cadena de caracteres
	 *  function: generear token chars
	 */ 
	public void procT(char[] contenido) {
		if (indice < contenido.length && (contenido[indice] =='h')) {
			cadena += Character.toString(contenido[indice]);
			indice++;    
			if (indice < contenido.length && (contenido[indice] =='a')) {
				cadena += Character.toString(contenido[indice]);
				indice++;
				if (indice < contenido.length && (contenido[indice] =='r')) {
					cadena += Character.toString(contenido[indice]);
					indice++;
					if (indice < contenido.length && (contenido[indice] =='s')) {
						cadena += Character.toString(contenido[indice]);
						indice++;
					} else procG(contenido);
				} else procG(contenido);
			} else procG(contenido);
		}else procG(contenido);
	}

	//GETTERS 
	public static int getIndice() {
		return indice;
	}

	public static Long getDigit() {
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

	public BufferedWriter getBw() {
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
