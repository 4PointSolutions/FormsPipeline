package com._4point.aem.formspipeline.spring.chunks;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunkImpl.XmlDataContextImpl;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataException;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XmlDataChunkImplTest {
	
    @DisplayName("Should throw exception when it encounters bad XML.")
    @Test
    void testInitializeXmlDoc_throwException() {
    	String badXmlDataFile = TestHelper.BAD_XML_DATA_FILE;
		byte[] fileContent = TestHelper.getFileBytesFromResource(badXmlDataFile);
		XmlDataException ex = assertThrows(XmlDataException.class, ()->XmlDataContextImpl.initializeXmlDoc(fileContent));
		String msg = ex.getMessage();
		assertThat(msg, containsStringIgnoringCase("failed to create XmlDataContext"));
    }
    
    // These tests use the "Tester" pattern, as outlined here: http://testerpattern.nl/
    @DisplayName("Should return empty() if data is not found.")
    @Test
    void testGetString_simpleXML_returnEmpty() throws Exception{    	
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML)
    			.contextShouldReturnEmpty();
    }
        

    @DisplayName("Should be able to return element data.")
    @Test
    void testGetString_simpleXML_FoundRepeatItemUsingAttribute_returnValue() throws Exception{
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath("/laptops/laptop[@name='Dell']/price")
    			.contextShouldContain("1000");
    }
    
    @DisplayName("Should be able to return attribute data.")
    @Test
    void testGetString_simpleXML_returnAttribute() throws Exception{
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath("/laptops/laptop[1]/@name")
    			.contextShouldContain("Lenonvo");
    }
    
    @DisplayName("Data from a previous context should be available.")
    @Test
    void testGetString_simpleXML_returnDataFromPreviousContext() throws Exception{
       	String prevKey = "some_key_value";
       	String EXPECTED_VALUE = "some_value";

       	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.withPrevContextContaining(prevKey, EXPECTED_VALUE)
    			.create()
    			.forXpath(prevKey)
    			.contextShouldContain(EXPECTED_VALUE);
    }
    
    @DisplayName("Data from current context should be have precendence over data in previous context with the same key.")
    @Test
    void testGetString_simpleXML_returnCorrectAttribute() throws Exception {
    	String xpath = "/laptops/laptop[1]/@name";
    	String EXPECTED_VALUE = "Lenonvo";
    	String NOT_EXPECTED_VALUE = "NotLenonvo";

    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.withPrevContextContaining(xpath, NOT_EXPECTED_VALUE)
    			.create()
    			.forXpath(xpath)
    			.contextShouldContain(EXPECTED_VALUE);
    }
    
    @DisplayName("Data from new context should be have precendence over data in current context with the same key when createed from another XmlDataChunk.")
    @Test
    void testGetString_createFrom_simpleXML() throws Exception {
    	String xpath = "/laptops/laptop[1]/@name";
    	String NOT_EXPECTED_VALUE = "Lenonvo";
    	String EXPECTED_VALUE = "NotLenonvo";
    	XmlDataChunk chunk = XmlDataChunk.create(TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE));
    	// validate starting condition
    	String origValue = chunk.dataContext().getString(xpath).orElseThrow();
    	assertEquals(NOT_EXPECTED_VALUE, origValue);
    	
    	// update the chunk's context
    	XmlDataChunk updatedChunk = chunk.updateContext(createSingletonContext(xpath, EXPECTED_VALUE));
    	
    	// validate that the value has changed.
    	String resultValue = updatedChunk.dataContext().getString(xpath).orElseThrow();
    	assertEquals(EXPECTED_VALUE, resultValue);
    }

    @Test
    void testGet_simpleXML_FoundRepeatItemUsingAttribute_returnValue() {
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.VALID_XPATH_FOR_SIMPLE_XML)
    			.contextShouldContain("1000");
    }
    
    @Test
    void testGet_simpleXML_CallFuncWithNonString_returnEmpty() {
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.VALID_XPATH_FOR_SIMPLE_XML)
    			.contextListShouldReturnEmpty();;
    }

    @Test
    void testGetString_complexXml_FoundRepeatItemUsingIndex_returnValue()throws Exception {
    	tester().withXmlLocation(TestHelper.COMPLEX_XML_DATA_FILE)
    			.create()
    			.forXpath("/Output/XMLInvoices/XMLInvoice[1]/DriverSection/DocumentType")
    			.contextShouldContain("LETTER");
    }
    
    
    @Test
    void testGetString_simpleXML_repeatedSection_throwException()throws Exception {
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML)
    			.shouldThrowEx(IllegalArgumentException.class);
    }

    @Test
    void testGetString_simpleXML_badXpath_throwException()throws Exception {
    	tester().withXmlLocation(TestHelper.SIMPLE_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.BAD_XPATH_EXPRESSION)
    			.shouldThrowEx(IllegalArgumentException.class);
    }
    
    @Test
    void testGetString_complexXML_throwException()throws Exception {
    	tester().withXmlLocation(TestHelper.COMPLEX_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.REPEAT_SECTION_XPATH_EXP_FOR_COMPLEX_XML)
    			.shouldThrowEx(IllegalArgumentException.class);
    }

    @Test
    void testGetString_complexXML_FoundRepeatItem_ChineseCharacter_returnValue()throws Exception {
    	String EXPECTED_VALUE = "河南自贸试验区郑州片区（郑东）正光北街28号1号楼东3单元10层1001号";
    	
    	tester().withXmlLocation(TestHelper.COMPLEX_XML_DATA_FILE_ASIAN)
    			.create()
    			.forXpath(TestHelper.CHINESECHAR_XPATH_EXP_FOR_COMPLEX_XML)
    			.contextShouldContain(EXPECTED_VALUE);
    }
    
    @Test
	//Note: There is no space or delimiter between parent text and child text
    void testGetString_complexXML_FoundMixContent_returnAllValue()throws Exception {
    	tester().withXmlLocation(TestHelper.COMPLEX_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.MIXCONTENT_XPATH_EXP_FOR_COMPLEX_XML)
    			.contextShouldContain("Text from parentText from child");
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
    	
    	tester().withXmlLocation(TestHelper.COMPLEX_XML_DATA_FILE)
    			.create()
    			.forXpath(TestHelper.HASCHILDREN_XPATH_EXP_FOR_COMPLEX_XML)
    			.contextShouldContainIgnoringWhitespace(EXPECTED_VALUE);
    }

	private static Context createSingletonContext(String prevKey, String value) {
		return new Context() {	// Simple implementation of Context that has only one value in it.
			@SuppressWarnings("unchecked")
			public <T> Optional<T> get(String key, Class<T> target) {
				return String.class.equals(target) && prevKey.equals(key) ? Optional.of((T)value) : Optional.empty();
			}
       	};
	}
	
    // These tests use the "Tester" pattern, as outlined here: http://testerpattern.nl/
    private CreateMethodTester tester() {
    	return new CreateMethodTester();
    }

    private static class CreateMethodTester {
    	private String xmlLocation;
    	private Context prevContext;
    	
    	CreateMethodTester withXmlLocation(String xmlLocation) {
    		this.xmlLocation = xmlLocation;
    		return this;
    	}
    	
    	CreateMethodTester withPrevContextContaining(String prevKey, String value) {
           	this.prevContext = createSingletonContext(prevKey, value);
    		return this;
    	}

    	Asserter create() {
    		if (xmlLocation != null && prevContext == null) {
    			return new Asserter(XmlDataChunk.create(TestHelper.getFileBytesFromResource(xmlLocation)));
    		} else if (xmlLocation != null && prevContext != null) {
    			return new Asserter(XmlDataChunk.create(TestHelper.getFileBytesFromResource(xmlLocation), prevContext));
    		} else {
    			throw new IllegalArgumentException("Bad argument combination xmlLocation=%s, prevContext=%s".formatted(xmlLocation, prevContext));
    		}
    	}
    	
    	private static class Asserter {
    		private final XmlDataChunk result;
    		private String xpath;

			public Asserter(XmlDataChunk result) {
				this.result = result;
			}
    		
			Asserter forXpath(String xpath) {
				this.xpath = xpath;
				return this;
			}
			
	    	<E extends Exception> void shouldThrowEx(Class<E> ex) {
	    		checkXPath();
	        	assertThrows(ex, ()->result.dataContext().getString(xpath));      	

	    	}

	    	void contextShouldContainIgnoringWhitespace(String expectedValue) {
	    		checkXPath();
    			String actualValue = result.dataContext().getString(xpath).orElseThrow();
    			assertEquals(expectedValue.replaceAll("\\s+",""), actualValue.replaceAll("\\s+",""));
    		}

    		void contextShouldContain(String expectedValue) {
	    		checkXPath();
    			String actualValue = result.dataContext().getString(xpath).orElseThrow();
    			assertEquals(expectedValue, actualValue);
    		}
    		
    		void contextShouldReturnEmpty() {
	    		checkXPath();
    			Optional<String> actualValue = result.dataContext().getString(xpath);
	    		assertTrue(actualValue.isEmpty());
	    	}

    		void contextListShouldReturnEmpty() {
	    		checkXPath();
    			Optional<List> value = result.dataContext().get(xpath, List.class);
				assertTrue(value.isEmpty(), ()->"Expected Optional to be empty but it wasn't.");
    			
    		}
    		private void checkXPath() {
    			if (xpath == null) { throw new IllegalArgumentException("You forgot to set the xpath before caling the final assertion."); }
    		}
    	}
    }
}
