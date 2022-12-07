package com._4point.aem.formspipeline.spring.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.common.TestHelper;
import com._4point.aem.formspipeline.spring.utils.XsltXmlDataTransformation.XmlTransformationException;

@ExtendWith(MockitoExtension.class)
class XsltXmlDataTransformationTest {
	private final byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private final byte[] xsltBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);
	private byte[] invalidXsltBytes = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
	
	@Mock Transformer mockTransformer;
	@Mock TransformerFactory mockTransformerFactory;
		
	private final String EXPECTED_TRANSFORMED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><laptops>\n"
			+ "	<laptop name=\"Lenonvo\">\n"
			+ "		<price>2000</price>\n"
			+ "		<MEMORY>250GB</MEMORY>\n"
			+ "		<hardDrive>2GB</hardDrive>\n"
			+ "	</laptop>\n"
			+ "	<laptop name=\"Dell\">\n"
			+ "		<price>1000</price>\n"
			+ "		<MEMORY>100GB</MEMORY>\n"
			+ "		<hardDrive value=\"1GB\"/>\n"
			+ "	</laptop>\n"
			+ "	<laptop name=\"Apple\">\n"
			+ "		<price>4000</price>\n"
			+ "		<MEMORY>400GB</MEMORY>\n"
			+ "		<hardDrive>4GB</hardDrive>\n"
			+ "	</laptop>\n"
			+ "</laptops>";
	
	@BeforeEach
	void setUp() throws Exception {
	}

    
    @Test
    void testConstructor_throwException() {
    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
    		new XsltXmlDataTransformation(invalidXsltBytes);
    	});
    	assertTrue(e.getMessage().contains("Failed to instantiate XsltXmlDataTransformation."));
    }
    
    @Test
    void testProcess_throwException() throws TransformerException {
    	Mockito.when(mockTransformerFactory.newTransformer(Mockito.any()))
    			.thenReturn(mockTransformer);
    	Mockito.doThrow(TransformerException.class)
    			.when(mockTransformer)    			
    			.transform(any(Source.class), any(Result.class));    	    	
    	XmlDataChunk xmlChunk = new XmlDataChunk(xmlBytes);
    	    	
    	XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes,mockTransformerFactory);
    	assertThrows(XmlTransformationException.class, () -> {
    		xmlTransformer.process(xmlChunk);
    	});
    }
	
	@Test
	void testProcess_success() {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes);		
		XmlDataChunk data = xmlTransformer.process(new XmlDataChunk(xmlBytes));
		assertThat(Input.fromByteArray(data.bytes()), isIdenticalTo(Input.fromString(EXPECTED_TRANSFORMED_XML)));
	}
}
