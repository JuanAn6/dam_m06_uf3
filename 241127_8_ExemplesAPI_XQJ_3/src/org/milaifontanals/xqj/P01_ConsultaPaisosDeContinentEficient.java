/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.xqj;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 *
 * @author Usuari
 */
public class P01_ConsultaPaisosDeContinentEficient {

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
        } catch (XQException ex) {
            System.out.println("Error en intentar connectar: " + ex.getMessage());
            System.exit(1);
        }

        Scanner s = new Scanner(System.in);

        XQPreparedExpression xqpe = null;
        // Volem cercar els noms dels països del continent amb id canviant
        String cad = "declare variable $id external;";
        cad = cad + path + "//country/encompassed[@continent=$id]/../name/string()";
        try {
            xqpe = xq.prepareExpression(cad);
            XQItemType xqit = xq.createAtomicType(XQItemType.XQBASETYPE_STRING);
            // Utilitzarem la mateixa consulta preparada per executar tantes vegades com vulgui
            // l'usuari, assignant prèviament el valor a la variable idContinent introduïda a la consulta
            while (true) {
                System.out.println("Introdueixi id de continent ($ per finalitzar)");
                String idContinent = s.next();
                if (idContinent.equals("$")) {
                    break;
                }
                // Assignem valor a la variable idContinent de la consulta:
                // XAPUÇA per ORACLE: Obliga a fer prepareExpression abans de cada execució
                if (args[0].contains("Oracle")) {
                    xqpe = xq.prepareExpression(cad);
                }
                xqpe.bindString(new QName("id"), idContinent, xqit);
                // Executem la consulta preparada amb el valor que s'acaba d'assignar a la variable "id"
                XQResultSequence xqrs = xqpe.executeQuery();
                System.out.println("\nNoms de països amb id = " + idContinent + ":");
                boolean trobat = false;
                while (xqrs.next()) {
                    System.out.println(xqrs.getItemAsString(null));
                    trobat = true;
                }
                if (!trobat) {
                    System.out.println("No hi ha cap país amb aquest id de continent");
                }
                System.out.println();
            }
        } catch (XQException ex) {
            System.out.println("Error: " + ex.getMessage());
        } finally {
            if (xqpe != null) {
                try {
                    xqpe.close();
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
