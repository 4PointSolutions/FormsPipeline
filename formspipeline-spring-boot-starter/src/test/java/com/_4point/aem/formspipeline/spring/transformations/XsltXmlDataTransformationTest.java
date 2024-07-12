package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.payloads.XmlPayload;
import com._4point.aem.formspipeline.spring.common.TestHelper;
import com._4point.aem.formspipeline.spring.transformations.XsltNonXmlDataTransformation.XmlTransformationException;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * The tests in this class are a bit more extensive than the amount of code in the class under test would 
 * seem to warrant.  This is because a) the class under test used to be bigger and b) this class is the 
 * most common use case.
 * 
 * The end result is that this class tests not only XsltXmlDataTransformation but also tests
 * XsltNonXmlDataTransformation as well.
 *
 */
@ExtendWith(MockitoExtension.class)
class XsltXmlDataTransformationTest {
	private static final byte[] XML_BYTES = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private static final byte[] XSLT_BYTES = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);	
		
	private static final byte[] XSLT_BYTES_V21 = TestHelper.getFileBytesFromResource(TestHelper.XSLTV21_DATA_FILE);

	private static final byte[] XML_BYTES_V20 = TestHelper.getFileBytesFromResource(TestHelper.XSLTV20_DATA_FILE);
	private static final byte[] XSLT_BYTES_V20 = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLTV20_DATA_FILE);
	
	private static final byte[] INVALID_XSLT_BYTES = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
	private static final byte[] XSLT_INCLUDE_BYTES = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_INCLUDE_FILE);
		
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
	
	private final String EXPECTED_XSLT20_TABLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><table><tr><th>Position</th><th>Country</th><th>City List</th><th>Population</th></tr><tr><td>1</td><td>Italia</td><td>Milano, Venezia</td><td>6</td></tr><tr><td>2</td><td>France</td><td>Paris, Lyon</td><td>9</td></tr><tr><td>3</td><td>Deutschland</td><td>Munchen</td><td>4</td></tr></table>";
	    
    @Test
    void testConstructor_throwException() {
    	Exception e = assertThrows(IllegalArgumentException.class, () -> {
    		new XsltXmlDataTransformation(INVALID_XSLT_BYTES);
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
    	XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(XSLT_BYTES,mockTransformerFactory);
    	assertThrows(XmlTransformationException.class, ()->underTest.process(createMessage(XML_BYTES)));
    }
	
	@Test
	void testProcess_success() {
		XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(XSLT_BYTES);		
    	Message<XmlPayload> data = underTest.process(createMessage(XML_BYTES));
    	assertThat(Input.fromByteArray(data.payload().bytes()), isIdenticalTo(Input.fromString(EXPECTED_ELEMENT_NAME_CHANGED_XML)));
	}
	
	@Test
	void testProcess_withInclude() throws Exception {
		XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(XSLT_INCLUDE_BYTES, TestHelper.getPathFromResource(TestHelper.SIMPLE_XSLT_INCLUDE_FILE).getParent());		
    	Message<XmlPayload> data = underTest.process(createMessage(XML_BYTES));
		assertThat(Input.fromByteArray(data.payload().bytes()), isIdenticalTo(Input.fromString(EXPECTED_ELEMENT_NAME_CHANGED_XML)));
	}
	
	@Test
	//xsl:perform-sort available in XSLT 2.0 and newer
	void testProcess_xsltVersion20_Sorting_success() {
		XsltXmlDataTransformation underTest2 = new XsltXmlDataTransformation(XSLT_BYTES_V21);		
    	Message<XmlPayload> data2 = underTest2.process(createMessage(XML_BYTES));
		assertThat(Input.fromByteArray(data2.payload().bytes()), isIdenticalTo(Input.fromString(EXPECTED_SORTED_XML)));
	}

	@Test
	//function current-group() available in XSLT 2.0 and newer
	void testProcess_xsltVersion20_Grouping_success() throws TransformerException {  			
		XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(XSLT_BYTES_V20);		
		Message<XmlPayload> data = underTest.process(createMessage(XML_BYTES_V20));
		
		String s = new String(data.payload().bytes(), StandardCharsets.UTF_8);
		System.out.println("testProcess_xsltVersion31_success ... " +s);
		assertEquals(EXPECTED_XSLT20_TABLE.trim(),s.trim());	
	}

	private Message<XmlPayload> createMessage(byte[] xmlBytes) {
		return MessageBuilder.createMessage(new XmlPayload(xmlBytes), EmptyContext.emptyInstance());
	}
}
