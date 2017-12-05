package tabla_simbolos;

import lexico.*;
import error.*;
import token.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TablaSimbolos {
 private int contador_registros = 0;
 private ArrayList<Object[]> tabla_simbolos;
 private int desplazamiento = 0;

 private void add_reservadas() throws DeclaracionIncompatibleException {
  this.addToken(new Token("FUNCTION", null));
  this.
 }

 private tabla_Simbolos() throws DeclaracionIncompatibleException{
  this.tabla_simbolos = new ArrayList<Object[]>();
  this.add_reservadas();
 }

 private void escribir_cabecera(BufferedWriter escritor_tabla) throws IOException {
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

 public void volcar_tabla(BufferedWriter escritor_tabla) throws IOException {
  TablaSimbolos tabla;

  if (this.tabla_simbolos.get(contador_registros - 1)[0] instanceof TablaSimbolos) {
   tabla = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   escritor_tabla.newLine();
   escritor_tabla.newLine();
   escribir_cabecera(escritor_tabla);
  }
  else {
   tabla = this;
   escritor_tabla.write("VOLCADO DE LA TABLA GLOBAL DEL PROGRAMA");
   escritor_tabla.newLine();
   escritor_tabla.newLine();
   escribir_cabecera(escritor_tabla);
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

 public void add_token(Token token) {
  boolean local_add = false;

  if(contador_registros > 0 && this.tabla_simbolos.get(contador_registros -1)[0] instanceof TablaSimbolos) {
   TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros - 1)[0];
   tabla_local.add_token(token);
   local_add = true;
  }
  if (!local_add) {
   this.tabla_simbolos.add(new Object[6]);
   this.tabla_simbolos.get(contador_registros)[0] = (String) token.getValor();
   contador_registros++;
  }
 }

 public void add_tipo(Token token, String tipo) throws DeclaracionIncompatibleException {
  if (this.busca_tabla(token.getValor())[0] != null) {
   if (this.getTipo(token) != null && !this.getTipo(token).equals(tipo) && ("FUNC".equals(tipo) || "FUNC".equals(this.getTipo(token)))) {
    throw new DeclaracionIncompatibleException("Error: linea " + lexico.linea + ". \nVariable/funcion: " + token.getValor() + ", ya ha sido declarado con anterioridad.");
   }

   Integer[] aux = busca_tabla(token.getValor());
   if (aux[1] == 1) {
    TablaSimbolos tabla_local = (TablaSimbolos) this.tabla_simbolos.get(contador_registros -1)[0];
    tabla_local.getTS().get(aux[0])[1] = tipo;
   } else {
    this.tabla_simbolos.get(aux[0])[1] = tipo;
   }
  }
 }

 public void add_parametros(int num_params) {
  this.tabla_simbolos.get(contador_registros - 2)[3] = num_params;
 }

 public void add_etiqueta() {
 }

 public ArrayList<Object[]> getTS() {
  return tabla_simbolos;
 }
}