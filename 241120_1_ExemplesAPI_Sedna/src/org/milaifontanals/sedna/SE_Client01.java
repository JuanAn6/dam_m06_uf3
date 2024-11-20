/*
 * Programa: SE_Client01.java
 * Objectiu: Programa que mostri els noms dels continents emmagatzemats a
 *           la base de dades BD - fitxer mondial.xml dins col·lecció geografia
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.sedna;

import ru.ispras.sedna.driver.*;

public final class SE_Client01 {
    // Amaguem el constructor per defecte. */

    private SE_Client01() {
    }

    public static void main(String[] args) {
        SednaConnection session = null;
        try {
            // Obrir sessió:
            session = DatabaseManager.getConnection("10.2.139.138:5050", "BD", "SYSTEM", "MANAGER");
            System.out.println("Classe:" + session.getClass().getName());
            // Obrir transacció
            session.begin();
            // Creem objecte SednaStatement per poder executar consultes
            SednaStatement st = session.createStatement();
            // Preparem la instrucció a executar
            //    doc('mondial.xml')//continent/name
            String cad = "doc('mondial.xml','geografia')//continent/name/string()";
            // Executem la consulta
            System.out.println("Executant consulta: " + cad);
            st.execute(cad);
            // Mostrem resultats.. No ens cal saber si és una consulta o
            // un altre tipus d'instrucció. Per això no recollim el valor
            // booleà que retorna el mètode execute.
            int nbRes = mostrarResultats(st);
            System.out.println("\nS'ha obtingut " + nbRes + " resultats.");
            // No ens cal fer "commit" doncs es tracta d'una consulta
        } catch (DriverException de) {
            de.printStackTrace();
        } finally {
            /* Tanquem sessió en qualsevol cas */
            try {
                if (session != null) {
                    session.close();
                }
            } catch (DriverException e) {
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
        System.out.println("\nResultats:\n");
        SednaSerializedResult pr = st.getSerializedResult();
        while ((item = pr.next()) != null) {
            comptador++;
            System.out.print(item);
            // No utilitzem "println" per què cada "item", a partir del 2n.
            // comença amb "\n" i això ja provoca el salt de línia
        }
        return comptador;
    }
}
