package com._4point.aem.formspipeline.spring.chunks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.XmlDataException;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XmlDataChunkTest {
	
	String xmlDataAsString;
	    
    void scenarioReturnXpath(String filePath, String xpath) {
    	byte[] fileContent = TestHelper.getFileBytesFromResource(filePath);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext xmlDataContext;
		try {
			xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
	    	Optional<String> actualValue = xmlDataContext.getString(xpath);
	    	assertEquals(Optional.ofNullable(xpath),actualValue);

		} catch (XmlDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
 
    
    @Test
    void testLoadBadXml_throwException() {
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.BAD_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    		
		assertThrows( XmlDataException.class, () -> {
			XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
	    });
    }
        
    @Test
    void testGetString_simpleXML_returnXpath() {
    	//Bad xpath
    	String xpath = "*laptops*laptop?price";
    	scenarioReturnXpath(TestHelper.SIMPLE_XML_DATA_FILE, xpath);
    	
    	//Found repeat sections value
    	xpath = "/laptops/laptop/price";
    	scenarioReturnXpath(TestHelper.SIMPLE_XML_DATA_FILE, xpath); 
    	
    	//Not found
    	xpath = "/laptops/laptop[@name='Thinkpad']/price";
    	scenarioReturnXpath(TestHelper.SIMPLE_XML_DATA_FILE, xpath); 
    }
        

    @Test
    void testGetString_simpleXML_FoundItemPrice_returnValue() {
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	String EXPECTED_VALUE = "1000";
        
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext xmlDataContext;
		try {
			xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
	        Optional<String> actualValue = xmlDataContext.getString(xpath);
	    	assertEquals(Optional.ofNullable(EXPECTED_VALUE),actualValue);   	

		} catch (XmlDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    @Test
    void testGetString_complexXml_FoundSingleRepeatItem_returnValue() {
		try {
	    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.COMPLEX_XML_DATA_FILE);
	    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
	    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);

	        
			String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType";
			String EXPECTED_VALUE = "LETTER";
			
			Optional<String> actualValue = xmlDataContext.getString(xpath);
	    	assertEquals(Optional.ofNullable(EXPECTED_VALUE),actualValue);  
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
    
    
    @Test
    void testGetString_complexXML_FoundRepeatItem_returnValue() {
		try {
	    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.COMPLEX_XML_DATA_FILE);
	    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
	    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
	    	
			String xpath = "/Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType";
			///List<String> EXPECTED_VALUE = Arrays.asList("LETTER","DOCUMENT");
				        
			Optional<String> actualValue = xmlDataContext.getString(xpath);
	    	assertEquals(Optional.ofNullable(xpath),actualValue);  
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }


    @Test
    void testGetString_complexXml_FoundSingleRepeatItem_ChineseCharacter_returnValue() {
		try {    	
	    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.COMPLEX_XML_DATA_FILE_ASIAN);
	    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
	    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
			
			String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo/Address[1]";
			String EXPECTED_VALUE = "河南自贸试验区郑州片区（郑东）正光北街28号1号楼东3单元10层1001号";
	        
			Optional<String> actualValue = xmlDataContext.getString(xpath);
	    	assertEquals(Optional.ofNullable(EXPECTED_VALUE),actualValue);  
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
    
}
