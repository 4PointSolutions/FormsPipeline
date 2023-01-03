package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.common.TestHelper;
import com._4point.aem.formspipeline.spring.transformations.XsltXmlDataTransformation.XmlTransformationException;

import net.sf.saxon.TransformerFactoryImpl;

@ExtendWith(MockitoExtension.class)
class XsltXmlDataTransformationTest {
	private final byte[] xmlBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XML_DATA_FILE);
	private final byte[] xsltBytes = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLT_DATA_FILE);	
		
	private final byte[] xsltBytesV21 = TestHelper.getFileBytesFromResource(TestHelper.XSLTV21_DATA_FILE);

	private final byte[] dataBytes20 = TestHelper.getFileBytesFromResource(TestHelper.XSLTV20_DATA_FILE);
	private final byte[] xsltBytesV20 = TestHelper.getFileBytesFromResource(TestHelper.SIMPLE_XSLTV20_DATA_FILE);
	
	private final byte[] invalidXsltBytes = TestHelper.getFileBytesFromResource(TestHelper.INVALID_XSLT_DATA_FILE);
	
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
    	    	
    	XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(xsltBytes,mockTransformerFactory);
    	assertThrows(XmlTransformationException.class, ()->underTest.process(xmlChunk));
    }
	
	@Test
	void testProcess_success() {
		XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(xsltBytes);		
		XmlDataChunk data = underTest.process(new XmlDataChunk(xmlBytes));
		assertThat(Input.fromByteArray(data.bytes()), isIdenticalTo(Input.fromString(EXPECTED_ELEMENT_NAME_CHANGED_XML)));
	}
	
	@Test
	//xsl:perform-sort available in XSLT 2.0 and newer
	void testProcess_xsltVersion20_Sorting_success() {
		XsltXmlDataTransformation underTest2 = new XsltXmlDataTransformation(xsltBytesV21);		
		XmlDataChunk data2 = underTest2.process(new XmlDataChunk(xmlBytes));
		assertThat(Input.fromByteArray(data2.bytes()), isIdenticalTo(Input.fromString(EXPECTED_SORTED_XML)));
	}
	
	@Test
	//function current-group() available in XSLT 2.0 and newer
	void testProcess_xsltVersion20_Grouping_success() throws TransformerException {  			
		XsltXmlDataTransformation underTest = new XsltXmlDataTransformation(xsltBytesV20);		
		XmlDataChunk data = underTest.process(new XmlDataChunk(dataBytes20));
		
		String s = new String(data.bytes(), StandardCharsets.UTF_8);
		System.out.println("testProcess_xsltVersion31_success ... " +s);
		assertEquals(EXPECTED_XSLT20_TABLE.trim(),s.trim());	
	}
	
}
