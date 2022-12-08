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

import net.sf.saxon.TransformerFactoryImpl;

@ExtendWith(MockitoExtension.class)
class XsltXmlDataTransformationTest {
	private final byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private final byte[] xsltBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);
	private final byte[] xsltBytesV2 = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLTV2_DATA_FILE);
	private byte[] invalidXsltBytes = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
	
	@Mock Transformer mockTransformer;
	@Mock TransformerFactoryImpl mockTransformerFactory;

	private final String EXPECTED_ELEMENT_NAME_CHANGED_XML = """
			<?xml version="1.0" encoding="UTF-8"?>
			<laptops>
				<laptop name="Lenonvo">
					<price>2000</price>
					<MEMORY>250GB</MEMORY>
					<hardDrive>2GB</hardDrive>
				</laptop>
				<laptop name="Dell">
					<price>1000</price>
					<MEMORY>100GB</MEMORY>
					<hardDrive value="1GB"/>
				</laptop>
				<laptop name="Apple">
					<price>4000</price>
					<MEMORY>400GB</MEMORY>
					<hardDrive>4GB</hardDrive>
				</laptop>
			</laptops>
			""";
	
	private final String EXPECTED_SORTED_XML ="""
<?xml version="1.0" encoding="UTF-8"?><laptops xmlns:xs="http://www.w3.org/2001/XMLSchema"><laptop name="Apple">
		<price>4000</price>
		<ram>400GB</ram>
		<hardDrive>4GB</hardDrive>
	</laptop><laptop name="Dell">
		<price>1000</price>
		<ram>100GB</ram>
		<hardDrive value="1GB"/>
	</laptop><laptop name="Lenonvo">
		<price>2000</price>
		<ram>250GB</ram>
		<hardDrive>2GB</hardDrive>
	</laptop></laptops>
			"""; 	
	
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
		assertThat(Input.fromByteArray(data.bytes()), isIdenticalTo(Input.fromString(EXPECTED_ELEMENT_NAME_CHANGED_XML)));
	}
	
	@Test
	void testProcess_xsltVersion20_success() {
		//xsl:perform-sort available in XSLT 2.0 and newer
		XsltXmlDataTransformation xmlTransformer = new XsltXmlDataTransformation(xsltBytesV2);		
		XmlDataChunk data = xmlTransformer.process(new XmlDataChunk(xmlBytes));
		assertThat(Input.fromByteArray(data.bytes()), isIdenticalTo(Input.fromString(EXPECTED_SORTED_XML)));
	}
}
