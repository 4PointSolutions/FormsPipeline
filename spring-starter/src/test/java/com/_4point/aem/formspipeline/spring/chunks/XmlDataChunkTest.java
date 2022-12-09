package com._4point.aem.formspipeline.spring.chunks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataException;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XmlDataChunkTest {
	
	//Helper method
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
    	XmlDataChunk underTest = new XmlDataChunk(xmlBytes);
    	assertTrue(Arrays.equals(xmlBytes, underTest.bytes()));    	
    	
    	XmlDataContext dataContext = underTest.dataContext();
    	assertNotNull(dataContext.getXmlDoc());
    	assertEquals("laptops",dataContext.getXmlDoc().getFirstChild().getNodeName());
    	assertEquals("laptop",dataContext.getXmlDoc().getFirstChild().getChildNodes().item(1).getNodeName());    	
    }
        
    @Test
    void testGetString_simpleXML_returnEmpty() throws Exception{    	
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);
    	XmlDataContext underTest = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
    	Optional<String> actualValue = underTest.getString(TestHelper.NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML);
    	assertEquals("",actualValue.orElseThrow());   
    }
        

    @Test
    void testGetString_simpleXML_FoundRepeatItemUsingAttribute_returnValue() throws Exception{
    	String xpath = "/laptops/laptop[@name='Dell']/price";
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	String EXPECTED_VALUE = "1000";

    	XmlDataContext underTest = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<String> actualValue = underTest.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());   	
    }
    
    @Test
    void testGet_simpleXML_FoundRepeatItemUsingAttribute_returnValue() {
    	String xpath = TestHelper.VALID_XPATH_FOR_SIMPLE_XML;
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	String EXPECTED_VALUE = "1000";

    	XmlDataContext underTest = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<String> actualValue = underTest.get(xpath,String.class);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow()); 
    }
    
    @Test
    void testGet_simpleXML_CallFuncWithNonString_returnEmpty() {
    	String xpath = TestHelper.VALID_XPATH_FOR_SIMPLE_XML;
    	byte[] fileContent = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
    	InputStream xmlStream = new ByteArrayInputStream(fileContent);

    	XmlDataContext underTest = XmlDataChunk.XmlDataContext.initializeXmlDoc(xmlStream);
        Optional<List> actualValue = underTest.get(xpath,List.class);
    	assertEquals(Optional.empty(),actualValue); 
    }

    @Test
    void testGetString_complexXml_FoundRepeatItemUsingIndex_returnValue()throws Exception {
    	XmlDataContext underTest = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);	        
		String xpath = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType";		

    	String EXPECTED_VALUE = "LETTER";
    	
		Optional<String> actualValue = underTest.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
    
    @Test
    void testGetString_simpleXML_throwException()throws Exception {
    	XmlDataContext underTest = getXmlDataContext(TestHelper.SIMPLE_XML_DATA_FILE);
    	
    	String xpath = TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML;    	
    	assertThrows(IllegalArgumentException.class, () -> {
    		underTest.getString(xpath);
    	});    	
    	
    	String xpath2 = TestHelper.BAD_XPATH_EXPRESSION;    	
    	assertThrows(IllegalArgumentException.class, () -> {
    		underTest.getString(xpath2);
    	});    	
    }
    
    @Test
    void testGetString_complexXML_throwException()throws Exception {
    	XmlDataContext underTest = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);
		String xpath3 = TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_COMPLEX_XML;
			        
    	assertThrows(IllegalArgumentException.class, () -> {
    		underTest.getString(xpath3);
    	});      	
    }

    @Test
    void testGetString_complexXML_FoundRepeatItem_ChineseCharacter_returnValue()throws Exception {
    	String EXPECTED_VALUE = "河南自贸试验区郑州片区（郑东）正光北街28号1号楼东3单元10层1001号";
    	
    	XmlDataContext underTest = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE_ASIAN);		
		String xpath = TestHelper.CHINESECHAR_XPATH_EXP_FOR_COMPLEX_XML;
		        
		Optional<String> actualValue = underTest.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());  
    }
    
    @Test
	//Note: There is no space or delimiter between parent text and child text
    void testGetString_complexXML_FoundMixContent_returnAllValue()throws Exception {
    	String EXPECTED_VALUE = "Text from parentText from child"; 
    	
    	XmlDataContext underTest = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);		
		String xpath = TestHelper.MIXCONTENT_XPATH_EXP_FOR_COMPLEX_XML;
		        
		Optional<String> actualValue = underTest.getString(xpath);
    	assertEquals(EXPECTED_VALUE,actualValue.orElseThrow());
    }
    
    @Test
	//Note: There are space due to the formatting in the xml
    void testGetString_complexXml_FoundElementWithChildren_returnAllChildrenValue()throws Exception {
    	String EXPECTED_VALUE = """
    							Some recipent street address
    							Ottawa
    			      			Ontario
    							K2B 2L2
    							Canada
    							""";
    	
    	XmlDataContext underTest = getXmlDataContext(TestHelper.COMPLEX_XML_DATA_FILE);		
		String xpath = TestHelper.HASCHILDREN_XPATH_EXP_FOR_COMPLEX_XML;
		
		Optional<String> actualValue = underTest.getString(xpath);
		assertEquals(EXPECTED_VALUE.replaceAll("\\s+",""), actualValue.get().replaceAll("\\s+",""));
    	//Below will fail due to white space differences
		//assertEquals(EXPECTED_VALUE, actualValue.get());
    }
}
