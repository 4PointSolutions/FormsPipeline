package com._4point.aem.formspipeline.spring.chunks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataException;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XmlDataChunkTest {
	
	String xmlDataAsString;
	    
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
    void testDataContext() {    	
    	byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	XmlDataChunk xmlDataChunk = new XmlDataChunk(xmlBytes);
    	assertTrue(Arrays.equals(xmlBytes, xmlDataChunk.bytes()));    	
    	
    	XmlDataContext dataContext = xmlDataChunk.dataContext();
    	assertNotNull(dataContext.getXmlDoc());
    	assertEquals("laptops",dataContext.getXmlDoc().getFirstChild().getNodeName());
    	assertEquals("laptop",dataContext.getXmlDoc().getFirstChild().getChildNodes().item(1).getNodeName());    	
    }
        
    @Test
    void testGetString_simpleXML_returnEmpty() throws Exception{    	
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
    	Optional<String> actualValue = xmlDataContext.getString(TestHelper.NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML);
    	assertEquals("",actualValue.orElseThrow());   
    }
        

    @Test
    void testGetString_simpleXML_FoundRepeatItemUsingAttribute_returnValue() throws Exception{
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	String EXPECTED_VALUE = "1000";

    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());   	
    }
    
    @Test
    void testGet_simpleXML_FoundRepeatItemUsingAttribute_returnValue() {
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	String EXPECTED_VALUE = "1000";

    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<String> actualValue = xmlDataContext.get(xpath,String.class);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow()); 
    }
    
    @Test
    @Disabled ("Needs to be fixed, assertion is failing")
    void testGet_simpleXML_ObjectNotString_returnEmpty() {
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	XmlDataContext EXPECTED_VALUE = null;

    	XmlDataContext xmlDataContext = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<XmlDataContext> actualValue = xmlDataContext.get(xpath,XmlDataContext.class);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow()); 
    }

    @Test
    void testGetString_complexXml_FoundRepeatItemUsingIndex_returnValue()throws Exception {
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);	        
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType";		

    	String EXPECTED_VALUE = "LETTER";
    	
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
    
    @Test
    void testGetString_throwException()throws Exception {
    	XmlDataContext simpleXML = getXmlDataContext(TestHelper.SIMPLE_XML_DATA_FILE);
    	
    	String xpath = TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML;    	
    	assertThrows(IllegalArgumentException.class, () -> {
    		simpleXML.getString(xpath);
    	});    	
    	
    	String xpath2 = TestHelper.BAD_XPATH_EXPRESSION;    	
    	assertThrows(IllegalArgumentException.class, () -> {
    		simpleXML.getString(xpath2);
    	});
    	
    	XmlDataContext complexXML = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);
		String xpath3 = "/Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType";
			        
    	assertThrows(IllegalArgumentException.class, () -> {
    		complexXML.getString(xpath3);
    	});      	

    }

    @Test
    void testGetString_complexXML_FoundRepeatItem_ChineseCharacter_returnValue()throws Exception {
    	String EXPECTED_VALUE = "河南自贸试验区郑州片区（郑东）正光北街28号1号楼东3单元10层1001号";
    	
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE_ASIAN);		
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo/Address[1]";
		        
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
    @Test
	//Note: There is no space or delimiter between parent text and child text
    void testGetString_complexXML_FoundMixContent_returnAllValue()throws Exception {
    	String EXPECTED_VALUE = "Text from parentText from child"; 
    	
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);		
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/MixContent";
		        
		Optional<String> actualValue = xmlDataContext.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());
    }
    
    @Test
    @Disabled("Needs to be fixed, assertion is failing")
    void testGetString_complexXml_FoundElementWithChildren_returnAllChildrenValue()throws Exception {
    	String EXPECTED_VALUE = "	            	Some recipent street address\r\n"
    			+ "	            	\r\n"
    			+ "	            	Ottawa\r\n"
    			+ "	            	Ontario\r\n"
    			+ "	            	K2B 2L2\r\n"
    			+ "	            	Canada ";
    	
    	XmlDataContext xmlDataContext = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);		
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo";
		
		Optional<String> actualValue = xmlDataContext.getString(xpath);
		
		System.out.println("actualValue\n" + actualValue.orElseThrow());
		System.out.println("EXPECTED_VALUE\n" + EXPECTED_VALUE);
		//assertTrue(actualValue.toString().contentEquals(new StringBuffer(EXPECTED_VALUE)));
    	//assertEquals(TestHelper.stripWhiteSpace(EXPECTED_VALUE).trim(),TestHelper.stripWhiteSpace(actualValue.orElseThrow().trim()));
		//assertTrue(actualValue.get().compareTo(EXPECTED_VALUE));
		
		assertThat(actualValue).containsSame(EXPECTED_VALUE.trim());
    	
    }
}
