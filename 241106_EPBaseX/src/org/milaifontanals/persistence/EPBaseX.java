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
import java.util.Properties;
import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
     */
    public Empresa getEmpresa(){
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
        
        return new Empresa(nom, data);
    }
    
    /**
     * Recupera un objecte departament en base a un codi
     * 
     * @return Empresa
     */
    public Departament getDepartament(int codi){
        
        if(codi <= 0 && codi >= 99){
            throw new EPBaseXException("Codi de departamnet NO vàlid");
        }
        
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
            
            
            return dept;
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar departament", ex);
        }finally{
            tancarQuery(cq);
        }
        
    }
    
    public Empleat getEmpleat(int codi){
        
        if(codi <= 0 && codi >= 9999){
            throw new EPBaseXException("Codi de empleat NO vàlid");
        }
        
        Empleat emp = null;
        ClientQuery cq = null;
        
        try {
            String getDept = path+"/empresa/empleats/emp[@codi='e"+codi+"']";
            
            cq = con.query(getDept);
            String emp_string = cq.execute();
            
            SAXBuilder buildSax = new SAXBuilder();
            
            Document document = buildSax.build(new StringReader(emp_string));

            // Obtener el elemento raíz
            Element rootElement = document.getRootElement();
            
            String cognom = rootElement.getChild("cognom").getValue();
            String ofici = rootElement.getChild("ofici").getValue();
            
            String deptCode = rootElement.getAttributeValue("dept");
            Departament dept = getDepartament(Integer.parseInt(deptCode.substring(1)));
            
            Calendar dataAlta = Calendar.getInstance();
            dataAlta.setTime(dateFormat.parse(rootElement.getChild("dataAlta").getValue()));
            
            Double salari = Double.parseDouble(rootElement.getChild("salari").getValue());

            Double comissio = null;
            
            if(rootElement.getChild("salari") != null){
                comissio = Double.parseDouble(rootElement.getChild("salari").getValue());
            }

            
            Empleat cap = null;
            if(rootElement.getAttribute("cap") != null){
                cap = this.getEmpleat(Integer.parseInt(rootElement.getAttributeValue("cap").substring(1)));
            }
            
            emp = new Empleat(codi, cognom, ofici, dataAlta, salari, comissio, cap, dept);
            
            return emp;
            
        } catch (Exception ex) {
            throw new EPBaseXException("Error en recuperar empleat", ex);
        }finally{
            tancarQuery(cq);
        }
    }
    
    
    
    
    /*METODES PRIVATS*/
    
    private void tancarQuery(ClientQuery q){
        try {
            if(q != null){
                q.close();                
            }
        } catch (IOException ex) {
            throw new EPBaseXException("Error en recuperar empresa", ex);
        }
    }
    
    
}
