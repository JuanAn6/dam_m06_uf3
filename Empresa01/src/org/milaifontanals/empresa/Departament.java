/*
 * Programa: Dept.java
 * Objectiu: Classe per gestionar departaments (codi, nom, localitat) i els seus
 *           empleats
 * Autor...: Isidre Guixà
 */
package org.milaifontanals.empresa;

import java.io.PrintStream;

/**
 * Classe per gestionar departaments (codi, nom, localitat) sense els seus
 * empleats. Codi i nom són obligatoris. Localitat és optatiu.
 */
public class Departament implements Comparable<Departament> {

    private byte codi;
    private String nom;
    private String localitat;

    /**
     * Constructor
     *
     * @param pCodi Codi del departament. Obligatori. Entre 1 i 255
     * @param pNom Nom del departament. Obligatori
     * @param pLocalitat Localitat del departament. Pot valer null.
     */
    public Departament(int pCodi, String pNom, String pLocalitat) {
        setCodi(pCodi);
        setNom(pNom);
        setLocalitat(pLocalitat);
    }

    /**
     * Canvia el codi a un departament. Els empleats del departament són
     * reassignats al nou codi de departament.
     *
     * @param newCodi Nou codi de departament
     */
    public final void setCodi(int newCodi) {
        if (newCodi < 1 || newCodi > 99) {
            throw new RuntimeException("El departament ha de tenir codi entre 1 i 99");
        }
        codi = (byte) newCodi;
    }

    /**
     * Canvia el nom a un departament.
     *
     * @param newNom Nou nom de departament
     */
    public final void setNom(String newNom) {
        if (newNom == null || newNom.length() == 0) {
            throw new RuntimeException("El departaments ha de tenir nom obligatòriament");
        }
        nom = newNom;
    }

    /**
     * Canvia la localitat a un departament.
     *
     * @param newLocalitat Nou codi de localitat
     */
    public final void setLocalitat(String newLocalitat) {
        localitat = newLocalitat;
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
     * Recupera el nom
     *
     * @return String Nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Recupera la localitat
     *
     * @return String Localitat. Valor null si no té localitat
     */
    public String getLocalitat() {
        return localitat;
    }

    @Override
    public String toString() {
        String aux = "Dept: " + codi + " - " + nom;
        if (localitat != null) {
            aux = aux + " - Localitat: " + localitat;
        }
        return aux;
    }

    /**
     * Igualtat de departaments en base a igualtat dels seus codis
     *
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Departament) {
            return codi == ((Departament) obj).codi;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.codi;
        return hash;
    }



    public void visualitzar(PrintStream ps) {
        ps.println("\tCodi:");
    }

    @Override
    public int compareTo(Departament o) {
        return (Byte.valueOf(codi).compareTo(Byte.valueOf(o.codi)));
    }
    
}
