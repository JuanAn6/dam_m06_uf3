/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.milaifontanals;

import org.milaifontanals.empresa.Departament;
import org.milaifontanals.empresa.Empleat;
import org.milaifontanals.empresa.Empresa;
import org.milaifontanals.persistence.EPBaseX;
import org.milaifontanals.persistence.EPBaseXException;

/**
 *
 * @author Juan Antonio
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        /* Establir la connexió */
        EPBaseX ep = null;
        try{
            ep = new EPBaseX();
            System.out.println("Connexió establerta");
        } catch (Exception ex){
            System.out.println("ERROR EN L'APPLICACIO");
            infoError(ex);
            return;
        }
        
        /* Feina de l'aplicació */
        System.out.println("Començament de les proves!");
        try{
            /*
            Empresa empresa = ep.getEmpresa();
            System.out.println(empresa.toString());
            
            
            Departament dept = ep.getDepartament(10);
            
            System.out.println("dept: "+dept);
            
            Empleat emp = ep.getEmpleat(7369);
            System.out.println("Emp: "+emp);
            
            boolean e = ep.existeixEmpleat(7499);
            System.out.println("Exists: "+e);
            
            int count = ep.getSubordinats(7839);
            System.out.println("Count: "+count);
            */
            
            System.out.println(ep.esSubordinatDirecteIndirecte(7839, 7499));
            
            
            
        }catch(Exception ex){
            System.out.println("Error en les consultes");
            infoError(ex);
        }
        
        /* Tancar la capa */
        try{
            ep.closeCapa();
            System.out.println("Tancan connexió");
        }catch(Exception ex){
            System.out.println("Error en tancar la capa de persistencia");
            infoError(ex);
        }
    }
    
    private static void infoError(Throwable aux){
        do{
           if(aux.getMessage() != null){
               System.out.println("\t "+aux.getMessage());
           }
           aux = aux.getCause();
        }while(aux != null);
    }
}
