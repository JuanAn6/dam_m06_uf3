/*
 * Programa: Emp.java
 * Objectiu: Classe per gestionar empleats (codi, cognom, ofici, dataAlta,
 *                                          salari, comissio, cap, dept).
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.empresa;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Classe per gestionar empleats (codi, cognom, ofici, dataAlta, salari,
 * comissio, cap, dept). Codi i cognom obligatoris. La resta, optatius.
 */
public class Empleat implements Comparable<Empleat> {

    private short codi;
    private String cognom;
    private String ofici;
    private Calendar dataAlta;
    private Double salari;
    private Double comissio;
    private Empleat cap;
    private Departament dept;

    /**
     * Constructor
     *
     * @param pCodi Codi de l'empleat. Obligatori. Entre 1 i 9999
     * @param pCognom Cognom de l'empleat. Obligatori
     * @param pOfici Ofici de l'empleat. Pot valer null.
     * @param pDataAlta Data d'alta de l'empleat. Pot valer null.
     * @param pSalari Salari de l'empleat. Pot valer null.
     * @param pComissio Comissio de l'empleat. Pot valer null.
     * @param pCap Cap de l'empleat. Pot valer null.
     * @param pDept Departament de l'empleat. Pot valer null.
     */
    public Empleat(int pCodi, String pCognom, String pOfici, Calendar pDataAlta, Double pSalari, Double pComissio,
            Empleat pCap, Departament pDept) {
        setCodi(pCodi);
        setCognom(pCognom);
        setOfici(pOfici);
        setDataAlta(pDataAlta);
        setSalari(pSalari);
        setComissio(pComissio);
        setCap(pCap);
        setDept(pDept);
    }

    /**
     * Constructor
     *
     * @param pCodi Codi de l'empleat. Obligatori
     * @param pCognom Cognom de l'empleat. Obligatori
     * @param pDept Codi del departament de l'empleat. Optatiu.
     */
    public Empleat(int pCodi, String pCognom, Departament pDept) {
        this(pCodi, pCognom, null, null, null, null, null, pDept);
    }

    /**
     * Canvia el codi d'empleat
     *
     * @param newCodi Nou codi de l'empleat
     */
    public final void setCodi(int newCodi) {
        if (newCodi < 1 || newCodi > 9999) {
            throw new RuntimeException("L'empleat ha de tenir codi entre 1 i 9999");
        }
        codi = (short) newCodi;
    }

    /**
     * Canvia el cognom d'empleat
     *
     * @param newCognom Nou cognom de l'empleat
     */
    public final void setCognom(String newCognom) {
        if (newCognom == null || newCognom.length() == 0) {
            throw new RuntimeException("L'empleat ha de tenir cognom obligatòriament");
        }
        cognom = newCognom;
    }

    /**
     * Canvia l'ofici d'empleat
     *
     * @param newOfici Nou ofici de l'empleat
     */
    public final void setOfici(String newOfici) {
        ofici = newOfici;
    }

    /**
     * Canvia la data d'alta d'empleat
     *
     * @param newDataAlta Nova data d'alta de l'empleat
     */
    public final void setDataAlta(Calendar newDataAlta) {
        if (newDataAlta == null) {
            dataAlta = null;
        } else {
            dataAlta = (Calendar) newDataAlta.clone();
        }
    }

    /**
     * Canvia el salari d'empleat
     *
     * @param newSalari Nopu salari de l'empleat
     */
    public final void setSalari(Double newSalari) {
        if (newSalari != null && newSalari < 0) {
            throw new RuntimeException("El salari d'empleat no pot ser negatiu");
        }
        salari = newSalari;
    }

    /**
     * Canvia la comisió d'empleat
     *
     * @param newComissio Nova comissió de l'empleat
     */
    public final void setComissio(Double newComissio) {
        if (newComissio != null && newComissio < 0) {
            throw new RuntimeException("La comissió d'empleat no pot ser negativa");
        }
        comissio = newComissio;
    }

    /**
     * Canvia el cap d'empleat
     *
     * @param newCap Nou cap de l'empleat
     */
    public final void setCap(Empleat newCap) {
        if (newCap != null && newCap.equals(cap)) {
            throw new RuntimeException("Un empleat no pot ser cap de si mateix");
        }
        /* Comprovació que la jerarquia és correcta */
        if (newCap != null) {
            Empleat aux = newCap.getCap();
            while (aux != null) {
                if (aux.equals(this)) {
                    throw new RuntimeException("Jerarquia de caps errònia!!!");
                }
                aux = aux.getCap();
            }
        }
        cap = newCap;
    }

    /**
     * Canvia el departament d'empleat
     *
     * @param newDept Nou departament de l'empleat
     */
    public final void setDept(Departament newDept) {
        dept = newDept;
    }

    /**
     * Recupera el codi
     *
     * @return String Codi
     */
    public int getCodi() {
        return codi;
    }

    /**
     * Recupera el cognom
     *
     * @return String Cognom
     */
    public String getCognom() {
        return cognom;
    }

    /**
     * Recupera l'ofici
     *
     * @return String Ofici. Valor null si no té ofici
     */
    public String getOfici() {
        return ofici;
    }

    /**
     * Recupera la data d'alta
     *
     * @return Calendar Data d'alta. Valor null si no té data d'alta
     */
    public Calendar getDataAlta() {
        return dataAlta;
    }

    /**
     * Recupera el salari
     *
     * @return String Salari. Valor null si no té salari
     */
    public Double getSalari() {
        return salari;
    }

    /**
     * Recupera la comissió
     *
     * @return String Comissió. Valor null si no té comissió
     */
    public Double getComissio() {
        return comissio;
    }

    /**
     * Recupera el departament
     *
     * @return Departament de l'empleat. Valor null si no té departament
     */
    public Departament getDept() {
        return dept;
    }

    /**
     * Recupera el cap
     *
     * @return Cap de l'empleat. Valor null si no té cap
     */
    public Empleat getCap() {
        return cap;
    }

    public String toString() {
        String aux = "Emp.: " + codi + " - " + cognom;
        if (ofici != null) {
            aux = aux + " -  Ofici: " + ofici;
        }
        if (dataAlta != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            aux = aux + " - Data Alta: " + sdf.format(dataAlta.getTime());
        }
        if (salari != null) {
            aux = aux + " - Salari: " + salari;
        }
        if (comissio != null) {
            aux = aux + " - Comissió: " + comissio;
        }
        if (cap != null) {
            aux = aux + " - Cap: " + cap.getCognom();
        }
        if (dept != null) {
            aux = aux + " - Departament: " + dept.getNom();
        }
        return aux;
    }

    /**
     * Igualtat d'empleats en base a igualtat dels seus codis
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Empleat) {
            return codi == ((Empleat) obj).codi;
        }
        return false;
    }

    @Override
    public int compareTo(Empleat o) {
        return Short.valueOf(codi).compareTo(Short.valueOf(o.codi));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.codi;
        return hash;
    }

}
