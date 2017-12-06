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
  public Sintactico() throws DeclaracionIncompatibleException, IOException {

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
    File archivoTablas = new File(miDir + "//impreso//tablas.txt");
    File archivoParse = new File(miDir + "//impreso//parse.txt");
    File archivoError = new File(miDir + "//impreso//error.txt");

    try {
      this.tablasWriter = new BufferedWriter(new FileWriter(archivoTablas));
      this.parseWriter = new BufferedWriter(new FileWriter(archivoParse));
      this.errorWriter = new BufferedWriter(new FileWriter(archivoError));
    } catch (IOException ex) {
      System.out.println("Ha habido un problema inicializando el fichero de tablas, probablemente no se cree correctamente.");
    }

  }

  //Empareja:
  public void empareja(Token valor) throws EmparejaException, ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException {
    
    if (valor != null && valor.equals(tokenDevuelto)) {
      tokenDevuelto = analizador.al(tS);
    } 
    else {
      throw new EmparejaException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token " + valor.toString() + " y se ha encontrado el token " + this.getTokenDevuelto().toString());
    }

  } 

  //ProcedP: 
  //First de P: var if id prompt write function eof
  public void procedP() throws FirstNoCoincideException, EmparejaException, CadenaException, OpLogicoException, ComentarioException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //P -> B Z P
    if ("VAR".equals(this.getTokenDevuelto().getId()) || "IF".equals(this.getTokenDevuelto().getId()) || "ID".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "WRITE".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "1 ");
      
      procedB();
      procedZ();
      procedP();
    }
    //P -> Fq Z P
    else if ("FUNCTION".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "2 ");
      
      procedFq();
      procedZ();
      procedP();
    }
    //P -> eof
    else if ("EOF".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "3 ");
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < EOF , _ >, < PR , for >, < PR , function >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedB:
  //First de B: var if id prompt write
  public void procedB() throws FirstNoCoincideException, EmparejaException, CadenaException, OpLogicoException, OtroSimboloException, ComentarioException, FueraDeRangoException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //B -> var F id D D1
    if ("VAR".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "4 ");
      
      flagDeclaracion = true;
      empareja(new Token("VAR", null));
      flagDeclaracion = false;

      procedF();
      empareja(new Token("ID", null));
      procedD();
      procedD1();
    }
    //B -> if ( E ) G
    else if ("IF".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "5 ");

      empareja(new Token("IF", null));
      empareja(new Token("PARARENTABIERTO", null));
      procedE();
      empareja(new Token("PARARENTCERRADO", null));
      procedG();
    }
    //B -> S
    else if ("WRITE".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "ID".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "6 ");

      procedS();
    } 
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < ID , id >, < PR , if >, < PR , prompt >, < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }
  }
  //ProcedS:
  //First de S: id prompt write
  public void procedS() throws FirstNoCoincideException, EmparejaException, ComentarioException, FueraDeRangoException, OtroSimboloException, CadenaException, IdException, IOException, OpLogicoException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //S -> id S1
    if ("ID".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "7 ");

          tokenLlamador = tokenDevuelto;
          empareja(new Token("ID", null));
          procedS1();
      }
      //S -> prompt ( id )
      else if ("PROMPT".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "8 ");
          
          empareja(new Token("PROMPT", null));
          empareja(new Token("PARENTABIERTO", null));

          //Varible no declarada: en este caso es global y entera.
          if (tS.getTipo(tokenDevuelto) == null) {
            tS.addTipo(tokenDevuelto, "ENTERA");
            tS.addDireccion(tokenDevuelto, 2);
          }

          empareja(new Token("ID", null));
          empareja(new Token("PARENTCERRADO", null));
      }
      //S -> write ( E )
      else if ("WRITE".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "9 ");
          
          empareja(new Token("WRITE", null));
          empareja(new Token("PARENTABIERTO", null));
          procedE();
          empareja(new Token("PARENTCERRADO", null));
      }
      else {
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , prompt >) pero se ha detectado: " + this.getTokenDevuelto().toString());
      }

  }

  //ProcedS1:
  //First de S1: ( = /
  public void procedS1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, IdException, OtroSimboloException, CadenaException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //S1 -> = E ;
    if ("IGUAL".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "10 ");

      empareja(new Token("IGUAL", null));
      procedE();
      if ("VOID".equals(tipo)) {
        System.out.println("Error de asignacion en el metodo de procedS1");
      }

      tS.addTipo(tokenLlamador, tipo);
      tS.addDireccion(tokenLlamador, ancho);
      empareja(new Token("PUNTOYCOMA",  null));
    }
    //S1 -> ( L ) ;
    else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "11 ");

      int contParam = 0;
      
      empareja(new Token("PARENTABIERTO", null));

      contParam = procedL();
      if (nombreFuncion != null && nombreFuncion.equals(tokenLlamador) && tS.getNParametrosGlobal(tokenLlamador) == contParam) {
              tS.buscaTSGlobal(tokenLlamador.getValor());
      }
      else if (tS.buscaTS(tokenLlamador.getValor())[0] == null || !"FUNCTION".equals(tS.getTipo(tokenLlamador))) {
          System.out.println("La funcion no ha sido declarada en el metodo procedS1");
      }
      else if (tS.getNParametros(tokenLlamador) != contParam) {
          System.out.println("Error en los parametros de la funcion que ha sido llamada");
      }
      empareja(new Token("PARENTCERRADO", null));
      empareja(new Token("PUNTOYCOMA", null));
    }
    //S1 -> /= E ;
    else if ("ASIGDIV".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "12 ");

      empareja(new Token("ASIGDIV", null));
      procedE();
      if ("VOID".equals(tipo)) {
        System.out.println("Error de asignacion en el metodo de procedS1");
      }
      this.tS.addTipo(tokenLlamador, tipo);
      this.tS.addDireccion(tokenLlamador, ancho);
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PARENTABIERTO , _ >, < ASIG , _ > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }
  }

  //ProcedFq:
  //First de Fq: function
  public void procedFq() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IOException, IdException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    int contParam = 0;

    //Fq -> function F id ( A ) Z { Z Cfun }
    if ("FUNCTION".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "13 ");

      flagDeclaracion = true;
      empareja(new Token("FUNCTION", null));
      nombreFuncion = this.getTokenDevuelto();
      flagDeclaracion = false;

      //Ahora creamos la tabla de simbolos local: suponemos que un puntero ocupa 4 bytes.
      this.tS.addTipo(tokenDevuelto, "FUNCTION");
      this.tS.addDireccion(tokenDevuelto, 4);
      this.tS.crearTSL();

      TablaSimbolos tLocal = (TablaSimbolos) this.tS.getTablaSimbolos().get(this.tS.getContadorRegistro() - 1)[0];
      if (tLocal != null) {
        //Palabras reservadas a la tabla local: REVISAR
        tLocal.vaciarTabla();
      }

      procedF();
      empareja(new Token("ID", null));

      flagDeclaracion = true;
      empareja(new Token("PARENTABIERTO", null));
      contParam = procedA();
      //Almacenamos el numero de parametros
      this.tS.addParametros(contParam);
      flagDeclaracion = false;

      empareja(new Token("PARENTCERRADO", null));
      procedZ();
      empareja(new Token("LLAVEABIERTA", null));
      procedZ();
      procedCfun();

      //Escribimos y posteriormente borramos la tabla de simbolos local.
      this.tS.addEtiqueta();
      this.tablasWriter.write("TABLA LOCAL DE LA FUNCION: "+nombreFuncion.getValor());
      this.tS.volcarTabla(tablasWriter);
      this.tS.borrarTS();

      empareja(new Token("LLAVECERRADA", null));

      if (flagReturn) {
        this.tipo = "VOID";
      }
      this.tS.addDevuelve(this.tipo);
      nombreFuncion = null;
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < PR , function > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedZ: debemos detectar el salto de linea.
  //First de Z: cr
  public void procedZ() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, OtroSimboloException, IdException, CadenaException, FueraDeRangoException, IOException, DeclaracionIncompatibleException {

    //Z -> cr Z1
    if ("CR".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "14 ");

      empareja(new Token("CR", null));
      procedZ1();
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedZ1: debemos detectar el salto de linea.
  //First de Z1: cr lambda
  public void procedZ1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, IdException, CadenaException, OtroSimboloException, FueraDeRangoException, IOException, DeclaracionIncompatibleException {

    //Z1 -> Z
    if ("CR".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "15 ");

      procedZ();
    }
    //Z1 -> lambda
    else {
      this.setParse(this.getParse() + "16 ");
    }

  }

  //ProcedA: ATENCIONC A ESTE METODO QUE TENEMOS QUE CREAR LA VARIABLE SEGUN EL TIPO EN EL PROCEDURE DE F
  //First de A: int bool chars lambda
  public int procedA() throws EmparejaException, CadenaException, OpLogicoException, ComentarioException, FueraDeRangoException, OtroSimboloException, IdException, IOException, DeclaracionIncompatibleException, FirstNoCoincideException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    int contParam = 0;

    //A -> F id A1
    if ("NUM".equals(this.getTokenDevuelto().getId()) || "CHARS".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "17 ");

      //Incluimos en la tabla de simbolos la variable.
      procedF();
      empareja(new Token("ID", null));

      if ("NUM".equals(this.getTokenDevuelto().getId())) {
        tS.addTipo(tokenDevuelto, "ENTERA");
        tS.addDireccion(tokenDevuelto, 2);
      }
      else if ("CHARS".equals(this.getTokenDevuelto().getId())) {
        tS.addTipo(tokenDevuelto, "CADENA");
        tS.addDireccion(tokenDevuelto, 4);
      }
      else {
        tS.addTipo(tokenDevuelto, "BOOL");
        tS.addDireccion(tokenDevuelto, 1);
      }

      contParam = procedA1();
      contParam++;
    }
    //A -> lambda
    else {
      this.setParse(this.getParse() + "18 "); 
    }

    return contParam;

  }

  //ProcedA1:
  //First de A1: , lambda
  public int procedA1() throws EmparejaException, FirstNoCoincideException, ComentarioException, CadenaException, OpLogicoException, OtroSimboloException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    int contParam = 0;
    //A1 -> , A
    if ("COMA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "19 ");

      empareja(new Token("COMA", null));
      procedF();
      empareja(new Token("ID", null));
      contParam = procedA1();
      contParam++;
    }
    //A1 -> lambda
    else {
      this.setParse(this.getParse() + "20 ");
    }

    return contParam;

  }

  //ProcedD:
  //First de D: = lambda
  public void procedD() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //D -> = E
    if ("IGUAL".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "21 ");

      empareja(new Token("IGUAL", null));
      procedE();
    }
    //D -> lambda
    else {
      this.setParse(this.getParse() + "22 ");
    }

  }

  //ProcedD1: CORREGIR EL ERROR POR MENSAJE
  //First de D1: , ;
  public void procedD1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //D1 -> , id D1
    if ("COMA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "23 ");

      empareja(new Token("COMA", null));
      empareja(new Token("ID", null));
      procedD1();
    }
    //D1 -> ;
    else if ("PUNTOYCOMA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "24 ");

      empareja(new Token("PUNTOYCOMA", null));      
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG: CORREGIR EL ERROR POR MENSAJE
  //First de G: { id prompt write
  public void procedG() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G -> { S } G1
    if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "25 ");

      empareja(new Token("LLAVEABIERTA", null));
      procedS();
      empareja(new Token("LLAVECERRADA", null));
      procedG1();
    }
    //G -> S
    else if ("ID".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "WRITE".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "26 ");

      procedS();
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG1: CORREGIR EL ERROR POR MENSAJE, OJO CON EL FIRST DE LAMBDA
  //First de G1: else lambda
  public void procedG1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G1 -> else { S }
    if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "27 ");

      empareja(new Token("ELSE", null));
      empareja(new Token("LLAVEABIERTA", null));
      procedS();
      empareja(new Token("LLAVECERRADA", null));
    }
    //G1 -> lambda
    else {
      this.setParse(this.getParse() + "28 ");
    }

  }

  //ProcedG2: CORREGIR EL ERROR POR MENSAJE
  //First de G2: { id prompt write return
  public void procedG2() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException, DevuelveCadenaException {

    //G2 -> { Sfun } G3
    if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "29 ");

      empareja(new Token("LLAVEABIERTA", null));
      procedSfun();
      empareja(new Token("LLAVECERRADA", null));
      procedG3();
    }
    //G2 -> Sfun
    else if ("ID".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "WRITE".equals(this.getTokenDevuelto().getId()) || "RETURN".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "30 ");

      procedSfun();
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG3: CORREGIR EL ERROR POR MENSAJE. OJO CON LAMBDA
  //First de G3: else lambda
  public void procedG3() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException, DevuelveCadenaException {

    //G3 -> else { Sfun }
    if ("ELSE".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "31 ");

      empareja(new Token("ELSE", null));
      empareja(new Token("LLAVEABIERTA", null));
      procedSfun();
      empareja(new Token("LLAVECERRADA", null));
    }
    //G3 -> lambda
    else {
       this.setParse(this.getParse() + "32 ");
    }

  }

  //ProcedL: CORREGIR EL ERROR POR MENSAJE.
  //First de L: id d l ( lambda
  private int procedL() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, IdException, CadenaException, FueraDeRangoException, OtroSimboloException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
      
      int contParam = 0;

      //L -> E Q
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "33 ");
          
          contParam++;
          procedE();
          contParam += procedQ();
      }
      //L -> lambda
      else {
          this.setParse(this.getParse() + "34 ");
      }

      return contParam;

  }

  //ProcedQ: CORREGIR EL ERROR POR MENSAJE.
  //First de Q: , lambda
  private int procedQ() throws FirstNoCoincideException, EmparejaException, OpLogicoException, CadenaException, FueraDeRangoException, IdException, OtroSimboloException, ComentarioException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
      
      int contParam = 0;
      
      //Q -> , E Q
      if ("COMA".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "35 ");
          
          contParam++;
          empareja(new Token("COMA", null));
          procedE();
          contParam += procedQ();
      }
      //Q  -> lambda
      else {
          this.setParse(this.getParse() + "36 ");
      }

      return contParam;
    
  }

  //ProcedX: CORREGIR EL ERROR POR MENSAJE.
  //First de X: id d l ( ;
  public void procedX() throws FirstNoCoincideException, EmparejaException, IOException, CadenaException, ComentarioException, OpLogicoException, FueraDeRangoException, IdException, OtroSimboloException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
      //X -> E ;
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "37 ");
          
          procedE();
          if ("CADENA".equals(tipo)) {
              throw new DevuelveCadenaException("Error en linea: " + Integer.toString(Lexico.linea) + ". Se ha intentado devolver una cadena y solo se permite devolver un entero o vacio.");
          }
          empareja(new Token("PUNTOYCOMA", null));
      }
      //X -> ; 
      else if ("PUNTOYCOMA".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "38 ");

          this.tipo = "VOID";
          empareja(new Token("PUNTOYCOMA", null));
      }
      else {
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
      }

  }

  //ProcedE: CORREGIR EL ERROR POR MENSAJE.
  //First de E: id d l ( 
  private void procedE() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, IdException, OtroSimboloException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
      
      //E -> T R1
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
          this.setParse(this.getParse() + "39 ");
          
          procedT();
          procedR1();
      }
      else {
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
      }
  }

  //ProcedR1: CORREGIR EL ERROR POR MENSAJE.
  //First de R1: && lambda
  public void procedR1() throws FirstNoCoincideException, EmparejaException, CadenaException, ComentarioException, OpLogicoException, IdException, OtroSimboloException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        
        String tipoR1 = "";
        
        //R1 ->&& T R1
        if ("AND".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "40 ");

            switch (tipo) {
                case "CADENA":
                    tipoR1 = "CADENA";
                    break;
                case "VOID":
                    tipoR1 = "VOID";
                    break;
                default:
                    tipoR1 = "ENTERA";
                    break;
            }
            empareja(new Token("AND", null));
            procedT();
            if ("VOID".equals(tipo) || "VOID".equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoR1) && tipo.equals("CADENA")) {
                throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(Lexico.linea) + ". La conjuncion de cadenas no se puede realizar.");
            }
            procedR1();
        }
        else {
            this.setParse(this.getParse() + "41 ");
        }
    }

  //ProcedT: CORREGIR EL ERROR POR MENSAJE.
  //First de T: id d l (
  public void procedT() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, OtroSimboloException, FueraDeRangoException, CadenaException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    String tipoT = "";

    //T -> H T1
    if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "42 ");
        
        procedH();
        procedT1();
    }
    else {
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }
  }

  //ProcedT1: CORREGIR EL ERROR POR MENSAJE.
  //First de T1: < lambda
  public void procedT1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, FueraDeRangoException, OtroSimboloException, CadenaException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    String tipoT1 = "";

    //T1 -> < H T1
    if ("MENORQUE".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "43 ");

        switch (tipo) {
            case "CADENA":
                tipoT1 = "CADENA";
                break;
            case "VOID":
                tipoT1 = "VOID";
                break;
            default:
                tipoT1 = "ENTERA";
                break;
        }
        empareja(new Token("MENORQUE", null));
        procedH();
        if ("VOID".equals(tipo) || "VOID".equals(tipoT1)) {
            throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
        } else if (!tipo.equals(tipoT1)) {
            throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Los tipos de los operandos no coinciden.");
        } else if (tipo.equals(tipoT1) && "CADENA".equals(tipo)) {
            tipo = "ENTERA";
            ancho = 2;
        }
        procedT1();
    }
    //T1 -> lambda
    else {
        this.setParse(this.getParse() + "44 ");
    }

  }

  //ProcedH: CORREGIR EL ERROR POR MENSAJE.
  //First de H: id d l (
  public void procedH() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, OtroSimboloException, FueraDeRangoException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
    //H -> F1 H1
    if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId()) || "PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "45 ");
        
        procedF1();
        procedH1();
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }
  }

  //ProcedH1: CORREGIR EL ERROR POR MENSAJE.
  //First de H1: + lambda
  public void procedH1() throws FirstNoCoincideException, EmparejaException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, ComentarioException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    String tipoH1 = "";

    //H1 -> + F1 H1
    if ("SUMA".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "46 ");
        
        switch (tipo) {
            case "CADENA":
                tipoH1 = "CADENA";
                break;
            case "VOID":
                tipoH1 = "VOID";
                break;
            default:
                tipoH1 = "ENTERA";
                break;
        }
        empareja(new Token("SUMA", null));
        procedF1();
        if ("VOID".equals(tipo) || "VOID".equals(tipoH1)) {
            throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
        } else if (!tipo.equals(tipoH1)) {
            throw new TiposDiferentesException("Error en linea: " + Integer.toString(Lexico.linea) + ". Los tipos de los sumandos no coinciden.");
        } else if (tipo.equals(tipoH1) && tipo.equals("CADENA")) {
            throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(Lexico.linea) + ". No esta implementada la concatenacion de cadenas.");
        }
        procedH1();
    }
    //H1 -> lambda
    else {
        this.setParse(this.getParse() + "47 ");
    }

  }

  //ProcedF: CORREGIR EL ERROR POR MENSAJE.
  //First de F: int bool chars
  public void procedF() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //F -> int
    if ("INT".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "48 ");

      empareja(new Token("INT", null));
    }
    //F -> chars
    else if ("CHARS".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "49 ");

      empareja(new Token("CHARS", null));
    }
    //F -> bool
    else if ("BOOL".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "50 ");

      empareja(new Token("BOOL", null));
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedF1: CORREGIR EL ERROR POR MENSAJE.
  //First de F1: id d l (
  public void procedF1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
    Token tokenAuxiliar = tokenLlamador;
    
    //F1 -> id F2
    if ("ID".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "51 ");
        
        tokenLlamador = tokenDevuelto;
        if (tS.getTipo(tokenDevuelto) == null) {
            tS.addTipo(tokenDevuelto, "ENTERA");
            tS.addDireccion(tokenDevuelto, 2);
        } else if ("CADENA".equals(tS.getTipo(tokenDevuelto))) {
            tipo = "CADENA";
            ancho = tokenDevuelto.getValor().length();
        } else if ("NUM".equals(tS.getTipo(tokenDevuelto))) {
            tipo = "ENTERA";
            ancho = 2;
        }
        empareja(new Token("ID", null));
        procedF2();
    }
    //F1 -> l
    else if ("CADENA".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "52 ");
        
        tipo = "CADENA";
        ancho = this.tokenDevuelto.getValor().length();
        empareja(new Token("CADENA", null));
    }
    //F1 -> d
    else if ("NUM".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "53 ");
        
        ancho = 2;
        tipo = "ENTERA";
        empareja(new Token("NUM", null));
    }
    //F1 -> ( E )
    else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "54 ");

        empareja(new Token("PARENTABIERTO", null));
        procedE();
        if ("CADENA".equals(tipo)) {
            throw new TipoIncorrectoException("Error en linea: " + Integer.toString(Lexico.linea) + ". La condicion de la sentencia condicional ternaria no puede ser una cadena.");
        } else if ("VOID".equals(tipo)) {
            throw new TipoIncorrectoException("Error en linea: " + Integer.toString(Lexico.linea) + ". La condicion de la sentencia condicional ternaria no puede ser una funcion que puede ser 'VOID'.");
        }
        empareja(new Token("PARENTCERRADO", null));
    }
    else {
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

    tokenLlamador = tokenAuxiliar;

  }

  //ProcedF2: CORREGIR EL ERROR POR MENSAJE.
  //First de F2: ( lambda
  public void procedF2() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, OtroSimboloException, IdException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //F2 -> ( L )
    if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
      if (tS.getTipo(tokenLlamador) == null) {
          throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(Lexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' no ha sido declarada.");
      }
      this.setParse(this.getParse() + "55 ");
      
      int contParam = 0;
      if (tS.buscaTS(tokenLlamador.getValor())[0] == null && "FUNCTION".equals(tS.getTipo(tokenLlamador))) {
          throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(Lexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' no ha sido declarada.");
      }
      
      empareja(new Token("PARENTABIERTO", null));
      contParam = procedL();
      if (tS.getNParametros(tokenLlamador) != contParam) {
          throw new FuncionNoDeclaradaException("Error en linea " + Integer.toString(Lexico.linea) + " La funcion '" + tokenLlamador.getValor() + "' debe ser llamada con " + tS.getNParametros(tokenLlamador) + " parametros y se ha llamado con " + contParam + ".");
      }
      empareja(new Token("PARENTCERRADO", null));
      if (tS.getDevuelve(tokenLlamador) != null) {
        tipo = tS.getDevuelve(tokenLlamador);
      }
    }
    //F2 -> lambda
    else {
      if ("FUNCTION".equals(tS.getTipo(tokenLlamador))) {
        throw new DeclaracionIncompatibleException("Error en linea " + Lexico.linea + ". La variable o funcion '" + tokenLlamador.getValor() + "' ha sido declarada previamente.");
      }
      this.setParse(this.getParse() + "56 ");
    }

  }

  //ProcedBfun: CORREGIR EL ERROR POR MENSAJE.
  //First de Bfun: var if id prompt write return
  private boolean procedBfun() throws ComentarioException, OpLogicoException, OtroSimboloException, FueraDeRangoException, CadenaException, IdException, EmparejaException, IOException, FirstNoCoincideException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
    //Bfun -> var F id D D1
    if ("VAR".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "57 ");
      
      flagDeclaracion = true;
      empareja(new Token("VAR", null));
      flagDeclaracion = false;
      
      procedF();
      empareja(new Token("ID", null));
      procedD();
      procedD1();
    } 
    //Bfun -> if ( E ) G2
    else if ("IF".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "58 ");

      empareja(new Token("IF", null));
      empareja(new Token("PARARENTABIERTO", null));
      procedE();
      empareja(new Token("PARARENTCERRADO", null));
      procedG2();
      flagReturn = true;
    }
    //Bfun -> Sfun
    else if ("WRITE".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "ID".equals(this.getTokenDevuelto().getId()) || "RETURN".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "59 ");

      flagReturn = procedSfun();
    }
    else {
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < PR , return >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

    return flagReturn;

  }

  //ProcedSfun: CORREGIR EL ERROR POR MENSAJE.
  //First de Sfun: id prompt write return
  private boolean procedSfun() throws EmparejaException, OpLogicoException, IdException, OtroSimboloException, ComentarioException, FueraDeRangoException, FirstNoCoincideException, CadenaException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
    //Sfun -> id S1
    if ("ID".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "60 ");
        
        tokenLlamador = tokenDevuelto;
        
        empareja(new Token("ID", null));
        procedS1();
    }
    //Sfun -> prompt ( id ) ;
    else if ("PROMPT".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "61 ");

        empareja(new Token("PROMPT", null));
        empareja(new Token("PARENTABIERTO", null));
        if (tS.getTipo(tokenDevuelto) == null) {
            tS.addTipo(tokenDevuelto, "ENTERA");
            tS.addDireccion(tokenDevuelto, 2);
        } else if ("FUNCTION".equals(tS.getTipo(tokenDevuelto))) {
            throw new DeclaracionIncompatibleException("Error en linea " + Lexico.linea + ". La variable o funcion '" + tokenDevuelto.getValor() + "' ha sido declarada previamente.");
        }
        empareja(new Token("ID", null));
        empareja(new Token("PARENTCERRADO", null));
    }
    //Sfun -> write ( E )
    else if ("WRITE".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "62 ");
        
        empareja(new Token("WRITE", null));
        empareja(new Token("PARENTABIERTO", null));
        procedE();
        empareja(new Token("PARENTCERRADO", null));
    }
    //Sfun -> return X
    else if ("RETURN".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "63 ");
        
        empareja(new Token("RETURN", null));
        flagReturn = false;
        procedX();
    }
    else {
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(Lexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , prompt>,  < PR , return >) pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

    return flagReturn;

  }

  //ProcedCfun: CORREGIR EL ERROR POR MENSAJE.
  //First de Cfun: var if id prompt write return lambda
  private boolean procedCfun() throws CadenaException, FirstNoCoincideException, ComentarioException, OpLogicoException, OtroSimboloException, EmparejaException, IdException, IOException, FueraDeRangoException, FuncionNoDeclaradaException, VariableNoDeclaradaException, DevuelveCadenaException, TiposDiferentesException, CodigoMuertoException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
    
    //Cfun -> Bfun Z Cfun
    if ("VAR".equals(this.getTokenDevuelto().getId()) || "IF".equals(this.getTokenDevuelto().getId()) || "WRITE".equals(this.getTokenDevuelto().getId()) || "PROMPT".equals(this.getTokenDevuelto().getId()) || "ID".equals(this.getTokenDevuelto().getId()) || "RETURN".equals(this.getTokenDevuelto().getId())) {
        this.setParse(this.getParse() + "64 ");

        flagReturn = procedBfun();
        procedZ();
        procedCfun();
    }
    //Cfun -> lambda
    else {
        this.setParse(this.getParse() + "65 ");
    }

    return flagReturn;

  }

  public Lexico getAnalizador() {
        return analizador;
  }

  public void setAnalizador(Lexico analizador) {
      this.analizador = analizador;
  }

  public TablaSimbolos gettS() {
      return tS;
  }

  public void settS(TablaSimbolos tS) {
      this.tS = tS;
  }

  public Token getTokenDevuelto() {
      return tokenDevuelto;
  }

  public void setTokenDevuelto(Token tokenDevuelto) {
      this.tokenDevuelto = tokenDevuelto;
  }

  public String getParse() {
      return parse;
  }

  public void setParse(String parse) {
      this.parse = parse;
  }

  public BufferedWriter getTablasWriter() {
      return tablasWriter;
  }

  public void setTablasWriter(BufferedWriter tablasWriter) {
      this.tablasWriter = tablasWriter;
  }

  public BufferedWriter getParseWriter() {
      return parseWriter;
  }

  public void setParseWriter(BufferedWriter parseWriter) {
      this.parseWriter = parseWriter;
  }

  public BufferedWriter getErrorWriter() {
      return errorWriter;
  }

  public void setErrorWriter(BufferedWriter errorWriter) {
      this.errorWriter = errorWriter;
  }

  //Metodo main
  public static void main(String[] args) {
   
    Sintactico as = null;
        
    try {
        File ficheroAAnalizar=null;
        if (args != null) {
            ficheroAAnalizar = new File(miDir.getCanonicalPath() + "//pruebas//" + args[0]);
        }
        as = new Sintactico();
        if (args.length != 1) {
            throw new FileNotFoundException("Se han pasado " + args.length + " ficheros para analizar y solo debe pasarse un fichero.");
        } else if (!ficheroAAnalizar.exists()) {
            throw new FileNotFoundException("El fichero a analizar " + args[0].toString() + " no existe.");
        }
        as.getAnalizador().leerFicheros(ficheroAAnalizar);
        as.setTokenDevuelto(as.getAnalizador().al(as.gettS()));
        while (!"EOF".equals(as.getTokenDevuelto().getId())) {
            as.procedP();
        }
        as.gettS().volcarTabla(as.getTablasWriter());
        as.getParseWriter().write("DescendenteParser " + as.getParse());
        as.getAnalizador().getBw().close();
        as.getTablasWriter().close();
        as.getParseWriter().close();
        if (as.getAnalizador().getFr() != null) {
            as.getAnalizador().getFr().close();
        }

    } catch (FirstNoCoincideException | FileNotFoundException | EmparejaException | ComentarioException | CadenaException | OpLogicoException | OtroSimboloException | FueraDeRangoException | IdException | FuncionNoDeclaradaException | VariableNoDeclaradaException | DevuelveCadenaException | TiposDiferentesException | CodigoMuertoException | DeclaracionIncompatibleException | ConcatenacionNoImplementadaException | TipoIncorrectoException ex) {
        System.out.println(ex.getMessage());
        try {
            if (as != null) {
                as.getErrorWriter().write(ex.getMessage());
            }
            as.getErrorWriter().close();
        } catch (IOException exc) {
            System.out.println("Error escribiendo en el fichero de error.");
        }
        
    }
    //Error en la escritura/tratamiento de los ficheros generados
    catch (IOException ex) {
        System.out.println("Error con la escritura o tratamiento de alguno de los ficheros generados.");
        try {
            if (as != null) {
                as.getErrorWriter().write("Error con la escritura o tratamiento de alguno de los ficheros generados.");
            }
            as.getErrorWriter().close();
        } catch (IOException exc) {
            System.out.println("Error escribiendo en el fichero de error.");
        }
    }
    //Excepcion no controlada
    catch (Exception ex) {
        System.out.println("Excepcion no controlada.");
        try {
            if (as != null) {
                as.getErrorWriter().write("Se ha producido una excepcion no controlada.");
            }
            as.getErrorWriter().close();
        } catch (IOException exc) {
            System.out.println("Error escribiendo en el fichero de error.");
        }
    }

  }

}