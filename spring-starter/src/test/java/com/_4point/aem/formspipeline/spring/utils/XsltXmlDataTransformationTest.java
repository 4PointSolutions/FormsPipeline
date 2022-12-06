package com._4point.aem.formspipeline.spring.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.XmlDataException;
import com._4point.aem.formspipeline.XmlTransformationException;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XsltXmlDataTransformationTest {
	private final byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private final byte[] xsltBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);
	private byte[] invalidXsltBytes = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
		
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
	void testTransform_throwException()  {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(invalidXsltBytes);		  
		
		InputStream xmlDoc = TestHelper.getFileFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
		Source source = new StreamSource(xmlDoc);
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 				
		assertThrows( TransformerException.class, ()->xmlTransformer.transform(source, output));	
	}
	
	@Test
	void testTransform_success() throws Exception  {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes);	
		
		InputStream xmlDoc = TestHelper.getFileFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
		Source source = new StreamSource(xmlDoc);
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 		
		
		xmlTransformer.transform(source, output);
		String transformedXML = new String(output.toByteArray(), StandardCharsets.UTF_8);
		assertThat(transformedXML, isIdenticalTo(Input.fromString(EXPECTED_TRANSFORMED_XML)));
	}
	
    
    @Test
    void testProcess_throwException() {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(invalidXsltBytes);		
		
		assertThrows( XmlTransformationException.class, () -> {
			xmlTransformer.process(new XmlDataChunk(xmlBytes));
	    });    
    }
	
	@Test
	void testProcess_success() {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes);		
		XmlDataChunk data = xmlTransformer.process(new XmlDataChunk(xmlBytes));
		assertThat(Input.fromByteArray(data.bytes()), isIdenticalTo(Input.fromString(EXPECTED_TRANSFORMED_XML)));
	}
}
