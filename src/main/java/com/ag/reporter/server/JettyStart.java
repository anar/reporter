package com.ag.reporter.server;

import org.dizitart.no2.mapper.JacksonFacade;
import org.dizitart.no2.mapper.MapperFacade;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.NitriteCollection;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 	@author Anar Gasimov
 */

public class JettyStart {

    public static void main(String[] args) throws Exception {
    	
    	Logger logger = LoggerFactory.getLogger(JettyStart.class);
    	
    	String REST_API_URL = "/api";
    	
    	// Initializing embedded Jetty server    	
        Server server = new Server(8080);        
        
        ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        
        ctx.setContextPath("/");
        ctx.setWelcomeFiles(new String[]{"index.html" });
        server.setHandler(ctx);
        
        // Rest API path
        ServletHolder restServletHolder = ctx.addServlet(ServletContainer.class, REST_API_URL + "/*");
        restServletHolder.setInitOrder(1);
        restServletHolder.setInitParameter("jersey.config.server.provider.packages","com.ag.reporter.service;org.codehaus.jackson.jaxrs");
        restServletHolder.setInitParameter("jersey.config.server.provider.classnames","org.glassfish.jersey.jackson.JacksonFeature");
        
        // Root path, this should be after REST API definition       
        ServletHolder defaultServletHolder = ctx.addServlet(DefaultServlet.class, "/*");
        defaultServletHolder.setInitParameter("dirAllowed","true");                      
        defaultServletHolder.setInitParameter("resourceBase", ClassLoader.getSystemClassLoader().getResource("static").toExternalForm());       
        defaultServletHolder.setInitParameter("pathInfoOnly","true");

        ErrorPageErrorHandler errorMapper = new ErrorPageErrorHandler();
        errorMapper.addErrorPage(404,"/");
        ctx.setErrorHandler(errorMapper);
                
        InputStream resourceStream = null;
        
        try {
        	
        	// Reading sample data from file
        	resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream("reports.json");
        	
        	if (resourceStream != null) {
    			           
            	// Populating the database with sample data if empty
            	NitriteCollection collection = DatabaseProvider.get().getCollection("reports");
            	collection.createIndex("id", IndexOptions.indexOptions(IndexType.Unique));
            	
            	if (collection.size() == 0) {
            	
            		// Converting sample data into separate documents
            		JSONParser jsonParser = new JSONParser();
            	
            		JSONObject jsonObject = (JSONObject)jsonParser.parse(
                	      new InputStreamReader(resourceStream, "UTF-8"));
                
            		resourceStream.close();
            		                
            		JSONArray reports = (JSONArray) jsonObject.get("elements");
               
            		MapperFacade facade = new JacksonFacade();
       		
            		// Inserting documents into DB
            		for(Object report: reports){
            		    if (report instanceof JSONObject) {
            		    	collection.insert(facade.parse(report.toString())); // parse JSON to document
            		    }
            		}
            	}
                                
    		} else {
    			logger.error("Couldn't find the sample data file.");
    		}
        	
        	// Starting the embedded server
            server.start();
            server.join();
            
            logger.info("Server is running. Serving REST API at " + REST_API_URL);
            
        } catch (Exception ex) {
            logger.error("Error intializing the server: ", ex);
        } finally {
        	DatabaseProvider.close();
            server.destroy();
        }
    }
}