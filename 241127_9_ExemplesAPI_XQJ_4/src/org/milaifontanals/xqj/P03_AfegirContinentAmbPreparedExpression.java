/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.xqj;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;

/**
 *
 * @author Usuari
 */
public class P03_AfegirContinentAmbPreparedExpression {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Programa P01 cal invocar-lo passant nom fitxer de propietats");
            System.exit(1);
        }
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(args[0]));
        } catch (IOException ex) {
            System.out.println("Error en carregar fitxer de propietats: " + ex.getMessage());
            System.exit(1);
        }
        // props conté totes les propietats de connexió i nom de la classe i path document inclòs
        String className = props.getProperty("className");
        if (className == null) {
            System.out.println("El fitxer de propietats " + args[0] + " no conté propietat className obligatòria");
            System.exit(1);
        }
        props.remove("className");

        String path = props.getProperty("path");
        if (path == null) {
            System.out.println("El fitxer de propietats " + args[0] + " no conté propietat path obligatòria");
            System.exit(1);
        }
        props.remove("path");

        String updateVersion = props.getProperty("updateVersion");
        if (updateVersion == null) {
            System.out.println("El fitxer de propietats " + args[0] + " no conté propietat updateVersion obligatòria");
            System.exit(1);
        }
        if (!updateVersion.equals("PL") && !updateVersion.equals("XQUF")) {
            System.out.println("El fitxer de propietats " + args[0] + " conté propietat updateVersion amb valor erroni");
            System.exit(1);
        }
        props.remove("updateVersion");

        String transactional = props.getProperty("transactional");
        if (transactional == null) {
            System.out.println("El fitxer de propietats " + args[0] + " no conté propietat transactional obligatòria");
            System.exit(1);
        }
        if (!transactional.equals("N") && !transactional.equals("Y")) {
            System.out.println("El fitxer de propietats " + args[0] + " conté propietat transactional amb valor erroni");
            System.exit(1);
        }
        props.remove("transactional");

        // props s'ha quedat amb les propietats que necessita per establir connexió
        XQDataSource xqs = null;
        try {
            xqs = (XQDataSource) Class.forName(className).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.out.println("Error en intentar obtenir XQDataSource: " + ex.getMessage());
            System.out.println("Classe Exception: " + ex.getClass().getName());
            System.exit(1);
        }
        XQConnection xq = null;
        try {
            xqs.setProperties(props);
            xq = xqs.getConnection();
            System.out.println("Connexió establerta!");
            if (transactional.equals("Y")) {
                // Desactivem autocommit, que per defecte, en XQJ està activat
                xq.setAutoCommit(false);
            }
        } catch (XQException ex) {
            System.out.println("Error en intentar connectar: " + ex.getMessage());
            System.exit(1);
        }

        XQPreparedExpression xqpe = null;
        XQExpression xqe = null;
        try {
            // El llenguatge d'actualització és diferent segons el SGBD (variable updateVersion)
            String cad;
            if (updateVersion.equals("PL")) {
                xqe = xq.createExpression();
                cad = "update insert <continent id='antartica'><name>Antartica</name><area>14000000</area></continent> ";
                cad = cad + "preceding " + path + "/mondial/organization[1]";
                xqe.executeCommand(cad);
            } else {    // XQUF
                cad = "insert node <continent id='antartica'><name>Antartica</name><area>14000000</area></continent> ";
                cad = cad + "before " + path + "/mondial/organization[1]";
                xqpe = xq.prepareExpression(cad);
                xqpe.executeQuery();
            }
            // La instrucció anterior funciona si hi ha alguna organització (que n'hi ha!!!) però segons DTD podria no haver-n'hi.
            // Si el llenguatge és transactional, caldrà fer commit!!!
            if (transactional.equals("Y")) {            
                xq.commit();
            }
            System.out.println("Inserció efectuada!");
        } catch (XQException ex) {
            System.out.println("Error en executar inserció: " + ex.getMessage());
            // No provoquem sortida de programa per poder fer el close
        } finally {
            if (xqe != null) {
                try {
                    xqe.close();
                } catch (XQException ex) {
                }
            }
        }

        try {
            xq.close();
            System.out.println("Connexió tancada!");
        } catch (XQException ex) {
            if (ex.getMessage().contains("ERROR SE4611")) {
                // Sedna, en tancar, genera una excepció "No hi ha cap transacció per fer rollback"
                // que no té cap sentit. Avisarem a Charles Foster... i mentrestant, l'evitarem!!!
                System.out.println("Connexió tancada!");
            } else {
                System.out.println("Error en tancar connexió: " + ex.getMessage());
            }
        }
    }

}