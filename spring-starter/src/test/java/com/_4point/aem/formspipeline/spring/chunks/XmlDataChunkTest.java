package com._4point.aem.formspipeline.spring.chunks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.XmlDataException;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XmlDataChunkTest {
	
	String xmlDataAsString;
	    
    void assertScenarioReturnIsEmpty(String filePath, String xpath) throws Exception {
    	byte[] fileContent = TestHelper.getFileBytesFromResource(filePath);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
    	Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertTrue(actualValue.isEmpty());
    }
 
	private XmlDataContext getXmlDataContext(String xmlFileName) {
		byte[] fileContent = TestHelper.getFileBytesFromResource(xmlFileName);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
		return xmlDataContext;
	}
    
    @Test
    void testInitializeXmlDoc_throwException() {
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.BAD_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    		
		assertThrows( XmlDataException.class, () -> {
			XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
	    });
    }
        
    @Test
    void testGetString_simpleXML_returnXpath() throws Exception{
    	assertScenarioReturnIsEmpty(TestHelper.SIMPLE_XML_DATA_FILE, TestHelper.BAD_XML_DATA_FILE);
    	assertScenarioReturnIsEmpty(TestHelper.SIMPLE_XML_DATA_FILE, TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML); 
    	assertScenarioReturnIsEmpty(TestHelper.SIMPLE_XML_DATA_FILE, TestHelper.NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML); 
    }
        

    @Test
    void testGetString_simpleXML_FoundItemPrice_returnValue() throws Exception{
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	String EXPECTED_VALUE = "1000";

    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());   	

    }

    @Test
    void testGetString_complexXml_FoundSingleRepeatItem_returnValue()throws Exception {
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);	        
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType";		

    	String EXPECTED_VALUE = "LETTER";
    	
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
    
    @Test
    void testGetString_complexXML_FoundRepeatItem_returnEmpty()throws Exception {
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);
		String xpath = "/Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType";
			        
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertTrue(actualValue.isEmpty());  
    }


    @Test
    void testGetString_complexXml_FoundSingleRepeatItem_ChineseCharacter_returnValue()throws Exception {
    	String EXPECTED_VALUE = "河南自贸试验区郑州片区（郑东）正光北街28号1号楼东3单元10层1001号";
    	
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE_ASIAN);		
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo/Address[1]";
		        
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
}
