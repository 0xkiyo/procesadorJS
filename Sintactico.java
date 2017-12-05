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
    File archivoTablas = new File(miDir + "\\tablas.txt");
    File archivoParse = new File(miDir + "\\parse.txt");
    File archivoError = new File(miDir + "\\error.txt");

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
      throw new EmparejaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token " + valor.toString() + " y se ha encontrado el token " + this.getTokenDevuelto().toString());
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
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < EOF , _ >, < PR , for >, < PR , function >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
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
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < ID , id >, < PR , if >, < PR , prompt >, < PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
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
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , prompt >) pero se ha detectado: " + this.getTokenDevuelto().toString());
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
      else if (tS.buscaTS(tokenLlamador.getValor())[0] == null || !"FUNC".equals(tS.getTipo(tokenLlamador))) {//tipo != FUNC porque si es variable o no declarada tiene que dar error.
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
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < PARENTABIERTO , _ >, < ASIG , _ > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
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
      this.tS.addDevuelve(this tipo);
      nombreFuncion = null;
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < PR , function > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedZ: debemos detectar el salto de linea.
  //First de Z: cr
  public void procedZ() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, OtroSimboloException, IdException, CadenaException, FueraDeRangoException, IOException, DeclaracionIncompatibleException {

    //Z -> cr Z1
    if ("CR".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse + "14 ");

      empareja(new Token("CR", null));
      procedZ1();
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
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
  public int procedA() throws EmparejaException, CadenaException, OpLogicoException, ComentarioException, FueraDeRangoException, OtroSimboloException, IdException, IOException, DeclaracionIncompatibleException {

    int contParam = 0;

    //A -> F id A1
    if ("NUM".equals(this.getTokenDevuelto().getId()) || "CHARS".equals(this.getTokenDevuelto().getId()) || "BOOL".equals(this.getTokenDevuelto().getId())) {
      this.setParse(this.getParse() + "17 ");

      //Incluimos en la tabla de simbolos la variable.
      procedF();
      empareja(new Token("ID", null));
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
  public int procedA1() throws EmparejaException, CadenaException, OtroSimboloException, OpLogicoException, ComentarioException, FueraDeRangoException, IdException, IOException, DeclaracionIncompatibleException {

    int contParam = 0;
    //A1 -> , A
    if ("COMA".equals()this.getTokenDevuelto().getId()) {
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
    if ("IGUAL".equals()this.getTokenDevuelto().getId()) {
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
  public void procedCD1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //D1 -> , id D1
    if ("COMA".equals()this.getTokenDevuelto().getId()) {
      this.setParse(this.getParse() + "23 ");

      empareja(new Token("COMA", null));
      empareja(new Token("ID", null));
      procedD1();
    }
    //D1 -> ;
    else if () {
      this.setParse(this.getParse() + "24 ");

      empareja(new Token("PUNTOYCOMA", null));      
    }
    else {
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG: CORREGIR EL ERROR POR MENSAJE
  //First de G: { id prompt write
  public void procedG() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G -> { S } G1
    if ("LLAVEABIERTA".equals()this.getTokenDevuelto().getId()) {
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
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG1: CORREGIR EL ERROR POR MENSAJE, OJO CON EL FIRST DE LAMBDA
  //First de G1: else lambda
  public void procedG1() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G1 -> else { S }
    if ("LLAVEABIERTA".equals()this.getTokenDevuelto().getId()) {
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
  public void procedG2() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G2 -> { Sfun } G3
    if ("LLAVEABIERTA".equals()this.getTokenDevuelto().getId()) {
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
      throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
    }

  }

  //ProcedG3: CORREGIR EL ERROR POR MENSAJE. OJO CON LAMBDA
  //First de G3: else lambda
  public void procedG3() throws FirstNoCoincideException, EmparejaException, ComentarioException, OtroSimboloException, IdException, OpLogicoException, FueraDeRangoException, IOException, CadenaException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {

    //G3 -> else { Sfun }
    if ("ELSE".equals()this.getTokenDevuelto().getId()) {
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
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId() || "PARENTABIERTO".equals(this.getTokenDevuelto().getId()) {
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
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId() || "PARENTABIERTO".equals(this.getTokenDevuelto().getId()) {
          this.setParse(this.getParse() + "37 ");
          
          procedE();
          if ("CADENA".equals(tipo)) {
              throw new DevuelveCadenaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Se ha intentado devolver una cadena y solo se permite devolver un entero o vacio.");
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
        throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar el token < CR , _ > pero se ha detectado: " + this.getTokenDevuelto().toString());
      }

  }

  //ProcedE: CORREGIR EL ERROR POR MENSAJE.
  //First de E: id d l ( 
  private void procedE() throws FirstNoCoincideException, EmparejaException, ComentarioException, OpLogicoException, CadenaException, FueraDeRangoException, IdException, OtroSimboloException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
      
      //E -> T R1
      if ("ID".equals(this.getTokenDevuelto().getId()) || "NUM".equals(this.getTokenDevuelto().getId()) || "CADENA".equals(this.getTokenDevuelto().getId() || "PARENTABIERTO".equals(this.getTokenDevuelto().getId()) {
          this.setParse(this.getParse() + "39 ");
          
          procedT();
          procedR1();
      } else {
          throw new FirstNoCoincideException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < NUM , num >, < ID , id > ) pero se ha detectado: " + this.getTokenDevuelto().toString());
      }
  }

  //ProcedR1: CORREGIR EL ERROR POR MENSAJE.
  //First de R1: && lambda
  public void procedR1() throws FirstNoCoincideException, EmparejaException, CadenaException, ComentarioException, OpLogicoException, IdException, OtroSimboloException, FueraDeRangoException, IOException, FuncionNoDeclaradaException, VariableNoDeclaradaException, TiposDiferentesException, DeclaracionIncompatibleException, ConcatenacionNoImplementadaException, TipoIncorrectoException {
        
        String tipoR1 = "";
        
        //R1 ->&& T R1
        if ("CONJUNCION".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "32 ");
            if (tipo.equals("CADENA")) {
                tipoR1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoR1 = "VOID";
            } else {
                tipoR1 = "ENT";
            }
            empareja(new Token("CONJUNCION", null));
            procedT();
            if ("VOID".equals(tipo) || "VOID".equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoR1) && tipo.equals("CADENA")) {
                throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La conjuncion de cadenas no se puede realizar.");
            }
            procedR1();
        } else if ("DISYUNCION".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "33 ");
            if (tipo.equals("CADENA")) {
                tipoR1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoR1 = "VOID";
            } else {
                tipoR1 = "ENT";
            }
            empareja(new Token("DISYUNCION", null));
            procedT();
            if ("VOID".equals(tipo) || "VOID".equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoR1)) {
                throw new TiposDiferentesException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoR1) && tipo.equals("CADENA")) {
                throw new ConcatenacionNoImplementadaException("Error en linea: " + Integer.toString(AnalizadorLexico.linea) + ". La disyuncion de cadenas no se puede realizar.");
            }
            procedR1();
        } else {
            this.setParse(this.getParse() + "34 ");
        }
    }

}