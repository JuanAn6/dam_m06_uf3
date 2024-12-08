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
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.milaifontanals.empresa.Departament;
import org.milaifontanals.empresa.Empleat;
import org.milaifontanals.empresa.Empresa;

/**
 *
 * @author Juan Antonio
 
 Capa de persistencia per la gestió de l'empresa amb els departaments empleats...
 Fitxer de propietats accecible via load(Reader) default file: "EPXQJ.properties"
 
 Parametres:
 <ul>
 *  <li>host</li>
 *  <li>port</li>
 *  <li>user</li>
 *  <li>pass</li>
 *  <li>path</li>
 * </ul>
 */
public class EPXQJ {
    private XQConnection con;
    /* variables que recuperarem dels fitxers de configuració */
    private String path;
    private String transactional;
    private String updateVersion;
    
    //En el cas de sedna es necessari saber si la transacció està oberta
    private boolean transOn;
    
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    //Estructures per guardar els objectes carregats en memoria
    private Empresa empresa = null; 
    private HashMap<Integer, Departament> hmDepts = new HashMap();
    private HashMap<Integer, Empleat> hmEmps = new HashMap();

    
    //PreparedExpression
    private XQItemType xqitStr;
    private XQPreparedExpression xqpeDept;
    private XQPreparedExpression xqpeEmp;
    private XQPreparedExpression xqpeGetSubordinat;
    
    //Els meus propis metodes de la capa
    private XQPreparedExpression xqpeExistexDept;
    private XQPreparedExpression xqpeInsertDept;

    
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer default
     * 
     */
    public EPXQJ (){
        this("EPXQJ.properties");
    }
    
    /**
     * Constructor per establir la connexió amb la base de dades amb el fitxer facilitat en cas de null o vuit 
     * amb el fitxer default
     * 
     * @param nomFitxerPropietats
     * @throws EPXQJException
     */
    public EPXQJ (String nomFitxerPropietats){
        if(nomFitxerPropietats == null || nomFitxerPropietats.equals("")){
            nomFitxerPropietats = "EPXQJ.properties";
        }
        
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(nomFitxerPropietats));
        } catch (FileNotFoundException ex) {
            throw new EPXQJException("No es troba el fitxer de propietats: "+nomFitxerPropietats, ex);
        } catch (IOException ex) {
            throw new EPXQJException("Error en intentar carregar el fitxer de propietats: "+nomFitxerPropietats, ex);
        }
        
        //Carregar les variables del fitxer de propietats necessaries
        String className = props.getProperty("className");
        if(className == null || className.equals("")){
            throw new EPXQJException("Error en carregar la propietat className del fitxer de propietats");
        }
        props.remove("className"); //per fer la connexió cal que les propietats només estiguin les que necessita la connexió
        
        path = props.getProperty("path");
        if(path == null || path.equals("")){
            throw new EPXQJException("El path no esta definit en el arciu de propietats "+nomFitxerPropietats);
        }
        props.remove("path");
       
        updateVersion = props.getProperty("updateVersion");
        if(updateVersion == null || updateVersion.equals("")){
            throw new EPXQJException("Error en carregar la propietat updateVersion del fitxer de propietats");
        }
        if(!updateVersion.equals("PL") && !updateVersion.equals("XQUF")){
            throw new EPXQJException("Error el valor de updateVersion no es valid <PL|XQUF>");
        }
        props.remove("updateVersion");
        
        transactional = props.getProperty("transactional");
        if(transactional == null || transactional.equals("")){
            throw new EPXQJException("Error en carregar la propietat transactional del fitxer de propietats");
        }
        if(!transactional.equals("Y") && !transactional.equals("N")){
            throw new EPXQJException("Error el valor de updateVersion no es valid <PL|XQUF>");
        }
        props.remove("transactional");
        
        
        
        // props s'ha quedat amb les propietats que necessita per establir connexió
        XQDataSource xqs = null;
        try {
            xqs = (XQDataSource) Class.forName(className).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new EPXQJException("Error en intentar obtenir XQDataSource amb className '"+className+"'",ex);
        }
        
        try {
            xqs.setProperties(props);
            con = xqs.getConnection();
            if (transactional.equals("Y")) {
                // Desactivem autocommit, que per defecte, en XQJ està activat
                con.setAutoCommit(false);
            }
        } catch (XQException ex) {
            throw new EPXQJException("Error en intentar connectar", ex);
        }
        
        XQExpression xqe = null;
        try {
            transOn = true;
            xqe = con.createExpression();
            // El llenguatge d'actualització és diferent segons el SGBD (variable updateVersion)
            String cad;            
            cad = path + "/empresa/nom/string()";
            XQResultSequence rs = xqe.executeQuery(cad);
            //Comporobació necessaria per eXistDB que no faria falta ni per sedna ni per baseX
            if(!rs.next()){
                throw new EPXQJException("Error en intentar comprobar el path: '"+path+"'");
            }
        }catch(XQException ex){
            closeCapa();
            throw new EPXQJException("Error en intentar comprobar el path: '"+path+"'", ex);
        } finally {
            if (con == null){
                tancarExpression(xqe);
            }
        }
        
    }
    
    /**
     * Tanca la capa de persistencia, tanca la connexio amb la BD
     * 
     * @throws EPXQJException
     */
    
    public void closeCapa(){
        try {
            if(con != null){
                con.close();            
            }else{
                System.out.println("La connexió no estaba establerta! ");
            }
        } catch (XQException ex) {
            if(!ex.getMessage().contains("SE4611")){//Error de sedna que no es un error realment!
                throw new EPXQJException("Error en tancar la connexió", ex);
            }
        } finally {
            con = null;
            transOn = false;
        }
    }
    
    
    /**
     * Recupera l'objecte empresa
     * 
     * @return Empresa
     * @throws EPXQJException
     */
    
    public Empresa getEmpresa(){
        
        //Si la empresa ja està carregada
        if(empresa != null) return empresa;
        
        String nom = null;
        Calendar data = Calendar.getInstance();
        XQExpression xqe = null;
        
        try {
            transOn = true;
            
            xqe = con.createExpression();
            // Noms dels continents
            String getNom = path+"/empresa/nom/string()";
            XQResultSequence rs = xqe.executeQuery(getNom);
            rs.next();
            nom = rs.getItemAsString(null);
            
            
            String getData = path+"/empresa/dataCreacio/string()";
            rs = xqe.executeQuery(getData);
            rs.next();
            String d = rs.getItemAsString(null);
            
            data.setTime(dateFormat.parse(d));
            
        } catch (XQException ex) {
            transOn = false;
            throw new EPXQJException("Error en recuperar empresa", ex);
        } catch (Exception ex){
            throw new EPXQJException("Error en recuperar empresa", ex);
        }finally{
            tancarExpression(xqe);
        }
        empresa = new Empresa(nom, data);
        return empresa;
    }
    
    
    
    /**
     * Recupera un objecte departament en base a un codi
     * @param codi
     * @return Departament
     * @throws EPXQJException
     */
    public Departament getDepartament(int codi){
        
        if(codi <= 0 && codi >= 99){
            throw new EPXQJException("Codi de departamnet NO vàlid");
        }
        
        //Si el departament ja esta en memoria el retorna
        if(hmDepts.containsKey(codi)) return hmDepts.get(codi);
        
        Departament dept = null;
        
        if(xqpeDept == null){
            String cad = "declare variable $codi external;";
            cad = cad+path+"/empresa/departaments/dept[@codi=$codi]";
            try{
                xqpeDept = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeDept.bindString(new QName("codi"), "d"+codi, xqitStr);
            
            XQResultSequence xqrs = xqpeDept.executeQuery();
            
            if(!xqrs.next()){
                return null;
            }
            
            String dept_string = xqrs.getItemAsString(null);
            
            SAXBuilder buildSax = new SAXBuilder();
            
            Document document = buildSax.build(new StringReader(dept_string));

            // Obtener el elemento raíz
            Element rootElement = document.getRootElement();
            
            String nom = rootElement.getChild("nom").getValue();
            String localitat = rootElement.getChild("localitat").getValue();
            
            dept = new Departament(codi, nom, localitat);
            
            hmDepts.put(codi, dept);
            return dept;
            
        } catch (XQException ex) {
            transOn = false;
            throw new EPXQJException("Error en recuperar departament", ex);
        }catch (Exception ex){
            throw new EPXQJException("Error en recuperar departament", ex);
        }
        
        
    }
    
    
    
    /**
     * Recupera un objecte Empleat en base a un codi
     * @param codi
     * @return Empleat
     * @throws EPXQJException
     */
    
    
    public Empleat getEmpleat(int codi){
        
//        if(codi <= 0 && codi >= 9999){
//            throw new EPXQJException("Codi de empleat NO vàlid");
//        }
        
        try{
            new Empleat(codi, "???", null);
        }catch(Exception ex){
            throw new EPXQJException("Error en generar el Empleat codi NO vàlid", ex);
        }
        
        if(hmEmps.containsKey(codi)) return hmEmps.get(codi);
        
        Empleat emp = null;
        if(xqpeEmp == null){
            String cad = "declare variable $codi external;";
            cad = cad+path+"/empresa/empleats/emp[@codi=$codi]";
            try{
                xqpeEmp = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeEmp.bindString(new QName("codi"), "e"+codi, xqitStr);
            
            XQResultSequence xqrs = xqpeEmp.executeQuery();
            
            if(!xqrs.next()){
                return null;
            }
            
            String emp_string = xqrs.getItemAsString(null);
            //System.out.println("Empleat: "+emp_string);
            
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
        } catch (XQException ex) {
            transOn = false;
            throw new EPXQJException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPXQJException("Error en recuperar empleat", ex);
        }
        
    }
    

    

    /**
     * Retorna el numero de subordinats del emplat amb el codi que es pasa
     * @param codi
     * @return boolean
     * @throws EPXQJException
     */
    
    
    public int getSubordinats(int codi){
        
        if(!existeixEmpleat(codi)){
            throw new EPXQJException("El empleat no existeix!");
        }
        
        
        if(xqpeGetSubordinat == null){
            String cad = "declare variable $codi external;";
            cad = cad+"count("+path+"/empresa/empleats/emp[@codi=$codi])";
            try{
                xqpeGetSubordinat = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeGetSubordinat.bindString(new QName("codi"), "e"+codi, xqitStr);
            
            XQResultSequence xqrs = xqpeGetSubordinat.executeQuery();
            
            
            String count = !xqrs.next() ? xqrs.getItemAsString(null): "0";
            
            return Integer.parseInt(count);
            
        }catch (XQException ex) {
            transOn = false;
            throw new EPXQJException("Error en recuperar suboardinat", ex);
        } catch (Exception ex) {
            throw new EPXQJException("Error en recuperar suboardinat", ex);
        }
        
        
    }
    
    
    /**
     * Retorna un boolean si el empleat existeix
     * @param codi
     * @reutrn boolean
     * @throws EPXQJException
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
        
        if(xqpeGetSubordinat == null){
            String cad = "declare variable $codi external;";
            cad = cad+path+"/empresa/empleats/emp[@codi=$codi]/@codi/string()";
            try{
                xqpeGetSubordinat = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeGetSubordinat.bindString(new QName("codi"), "e"+codi, xqitStr);
            
            XQResultSequence xqrs = xqpeGetSubordinat.executeQuery();
            
            if(!xqrs.next()){
                exist = true;
            }
        }catch (XQException ex) {
            transOn = false;
            throw new EPXQJException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPXQJException("Error en recuperar empleat", ex);
        }
        
        return exist;
    }
    
//    
//     /**
//     * Eliminar un empleat i si ens pasen un codi de cap se asigna un nou cap als empleats que esquedarien sense
//     * si es pasa 0 els empleats es queden sense cap.
//     * @param codi
//     * @param actCap
//     * @reutrn boolean
//     * @throws EPXQJException
//     */
//    
//    public boolean eliminarEmpleat(int codi, int actCap){
//        
//        //per saber si el codi es valid abans de fer la petició a la base de dades
//        try{
//            new Empleat(codi, "???", null);
//        }catch(Exception ex){
//            throw new EPXQJException("Codi de empleat erroni", ex);
//        }
//        
//        if(codi == actCap){
//            throw new EPXQJException("Han de ser diferents");
//        }
//        
//        if(actCap < 0){
//            throw new EPXQJException("El codi actCap es erroni");
//        }
//        
//        
//        ClientQuery cq = null;
//        String query = "";
//        try{
//            
//            
//            if(actCap == 0){
//                
//                //S'elimina els atributs cap dels subordinats
//                query = "delete node "+path+"//emp[@cap='e"+codi+"']/@cap";
//                query = query+" , ";
//            
//            }else{
//               
//                if(!this.existeixEmpleat(codi)){
//                    throw new EPXQJException("El Empleat no existex");
//                }
//                
//                if(this.esSubordinatDirecteIndirecte(codi, actCap)){
//                    throw new EPXQJException("El nou cap es subordinat del que s'elimina!");
//                }
//
//                //Això només per quan es segur que es nomes un únic camp el que es modifica
//                //query = "replace value of node "+path+"//emp[@cap='e"+codi+"']/@cap with 'e"+actCap+"'";
//                
//                //Es modifica els atributs cap dels subordinats
//                query = query + "for $n in "+path+"//emp[@cap='e"+codi+"']\n " 
//                    + "return replace value of node $n /@cap with 'e"+actCap+"'\n";
//                query = query+" , ";
//                
//            }
//            
//            
//            query = "delete node "+path+"//emp[@codi='e"+codi+"']";
//            cq = con.query(query);
//            cq.execute();
//            
//            //Actualitzar els empleats en memoria
//            hmEmps.remove(codi);
//            
//            Collection empleats = hmEmps.values();
//            Iterator <Empleat> iteEmps = null;
//            
//            if(actCap == 0){
//                iteEmps = empleats.iterator();
//                while(iteEmps.hasNext()){
//                    Empleat e = iteEmps.next();
//                    if(e.getCap() != null && e.getCap().getCodi() == codi){
//                        e.setCap(null);
//                    }
//                }
//                
//            }else{
//                Empleat nouCap = this.getEmpleat(actCap);
//                
//                //El iterator ha de ser creat abans de modificar el HashMap perque si no no funciona bé
//                iteEmps = empleats.iterator();
//                
//                while(iteEmps.hasNext()){
//                    Empleat e = iteEmps.next();
//                    if(e.getCap() != null && e.getCap().getCodi() == codi){
//                        e.setCap(nouCap);
//                    }
//                }
//            }
//            
//            
//        }catch(Exception ex){
//            throw new EPXQJException("Error en eliminar empleat", ex);
//        }finally{
//            tancarQuery(cq);
//        }
//        
//        return false;
//    }
//    
//    
//    /**
//     * Retorna un boolean si el empleat existeix
//     * @param codi
//     * @reutrn boolean
//     * @throws EPXQJException
//     */
//    
//    public boolean esSubordinatDirecteIndirecte(int cap, int emp){
//        
//        if(!existeixEmpleat(cap) || !existeixEmpleat(emp)){
//            throw new EPXQJException("El empleat o el cap no existeixen a la base de dades");
//        }
//        
//        boolean exist = false;
//        ClientQuery cq = null;
//        try{
//            
//            String getEmp = path+"//emp[@cap='e"+cap+"']/@codi/string()";
//            cq = con.query(getEmp);
//            String emps = cq.execute();
//            
//            
//            
//            if(emps.equals("")){
//                return false;
//            }
//            
//            String [] subordinats = emps.split(System.getProperty("line.separator"));
//           
//            for (String subordinat : subordinats) {
//                if (Integer.parseInt(subordinat.substring(1)) == emp) {
//                    return true;
//                }
//                if (esSubordinatDirecteIndirecte(Integer.parseInt(subordinat.substring(1)), emp)) {
//                    return true;
//                }
//            }
//            
//        } catch (Exception ex) {
//            throw new EPXQJException("Error en recuperar empleat", ex);
//        }finally{
//            tancarQuery(cq);
//        }
//        
//        return exist;
//    }
//    
//    
//    
//    
//    
    
    
    /**
     * Retorna true si sha isnerti
     * Insereix el ultim dins de "departaments"
     * @param codi
     * @reutrn boolean
     * @throws EPBaseXException
     */
    
    public boolean insertDepartament(Departament dept){
        
        //comprobar que el deprtament es valid i que no existeix en la base de dades
        
        
        if(dept.getCodi() <= 0 && dept.getCodi() >= 99){
            throw new EPXQJException("El codi del departament no es vàlid");
        }
        
        if(existeixDept(dept.getCodi())){
            throw new EPXQJException("El departament ja existeix a la base de dades");
        }
        
        
        if(xqpeInsertDept == null){
            String cad = "declare variable $codi external;declare variable $nom external;declare variable $localitat external;";
            
            if(updateVersion.equals("PL")){
                cad = cad+"update insert (<dept codi='{$codi}'><nom>{$nom}</nom>"
                    + "<localitat>{$localitat}</localitat></dept>) "
                    + "into "+path+"//departaments";
            
            }else{
                cad = cad+"insert node (<dept codi='{$codi}'><nom>{$nom}</nom>"
                    + "<localitat>{$localitat}</localitat></dept>) "
                    + "into "+path+"//departaments";
            }
            
            System.out.println("CAD: "+cad);
            try{
                xqpeInsertDept = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeInsertDept.bindString(new QName("codi"), "d"+dept.getCodi(), xqitStr);
            xqpeInsertDept.bindString(new QName("nom"), dept.getNom(), xqitStr);
            xqpeInsertDept.bindString(new QName("localitat"), dept.getLocalitat(), xqitStr);
            
            XQResultSequence xqrs = xqpeInsertDept.executeQuery();
            xqrs.next();
            
            hmDepts.put(dept.getCodi(), dept);
            
            return true;
            
        } catch (XQException ex){
            transOn = false;
            throw new EPXQJException("Error en recuperar empleat", ex);
        } catch (Exception ex) {
            throw new EPXQJException("Error en insertar departament", ex);
        }
        
    }
    
    /**
     * Retorna true ja existeix a la base de dades el departament
     * @param codi
     * @return 
     */
    
    public boolean existeixDept(int codi){
     
        if(codi <= 0 && codi >= 99){
            throw new EPXQJException("El codi del departament no es vàlid");
        }
        
        if(hmDepts.containsKey(codi)){
            return true;
        }
        
        if(xqpeExistexDept == null){
            String cad = "declare variable $codi external;";
            cad = cad+"count("+path+"//dept[@codi=$codi])";
            try{
                xqpeExistexDept = con.prepareExpression(cad);
            } catch(XQException ex){
                transOn = false;
                throw new EPXQJException("Error en crear el prepared expression", ex);
            }
        }
        
        try {
            transOn = true;
            
            xqpeExistexDept.bindString(new QName("codi"), "d"+codi, xqitStr);
            
            XQResultSequence xqrs = xqpeExistexDept.executeQuery();
            xqrs.next();
            
            if(Integer.parseInt(xqrs.getItemAsString(null)) > 0 ){
                return true;
            }

            return false;
            
        } catch (XQException ex){
            transOn = false;
            throw new EPXQJException("Error en recuperar empleat", ex);
        
        } catch (Exception ex) {
            throw new EPXQJException("Error en recuperar el departament", ex);
        }
        
    }
    
    
    
    
    /*METODES PRIVATS*/
    
    private void tancarExpression(XQExpression q){
        try {
            if(q != null){
                q.close();
            }
        } catch (XQException ex) {
            throw new EPXQJException("Error en tancar la connexió", ex);
        }
    }
    
    
    /**
     * Commit en el gestor i tanca la transacció activadd
     */
    public void commit(){
        
        if(transOn && transactional.equals("Y")){
            try {
                con.commit();
                transOn = false;
            } catch (XQException ex) {
                transOn = false;
                throw new EPXQJException("Error en fer commit", ex);
            }
        }
            
    }
    
    
    /**
     * Rollback en el gestor i tanca la transacció activa
     */
    public void rollback(){
        if(transOn && transactional.equals("Y")){
            try {
                con.rollback();
                transOn = false;
            } catch (XQException ex) {
                transOn = false;
                throw new EPXQJException("Error en fer rollback", ex);
            }
        }
    }
    
}
