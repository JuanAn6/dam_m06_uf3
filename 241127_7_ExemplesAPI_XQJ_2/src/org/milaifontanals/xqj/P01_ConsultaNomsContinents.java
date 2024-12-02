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
import javax.xml.xquery.XQResultSequence;

/**
 *
 * @author Usuari
 */
public class P01_ConsultaNomsContinents {

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

        // props s'ha quedat amb les propietats que necessita per establir connexió
        XQDataSource xqs = null;
        try {
            xqs = (XQDataSource) Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            System.out.println("Error en intentar obtenir XQDataSource: " + ex.getMessage());
            System.out.println("Classe Exception: " + ex.getClass().getName());
            System.exit(1);
        } catch (InstantiationException ex) {
            System.out.println("Error en intentar obtenir XQDataSource: " + ex.getMessage());
            System.out.println("Classe Exception: " + ex.getClass().getName());
            System.exit(1);
        } catch (IllegalAccessException ex) {
            System.out.println("Error en intentar obtenir XQDataSource: " + ex.getMessage());
            System.out.println("Classe Exception: " + ex.getClass().getName());
            System.exit(1);
        }
        XQConnection xq = null;
        try {
            xqs.setProperties(props);
            xq = xqs.getConnection();
            System.out.println("Connexió establerta!");
        } catch (XQException ex) {
            System.out.println("Error en intentar connectar: " + ex.getMessage());
            System.exit(1);
        }

        XQExpression xqe = null;
        try {
            xqe = xq.createExpression();
            // Noms dels continents
            String cad = path + "//continent/name/string()";
            XQResultSequence xqrs = xqe.executeQuery(cad);
            System.out.println("Noms de continents:");
            while(xqrs.next()) {
                System.out.println(xqrs.getItemAsString(null));
            }
        } catch (XQException ex) {
            System.out.println("Error en executar consulta: " + ex.getMessage());
            // No provoquem sortida de programa per poder fer el close
        } finally {
            if (xqe!=null) {
                /* Aquí no té massa sentit per què immediatament farem xq.close()
                   que en tancar la connexió, també tanca les XQExpression obertes
                */
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
            System.out.println("Error en tancar connexió: " + ex.getMessage());
        }
    }

}
