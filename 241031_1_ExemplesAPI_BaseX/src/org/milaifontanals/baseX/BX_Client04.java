/*
 * Programa: BX_Client04.java
 * Objectiu: Programa que permet executar qualsevol ordre introduïda
 *           per l'usuari i que sigui admesa pel BaseX.
 *           Podeu obtenir un llistat de les ordres que admet un servidor
 *           BaseX, tot posant en marxa la consola BaseX Client que el
 *           procés d'instal·lació deixa a l'arbre de programes, entrant-hi amb
 *           usuari i contrasenya admin i demanant ajuda via l'ordre help
 *           o també de BaseX Gui introduint "help" en el camp superior "Command"
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.baseX;

import java.io.*;
import org.basex.api.client.ClientSession;

public final class BX_Client04 {
    // Amaguem el constructor per defecte. */

    private BX_Client04() {
    }

    private static String introduirInstruccio() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String instr = "";
        System.out.println("Introdueixi la instrucció a executar...");
        System.out.println("Per finalitzar, introdueixi una línia buida (en blanc)");
        try {
            String text;
            do {
                text = br.readLine();
                if (!(text.isEmpty())) {
                    instr = instr.concat(text + "\n");
                }
            } while (!(text.isEmpty()));
        } catch (IOException e) {
            System.out.println("S'ha produït una excepció en la lectura de la instrucció:");
            System.err.println(e);
        }
        return instr.trim();
    }

    public static void main(String[] args) {
        ClientSession session = null;
        try {
            // Obrir sessió:
            session = new ClientSession("10.2.58.115", 1984, "admin", "admin");
            while (true) {
                // Demanem la instrucció a executar
                String cad = introduirInstruccio();
                if ("".equals(cad)) {
                    break;
                }
                try {
                    // Executem la instrucció introduïda per l'usuari
                    session.execute(cad);
                    // Informació final del servidor
                    System.out.println("\nInformació final del servidor:");
                    System.out.println(session.info());
                } catch (IOException bxe) {
                    System.out.println("La instrucció introduïda no és vàlida.");
                    System.out.println("Error reportat pel servidor:");
                    bxe.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            /* Tanquem sessió en qualsevol cas */
            try {
                if (session != null) {
                    session.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
