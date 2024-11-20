/*
 * Programa: SE_Client02.java
 * Objectiu: Programa que mostri els noms dels països emmagatzemats a
 *           la base de dades "mondial", junt amb la seva població
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.sedna;

import ru.ispras.sedna.driver.*;

public final class SE_Client02 {
    // Amaguem el constructor per defecte. */

    private SE_Client02() {
    }

    /* El programa principal admet com a paràmetre la cadena "traceOn" o
   * "traceOff" per indicar si es vol activar o no la funcionalitat de 
   * la funció trace.
   * En cas de no indicar res, la funcionalitat quedarà desactivada.
     */
    public static void main(String[] args) {
        if (args.length > 0 && !("traceOn".equals(args[0]))
                && !("traceOff".equals(args[0]))) {
            System.out.println("En cas d'indicar paràmetre cal que sigui "
                    + "\"traceOn\" o \"traceOff\"");
            System.exit(1);
        }
        SednaConnection session = null;
        try {
            // Obrir sessió:
            session = DatabaseManager.getConnection("10.2.139.138:5050", "BD", "SYSTEM", "MANAGER");
            if (args.length == 0 || args[0].equals("traceOff")) // Desactivem l'execució de la funció trace
            {
                session.setTraceOutput(false);
            }
//       Obrir transacció
            session.begin();
            // Creem objecte SednaStatement per poder executar consultes
            SednaStatement st = session.createStatement();
            // Preparem la instrucció a executar
//                for $i in trace(doc("mondial.xml","geografia")//country,'###')
//                return concat($i/name[1],"-",$i/population[1])
            String cad = "for $i in trace(doc('mondial.xml','geografia')//country,'###')";
            cad = cad + "\nreturn concat($i/name[1],\"-\",$i/population[1])";
            // Executem la consulta
            System.out.println("Executant consulta:\n" + cad);
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
        System.out.println();
        return comptador;
    }
}
