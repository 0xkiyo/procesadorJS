package analizadorSintactico;

import analizadorLexico.AnalizadorLexico;
import errores.ParserException;
import tablaSimbolos.TablaSimbolos;
import token.Token;

import java.io.*;

public class AnalizadorSintactico {

    public boolean flagDeclaracionLocal;
    public boolean flagDeclaracion;
    private Token nombreFuncion;
    private boolean flagReturn = true;
    //Atributos de clase
    private String parse;
    private AnalizadorLexico analizador;
    private TablaSimbolos tS;
    private Token tokenDevuelto;
    private BufferedWriter tablasWriter;
    private BufferedWriter parseWriter;
    private BufferedWriter errorWriter;
    //Atributos semantico
    private String tipo;
    private Token idFunction;
    private String[] tiposParam;
    private int ancho;
    private int contParamG;
    private Token tokenLlamador;

    private AnalizadorSintactico()
            throws ParserException {

        //Inicializando atributos de clase
        this.analizador = new AnalizadorLexico(this);
        this.tS = new TablaSimbolos(analizador);
        this.tokenDevuelto = new Token(null, null);
        this.tokenLlamador = new Token(null, null);
        nombreFuncion = new Token(null, null);
        this.tiposParam = new String[10];

        //Inicializando los atributos basicos
        this.parse = "";
        this.idFunction = null;
        this.contParamG = 0;

        //Nuevos archivos
        File archivoTablas = new File(".//impreso//tablas.txt");
        File archivoParse = new File(".//impreso//parse.txt");
        File archivoError = new File(".//impreso//error.txt");

        try {
            this.tablasWriter =
                    new BufferedWriter(new FileWriter(archivoTablas));
            this.parseWriter = new BufferedWriter(new FileWriter(archivoParse));
            this.errorWriter = new BufferedWriter(new FileWriter(archivoError));
        } catch (IOException ex) {
            System.out.println(
                    "Ha habido un problema inicializando el fichero de tablas, probablemente no se cree correctamente.");
        }

    }

    public static void main(String... args) {

        AnalizadorSintactico as = null;

        try {

            File ficheroAAnalizar = null;
            if (args != null) {
                ficheroAAnalizar = new File(".//pruebas//" + args[0]);
            }

            as = new AnalizadorSintactico();

            if (args.length != 1) {
                throw new FileNotFoundException("Se han pasado " + args.length +
                        " ficheros para analizar y solo debe pasarse un fichero.");
            } else if (!ficheroAAnalizar.exists()) {
                throw new FileNotFoundException(
                        "El fichero a analizar " + args[0] +
                                " no existe.");
            }

            as.getAnalizador().leerFichero(ficheroAAnalizar);
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

        } catch (FileNotFoundException | ParserException ex) {
            System.out.println(ex.getMessage());
//            ex.printStackTrace();
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
            System.out.println(
                    "Error con la escritura o tratamiento de alguno de los ficheros generados.");
            try {
                as.getErrorWriter().write(
                        "Error con la escritura o tratamiento de alguno de los ficheros generados.");
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
                    as.getErrorWriter().write(
                            "Se ha producido una excepcion no controlada.");
                }

            } catch (IOException exc) {
                System.out.println("Error escribiendo en el fichero de error.");
            }
        }
    }

    private void empareja(Token valor)
            throws ParserException,
            IOException {
        if (valor != null && valor.equals(tokenDevuelto)) {
            tokenDevuelto = analizador.al(tS);
        } else {
            throw new ParserException(ParserException.Reason.EMPAREJA, "Empareja: Error en linea: " +
                    Integer.toString(
                            analizador.linea) +
                    " Se esperaba detectar el token " +
                    valor.toString() +
                    " y se ha encontrado el token " +
                    this.getTokenDevuelto().toString());
        }
    }

    private void procedP()
            throws ParserException,
            IOException {

        //P -> B P = { var if id prompt write }
        if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())
                        || "var".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "1 ");

            procedB();
            procedP();
        }
        //P -> Fq P = { function }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "function".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "2 ");

            procedFq();
            procedP();
        }
        //P -> eof = { eof }
        else if ("EOF".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "3 ");
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < EOF , _ >, < PR , function >, < ID , id >, < PR , if >, < PR , prompt >, < PR , var >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedB()
            throws ParserException,
            IOException {

        //B -> var F2 id ; = { var }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "var".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "4 ");
            flagDeclaracion = true;
            empareja(new Token("PR", "var"));
            flagDeclaracion = false;

            //En este caso asignamos el tipo segun lo que recibamos: entero, chars y bool CORREGIR
            this.tokenLlamador = this.tokenDevuelto;

            procedF2();
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            } else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            } else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
            empareja(new Token("PUNTCOM", null));
        }
        //B -> S = { write prompt id if lambda }
        else if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "5 ");
            procedS();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , if >, < PR , prompt >, < PR , var >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedS()
            throws
            ParserException,
            IOException {

        //S -> id S1 ; = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "6 ");
            //Semantico
            tokenLlamador = tokenDevuelto;
            idFunction = tokenDevuelto;

            //Semantico
            empareja(new Token("ID", null));
            procedS1();
            empareja(new Token("PUNTCOM", null));
        } else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "prompt".equals(this.getTokenDevuelto().getValor())) {
            //S -> prompt ( id ) ; = { prompt }

            this.setParse(this.getParse() + "7 ");
            empareja(new Token("PR", "prompt"));
            empareja(new Token("PARENTABIERTO", null));
            if (tS.getTipo(tokenDevuelto) == null) {
                //caso en que no este declarada la variable
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("FUNC".equals(tS.getTipo(tokenDevuelto))) {
                throw new ParserException(ParserException.Reason.DECLARACION_INCOMPATIBLE,
                        "Error en linea " + analizador.linea +
                                ". La variable o funcion '" + tokenDevuelto.getValor() +
                                "' ha sido declarada previamente.");
            }
            empareja(new Token("ID", null));
            empareja(new Token("PARENTCERRADO", null));
            empareja(new Token("PUNTCOM", null));
        }
        //S -> write ( E ) ; = { write }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "write".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "8 ");
            empareja(new Token("PR", "write"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
            empareja(new Token("PUNTCOM", null));
        }
        //S -> if ( E ) C = { if }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "if".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "9 ");

            empareja(new Token("PR", "if"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
            procedC();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , if >, < PR , prompt >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedS1()
            throws
            ParserException,
            IOException {

        //S1 -> = E = { = }
        if ("ASIG".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "10 ");
            empareja(new Token("ASIG", null));
            procedE();
            if ("VOID".equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO,
                        "Error en linea: " + analizador.linea +
                                ". Esta intentando asignar una funcion que puede ser 'VOID' a una variable.");
            }
            //Comprobacion de tipos en la asignacion
            if (!tS.getTipo(tokenLlamador).equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO,
                        "Error en linea: " + analizador.linea +
                                ". Error de tipos en la asignacion.");
            }
            tS.addTipo(tokenLlamador, tipo);
            tS.addDireccion(tokenLlamador, ancho);
        } else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            //S1 -> ( L ) = { ( }
            this.setParse(this.getParse() + "11 ");
            //Semantico
            int contParam = 0;
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedL();

            //Comprobaciones
            if (nombreFuncion != null && nombreFuncion.equals(tokenLlamador) &&
                    tS.getNParametrosGlobal(tokenLlamador) == contParam) {
                tS.buscaTSGlobal(tokenLlamador.getValor());
            } else if (tS.buscaTS(tokenLlamador.getValor())[0] == null ||
                    !"FUNC".equals(tS.getTipo(
                            tokenLlamador))) {//tipo != FUNC porque si es variable o no declarada tiene que dar error.
                throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                        Integer.toString(
                                analizador.linea) +
                        " La funcion '" +
                        tokenLlamador.getValor() +
                        "' no ha sido declarada.");
            } else if (tS.getNParametros(tokenLlamador) != contParam) {
                throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                        Integer.toString(
                                analizador.linea) +
                        " La funcion '" +
                        tokenLlamador.getValor() +
                        "' debe ser llamada con " +
                        tS.getNParametros(
                                tokenLlamador) +
                        " parametros y se ha llamado con " +
                        contParam + ".");
            }
            for (int i = 0; i < this.tS.getTipoParametros(idFunction).length &&
                    this.tS.getTipoParametros(idFunction) != null &&
                    this.tiposParam[i] != null; i++) {
                if (!this.tS.getTipoParametros(idFunction)[i].equals(
                        this.tiposParam[i])) {
                    throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                            Integer.toString(
                                    analizador.linea) +
                            " La funcion '" +
                            tokenLlamador.getValor() +
                            "' debe ser llamada con los tipos correspondientes.");
                }
            }
            empareja(new Token("PARENTCERRADO", null));
        }
        //S1 -> /= E = { / }
        else if ("ASIGDIV".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "12 ");
            empareja(new Token("ASIGDIV", null));
            procedE();
            if ("VOID".equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO,
                        "Error en linea: " + analizador.linea +
                                ". Esta intentando asignar una funcion que puede ser 'VOID' a una variable.");
            }
            //Comprobacion de tipos enteros
            if (!tS.getTipo(tokenLlamador).equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO,
                        "Error en linea: " + analizador.linea +
                                ". Error de tipos en la asignacion con division.");
            } else if (!tipo.equals("ENTERA")) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO,
                        "Error en linea: " + analizador.linea +
                                ". Error de tipos en la asignacion con division. Deben de ser enteros.");
            }
            tS.addTipo(tokenLlamador, tipo);
            tS.addDireccion(tokenLlamador, ancho);
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < PARENTABIERTO , _ >, < ASIG , _ >, < ASIGDIV , _ >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedS2()
            throws
            ParserException,
            IOException {

        //S2 -> S = { id prompt write if }
        if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "13 ");

            procedS();
            procedS2();
        }
        //S2 -> lambda
        else {
            this.setParse(this.getParse() + "14 ");
        }
    }

    private void procedFq()
            throws
            ParserException,
            IOException {
        int contParam = 0;

        //Fq -> function F3 id ( A ) { Cfun } = { function }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "function".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "15 ");
            flagDeclaracion = true;
            empareja(new Token("PR", "function"));
            flagDeclaracion = false;

            //AQUI CREAMOS LA TABLA DE SIMBOLOS LOCAL Y EL DESPLAZAMIENTO LOCAL
            procedF3(); // En este paso tenemos que almacenar el tipo de la funcion CORREGIR
            this.tipo = "FUNC";
            this.ancho = 4;
            this.tS.addTipo(tokenDevuelto, tipo);
            this.tS.addDireccion(tokenDevuelto, 4);
            tS.crearTSL();
            TablaSimbolos tLocal = (TablaSimbolos) tS.getTablaSimbolos().get(
                    tS.getContadorRegistros() - 1)[0];
            if (tLocal != null) {
                tLocal.vaciarTabla();
            }
            nombreFuncion = this.getTokenDevuelto();
            empareja(new Token("ID", null));


            flagDeclaracionLocal = true;
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedA();
            //Almacenamos los tipos y el numero de parametros en la tS.
            this.tS.addParametros(contParam);
            this.tS.addTipoParametros(this.tiposParam);
            this.tiposParam = new String[10];

            flagDeclaracionLocal = false;
            empareja(new Token("PARENTCERRADO", null));

            empareja(new Token("LLAVEABIERTA", null));

            procedCfun();
            //Borramos la local.
            this.tS.addEtiqueta();
            //Justo antes de borrar volcamos la tabla de simbolos local correspondiente
            tablasWriter.write(
                    "TABLA DE LA FUNCION " + nombreFuncion.getValor() + " #" +
                            this.tS.indice++);
            this.tS.volcarTabla(tablasWriter);
            this.tS.borraTS();
            empareja(new Token("LLAVECERRADA", null));
            if (flagReturn) {
                tipo = "VOID";
            }
            tS.addDevuelve(tipo);
            nombreFuncion = null;

        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar el token < PR , function > pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private int procedA()
            throws ParserException,
            IOException {
        int contParam = 0;

        //A -> F2 id D = { int chars bool }
        if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("int".equals(this.getTokenDevuelto().getValor())
                        || "chars".equals(this.getTokenDevuelto().getValor()))
                || "bool".equals(this.getTokenDevuelto().getValor()))) {
            this.setParse(this.getParse() + "16 ");

            procedF2();
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            } else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            } else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            this.tiposParam[contParamG] =
                    this.tipo;  //almacenamos el tipo de parametro de la funcion
            tS.addDireccion(tokenDevuelto, ancho);
            empareja(new Token("ID", null));
            contParam++;
            contParam += procedD();
        }
        //A -> lambda
        else {
            this.setParse(this.getParse() + "17 ");
        }
        contParamG = 0;
        return contParam;
    }

    private int procedD()
            throws ParserException,
            IOException {
        int contParam = 0;

        //D -> , F2 id D = { , }
        if ("COMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "18 ");
            contParam++;
            empareja(new Token("COMA", null));
            this.tokenLlamador = this.tokenDevuelto;

            //Almacenamos el tipo del parametro
            procedF2();
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            } else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 4;
            } else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            contParamG++;
            tS.addTipo(this.tokenDevuelto, this.tipo);
            this.tiposParam[contParamG] =
                    this.tipo;  //almacenamos el tipo de parametro de la funcion
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
            contParam += procedD();
        }
        //D -> lambda
        else {
            this.setParse(this.getParse() + "19 ");
        }
        return contParam;
    }

    private void procedC()
            throws
            IOException, ParserException {

        //C -> { S S2 } C1 = { { }
        if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "20 ");

            empareja(new Token("LLAVEABIERTA", null));
            procedS();
            procedS2();
            empareja(new Token("LLAVECERRADA", null));
            procedC1();
        }
        //C -> S S2 = { write prompt id }
        else if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "21 ");

            procedS();
            procedS2();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < PR , write >, < ID , id >, < LLAVEABIERTA , - >, < PR , prompt > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }

    }

    private void procedC1()
            throws
            IOException, ParserException {

        //C1 -> else { S S2 } = { else }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "else".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "22 ");

            empareja(new Token("PR", "else"));
            empareja(new Token("LLAVEABIERTA", null));
            procedS();
            procedS2();
            empareja(new Token("LLAVECERRADA", null));
        }
        //C1 -> lambda
        else {
            this.setParse(this.getParse() + "23 ");
        }

    }

    private void procedC2()
            throws
            IOException, ParserException {

        //C2 -> { Sfun S2fun } C3 = { { }
        if ("LLAVEABIERTA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "24 ");

            empareja(new Token("LLAVEABIERTA", null));
            procedSfun();
            procedS2fun();
            empareja(new Token("LLAVECERRADA", null));
            procedC3();
        }
        //C2 -> Sfun S2fun = { write prompt id }
        else if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "25 ");

            procedSfun();
            procedS2fun();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar el token ( < PR , write >, < ID , id >, < LLAVEABIERTA , - >, < PR , prompt > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }

    }

    private void procedC3()
            throws
            IOException, ParserException {

        //C3 -> else { Sfun S2fun }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "else".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "26 ");

            empareja(new Token("PR", "else"));
            empareja(new Token("LLAVEABIERTA", null));
            procedSfun();
            procedS2fun();
            empareja(new Token("LLAVECERRADA", null));
        }
        //C3 -> lambda
        else {
            this.setParse(this.getParse() + "27 ");
        }

    }

    private int procedL()
            throws
            ParserException,
            IOException {
        int contParam = 0;

        //L -> E Q = { id cadena bool num }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) ||
                "CADENA".equals(this.getTokenDevuelto().getId()) ||
                "BOOL".equals(this.getTokenDevuelto().getId())
                || "ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "28 ");

            contParam++;
            //Debemos comprobar los tipos de los paramtros.
            if ("ID".equals(this.getTokenDevuelto().getId())) {
                this.tiposParam[contParamG++] =
                        this.tS.getTipo(this.getTokenDevuelto());
            } else {
                this.tiposParam[contParamG++] = this.getTokenDevuelto().getId();
            }
            procedE();
            contParam += procedQ();
        }
        //L -> lambda
        else {
            this.setParse(this.getParse() + "29 ");
        }
        contParamG = 0;
        return contParam;
    }

    private int procedQ()
            throws
            ParserException,
            IOException {
        int contParam = 0;

        //Q -> , E Q = { , }
        if ("COMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "30 ");
            contParam++;
            empareja(new Token("COMA", null));
            //Debemos comprobar los tipos de los paramtros.
            if ("ID".equals(this.getTokenDevuelto().getId())) {
                this.tiposParam[contParamG++] =
                        this.tS.getTipo(this.getTokenDevuelto());
            } else {
                this.tiposParam[contParamG++] = this.getTokenDevuelto().getId();
            }
            procedE();
            contParam += procedQ();
        }
        //Q -> lambda
        else {
            this.setParse(this.getParse() + "31 ");
        }
        return contParam;
    }

    private void procedX()
            throws IOException,
            ParserException {

        //X -> E = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) ||
                "CADENA".equals(this.getTokenDevuelto().getId()) ||
                "BOOL".equals(this.getTokenDevuelto().getId())
                || "ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "32 ");
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new ParserException(ParserException.Reason.DEVUELVE_CADENA, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Se ha intentado devolver una cadena y solo se permite devolver un entero o vacio.");
            }
        }
        //X -> lambda
        else {
            this.setParse(this.getParse() + "33 ");
            tipo = "VOID";
        }

    }

    private void procedE()
            throws
            ParserException,
            IOException {

        //E -> T R1 = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) ||
                "CADENA".equals(this.getTokenDevuelto().getId()) ||
                "BOOL".equals(this.getTokenDevuelto().getId())
                || "ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "34 ");
            procedT();
            procedR1();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < BOOL , cadena >, < ENTERA , num >, < ID , id > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedR1()
            throws ParserException,
            IOException {
        String tipoR1 = "";

        //R1 -> && T R1 = { && }
        if ("CONJUNCION".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "35 ");
            if (tipo.equals("CADENA")) {
                tipoR1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoR1 = "VOID";
            } else if ("BOOL".equals(tipo)) {
                tipoR1 = "BOOL";
            } else {
                tipoR1 = "ENTERA";
            }
            empareja(new Token("CONJUNCION", null));
            procedT();
            if ("VOID".equals(tipo) || "VOID".equals(tipoR1)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoR1)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoR1) && tipo.equals("CADENA")) {
                throw new ParserException(ParserException.Reason.CONCATENACION_NO_IMPLEMENTADA,
                        "Error en linea: " +
                                Integer.toString(analizador.linea) +
                                ". La conjuncion de cadenas no se puede realizar.");
            }
            procedR1();
        }
        //R1-> lambda
        else {
            this.setParse(this.getParse() + "36 ");
        }
    }

    private void procedT()
            throws
            ParserException, IOException {
        String tipoT = "";

        //T -> H T1 = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) ||
                "CADENA".equals(this.getTokenDevuelto().getId()) ||
                "BOOL".equals(this.getTokenDevuelto().getId())
                || "ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "37 ");
            procedH();
            procedT1();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < BOOL , cadena >, < ENTERA , num >, < ID , id > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedT1()
            throws
            ParserException, IOException {
        String tipoT1 = "";

        //T1 -> < H T1 = { < }
        if ("MENORQUE".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "38 ");
            if (tipo.equals("CADENA")) {
                tipoT1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoT1 = "VOID";
            } else if ("BOOL".equals(tipo)) {
                tipoT1 = "BOOL";
            } else {
                tipoT1 = "ENTERA";
            }
            empareja(new Token("MENORQUE", null));
            procedH();
            if ("VOID".equals(tipo) || "VOID".equals(tipoT1)) {
                throw new ParserException(ParserException.Reason.TIPOS_DIFERENTES, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoT1)) {
                throw new ParserException(ParserException.Reason.TIPOS_DIFERENTES, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Los tipos de los operandos no coinciden.");
            } else if (tipo.equals(tipoT1) && "CADENA".equals(tipo)) {
                tipo = "ENTERA";
                ancho = 2;
            }
            procedT1();
        }
        //T1 -> lambda
        else {
            this.setParse(this.getParse() + "39 ");
        }
    }

    private void procedH()
            throws
            ParserException,
            IOException {

        //H -> F H1 = { id cadena ent bool }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())
                || "ID".equals(this.getTokenDevuelto().getId()) ||
                "CADENA".equals(this.getTokenDevuelto().getId()) ||
                "BOOL".equals(this.getTokenDevuelto().getId())
                || "ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "40 ");
            procedF();
            procedH1();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < BOOL , cadena >, < ENTERA , num >, < ID , id > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
    }

    private void procedH1()
            throws
            ParserException,
            IOException {
        String tipoH1 = "";

        //H1 -> + F H1 = { + }
        if ("SUMA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "41 ");
            if (tipo.equals("CADENA")) {
                tipoH1 = "CADENA";
            } else if ("VOID".equals(tipo)) {
                tipoH1 = "VOID";
            } else if ("BOOL".equals(tipo)) {
                tipoH1 = "BOOL";
            } else {
                tipoH1 = "ENTERA";
            }
            empareja(new Token("SUMA", null));
            procedF();
            if ("VOID".equals(tipo) || "VOID".equals(tipoH1)) {
                throw new ParserException(ParserException.Reason.TIPOS_DIFERENTES, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Esta realizando una operacion con una funcion que puede ser 'VOID'.");
            } else if (!tipo.equals(tipoH1)) {
                throw new ParserException(ParserException.Reason.TIPOS_DIFERENTES, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". Los tipos de los sumandos no coinciden.");
            } else if (tipo.equals(tipoH1) && tipo.equals("CADENA")) {
                throw new ParserException(ParserException.Reason.CONCATENACION_NO_IMPLEMENTADA,
                        "Error en linea: " +
                                Integer.toString(analizador.linea) +
                                ". No esta implementada la concatenacion de cadenas.");
            }
            procedH1();
        }
        //H1 -> lambda
        else {
            this.setParse(this.getParse() + "42 ");
        }
    }

    private void procedF()
            throws
            ParserException,
            IOException {
        Token tokenAuxiliar = tokenLlamador;
        //F -> id F1 = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "43 ");
            //Semantico
            tokenLlamador = tokenDevuelto;
            if (tS.getTipo(tokenDevuelto) == null) {
                //Si la variable no esta declarada: chars "", int a cero y bool a false
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("CADENA".equals(tS.getTipo(tokenDevuelto))) {
                tipo = "CADENA";
                ancho = tokenDevuelto.getValor().length();
            } else if ("ENTERA".equals(tS.getTipo(tokenDevuelto))) {
                tipo = "ENTERA";
                ancho = 2;
            } else if ("BOOL".equals(tS.getTipo(tokenDevuelto))) {
                tipo = "BOOL";
                ancho = 1;
            }

            empareja(new Token("ID", null));
            procedF1();
        }
        //F -> cadena = { cadena }
        else if ("CADENA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "44 ");
            tipo = "CADENA";
            ancho = this.tokenDevuelto.getValor().length();
            empareja(new Token("CADENA", null));
        }
        //F -> ent = { ent }
        else if ("ENTERA".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "45 ");
            ancho = 2;
            tipo = "ENTERA";
            empareja(new Token("ENTERA", null));
        }
        //F -> bool = { bool }
        else if ("BOOL".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "46 ");
            ancho = 1;
            tipo = "BOOL";
            empareja(new Token("BOOL", null));
        }
        //F -> ( E ) = { ( }
        else if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "47 ");
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            if ("CADENA".equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". La condicion de la sentencia condicional ternaria no puede ser una cadena.");
            } else if ("VOID".equals(tipo)) {
                throw new ParserException(ParserException.Reason.TIPO_INCORRECTO, "Error en linea: " +
                        Integer.toString(
                                analizador.linea) +
                        ". La condicion de la sentencia condicional ternaria no puede ser una funcion que puede ser 'VOID'.");
            }
            empareja(new Token("PARENTCERRADO", null));
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < CADENA , cadena >, < PARENTABIERTO , _ >, < BOOL , cadena >, < ENTERA , num >, < ID , id > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
        tokenLlamador = tokenAuxiliar;
    }

    private void procedF1()
            throws
            ParserException,
            IOException {

        //F1 -> ( L ) = { ( }
        if ("PARENTABIERTO".equals(this.getTokenDevuelto().getId())) {
            if (tS.getTipo(tokenLlamador) == null) {
                throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                        Integer.toString(
                                analizador.linea) +
                        " La funcion '" +
                        tokenLlamador.getValor() +
                        "' no ha sido declarada.");
            }
            this.setParse(this.getParse() + "48 ");
            //Semantico
            int contParam = 0;
            //Comprobamos si la funcion existe
            if (tS.buscaTS(tokenLlamador.getValor())[0] == null &&
                    "FUNC".equals(tS.getTipo(tokenLlamador))) {
                throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                        Integer.toString(
                                analizador.linea) +
                        " La funcion '" +
                        tokenLlamador.getValor() +
                        "' no ha sido declarada.");
            }
            empareja(new Token("PARENTABIERTO", null));
            contParam = procedL();

            //Comprobamos el numero de parametros de la llamada.
            if (tS.getNParametros(tokenLlamador) != contParam) {
                throw new ParserException(ParserException.Reason.FUNCION_NO_DECLARADA, "Error en linea " +
                        Integer.toString(
                                analizador.linea) +
                        " La funcion '" +
                        tokenLlamador.getValor() +
                        "' debe ser llamada con " +
                        tS.getNParametros(
                                tokenLlamador) +
                        " parametros y se ha llamado con " +
                        contParam + ".");
            }
            empareja(new Token("PARENTCERRADO", null));

            if (tS.getDevuelve(tokenLlamador) != null) {
                tipo = tS.getDevuelve(tokenLlamador);
            }
        }
        //F1 -> lambda
        else {
            if ("FUNC".equals(tS.getTipo(tokenLlamador))) {
                throw new ParserException(ParserException.Reason.DECLARACION_INCOMPATIBLE,
                        "Error en linea " + analizador.linea +
                                ". La variable o funcion '" + tokenLlamador.getValor() +
                                "' ha sido declarada previamente.");
            }
            this.setParse(this.getParse() + "49 ");
        }
    }

    private void procedF2()
            throws
            ParserException,
            IOException {

        //F2 -> int = { int }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "int".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "50 ");

            this.tokenLlamador = this.getTokenDevuelto();
            empareja(new Token("PR", "int"));
        }
        //F2 -> chars = { chars }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "chars".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "51 ");

            this.tokenLlamador = this.getTokenDevuelto();
            empareja(new Token("PR", "chars"));
        }
        //F2 -> bool = { bool }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "bool".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "52 ");

            this.tokenLlamador = this.getTokenDevuelto();
            empareja(new Token("PR", "bool"));
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < PR , int >, < PR , chars >, < PR , bool > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }

    }

    private void procedF3()
            throws
            ParserException,
            IOException {

        //F3 -> int = { int }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "int".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "53 ");

            empareja(new Token("PR", "int"));
        }
        //F3 -> chars = { chars }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "chars".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "54 ");

            empareja(new Token("PR", "chars"));
        }
        //F3 -> bool = { bool }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "bool".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "55 ");

            empareja(new Token("PR", "bool"));
        }
        //F3 -> void = { void }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "void".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "56 ");

            empareja(new Token("PR", "void"));
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens ( < PR , int >, < PR , chars >, < PR , bool >, < PR, void > ) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }

    }

    private boolean procedBfun()
            throws
            ParserException,
            IOException {

        //Bfun - > var F2 id ; = { var }
        if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "var".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "57 ");
            flagDeclaracionLocal = true;
            empareja(new Token("PR", "var"));

            this.tokenLlamador = this.tokenDevuelto;

            procedF2();
            if ("int".equals(this.tokenLlamador.getValor())) {
                this.tipo = "ENTERA";
                this.ancho = 2;
            } else if ("chars".equals(this.tokenLlamador.getValor())) {
                this.tipo = "CADENA";
                this.ancho = 1;
            } else {
                this.tipo = "BOOL";
                this.ancho = 1;
            }

            tS.addTipo(this.tokenDevuelto, this.tipo);
            tS.addDireccion(tokenDevuelto, ancho);

            empareja(new Token("ID", null));
            flagDeclaracionLocal = false;

            empareja(new Token("PUNTCOM", null));

        }
        //Bfun -> Sfun = { write prompt id if return }
        else if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())
                        || "return".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "58 ");
            flagReturn = procedSfun();
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , return >, < ID , id >, < PR , if >, < PR , prompt >,  < PR , var >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
        return flagReturn;
    }

    private boolean procedSfun()
            throws
            ParserException, IOException {

        //Sfun -> id S1 ; = { id }
        if ("ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "59 ");
            //Semantico
            tokenLlamador = tokenDevuelto;
            idFunction = tokenDevuelto;

            //Semantico
            empareja(new Token("ID", null));
            procedS1();
            empareja(new Token("PUNTCOM", null));
        }
        //Sfun -> prompt ( id ) ; = { prompt }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "prompt".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "60 ");
            empareja(new Token("PR", "prompt"));
            empareja(new Token("PARENTABIERTO", null));
            if (tS.getTipo(tokenDevuelto) == null) {
                //No esta declarada la variable
                tipo = "ENTERA";
                ancho = 2;
                tS.addTipo(tokenDevuelto, tipo);
                tS.addDireccion(tokenDevuelto, ancho);
            } else if ("FUNC".equals(tS.getTipo(tokenDevuelto))) {
                throw new ParserException(ParserException.Reason.DECLARACION_INCOMPATIBLE,
                        "Error en linea " + analizador.linea +
                                ". La variable o funcion '" + tokenDevuelto.getValor() +
                                "' ha sido declarada previamente.");
            }
            empareja(new Token("ID", null));
            empareja(new Token("PARENTCERRADO", null));
            empareja(new Token("PUNTCOM", null));
        }
        //Sfun -> write ( E ) ; = { write }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "write".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "61 ");
            empareja(new Token("PR", "write"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
            empareja(new Token("PUNTCOM", null));
        }
        //P -> if ( E ) C2  = { if }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "if".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "62 ");

            empareja(new Token("PR", "if"));
            empareja(new Token("PARENTABIERTO", null));
            procedE();
            empareja(new Token("PARENTCERRADO", null));
            procedC2();
            flagReturn = true;
        }
        //Sfun -> return X ; = { return }
        else if ("PR".equals(this.getTokenDevuelto().getId()) &&
                "return".equals(this.getTokenDevuelto().getValor())) {
            this.setParse(this.getParse() + "63 ");
            empareja(new Token("PR", "return"));
            flagReturn = false;
            procedX();
            empareja(new Token("PUNTCOM", null));
        } else {
            throw new ParserException(ParserException.Reason.FIRST_NO_COINCIDE,
                    "Error en linea: " + Integer.toString(analizador.linea) +
                            " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < ID , id >, < PR , if >, < PR , prompt>, < PR , return >) pero se ha detectado: " +
                            this.getTokenDevuelto().toString());
        }
        return flagReturn;
    }

    private void procedS2fun()
            throws ParserException,
            IOException {

        //S2fun -> Sfun S2fun = { id prompt write if return }
        if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())
                        || "return".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "64 ");

            procedSfun();
            procedS2fun();
        }
        //S2fun -> lambda
        else {
            this.setParse(this.getParse() + "65 ");
        }
    }

    private boolean procedCfun()
            throws ParserException,
            IOException {

        //Cfun -> Bfun ; C1fun = { id prompt write if var return }
        if (("PR".equals(this.getTokenDevuelto().getId()) &&
                ("write".equals(this.getTokenDevuelto().getValor())
                        || "prompt".equals(this.getTokenDevuelto().getValor())
                        || "if".equals(this.getTokenDevuelto().getValor())
                        || "return".equals(this.getTokenDevuelto().getValor())
                        || "var".equals(this.getTokenDevuelto().getValor())))
                || "ID".equals(this.getTokenDevuelto().getId())) {
            this.setParse(this.getParse() + "66 ");

            flagReturn = procedBfun();
            procedCfun();
        }
        //Cfun -> lambda
        else {
            //throw new FirstNoCoincideException("Error en linea: " + Integer.toString(analizador.linea) + " Se esperaba detectar uno de los siguientes tokens (< PR , write >, < PR , for >, < ID , id >, < PR , if >, < PR , prompt>, < PR , return >,  <PR , var >) pero se ha detectado: " + this.getTokenDevuelto().toString());
            this.setParse(this.getParse() + "67 ");
        }
        return flagReturn;
    }

    private AnalizadorLexico getAnalizador() {
        return analizador;
    }

    private TablaSimbolos gettS() {
        return tS;
    }

    private Token getTokenDevuelto() {
        return tokenDevuelto;
    }

    private void setTokenDevuelto(Token tokenDevuelto) {
        this.tokenDevuelto = tokenDevuelto;
    }

    private String getParse() {
        return parse;
    }

    private void setParse(String parse) {
        this.parse = parse;
    }

    private BufferedWriter getTablasWriter() {
        return tablasWriter;
    }

    private BufferedWriter getParseWriter() {
        return parseWriter;
    }

    private BufferedWriter getErrorWriter() {
        return errorWriter;
    }

}
