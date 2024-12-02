/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package milaifontanals;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import oracle.xml.xquery.xqjdb.OXQDDataSource;

/**
 *
 * @author Usuari
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Exemple de com connectar
        XQDataSource xqs = null;
        XQConnection con = null;   // Referència per apuntar a la connexió
        try {
            xqs = new OXQDDataSource();
            System.out.println("Factoria obtinguda");
            con = xqs.getConnection();
            System.out.println("Connexió establerta");
            /**
             * Treball
             */
        } catch (NullPointerException ex) {
            System.out.println("No tenim XQDataSource");
        } catch (XQException ex) {
            System.out.println("No s'ha pogut establir la connexió");
            System.out.println("+Info: " + ex.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (XQException ex) {
                    System.out.println("Error en tancar la connexió");
                    System.out.println("+Info: " + ex.getMessage());
                }
            }
        }
    }

}
