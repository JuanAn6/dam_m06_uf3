/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.empresa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Usuari
 */
public class Empresa {

    private String nom;     // Obligatòria
    private Calendar dataCreacio;   // Obligatòria

    public Empresa(String nom, Calendar dataCreacio) {
        setNom(nom);
        setDataCreacio(dataCreacio);
    }

    public String getNom() {
        return nom;
    }

    public final void setNom(String nom) {
        if (nom == null || nom.length() == 0) {
            throw new RuntimeException("Nom de l'empresa obligatori i no buit");
        }
        this.nom = nom;
    }

    public Calendar getDataCreacio() {
        return dataCreacio;
    }

    public final void setDataCreacio(Calendar dataCreacio) {
        if (dataCreacio == null) {
            throw new RuntimeException("Data de creació obligatòria");
        }
        this.dataCreacio = (Calendar) dataCreacio.clone();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return "Empresa{" + "nom=" + nom + ", dataCreacio=" + sdf.format(dataCreacio.getTime()) + '}';
    }

}
