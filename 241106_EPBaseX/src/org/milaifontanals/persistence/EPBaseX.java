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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.milaifontanals.empresa.Departament;
import org.milaifontanals.empresa.Empleat;
import org.milaifontanals.empresa.Empresa;

/**
 *
 * @author Juan Antonio
 * 
 * Capa de persistencia per la gestió de l'empresa amb els departaments empleats...
 * Fitxer de propietats accecible via load(Reader) default file: "EPBaseX.properties"
 * 
 * Parametres:
 * <ul>
 *  <li>host</li>
 *  <li>port</li>
 *  <li>user</li>
 *  <li>pass</li>
 *  <li>path</li>
 * </ul>
 */
public class EPBaseX {
    private ClientSession con;
    private String path;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    //Estructures per guardar els objectes carregats en memoria
    private Empresa empresa = null; 
    private HashMap<Integer, Departament> hmDepts = new HashMap();
    private HashMap<Integer, Empleat> hmEmps = new HashMap();

    
    
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer default
     * 
     */
    public EPBaseX (){
        this("EPBaseX.properties");
    }
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer facilitat en cas de null o vuit 
     * amb el fitxer default
     * 
     * @param nomFitxerPropietats
     * @throws EPBaseXException
     */
    public EPBaseX (String nomFitxerPropietats){
        if(nomFitxerPropietats == null || nomFitxerPropietats.equals("")){
            nomFitxerPropietats = "EPBaseX.properties";
        }
        
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(nomFitxerPropietats));
        } catch (FileNotFoundException ex) {
            throw new EPBaseXException("No es troba el fitxer de propietats: "+nomFitxerPropietats, ex);
        } catch (IOException ex) {
            throw new EPBaseXException("Error en intentar carregar el fitxer de propietats: "+nomFitxerPropietats, ex);
        }
        
        
        String host = props.getProperty("host");
        int port;
        try {
            port = Integer.parseInt(props.getProperty("port"));
        } catch (NumberFormatException ex){
            throw new EPBaseXException("El port es obligatori i ha de ser un valor enter valid", ex);
        }
        String user = props.getProperty("user");
        String pass = props.getProperty("pass");
        
        try {
            con = new ClientSession(host, port, user, pass);
        } catch (IOException ex) {
            throw new EPBaseXException("Error en establir la connexió", ex);
        }
        
        path = props.getProperty("path");
        if(path == null || path.equals("")){
            closeCapa();
            throw new EPBaseXException("El path no esta definit en el arciu de propietats "+nomFitxerPropietats);
        }
        
        /* Prova de path correcte */
        try {
            String tryQuery = path+"//x";
            ClientQuery cq;
            cq = con.query(tryQuery);
            cq.execute();
            cq.close();
        } catch (IOException ex) {
            closeCapa();
            throw new EPBaseXException("El path no es correcte ( "+path+" )");
        }
        
    }
    
    /**
     * Tanca la capa de persistencia, tanca la connexio amb la BD
     * 
     * @throws EPBaseXException
     */
    
    public void closeCapa(){
        try {
            if(con != null){
                con.close();            
            }else{
                System.out.println("La connexió no estaba establerta! ");
            }
        } catch (IOException ex) {
            throw new EPBaseXException("Error en tancar la connexió", ex);
        } finally {
            con = null;
        }
    }
    
    
    /**
     * Recupera l'objecte empresa
     * 
     * @return Empresa
     * @throws EPBaseXException
     */
    public Empresa getEmpresa(){
        
        //Si la empresa ja està carregada
        if(empresa != null) return empresa;
        
        String nom = null;
        Calendar data = Calendar.getInstance();
        ClientQuery cq = null;
        
        try {
            String getNom = path+"/empresa/nom/string()";
        
            cq = con.query(getNom);
            nom = cq.execute();
            
            String getData = path+"/empresa/dataCreacio/string()";
            cq = con.query(getData);
            String d = cq.execute();
            data.setTime(dateFormat.parse(d));
            
        } catch (ParseException | IOException ex) {
            throw new EPBaseXException("Error en recuperar empresa", ex);
        }finally{
            tancarQuery(cq);
        }
        empresa = new Empresa(nom, data);
        return empresa;
    }
    
    /**
     * Recupera un objecte departament en base a un codi
     * @param codi
     * @return Departament
     * @throws EPBaseXException
     */
    public Departament getDepartament(int codi){
        
        if(codi <= 0 && codi >= 99){
            throw new EPBaseXException("Codi de departamnet NO vàlid");
        }
        
        //Si el departament ja esta en memoria el retorna
        if(hmDepts.containsKey(codi)) return hmDepts.get(codi);
        
        Departament dept = null;
        ClientQuery cq = null;
        
        try {
            String getDept = path+"/empresa/departaments/dept[@codi='d"+codi+"']";
            cq = con.query(getDept);
            String dept_string = cq.execute();
            
            SAXBuilder buildSax = new SAXBuilder();
            
            Document document = buildSax.build(new StringReader(dept_string));

            // Obtener el elemento raíz
            Element rootElement = document.getRootElement();
            
            String nom = rootElement.getChild("nom").getValue();
            String localitat = rootElement.getChild("localitat").getValue();
            
            dept = new Departament(codi, nom, localitat);
            
            hmDepts.put(codi, dept);
            return dept;
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar departament", ex);
        }finally{
            tancarQuery(cq);
        }
        
    }
    
    
    
    /**
     * Recupera un objecte Empleat en base a un codi
     * @param codi
     * @return Empleat
     * @throws EPBaseXException
     */
    
    
    public Empleat getEmpleat(int codi){
        
//        if(codi <= 0 && codi >= 9999){
//            throw new EPBaseXException("Codi de empleat NO vàlid");
//        }
        
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            throw new EPBaseXException("Error en generar el Empleat codi NO vàlid", ex);
        }
        
        if(hmEmps.containsKey(codi)) return hmEmps.get(codi);
        
        Empleat emp = null;
        ClientQuery cq = null;
        
        try {
            String getEmp = path+"/empresa/empleats/emp[@codi='e"+codi+"']";
            
            cq = con.query(getEmp);
            String emp_string = cq.execute();
            
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
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar empleat", ex);
        }finally{
            tancarQuery(cq);
        }
    }
    
    
    
    /**
     * Retorna el numero de subordinats del emplat amb el codi que es pasa
     * @param codi
     * @return boolean
     * @throws EPBaseXException
     */
    
    
    public int getSubordinats(int codi){
        
        if(!existeixEmpleat(codi)){
            throw new EPBaseXException("El empleat no existeix!");
        }
        
        ClientQuery cq = null;
        try{
            
            String getEmpsCount = "count("+path+"/empresa/empleats/emp[@cap='e"+codi+"'])";
            cq = con.query(getEmpsCount);
            String count = cq.execute();
            
            return Integer.parseInt(count);
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar empleat", ex);
        }finally{
            tancarQuery(cq);
        }
        
        
    }
    
    
    /**
     * Retorna un boolean si el empleat existeix
     * @param codi
     * @reutrn boolean
     * @throws EPBaseXException
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
        ClientQuery cq = null;
        try{
            
            String getEmp = path+"/empresa/empleats/emp[@codi='e"+codi+"']/@codi/string()";
            cq = con.query(getEmp);
            String emp_string = cq.execute();
            
            if(!emp_string.equals("")){
                exist = true;
            }
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar empleat", ex);
        }finally{
            tancarQuery(cq);
        }
        
        return exist;
    }
    
    
     /**
     * Eliminar un empleat i si ens pasen un codi de cap se asigna un nou cap als empleats que esquedarien sense
     * si es pasa 0 els empleats es queden sense cap.
     * @param codi
     * @param actCap
     * @reutrn boolean
     * @throws EPBaseXException
     */
    
    public boolean eliminarEmpleat(int codi, int actCap){
        
        //per saber si el codi es valid abans de fer la petició a la base de dades
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            throw new EPBaseXException("Codi de empleat erroni", ex);
        }
        
        if(codi == actCap){
            throw new EPBaseXException("Han de ser diferents");
        }
        
        if(actCap < 0){
            throw new EPBaseXException("El codi actCap es erroni");
        }
        
        
        ClientQuery cq = null;
        String query = "";
        try{
            
            
            if(actCap == 0){
                
                //S'elimina els atributs cap dels subordinats
                query = "delete node "+path+"//emp[@cap='e"+codi+"']/@cap";
                query = query+" , ";
            
            }else{
               
                if(!this.existeixEmpleat(codi)){
                    throw new EPBaseXException("El Empleat no existex");
                }
                
                if(this.esSubordinatDirecteIndirecte(codi, actCap)){
                    throw new EPBaseXException("El nou cap es subordinat del que s'elimina!");
                }

                //Això només per quan es segur que es nomes un únic camp el que es modifica
                //query = "replace value of node "+path+"//emp[@cap='e"+codi+"']/@cap with 'e"+actCap+"'";
                
                //Es modifica els atributs cap dels subordinats
                query = query + "for $n in "+path+"//emp[@cap='e"+codi+"']\n " 
                    + "return replace value of node $n /@cap with 'e"+actCap+"'\n";
                query = query+" , ";
                
            }
            
            
            query = "delete node "+path+"//emp[@codi='e"+codi+"']";
            cq = con.query(query);
            cq.execute();
            
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
            
            
        }catch(Exception ex){
            throw new EPBaseXException("Error en eliminar empleat", ex);
        }
        
        if(actCap < 0 ){
            throw new EPBaseXException("Codi de empleat erroni");
        }
        return false;
    }
    
    
    /**
     * Retorna un boolean si el empleat existeix
     * @param codi
     * @reutrn boolean
     * @throws EPBaseXException
     */
    
    public boolean esSubordinatDirecteIndirecte(int cap, int emp){
        
        if(!existeixEmpleat(cap) || !existeixEmpleat(emp)){
            throw new EPBaseXException("El empleat o el cap no existeixen a la base de dades");
        }
        
        boolean exist = false;
        ClientQuery cq = null;
        try{
            
            String getEmp = path+"//emp[@cap='e"+cap+"']/@codi/string()";
            cq = con.query(getEmp);
            String emps = cq.execute();
            
            
            
            if(emps.equals("")){
                return false;
            }
            
            String [] subordinats = emps.split(System.getProperty("line.separator"));
           
            for (String subordinat : subordinats) {
                if (Integer.parseInt(subordinat.substring(1)) == emp) {
                    return true;
                }
                if (esSubordinatDirecteIndirecte(Integer.parseInt(subordinat.substring(1)), emp)) {
                    return true;
                }
            }
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar empleat", ex);
        }finally{
            tancarQuery(cq);
        }
        
        return exist;
    }
    
    
    
    
    
    /*METODES PRIVATS*/
    
    private void tancarQuery(ClientQuery q){
        try {
            if(q != null){
                q.close();                
            }
        } catch (IOException ex) {
            throw new EPBaseXException("Error en tancar la connexió", ex);
        }
    }
    
    
}
