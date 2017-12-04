
package tabla_simbolos;

import analizadorLexico.AnalizadorLexico;
import errores.DeclaracionIncompatibleException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import token.Token;

public class TablaSimbolos {

   private int contadorRegistros = 0;
   private ArrayList<Object[]> tablaSimbolos;
   private int desplazamiento = 0;

   private void addPalabrasReservadas() throws DeclaracionIncompatibleException {
       this.addToken(new Token("FUNCTION", null));
       this.addDireccion(new Token("FUNCTION", null), "FUNCTION".length());
       this.addTipo(new Token("FUNCTION", null), "FUNCTION");

       this.addToken(new Token("IF", null));
       this.addDireccion(new Token("IF", null), "IF".length());
       this.addTipo(new Token("IF", null), "IF");

       this.addToken(new Token("ELSE", null));
       this.addDireccion(new Token("ELSE", null), "ELSE".length());
       this.addTipo(new Token("ELSE", null), "ELSE");

       this.addToken(new Token("WRITE", null));
       this.addDireccion(new Token("WRITE", null), "WRITE".length());
       this.addTipo(new Token("WRITE", null), "WRITE");

       this.addToken(new Token("PROMPT", null));
       this.addDireccion(new Token("PROMPT", null), "PROMPT".length());
       this.addTipo(new Token("PROMPT", null), "PROMPT");

       this.addToken(new Token("RETURN", null));
       this.addDireccion(new Token("RETURN", null), "RETURN".length());
       this.addTipo(new Token("RETURN", null), "RETURN");

       this.addToken(new Token("VAR", null));
       this.addDireccion(new Token("VAR", null), "VAR".length());
       this.addTipo(new Token("VAR", null), "VAR");

        this.addToken(new Token("INT", null));
        this.addDireccion(new Token("INT", null), "INT".length());
        this.addTipo(new Token("INT", null), "INT");

        this.addToken(new Token("CHARS", null));
        this.addDireccion(new Token("CHARS", null), "CHARS".length());
        this.addTipo(new Token("CHARS", null), "CHARS");

        this.addToken(new Token("ID", null));
        this.addDireccion(new Token("ID", null), "ID".length());
        this.addTipo(new Token("ID", null), "ID"); 
   }

   public TablaSimbolos() throws DeclaracionIncompatibleException {
       this.tablaSimbolos = new ArrayList<Object[]>();
       this.addPalabrasReservadas();
   }

   private void escribirCabecera(BufferedWriter escritorTabla) throws IOException {
       escritorTabla.write("LEXEMA");
       escritorTabla.write("\t\t\t\t\t");
       escritorTabla.write("TIPO");
       escritorTabla.write("\t\t\t\t\t");
       escritorTabla.write("DIRECCION");
       escritorTabla.write("\t\t\t\t\t");
       escritorTabla.write("NUMERODEPARAMETROs");
       escritorTabla.write("\t\t\t\t\t");
       escritorTabla.write("TIPODEVUELTO");
       escritorTabla.write("\t\t\t\t\t");
       escritorTabla.write("ETIQUETA");
       escritorTabla.newLine();
   }

   public void volcarTabla(BufferedWriter escritorTabla) throws IOException {
       TablaSimbolos tabla;
       //Local
       if (this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {
           tabla = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           escritorTabla.newLine();
           escritorTabla.newLine();
           escribirCabecera(escritorTabla);

       } 
       //Global
       else {
           tabla = this;
           escritorTabla.write("VOLCADO DE TABLA GLOBAL DEL PROGRAMA");
           escritorTabla.newLine();
           escritorTabla.newLine();
           escribirCabecera(escritorTabla);

       }
       for (int i = 0; i < tabla.tablaSimbolos.size(); i++) {
           Object[] fila = tabla.tablaSimbolos.get(i);
           for (int j = 0; j < fila.length; j++) {
               String imprimir = "";
               if (fila[j] != null) {
                   imprimir = fila[j].toString();
                   escritorTabla.write(imprimir);
               }
               if (imprimir.length() >= 16) {
                   escritorTabla.write("\t\t");
               } else if (imprimir.length() >= 12) {
                   escritorTabla.write("\t\t\t");
               } else if (imprimir.length() >= 8) {
                   escritorTabla.write("\t\t\t\t");
               } else if (imprimir.length() >= 4) {
                   escritorTabla.write("\t\t\t\t\t");
               } else {
                   escritorTabla.write("\t\t\t\t\t\t");
               }

           }
           escritorTabla.newLine();
       }
       escritorTabla.newLine();
   }

   public void addToken(Token token) {
       /*
        Posicion 0 = lexema
        Posicion 1 = tipo
        Posicion 2 = direccion
        Posicion 3 = numero parametros (solo para funciones)
        Posicion 4 = tipo devuelto (solo para funciones)
        Posicion 5 = etiqueta (solo para funciones)
        */
       boolean localAdd = false;
       //Local
       if (contadorRegistros > 0 && this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {
           TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           tablaLocal.addToken(token);
           localAdd = true;
       }
       if (!localAdd) {
           this.tablaSimbolos.add(new Object[6]);
           this.tablaSimbolos.get(contadorRegistros)[0] = (String) token.getValor();
           contadorRegistros++;
       }
   }

   public void addTipo(Token token, String tipo) throws DeclaracionIncompatibleException {
       if (this.buscaTS(token.getValor())[0] != null) {//Solo entra si esta en la tabla
           if (this.getTipo(token) != null && !this.getTipo(token).equals(tipo) && ("FUNC".equals(tipo) || "FUNC".equals(this.getTipo(token)))) {
               throw new DeclaracionIncompatibleException("Error en linea " + AnalizadorLexico.linea + ". La variable o funcion '" + token.getValor() + "' ha sido declarada previamente.");
           }
           Integer[] contTemporal = buscaTS(token.getValor());
           if (contTemporal[1] == 1) {
               TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
               tablaLocal.getTablaSimbolos().get(contTemporal[0])[1] = tipo;
           } else {
               this.tablaSimbolos.get(contTemporal[0])[1] = tipo;
           }
       }
   }

   public void addParametros(int nParam) {
       //Insertamos en la ultima posicion rellenada que va a ser la funcion. 
       this.tablaSimbolos.get(contadorRegistros - 2)[3] = nParam;
   }

   public void addEtiqueta() {
       //El nombre de la etiqueta va a ser "FUNC.ID" donde ID es el nombre de la funcion
       String etiqueta = "FUNC.";
       etiqueta += "" + this.tablaSimbolos.get(contadorRegistros - 2)[0];//.ID
       this.tablaSimbolos.get(contadorRegistros - 2)[5] = etiqueta;
   }

   public void addDevuelve(String tipo) {
       this.tablaSimbolos.get(contadorRegistros - 1)[4] = tipo;
   }

   public void addDireccion(Token token, int tamaño) {
       if (this.buscaTS(token.getValor())[0] != null) {
           Integer[] contTemporal = buscaTS(token.getValor());
           if (contTemporal[1] == 1) {
               TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
               tablaLocal.tablaSimbolos.get(contTemporal[0])[2] = tablaLocal.desplazamiento;
               tablaLocal.desplazamiento += tamaño;
           } else {
               this.tablaSimbolos.get(contTemporal[0])[2] = desplazamiento;
               desplazamiento += tamaño;
           }
       }
   }

   //Si se encuentra la palbra devuelve la posicion en la que se encuentra la palabra, sino devuelve null.
   //En la primera posicion devuelve la posicion en la que se encuentra la palabra. En la segunda posicion devuelve 0 si esta en global y 1 si esta en local.
   public Integer[] buscaTS(String palabra) {
       Integer[] estaEnLocal = new Integer[2];
       if (contadorRegistros > 0 && this.tablaSimbolos.get(contadorRegistros - 1)[0] instanceof TablaSimbolos) {//Estamos en la tabla local
           TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           estaEnLocal = tablaLocal.buscaTS(palabra);
           estaEnLocal[1] = 1;
       }
       if (estaEnLocal[0] == null) {
           Integer[] contador = new Integer[2];
           contador[0] = 0;//Pos
           contador[1] = 0;//Tabla global
           boolean encontrado = false;
           while (contador[0] < this.tablaSimbolos.size() && !encontrado) {
               if (this.tablaSimbolos.get(contador[0])[0].equals(palabra)) {//comparamos con el lexema
                   encontrado = true;
               } else {
                   contador[0]++;
               }
           }
           if (!encontrado) {
               contador[0] = null;
           }
           return contador;
       }
       return estaEnLocal;
   }

   public Integer[] buscaTSGlobal(String palabra) {
       Integer[] estaEnLocal = new Integer[2];
       Integer[] contador = new Integer[2];
       contador[0] = 0;//Pos
       contador[1] = 0;//Tabla global
       boolean encontrado = false;
       while (contador[0] < this.tablaSimbolos.size() && !encontrado) {
           if (this.tablaSimbolos.get(contador[0])[0].equals(palabra)) {//comparamos con el lexema
               encontrado = true;
           } else {
               contador[0]++;
           }
       }
       if (!encontrado) {
           contador[0] = null;
       }
       return contador;
   }

   public void crearTSL() throws DeclaracionIncompatibleException {
       this.tablaSimbolos.add(new Object[6]);
       this.tablaSimbolos.get(contadorRegistros)[0] = new TablaSimbolos();
       contadorRegistros++;
   }

   public void borraTS() {
       this.tablaSimbolos.remove(contadorRegistros - 1);
       contadorRegistros--;
   }

   public TablaSimbolos accederTSL() {
       if ("TablaSimbolos".equals(this.tablaSimbolos.get(contadorRegistros - 1)[0].getClass().getCanonicalName())) {
           return (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
       } else {
           //LANZAR EXCEPCION
           return null;
       }
   }

   public int getNParametros(Token token) {
       int toReturn = -1;
       Integer[] posYTab = this.buscaTS(token.getValor());
       if (posYTab[1] == 1) {
           System.out.println("ERROR en getNParametros. Estas tonto y accedes a la local machoooo.");
       } else {//Estamos en la global
           Integer nParam = (Integer) this.tablaSimbolos.get(posYTab[0].intValue())[3];
           toReturn = nParam;
       }
       return toReturn;
   }

   public int getNParametrosGlobal(Token token) {
       Integer[] posYTab = this.buscaTSGlobal(token.getValor());
       Integer nParam=null;
       if (posYTab[0] != null) {
           nParam = (Integer) this.tablaSimbolos.get(posYTab[0].intValue())[3];
       }
       if(nParam!=null){
           return nParam;
       }else{
           return -1;
       }
   }

   public String getTipo(Token token) {
       //Podriamos lanzar una excepcion si el token no esta en la tabla de simbolos.
       Integer[] posYTab = this.buscaTS(token.getValor());
       String tipo = null;
       if (posYTab[1] == 0 && posYTab[0] != null) {//Estamos en la global
           tipo = (String) this.tablaSimbolos.get(posYTab[0])[1];
       } else if (posYTab[1] == 1 && posYTab[0] != null) {//Estamos en la local
           TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           tipo = (String) tablaLocal.tablaSimbolos.get(posYTab[0])[1];
       }
       return tipo;
   }

   public String getDevuelve(Token token) {
       //Podriamos lanzar una excepcion si el token no esta en la tabla de simbolos.
       Integer[] posYTab = this.buscaTS(token.getValor());
       String devuelve = null;
       if (posYTab[1] == 0 && posYTab[0] != null) {//Estamos en la global
           devuelve = (String) this.tablaSimbolos.get(posYTab[0])[4];
       } else if (posYTab[1] == 1 && posYTab[0] != null) {//Estamos en la local
           TablaSimbolos tablaLocal = (TablaSimbolos) this.tablaSimbolos.get(contadorRegistros - 1)[0];
           devuelve = (String) tablaLocal.tablaSimbolos.get(posYTab[0])[4];
       }
       return devuelve;
   }

   public void vaciarTabla() {
       while (!this.tablaSimbolos.isEmpty()) {
           this.tablaSimbolos.remove(0);
       }
       this.contadorRegistros = 0;
       this.desplazamiento = 0;
   }

   public boolean isPR(String cadena) {
       return "var".equals(cadena) || "write".equals(cadena) || "prompt".equals(cadena) || "if".equals(cadena) || "FUNCTION".equals(cadena) || "do".equals(cadena) || "return".equals(cadena) || "while".equals(cadena);
   }

   public int getContadorRegistros() {
       return contadorRegistros;
   }

   public void setContadorRegistros(int contadorRegistros) {
       this.contadorRegistros = contadorRegistros;
   }

   public ArrayList<Object[]> getTablaSimbolos() {
       return tablaSimbolos;
   }

   public void setTablaSimbolos(ArrayList<Object[]> tablaSimbolos) {
       this.tablaSimbolos = tablaSimbolos;
   }

}
