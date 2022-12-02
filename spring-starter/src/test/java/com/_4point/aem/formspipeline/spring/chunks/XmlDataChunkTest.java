package com._4point.aem.formspipeline.spring.chunks;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class XmlDataChunkTest {
	
	String SIMPLE_XML_DATA_FILE = "/xml/laptop.xml";
	String COMPLEX_XML_DATA_FILE = "/xml/English/R42565_FB0470022B_16161694_T2_4.xml";
	String xmlDataAsString;
	
	XmlDataChunk xmlDataChunk;
	
    @BeforeEach
    protected void setUp() {
		try {
			
			xmlDataChunk = new XmlDataChunk();
					
			ClassPathResource resource = new org.springframework.core.io.ClassPathResource(SIMPLE_XML_DATA_FILE);
			List<String> content = Files.readAllLines(Paths.get(
					resource.getURI()), Charset.defaultCharset());
                            
			String s = content.stream().map(e -> e.toString()).reduce("", String::concat);
			System.out.println("XML data\n\n" + s);
			xmlDataChunk.loadXMLFromString(s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
           	
    }
    
    @Test
    void testGetDataForXpath_simpleXML_FoundItemPrice_return1000() {
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	List<String> EXPECTED_VALUE = Arrays.asList("1000");
        
        List<String> actualValue = xmlDataChunk.getXmlDataValue(xpath);
    	System.out.println("actualValue " + actualValue);
    	assertEquals(EXPECTED_VALUE,actualValue);   	
    }
    
    @Test
    void testGetDataForXpath_simpleXML_FoundAllInstance_returnList() {
    	String xpath = "/laptops/laptop/price";
    	List<String> EXPECTED_VALUE = Arrays.asList("2000","1000","4000");
        
        List<String> actualValue = xmlDataChunk.getXmlDataValue(xpath);
    	System.out.println("actualValue " + actualValue);
    	assertEquals(EXPECTED_VALUE,actualValue);   	
    }
    
    @Test
    void testGetDataForXpath_simpleXML_NotFound_returnEmpty() {
    	String xpath = "/laptops/laptop[@name='Thinkpad']/price";
    	List<String> EXPECTED_VALUE = Arrays.asList(xpath); 
        
        List<String> actualValue = xmlDataChunk.getXmlDataValue(xpath);
    	System.out.println("actualValue " + actualValue);
    	assertEquals(EXPECTED_VALUE,actualValue);   	
    }
    
    @Test
    void testGetDataForXpath_complexXML_FoundRepeatItem_returnValue() {
    	XmlDataChunk complexXmlDataChunk = new XmlDataChunk();
		
		ClassPathResource resource = new org.springframework.core.io.ClassPathResource(COMPLEX_XML_DATA_FILE);
		List<String> content;
		try {
			content = Files.readAllLines(Paths.get(resource.getURI()), Charset.defaultCharset());
			String s = content.stream().map(e -> e.toString()).reduce("", String::concat);
			System.out.println("\n\nXML data\n" + s);
			complexXmlDataChunk.loadXMLFromString(s);
			
			String xpath = "/E1_Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType_4";
			List<String> EXPECTED_VALUE = Arrays.asList("INVS01","INVS01","INVS01","INVS01");
	        
	        List<String> actualValue = complexXmlDataChunk.getXmlDataValue(xpath);
	    	System.out.println("testComplexXml_FoundItem_returnValue actualValue " + actualValue);
	    	assertEquals(EXPECTED_VALUE,actualValue);  
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    @Test
    void testComplexXml_FoundSingleRepeatItem_returnValue() {
    	XmlDataChunk complexXmlDataChunk = new XmlDataChunk();
		
		ClassPathResource resource = new org.springframework.core.io.ClassPathResource(COMPLEX_XML_DATA_FILE);
		List<String> content;
		try {
			content = Files.readAllLines(Paths.get(resource.getURI()), Charset.defaultCharset());
			String s = content.stream().map(e -> e.toString()).reduce("", String::concat);
			System.out.println("\n\nXML data\n" + s);
			complexXmlDataChunk.loadXMLFromString(s);
			
			String xpath = "/E1_Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType_4";
			List<String> EXPECTED_VALUE = Arrays.asList("INVS01");
	        
	        List<String> actualValue = complexXmlDataChunk.getXmlDataValue(xpath);
	    	System.out.println("testComplexXml_FoundItem_returnValue actualValue " + actualValue);
	    	assertEquals(EXPECTED_VALUE,actualValue);  
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    
    

}
