package com.ag.reporter.server;

import org.dizitart.no2.Nitrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/*
 * 	@author Anar Gasimov
 */

public class DatabaseProvider {
     
    private static final Logger logger = LoggerFactory.getLogger(DatabaseProvider.class);
     
    private static Nitrite database;
    
    private DatabaseProvider() {
    }
 
    public static void close(){ 
        if (database != null){
            try {
            	database.close();
            	database = null;
            } catch (Exception e){
                logger.error("Can't close database.", e);
            }
        }
    }
     
    public static Nitrite get() {
         
        if (database == null){
             
             logger.info("Connecting to Database ...");
               
            try {
                             	
            	// database = Nitrite.builder().disableAutoCompact().filePath("/tmp/reporter.db").openOrCreate();
            	database = Nitrite.builder().disableAutoCompact().openOrCreate(); // in-memory database
                                  
            } catch (Exception e){
                logger.error("Can't open/create database.", e);
            }
        } 
        return database;
    }
     
}