/*
 * Programa: BX_Client02_Ver2.java
 * Objectiu: Programa que efectua la creació d'una BD buida amb el nom
             indicat com argument en la crida d'execució del programa
 *           Usa clase CreateDB
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.baseX;
import org.basex.api.client.ClientSession;
import java.io.IOException;
import org.basex.core.cmd.CreateDB;

public final class BX_Client02_Ver2 {
  // Amaguem el constructor per defecte. */
  private BX_Client02_Ver2() { }

  /* El programa principal admet com a paràmetre el nom de la base de
   * dades a crear. Cal comprovar la no existència d'una base de dades
   * amb idèntic nom, doncs en la versió actual de BaseX, la
   * creació d'una base de dades no comprova l'existència d'una base de
   * dades amb igual nom i sobreescriu la base de dades que pogués existir.
   */
  public static void main(String[] args) {
    if (args.length!=1) {
      System.out.println("Cal indicar el nom de la BD a crear");
      System.exit(1);
    }
    ClientSession session=null;
    try {
      // Obrir sessió:
      session = new ClientSession("7.242.33.147", 1984, "admin", "admin");
      // Procedim a comprovar si la base de dades existeix, tot obrint-la:
      boolean existeix=true;
      try {
        session.execute("open "+args[0]);
      } 
      catch (IOException bxe) {
          existeix=false;
      }
      // Actuem segons la BD existeix o no existeix
      if (existeix) {
        System.out.println("Ja existeix una BD amb aquest nom.");
      }
      else {
        System.out.println("No existeix cap BD amb aquest nom. Procedim...");
        session.execute(new CreateDB(args[0]));
        // Forma alternativa de crear una base de dades buida:
        // session.execute ("create database "+args[0]);

        // Per obtenir informació de la creació:
        System.out.println(session.info());
      }
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
    }
    finally { /* Tanquem sessió en qualsevol cas */
      try {
          if(session != null) session.close();
      }
      catch(IOException ioe) {
          ioe.printStackTrace();
      }
    }
  }
}