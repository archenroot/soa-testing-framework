/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gibello.zql.ParseException;
import org.gibello.zql.ZInsert;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZUpdate;
import org.gibello.zql.ZqlParser;

/**
 *
 * @author zANGETSu
 */
public class Main {
    public static void main(String[] args){
        try {
            ZqlParser zqlParser = new ZqlParser();
            zqlParser.initParser(new FileInputStream(new File("insert.sql")));
            
            ZStatement zs = zqlParser.readStatement();
            System.out.println("Input statement: " + zs.toString());
            
            if(zs instanceof ZQuery){
                
                System.out.println("Je to select statement");
            } else if (zs instanceof ZInsert){
            System.out.println("Je to insert statement");
            ZInsert zi = (ZInsert) zs;
            Vector columns = zi.getColumns();
            Enumeration colEnum = columns.elements();
            while (colEnum.hasMoreElements()){
                System.out.println("Column: " + colEnum.nextElement());
            }
            
            
            Vector values = zi.getValues();
            Enumeration valEnum = values.elements();
            while (valEnum.hasMoreElements()){
                System.out.println("Value: " + valEnum.nextElement());
            }
            
            
           System.out.println("Table: " + zi.getTable());
           ZQuery tab = zi.getQuery();
                        
        } else if (zs instanceof ZUpdate){
            System.out.println("Je to update statement");
        }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
}
