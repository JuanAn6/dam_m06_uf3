/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.xqj;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xquery.XQDataSource;

/**
 *
 * @author Usuari
 */
public class P01_EsbrinarPropietatsConnexio {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Programa P01 cal invocar-lo passant nom XQDataSource del fabricant");
            System.exit(1);
        }
        XQDataSource xqs = null;
        
        try {
            xqs = (XQDataSource) Class.forName(args[0]).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.out.println("Error en intentar obtenir XQDataSource: "+ ex.getMessage());
            System.out.println("Classe Exception: "+ ex.getClass().getName());
            System.exit(1);
        }
        // Esbrinem les propietats del connector:
        String propietats[] = xqs.getSupportedPropertyNames();
        System.out.println("Propietats connexió de: "+args[0]);
        for (String p:propietats) {
            System.out.println(p);
        }
    }

}
