/*
 * Programa: BX_Client03.java
 * Objectiu: Programa que permet executar qualsevol consulta introduïda
 *           per l'usuari. També podeu provar amb instruccions XQUF ja que
 *           les sentències XQUF són considerades "consultes"
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.baseX;

import java.io.*;
import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;

public final class BX_Client03 {

    // Amaguem el constructor per defecte. */
    private BX_Client03() {
    }

    private static String introduirInstruccio() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String instr = "";
        System.out.println("Introdueixi la instrucció a executar...");
        System.out.println("Pensi a indicar el path de l'arxiu a gestionar");
        System.out.println("Per finalitzar, introdueixi una línia buida (en blanc):");
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
            // Demanem la instrucció a executar
            String cad = introduirInstruccio();
            if ("".equals(cad)) {
                System.out.println("No ha introduït cap consulta.");
                System.exit(1);
            }
            // Obrir sessió:
            session = new ClientSession("10.2.58.115", 1984, "admin", "admin");
            // Preparem la instrucció introduïda com a consulta:
            ClientQuery cq = session.query(cad);
            try {
                System.out.println("Consulta a executar:\n" + cad);
                String resultat = cq.execute();
                System.out.println("\nResultats:");
                System.out.println(resultat);
                // Informació de la consulta executada
                System.out.println("\nInformació de la consulta executada:");
                System.out.println(cq.info());
            } catch (IOException bxe) {
                System.out.println("La instrucció introduïda o no és executable com"
                        + " a consulta, o té algun error sintàctic.");
                System.out.println("Error reportat pel servidor:");
                System.out.println(bxe.getMessage());
//                bxe.printStackTrace();
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
