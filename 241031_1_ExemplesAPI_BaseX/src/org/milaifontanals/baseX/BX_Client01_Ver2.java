/*
 * Programa: BX_Client01_Ver2.java
 * Objectiu: Programa que mostri els noms dels continents emmagatzemats en
 *           el document "mondial.xml" de la base de dades "BD", dins la
 *           col·lecció "geografia"
 *           Obre-tanca la BD. La funció doc NO incorpora el nom de la BD
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.baseX;

import org.basex.api.client.ClientSession;
import java.io.IOException;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;

public final class BX_Client01_Ver2 {

    // Amaguem el constructor per defecte. */
    private BX_Client01_Ver2() {
    }

    public static void main(String[] args) {
        ClientSession session = null;
        try {
            // Obrir sessió:
            session = new ClientSession("10.2.58.115", 1984, "admin", "admin");
            // En establir sessió no s'indica la BD
            // Versió 2: Obrim la BD
            session.execute(new Open("BD"));
            // Les següents instruccions no funcionen, tot i que sembla que en versions anteriors
            // alguna d'elles SÍ que funcionava:
//            String cad = "doc('geografia/mondial.xml')//continent/name/string()";
            // Alternativament, localitzarem el document dins la col·lecció i
            // farem la consulta
            String cad = "for $i in collection()\n"
                    + "where ends-with(document-uri($i),'geografia/mondial.xml')\n"
                    + "return doc(document-uri($i))//continent/name/string()";
            // Executem la consulta
            System.out.println("Executant consulta:\n\n" + cad);
            System.out.println("\nResultat:\n");
            System.out.println(session.query(cad).execute());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            /* Tanquem BD i sessió en qualsevol cas */
            try {
                if (session != null) {
                    session.execute(new Close());
                    session.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
