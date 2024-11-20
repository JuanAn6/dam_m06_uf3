
/*
 * Programa: SE_Client03.java
 * Objectiu: Programa que permet executar qualsevol instrucció introduïda
 *           per l'usuari, a la base de dades "mondial"
 * Autor...: Isidre Guixà
 */

package org.milaifontanals.sedna;
import ru.ispras.sedna.driver.*;
import java.io.*;

public final class SE_Client03 {
  // Amaguem el constructor per defecte. */
  private SE_Client03() { }

  private static String introduirInstruccio() {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String instr="";
    System.out.println("Introdueixi la instrucció a executar...");
    System.out.println("Per finalitzar, introdueixi una línia buida (en blanc):");
    try {
      String text;
      do {
        text = br.readLine();
        if (!(text.isEmpty())) instr=instr.concat(text+"\n");
      } while (!(text.isEmpty()));
    }
    catch (IOException e) {
       System.out.println("S'ha produït una excepció en la lectura de la instrucció:");
       System.err.println(e);
    }
    return instr.trim();
  }
      
  public static void main(String[] args) {
    SednaConnection session=null;
    try {
      // Demanem la instrucció a executar
      String cad = introduirInstruccio();
      if ("".equals(cad))
      {
        System.out.println("No ha introduït cap consulta.");
        System.exit(1);
      }
      // Obrir sessió:
      session = DatabaseManager.getConnection
                ("10.2.139.138:5050","BD","SYSTEM","MANAGER");
      // Obrir transacció
      session.begin();
      // Creem objecte SednaStatement per poder executar consultes
      SednaStatement st = session.createStatement();
      // Executem la consulta
      System.out.println("Executant instrucció:\n"+cad);  
      boolean teResultats=st.execute(cad);
      // Mostrem resultats
      if (!teResultats)
        System.out.println("Instrucció executada");
        // El commit podria estar aquí
      else {
        int nbRes=mostrarResultats(st);
        System.out.println("\nS'ha obtingut "+nbRes+" resultats.");
      }
      // Enregistrem els canvis produïts durant la transacció
      // Necessari per si s'ha executat instruccions "no consulta"
      session.commit();
      // Si no es fa commit, en tancar fa rollback!!!
    }
    catch (DriverException de) {
      de.printStackTrace();
    }
    finally { /* Tanquem sessió en qualsevol cas */
      try {
          if(session != null) session.close();
      }
      catch(DriverException e) {
          e.printStackTrace();
      }
    }
  }

  /* Mètode per mostrar resultats per la sortida estàndard
   * Retorna el nombre d'elements de la consulta
   */
  private static int mostrarResultats(SednaStatement st)  
    throws DriverException {  
    int comptador = 0;
    String item;
    System.out.println("Resultats:\n");
    SednaSerializedResult pr = st.getSerializedResult();
    while ((item = pr.next()) != null) {
      comptador++;
      System.out.print(item);
      // No utilitzem "println" per què cada "item", a partir del 2n.
      // comença amb "\n" i això ja provoca el salt de línia
    }
    System.out.println();
    return comptador;
  }
}
