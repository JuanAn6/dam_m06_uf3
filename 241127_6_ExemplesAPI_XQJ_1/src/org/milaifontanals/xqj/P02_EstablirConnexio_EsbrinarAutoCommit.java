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

/**
 *
 * @author Usuari
 */
public class P02_EstablirConnexio_EsbrinarAutoCommit {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Programa P02 cal invocar-lo passant nom fitxer de propietats");
            System.exit(1);
        }
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(args[0]));
        } catch (IOException ex) {
            System.out.println("Error en carregar fitxer de propietats: " + ex.getMessage());
            System.exit(1);
        }
        // props conté totes les propietats, nom de la classe inclòs
        String className = props.getProperty("className");
        if (className == null) {
            System.out.println("El fitxer de propietats " + args[0] + " no conté propietat className obligatòria");
            System.exit(1);
        }
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
            props.remove("className");
            // props s'ha quedat amb les propietats que necessita per establir connexió
            xqs.setProperties(props);
            xq = xqs.getConnection();
            System.out.println("Connexió establerta!");
        } catch (XQException ex) {
            System.out.println("Error en intentar connectar: " + ex.getMessage());
            Throwable t = ex.getCause();
            while (t != null) {
                System.out.println(t);
                t = t.getCause();
            }
            System.exit(1);
        }

        try {
            System.out.println("Autocommit: " + xq.getAutoCommit());      // Sempre a cert després d'establir connexió
        } catch (XQException ex) {
            System.out.println("Error en esbrinar l'estat d'autocommit: " + ex.getMessage());
        }
        try {
            xq.close();
            System.out.println("Connexió tancada!");
        } catch (XQException ex) {
            System.out.println("Error en tancar connexió: " + ex.getMessage());
        }
    }

}
