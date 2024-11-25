/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.milaifontanals.persistence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.milaifontanals.empresa.Departament;
import org.milaifontanals.empresa.Empleat;
import org.milaifontanals.empresa.Empresa;
import ru.ispras.sedna.driver.DatabaseManager;
import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

/**
 *
 * @author Juan Antonio
 *
 * Capa de persistencia per la gestió de l'empresa amb els departaments empleats...
 * Fitxer de propietats accecible via load(Reader) default file: "EPSedna.properties"
 *
 * Parametres:
 * <ul>
 *  <li>host</li>
 *  <li>port</li>
 *  <li>database</li>
 *  <li>user</li>
 *  <li>pass</li>
 *  <li>path</li>
 * </ul>
 */
public class EPSedna {
    private SednaConnection con;
    private String path;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    //Variable per controlar si la transacció està oberta
    private boolean transOn;
    
    //Estructures per guardar els objectes carregats en memoria
    private Empresa empresa = null; 
    private HashMap<Integer, Departament> hmDepts = new HashMap();
    private HashMap<Integer, Empleat> hmEmps = new HashMap();

    
    
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer default EPSedna.properties
     * 
     */
    public EPSedna (){
        this("EPSedna.properties");
    }
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer facilitat en cas de null o vuit 
     * amb el fitxer default EPSedna.properties
     * 
     * @param nomFitxerPropietats
     * @throws EPSednaException
     */
    public EPSedna (String nomFitxerPropietats){
        if(nomFitxerPropietats == null || nomFitxerPropietats.equals("")){
            nomFitxerPropietats = "EPSedna.properties";
        }
        
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(nomFitxerPropietats));
        } catch (FileNotFoundException ex) {
            throw new EPSednaException("No es troba el fitxer de propietats: "+nomFitxerPropietats, ex);
        } catch (IOException ex) {
            throw new EPSednaException("Error en intentar carregar el fitxer de propietats: "+nomFitxerPropietats, ex);
        }
        
        
        String host = props.getProperty("host");
        int port;
        try {
            port = Integer.parseInt(props.getProperty("port"));
        } catch (NumberFormatException ex){
            throw new EPSednaException("El port es obligatori i ha de ser un valor enter valid", ex);
        }
        String database = props.getProperty("database");
        String user = props.getProperty("user");
        String pass = props.getProperty("pass");
        
        try {
            con = DatabaseManager.getConnection(host+":"+port,database, user, pass);
        } catch (Exception ex) {
            throw new EPSednaException("Error en establir la connexió", ex);
        }
        
        path = props.getProperty("path");
        if(path == null || path.equals("")){
            closeCapa();
            throw new EPSednaException("El path no esta definit en el arciu de propietats "+nomFitxerPropietats);
        }
        
        /* Prova de path correcte */
        SednaStatement st;
        
        try {
            con.begin();
            transOn = true;
            
            String tryQuery = path+"/x";
            st = con.createStatement();
            st.execute(tryQuery);
            
        } catch (Exception ex) {
            closeCapa();
            throw new EPSednaException("El path no es correcte ( "+path+" )", ex);
        }
        
    }
    
    /**
     * Tanca la capa de persistencia, tanca la connexio amb la BD
     * 
     * @throws EPSednaException
     */
    
    public void closeCapa(){
        try {
            if(con != null){
                con.close();            
            }else{
                System.out.println("La connexió no estaba establerta! ");
            }
        } catch (DriverException ex) {
            throw new EPSednaException("Error en tancar la connexió", ex);
        } finally {
            con = null;
        }
    }
    
    /**
     * Commit en el gestor i tanca la transacció activadd
     */
    public void commit(){
        
        if(transOn){
            try {
                con.commit();
                transOn = false;
            } catch (DriverException ex) {
                transOn = false;
                throw new EPSednaException("Error en fer commit", ex);
            }
        }
            
    }
    
    
    /**
     * Rollback en el gestor i tanca la transacció activa
     */
    public void rollback(){
        if(transOn){
            try {
                con.rollback();
                transOn = false;
            } catch (DriverException ex) {
                transOn = false;
                throw new EPSednaException("Error en fer rollback", ex);
            }
        }
    }
    
    
    /**
     * Recupera l'objecte empresa
     * 
     * @return Empresa
     * @throws EPSednaException
     */
    public Empresa getEmpresa(){
        
        //Si la empresa ja està carregada
        if(empresa != null) return empresa;
        
        String nom = null;
        Calendar data = Calendar.getInstance();
        SednaStatement st = null;
        
        try {
            obrirTrans();
            
            String getNom = path+"/empresa/nom/string()";
            st = con.createStatement();
            
            st.execute(getNom);
            nom = st.getSerializedResult().next();

            String d = null;
            String getData = path+"/empresa/dataCreacio/string()";
            
            st.execute(getData);
            d = st.getSerializedResult().next();
                
            data.setTime(dateFormat.parse(d));
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empresa", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar empresa", ex);
        }
        empresa = new Empresa(nom, data);
        return empresa;
    }
    
    /**
     * Recupera un objecte departament en base a un codi
     * @param codi
     * @return Departament
     * @throws EPSednaException
     */
    public Departament getDepartament(int codi){
        
        if(codi <= 0 && codi >= 99){
            throw new EPSednaException("Codi de departamnet NO vàlid");
        }
        
        //Si el departament ja esta en memoria el retorna
        if(hmDepts.containsKey(codi)) return hmDepts.get(codi);
        
        Departament dept = null;
        SednaStatement st = null;
        
        try {
            
            obrirTrans();
            
            String getDept = path+"/empresa/departaments/dept[@codi='d"+codi+"']";
            
            st = con.createStatement();
            st.execute(getDept);
            String dept_string = st.getSerializedResult().next();
            
            if(dept_string == null){
               return null;
            }
            
            SAXBuilder buildSax = new SAXBuilder();
            
            Document document = buildSax.build(new StringReader(dept_string));

            // Obtener el elemento raíz
            Element rootElement = document.getRootElement();
            
            String nom = rootElement.getChild("nom").getValue();
            String localitat = rootElement.getChild("localitat").getValue();
            
            dept = new Departament(codi, nom, localitat);
            
            hmDepts.put(codi, dept);
            return dept;
            
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar departament", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar departament", ex);
        }
        
    }


    /**
     * Recupera un objecte Empleat en base a un codi
     * @param codi
     * @return Empleat
     * @throws EPSednaException
     */
    
    
    public Empleat getEmpleat(int codi){
        
//        if(codi <= 0 && codi >= 9999){
//            throw new EPSednaException("Codi de empleat NO vàlid");
//        }
        
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            throw new EPSednaException("Error en generar el Empleat codi NO vàlid", ex);
        }
        
        if(hmEmps.containsKey(codi)) return hmEmps.get(codi);
        
        Empleat emp = null;
        SednaStatement st = null;
        
        try {
            
            obrirTrans();
            
            String getEmp = path+"/empresa/empleats/emp[@codi='e"+codi+"']";
            
            st = con.createStatement();
            st.execute(getEmp);
            String emp_string = st.getSerializedResult().next();
            
            SAXBuilder buildSax = new SAXBuilder();
            
            Document document = buildSax.build(new StringReader(emp_string));

            // Obtener el elemento raíz
            Element rootElement = document.getRootElement();
            
            String cognom = rootElement.getChild("cognom").getValue();
            
            String ofici = rootElement.getChild("ofici").getValue();
            String deptCode = rootElement.getAttributeValue("dept");
            
            Departament dept = null;
            if(deptCode != null){
                dept = getDepartament(Integer.parseInt(deptCode.substring(1)));
            }
            
            Calendar dataAlta = null;
            if(rootElement.getChildText("dataAlta") != null){
                dataAlta = Calendar.getInstance();
                dataAlta.setTime(dateFormat.parse(rootElement.getChildText("dataAlta")));
            }
            
            Double salari = null;
            if(rootElement.getChildText("salari") != null && rootElement.getChildText("salari").equals("")){
                salari = Double.parseDouble(rootElement.getChildText("salari"));            
            }
            
            Double comissio = null;
            if(rootElement.getChildText("comissio") != null && rootElement.getChildText("comissio").equals("")){
                comissio = Double.parseDouble(rootElement.getChildText("comissio"));
            }
            
            Empleat cap = null;
            if(rootElement.getAttribute("cap") != null){
                cap = this.getEmpleat(Integer.parseInt(rootElement.getAttributeValue("cap").substring(1)));
            }
            
            emp = new Empleat(codi, cognom, ofici, dataAlta, salari, comissio, cap, dept);
            hmEmps.put(codi, emp);
            return emp;
            
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar empleat", ex);
        }
    }
    
    
    
    /**
     * Retorna el numero de subordinats del emplat amb el codi que es pasa
     * @param codi
     * @return boolean
     * @throws EPSednaException
     */
    
    
    public int getSubordinats(int codi){
        
        if(!existeixEmpleat(codi)){
            throw new EPSednaException("El empleat no existeix!");
        }
        
        SednaStatement st = null;
        try{
            obrirTrans();
            String getEmpsCount = "count("+path+"/empresa/empleats/emp[@cap='e"+codi+"'])";
            
            st = con.createStatement();
            st.execute(getEmpsCount);
            String count = st.getSerializedResult().next();
            
            return Integer.parseInt(count);
        
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar empleat", ex);
        }
        
    }
    
    
    /**
     * Retorna un boolean si el empleat existeix
     * @param codi
     * @reutrn boolean
     * @throws EPSednaException
     */
    
    public boolean existeixEmpleat(int codi){
        
        //per saber si el codi es valid abans de fer la petició a la base de dades
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            return false;
        }
        
        if(hmEmps.containsKey(codi)){
            return true;
        }
        
        boolean exist = false;
        SednaStatement st = null;
        try{
            obrirTrans();
            String getEmp = path+"/empresa/empleats/emp[@codi='e"+codi+"']/@codi/string()";
            
            st = con.createStatement();
            st.execute(getEmp);
            String emp_string = st.getSerializedResult().next();
            
            
            if(emp_string != null && !emp_string.equals("")){
                exist = true;
            }
            
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar empleat", ex);
        }
        
        return exist;
    }
    
    
     /**
     * Eliminar un empleat i si ens pasen un codi de cap se asigna un nou cap als empleats que esquedarien sense
     * si es pasa 0 els empleats es queden sense cap.
     * @param codi
     * @param actCap
     * @reutrn boolean
     * @throws EPSednaException
     */
    
    public boolean eliminarEmpleat(int codi, int actCap){
        
        //per saber si el codi es valid abans de fer la petició a la base de dades
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            throw new EPSednaException("Codi de empleat erroni", ex);
        }
        
        if(codi == actCap){
            throw new EPSednaException("Han de ser diferents");
        }
        
        if(actCap < 0){
            throw new EPSednaException("El codi actCap es erroni");
        }
        
        
        SednaStatement st = null;
        //En sedna s'ha d'executar les instruccions una a una.
        List<String> querys = new ArrayList();
        try{
            
            
            
            
            if(actCap == 0){
                
                //S'elimina els atributs cap dels subordinats
                querys.add("update delete "+path+"//emp[@cap='e"+codi+"']/@cap");
            
            }else{
               
                if(!this.existeixEmpleat(codi)){
                    throw new EPSednaException("El Empleat no existex");
                }
                
                if(this.esSubordinatDirecteIndirecte(codi, actCap)){
                    throw new EPSednaException("El nou cap es subordinat del que s'elimina!");
                }

                //Això només per quan es segur que es nomes un únic camp el que es modifica
                //query = "replace value of node "+path+"//emp[@cap='e"+codi+"']/@cap with 'e"+actCap+"'";
                
                //Es modifica els atributs cap dels subordinats
                querys.add("update replace $n in "+path+"//emp[@cap='e"+codi+"']\n " 
                    + "with (attribute cap {'e"+actCap+"'})\n");
                
            }
            
            
            querys.add("update delete "+path+"//emp[@codi='e"+codi+"']");
            
            obrirTrans();
            
            //Execució de totes les consultes
            st = con.createStatement();
            for(String query : querys){
                st.execute(query);
            }
            
            
            //Actualitzar els empleats en memoria
            hmEmps.remove(codi);
            
            Collection empleats = hmEmps.values();
            Iterator <Empleat> iteEmps = null;
            
            if(actCap == 0){
                iteEmps = empleats.iterator();
                while(iteEmps.hasNext()){
                    Empleat e = iteEmps.next();
                    if(e.getCap() != null && e.getCap().getCodi() == codi){
                        e.setCap(null);
                    }
                }
                
            }else{
                Empleat nouCap = this.getEmpleat(actCap);
                
                //El iterator ha de ser creat abans de modificar el HashMap perque si no no funciona bé
                iteEmps = empleats.iterator();
                
                while(iteEmps.hasNext()){
                    Empleat e = iteEmps.next();
                    if(e.getCap() != null && e.getCap().getCodi() == codi){
                        e.setCap(nouCap);
                    }
                }
            }
            
            
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empleat", ex);
        }catch(Exception ex){
            throw new EPSednaException("Error en eliminar empleat", ex);
        }
        
        return false;
    }
    
    
    /**
     * Retorna un boolean si el empleat existeix
     * @param codi
     * @reutrn boolean
     * @throws EPSednaException
     */
    
    public boolean esSubordinatDirecteIndirecte(int cap, int emp){
        
        if(!existeixEmpleat(cap) || !existeixEmpleat(emp)){
            throw new EPSednaException("El empleat o el cap no existeixen a la base de dades");
        }
        
        boolean exist = false;
        SednaStatement st = null;
        try{
            obrirTrans();
            
            String getEmps = path+"//emp[@cap='e"+cap+"']/@codi/string()";
            
            st = con.createStatement();
            st.execute(getEmps);
            SednaSerializedResult pr = st.getSerializedResult();
            
            String subordinat;
            
            subordinat = pr.next();
            if(subordinat == null){
                return false;
            }
            
            //Mentres s'esta porcessant un sednaSerialized result no es pot crear un altre;
            List<String>subordinats = new ArrayList();
            while (subordinat != null) {
                subordinats.add(subordinat);
                subordinat = pr.next();
            }
            
               
            for (String sub : subordinats) {
                
                //A partir del segon resultat retorna tambe un salt de lina per aixo anem directes a la "e" que es el començament del codi
                sub = sub.substring(sub.indexOf("e")+1);
                
                if (Integer.parseInt(sub) == emp) {
                    return true;
                }
                if (esSubordinatDirecteIndirecte(Integer.parseInt(sub), emp)) {
                    return true;
                }
            }
        
        } catch (DriverException ex){
            transOn = false;
            throw new EPSednaException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPSednaException("Error en recuperar empleat", ex);
        }
        
        return exist;
    }
    
    
    
    
    
    /*METODES PRIVATS*/
    
    
    
    private void obrirTrans() throws DriverException{
        if(!transOn){
            con.begin();
            transOn = true;
        }
    }
}
