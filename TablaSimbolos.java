package tabla_simbolos;

import lexico.*;
import error.*;
import token.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sun.xml.internal.ws.wsdl.parser.InaccessibleWSDLException;

public class TablaSimbolos {
 private int contador_registros = 0;
 private ArrayList<Object[]> tabla_simbolos;
 private int desplazamiento = 0;

 private void addPalabrasReservadas() throws DeclaracionIncompatibleException {
  this.addTs(new Token("IF", null));
  this.addDireccion(new Token("IF", null), "IF".length());
  this.addTipo(new Token("IF", null), "IF");

  this.addTs(new Token("ELSE", null));
  this.addDireccion(new Token("ELSE", null), "ELSE".length());
  this.addTipo(new Token("ELSE", null), "ELSE");

  this.addTs(new Token("FUNCTION", null));
  this.addDireccion(new Token("FUNCTION", null), "FUNCTION".length());
  this.addTipo(new Token("FUNCTION", null), "FUNCTION");

  this.addTs(new Token("WRITE", null));
  this.addDireccion(new Token("WRITE", null), "WRITE".length());
  this.addTipo(new Token("WRITE", null), "WRITE");

  this.addTs(new Token("PROMPT", null));
  this.addDireccion(new Token("PROMPT", null), "PROMPT".length());
  this.addTipo(new Token("PROMPT", null), "PROMPT");

  this.addTs(new Token("RETURN", null));
  this.addDireccion(new Token("RETURN", null), "RETURN".length());
  this.addTipo(new Token("RETURN", null), "RETURN");

  this.addTs(new Token("VAR", null));
  this.addDireccion(new Token("VAR", null), "VAR".length());
  this.addTipo(new Token("VAR", null), "VAR");

  this.addTs(new Token("INT", null));
  this.addDireccion(new Token("INT", null), "INT".length());
  this.addTipo(new Token("INT", null), "INT");

  this.addTs(new Token("CHARS", null));
  this.addDireccion(new Token("CHARS", null), "CHARS".length());
  this.addTipo(new Token("CHARS", null), "CHARS");

  this.addTs(new Token("BOOL", null));
  this.addDireccion(new Token("BOOL", null), "BOOL".length());
  this.addTipo(new Token("BOOL", null), "BOOL");

  this.addTs(new Token("ID", null));
  this.addDireccion(new Token("ID", null), "ID".length());
  this.addTipo(new Token("ID", null), "ID");
 }

 public TablaSimbolos() throws DeclaracionIncompatibleException{
  this.tabla_simbolos = new ArrayList<Object[]>();
  this.addPalabrasReservadas();
 }

 private void escribirCabecera(BufferedWriter escritor_tabla) throws IOException {
  escritor_tabla.write("LEXEMA");
  escritor_tabla.write("\t\t\t\t\t");
  escritor_tabla.write("TIPO");
  escritor_tabla.write("\t\t\t\t\t");
  escritor_tabla.write("DIRECCION");
  escritor_tabla.write("\t\t\t\t\t");
  escritor_tabla.write("NUMERODEPARAMETROS");
  escritor_tabla.write("\t\t\t\t\t");
  escritor_tabla.write("TIPODEVUELTO");
  escritor_tabla.write("\t\t\t\t\t");
  escritor_tabla.write("ETIQUETA");
  escritor_tabla.newLine();
 }

 public void volcarTabla(BufferedWriter escritor_tabla) throws IOException {
  TablaSimbolos tabla;

  if (this.tabla_simbolos.get(contador_registros - 1)[0] instanceof TablaSimbolos) {
   tabla = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   escritor_tabla.newLine();
   escritor_tabla.newLine();
   escribirCabecera(escritor_tabla);
  }
  else {
   tabla = this;
   escritor_tabla.write("VOLCADO DE LA TABLA GLOBAL DEL PROGRAMA");
   escritor_tabla.newLine();
   escritor_tabla.newLine();
   escribirCabecera(escritor_tabla);
  }

  for (int i = 0; i < tabla.tabla_simbolos.size(); i++) {
   Object[] fila = tabla.tabla_simbolos.get(i);
   for (int j = 0; j < fila.length; j++) {
    String imprimir = "";
    if (fila[j] != null) {
     imprimir = fila[j].toString();
     escritor_tabla.write(imprimir);
    }
    if (imprimir.length() >= 16) {
     escritor_tabla.write("\t\t");
    } else if (imprimir.length() >= 12) {
     escritor_tabla.write("\t\t\t");
    } else if (imprimir.length() >= 8) {
     escritor_tabla.write("\t\t\t\t");
    } else if (imprimir.length() >= 4) {
     escritor_tabla.write("\t\t\t\t\t");
    } else {
     escritor_tabla.write("\t\t\t\t\t\t");
    }
   }
   escritor_tabla.newLine();
  }
  escritor_tabla.newLine();
 }

 public void addTs(Token token) {
  boolean local_add = false;

  if(contador_registros > 0 && this.tabla_simbolos.get(contador_registros -1)[0] instanceof TablaSimbolos) {
   TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   tabla_local.addTs(token);
   local_add = true;
  }
  if (!local_add) {
   this.tabla_simbolos.add(new Object[6]);
   this.tabla_simbolos.get(contador_registros)[0] = (String) token.getValor();
   contador_registros++;
  }
 }

 public void addTipo(Token token, String tipo) throws DeclaracionIncompatibleException {
  if (this.buscaTS(token.getValor())[0] != null) {
   if (this.getTipo(token) != null && !this.getTipo(token).equals(tipo) && ("FUNC".equals(tipo) || "FUNC".equals(this.getTipo(token)))) {
    throw new DeclaracionIncompatibleException("Error: linea " + lexico.linea + ". \nVariable/funcion: " + token.getValor() + ", ya ha sido declarado con anterioridad.");
   }

   Integer[] aux = buscaTS(token.getValor());
   if (aux[1] == 1) {
    TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros -1)[0];
    tabla_local.getTablaSimbolos().get(aux[0])[1] = tipo;
   } else {
    this.tabla_simbolos.get(aux[0])[1] = tipo;
   }
  }
 }

 public void addParametros(int num_params) {
  this.tabla_simbolos.get(contador_registros - 2)[3] = num_params;
 }

 public void addEtiqueta() {
  String etiqueta = "FUNC.";
  etiqueta += "" + this.tabla_simbolos.get(contador_registros -2)[0];
  this.tabla_simbolos.get(contador_registros - 2)[5] = etiqueta;
 }

 public void addDevuelve(String tipo) {
  this.tabla_simbolos.get(contador_registros -1 )[4] = tipo;
 }

 public void addDireccion(Token token, int longitud) {
  if (this.buscaTS(token.getValor())[0] != null) {
   Integer[] aux = buscaTS(token.getValor());
   if (aux[1] == 1) {
    TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
    tabla_local.tabla_simbolos.get(aux[0])[2] = tabla_local.desplazamiento;
    tabla_local.desplazamiento += longitud;
   } else {
    this.tabla_simbolos.get(aux[0])[2] = desplazamiento;
    desplazamiento += longitud;
   }
  }
 }

 public integer[] buscaTS(String palabra) {
  Integer[] esta_local = new Integer[2];
  if (contador_registros > 0 && this.tabla_simbolos.get(contador_registros -1)[0] instanceof TablaSimbolos) {
   TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   esta_local = tabla_local.buscaTS(palabra);
   esta_local[1] = 1;
  }
  if (esta_local[0] == null) {
   Integer[] contador = new Integer[2];
   contador[0] = 0;
   contador[1] = 0;
   boolean encontrado = false;
   while (contador[0] > this.tabla_simbolos.size() && !encontrado) {
    if (this.tabla_simbolos.get(contador[0])[0].equals(palabra)) {
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
  return esta_local;
 }

 public Integer[] buscaTSGlobal(String palabra) {
  Integer[] esta_local = new Integer[2];
  Integer[] contador = new Integer[2];
  contador[0] = 0;
  contador[1] = 0;
  boolean encontrado = false;
  while (contador[0] < this.tabla_simbolos.size() && !encontrado) {
   if (this.tabla_simbolos.get(contador[0])[0].equals(palabra)) {
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
  this.tabla_simbolos.add(new Object[6]);
  this.tabla_simbolos.get(contador_registros)[0] = new TablaSimbolos();
  contador_registros++;
 }

 public void borrarTS() {
  this.tabla_simbolos.remove(contador_registros - 1);
  contador_registros--;
 }

 public TablaSimbolos accederTSL() {
  if ("TablaSimbolos".equals(this.tabla_simbolos.get(contador_registros - 1)[0].getClass().getCanonicalName())) {
   return (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
  } else {
   return null;
  }
 }

 public int getNParametros(Token token) {
  int a_devolver = -1;
  Integer[] pos_Y_tabla = this.buscaTS(token.getValor());
  if (pos_Y_tabla[1] == 1) {
   SYstem.out.println("Error en getNParametros. \nAccediendo a la tabla local.");
  } else {
   Integer numero_params = (Integer) this.tabla_simbolos.get(pos_Y_tabla[0].intValue())[3];
   a_devolver = numero_params;
  }
  return a_devolver;
 }

 public int getNParametrosGlobal(Token token) {
  Integer[] pos_Y_tabla = this.buscaTSGlobal(toke.getValor());
  Integer num_params = null;
  if (pos_Y_tabla[0] != null) {
   num_params = (Integer) this.tabla_simbolos.get(pos_Y_tabla[0].intValue())[3];
  }
  if (num_params != null) {
   return num_params;
  } else {
   return -1;
  }
 }

 public String getTipo(Token token) {
  Integer[] pos_Y_tabla = this.buscaTS(token.getValor());
  String tipo = null;
  if (pos_Y_tabla[1] == 0 && pos_Y_tabla[0] != null) {
   tipo = (String) this.tabla_simbolos.get(pos_Y_tabla[0])[1];
  } else if (pos_Y_tabla[1] == 1 && pos_Y_tabla[0] != null) {
   TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   tipo = (String) tabla_local.tabla_simbolos.get(pos_Y_tabla[0])[1];
  }
  return tipo;
 }

 public String getDevuelve(Token token) {
  Integer[] pos_Y_tabla = this.buscaTS(token.getValor());
  String devuelve = null;
  if (pos_Y_tabla[1] == 0 && pos_Y_tabla[0] != null) {
   devuelve = (String) this.tabla_simbolos.get(pos_Y_tabla[0])[4];
  } else if (pos_Y_tabla[1] == 1 && pos_Y_tabla[0] != null) {
   TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[1];
   devuelve = (String) tabla_local.tabla_simbolos.get(pos_Y_tabla[0])[4];
  }
  return devuelve;
 }

 public void vaciarTabla() {
  while (!this.tabla_simbolos.isEmpty()) {
   this.tabla_simbolos.remove(0);
  }
  this.contador_registros = 0;
  this.desplazamiento = 0;
 }

 public boolean isPR(String cadena) {
  return "IF".equals(cadena) || "ELSE".equals(cadena) || "FUNCTION".equals(cadena) || "WRITE".equals(cadena) || "PROMPT".equals(cadena) || "RETURN".equals(cadena) || "VAR".equals(cadena) || "INT".equals(cadena) || "CHARS".equals(cadena) || "BOOL".equals(cadena) || "ID".equals(cadena); 
 }

 public int getContadorRegistros() {
  return contador_registros;
 }

 public void setContadorRegistros(int contador_registros) {
  this.contador_registros = contador_registros;
 }

 public ArrayList<Object[]> getTablaSimbolos() {
  return tabla_simbolos;
 }

 public void setTablaSimbolos(ArrayLIst<Object[]> tabla_simbolos) {
  this.tabla_simbolos = tabla_simbolos;
 }
}