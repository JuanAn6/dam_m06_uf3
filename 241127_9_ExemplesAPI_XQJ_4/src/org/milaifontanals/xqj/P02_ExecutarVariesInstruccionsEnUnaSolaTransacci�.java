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

/**
 * Programa exemple de transacció formada per vàries instruccions Afegir un nou
 * país (car_code="LL" - name='Lilliput' - membre de Nacions Unides: org-UN)
 * Això implica afegir node country adequadament i actualitzar contingut de
 * org-UN
 *
 * @author Usuari
 */
public class P02_ExecutarVariesInstruccionsEnUnaSolaTransacció {

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

        XQExpression xqe = null;
        try {
            xqe = xq.createExpression();
            // El llenguatge d'actualització és diferent segons el SGBD (variable updateVersion)
            String cad;
            if (updateVersion.equals("PL")) {
                // En llenguatges XUpdate, cal executar les dues instruccions per separat
                // Si el SGBD (com Sedna) gestiona transaccions, podrem assegurar que es fa dins una
                // transacció i, si no les gestiona (com eXist-db), haurem de "pregar!!!"
                // Instrucció que afegeix country
                cad = "update insert <country car_code='LL' memberships='org-UN'><name>Lilliput</name></country> ";
                cad = cad + "preceding " + path + "/mondial/continent[1]";
                System.out.println("Instrucció PL que afegeix el pais:");
                System.out.println(cad);
                xqe.executeCommand(cad);
                // Instrucció que retoca organització org-UN, afegint el nou pais com a membre
                // Com que la instrucció "replace" a utilitzar NO és exacta... provem de les diverses maneres
                try {   // Sintaxis segons Sedna...
                    cad = "update replace $n in " + path + "/mondial/organization[@id='org-UN']/members[@type='member']/@country ";
                    cad = cad + "with attribute country {concat($n,' LL')}";
                    System.out.println("Instrucció PL que afegeix el país a l'organització - Ver1");
                    System.out.println(cad);
                    xqe.executeCommand(cad);
                } catch (XQException ex) {
                    // Sintaxis segons eXist-db...
                    cad = "for $n in " + path + "/mondial/organization[@id='org-UN']/members[@type='member']/@country ";
                    cad = cad + "return update value $n with concat($n,' LL')";
                    System.out.println("Instrucció PL que afegeix el país a l'organització - Ver2");
                    System.out.println(cad);
                    xqe.executeCommand(cad);
                }
            } else {    // XQUF - Totes les instruccions han d'anar en una única instrucció XQUF, separades per ','
                // Instrucció que afegeix country
                cad = "insert node <country car_code='LL' memberships='org-UN'><name>Lilliput</name></country> ";
                cad = cad + "before " + path + "/mondial/continent[1]";
                // Separador
                cad = cad + ",";
                // Instrucció que retoca organització org-UN, afegint el nou pais com a membre
                cad = cad + "for $n in " + path + "/mondial/organization[@id='org-UN']/members[@type='member']/@country ";
                cad = cad + "return replace value of node $n with concat($n,' LL')";
                    System.out.println("Instrucció XQUF que fa les dues modificacions");
                    System.out.println(cad);
                xqe.executeQuery(cad);
            }
            // La instrucció anterior funciona si hi ha alguna organització (que n'hi ha!!!) però segons DTD podria no haver-n'hi.
            // Si el llenguatge és transactional, caldrà fer commit!!!
            if (transactional.equals("Y")) {
                xq.commit();
            }
            System.out.println("Inserció efectuada!");
        } catch (XQException ex) {
            System.out.println("Error en executar inserció: " + ex.getMessage());
            ex.printStackTrace();
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
