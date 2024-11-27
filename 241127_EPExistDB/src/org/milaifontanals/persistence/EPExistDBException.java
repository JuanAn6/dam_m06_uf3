/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.milaifontanals.persistence;

/**
 *
 * @author Juan Antonio
 */
public class EPExistDBException extends RuntimeException{
    
    public EPExistDBException (String message) {
        super(message);
    }

    public EPExistDBException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
