/*
 * Programa: BX_Client01_Ver1.java
 * Objectiu: Programa que mostri els noms dels continents emmagatzemats en
 *           el document "mondial.xml" de la base de dades "BD", dins la
 *           col·lecció "geografia".
 *           No obre-tanca la BD. La funció doc incorpora el nom de la BD
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.baseX;

import org.basex.api.client.ClientSession;
import java.io.IOException;
import org.basex.api.client.ClientQuery;

public final class BX_Client01_Ver1 {

    // Amaguem el constructor per defecte. */
    private BX_Client01_Ver1() {
    }

    public static void main(String[] args) {
        ClientSession session = null;
        try {
            // Obrir sessió:
            session = new ClientSession("10.2.139.138", 1984, "admin", "admin");
            System.out.println("Connectat");
            // En establir sessió no s'indica la BD
            // Versió 1: En efectuar la consulta indicarem la BD en seleccionar
            //           el document sobre el què treballar
            String cad = "doc('BD/geografia/mondial.xml')//continent/name/string()";
            // Executem la consulta
            System.out.println("Executant consulta: " + cad);
            ClientQuery cq = session.query(cad);
            System.out.println(cq.execute());
        } catch (IOException ioe) {
            System.out.println("Alguna cosa ha fallat:" + ioe.getMessage());
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
