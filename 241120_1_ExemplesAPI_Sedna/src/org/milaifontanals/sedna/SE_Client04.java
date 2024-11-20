/*
 * Programa: SE_Client04.java
 * Objectiu: Programa que permet comprovar que Sedna tanca la transacció
 *           en generar-se una DriverException
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.sedna;

import ru.ispras.sedna.driver.*;

public final class SE_Client04 {
    // Amaguem el constructor per defecte. */

    private SE_Client04() {
    }

    public static void main(String[] args) {
        SednaConnection session = null;
        try {
            // Obrir sessió:
            session = DatabaseManager.getConnection("10.2.139.138:5050", "BD", "SYSTEM", "MANAGER");
            // Obrir transacció
            session.begin();
            // Creem objecte SednaStatement per poder executar consultes
            SednaStatement st = session.createStatement();
            // Executem una consulta sense errors:
            System.out.println("Executem instrucció 1 correcta ");
            st.execute("doc('mondial.xml','geografia')//country[@car_code='F']/name/text()");
            System.out.println("Resultat: " + st.getSerializedResult().next());
            System.out.println("Executem instrucció 2 correcta ");
            st.execute("doc('mondial.xml','geografia')//country[@car_code='D']/name/text()");
            System.out.println("Resultat: " + st.getSerializedResult().next());
            try {
                System.out.println("Executem instrucció 3 errònia ");
                st.execute("doc('mondial.xml','geografia')//country[@car_code==='D']/name/text()");
                System.out.println("Resultat: " + st.getSerializedResult().next());
            } catch (DriverException ex) {
                System.out.println("Ha petat!");               
            }
            System.out.println("Sense obrir transacció, intentem una nova instrucció 4");
            st.execute("doc('mondial.xml','geografia')//country[@car_code='P']/name/text()");
            System.out.println("Resultat: "+st.getSerializedResult().next());
            session.commit();
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
