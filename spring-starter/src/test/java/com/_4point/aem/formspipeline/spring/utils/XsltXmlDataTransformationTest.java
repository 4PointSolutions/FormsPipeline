package com._4point.aem.formspipeline.spring.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.spring.common.TestHelper;

class XsltXmlDataTransformationTest {
	private final byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private final byte[] xsltBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);
	private byte[] invalidXsltBytes = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
		
	private final String EXPECTED_TRANSFORMED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><laptops>\r\n"
			+ "	<laptop name=\"Lenonvo\">\r\n"
			+ "		<price>2000</price>\r\n"
			+ "		<MEMORY>250GB</MEMORY>\r\n"
			+ "		<hardDrive>2GB</hardDrive>\r\n"
			+ "	</laptop>\r\n"
			+ "	<laptop name=\"Dell\">\r\n"
			+ "		<price>1000</price>\r\n"
			+ "		<MEMORY>100GB</MEMORY>\r\n"
			+ "		<hardDrive value=\"1GB\"/>\r\n"
			+ "	</laptop>\r\n"
			+ "	<laptop name=\"Apple\">\r\n"
			+ "		<price>4000</price>\r\n"
			+ "		<MEMORY>400GB</MEMORY>\r\n"
			+ "		<hardDrive>4GB</hardDrive>\r\n"
			+ "	</laptop>\r\n"
			+ "</laptops>";
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testTransform_throwException()  {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(invalidXsltBytes,xmlBytes);		
		
		InputStream xmlDoc = TestHelper.getFileFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
		Source source = new StreamSource(xmlDoc);
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 				
		assertThrows( TransformerException.class, () -> {
			xmlTransformer.transform(source, output);
		});	
	}
	
	@Test
	void testTransform_success()  {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes,xmlBytes);	
		
		InputStream xmlDoc = TestHelper.getFileFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
		Source source = new StreamSource(xmlDoc);
		ByteArrayOutputStream output = new ByteArrayOutputStream(); 		
		
		try {			
			xmlTransformer.transform(source, output);
			String transformedXML = new String(output.toByteArray(), StandardCharsets.UTF_8);
			assertEquals(EXPECTED_TRANSFORMED_XML,transformedXML);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
	}
	
	@Test
	void testProcess_success() {
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytes,xmlBytes);		
		
		//Something not right here, needs to be re-factored.
		DataChunk<Context> data = xmlTransformer.process(xmlTransformer);
		assertEquals(EXPECTED_TRANSFORMED_XML,new String(data.bytes(), StandardCharsets.UTF_8));
	}
}
