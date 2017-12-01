package sintactico;

//Paquetes internos
import lexico.*;
import error.*;
import tabla_simbolos.*;
import token.*;

//Paquetes externos
import java.io.*;

public class Sintactico {

  //Atributos de clase
  private Lexico analizador;
  private TablaSimbolos tS;
  private Token tokenDevuelto;
  private Token tokenLlamador;
  private static Token nombreFuncion;
  public static File miDir = new File(".");

  //Atributos para escritura de fichero
  private BufferedWriter tablasWriter;
  private BufferedWriter parseWriter;
  private BufferedWriter errorWriter;

  //Atributos basicos
  private String parse = null;
  private String tipo = null;
  private int ancho = 0;
  private int tabla = 0;
  private boolean declaracion;
  public static boolean flagDeclaracionLocal = false;
  public static boolean flagDeclaracion = false;
  private static boolean flagReturn = true;

  //Constructor: inicializar los archivos y atributos necesarios.
  public Sintactico() {

    //Inicializando atributos de clase
    this.analizador = new Lexico();
    this.tS = new TablaSimbolos();
    this.tokenDevuelto = new Token(null, null);
    this.tokenLlamador = new Token(null, null);
    this.nombreFuncion = new Token(null, null);

    //Inicializando los atributos basicos
    this.parse = "";
    this.tabla = 0;

    //Nuevos archivos
    File archivoTablas = new File(miDir + "\\tablas.txt");
    File archivoParse = new File(miDir + "\\parse.txt");
    File archivoError = new File(miDir + "\\error.txt");

    try {
      this.tablasWriter = new BufferedWriter(new FileWriter(archivoTablas));
      this.parseWriter = new BufferedWriter(new FileWriter(archivoParse));
      this.errorWriter = new BufferedWriter(new FileWriter(archivoError));
    } catch (IOException e) {
      System.out.println("Error: inicializacion de los ficheros tablas, parse y error.");
    }

  }

  //Empareja:
  public void empareja(Token valor) {
    
    if (valor != null && valor.equals(tokenDevuelto)) {
      tokenDevuelto = analizador.al(tS);
    }
    else {
      throw new Error("Error en la linea: "+Integer.toString(Lexico.linea));
    }

  } 

  //procedP:
  

}