package com._4point.aem.formspipeline.spring.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestHelper {
	public static final String BAD_XML_DATA_FILE = "/transformations/InvalidXml.xml";
	public static final String SIMPLE_XML_DATA_FILE = "/transformations/simpleXmlSample.xml";	
	public static final String COMPLEX_XML_DATA_FILE = "/transformations/English/complexXmlSample.xml";
	public static final String COMPLEX_XML_DATA_FILE_ASIAN = "/transformations/Asian/complexXmlSample.xml";
	
	public static final String SIMPLE_XSLT_DATA_FILE = "/transformations/simpleXmlSample.xslt";
	public static final String SIMPLE_XSLTV2_DATA_FILE = "/transformations/simpleTransformation2_0.xslt";
	public static final String INVALID_XSLT_DATA_FILE = "/transformations/invalidXmlSample.xslt";
	
	public static final String BAD_XPATH_EXPRESSION = "*laptops*laptop?price";
	public static final String REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML = "/laptops/laptop/price";
	public static final String VALID_XPATH_FOR_SIMPLE_XML = "/laptops/laptop[@name='Dell']/price";
	public static final String NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML = "/laptops/laptop/notavalidtag";
	
	public static final String REPEAT_SECTION_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType";
	public static final String CHINESECHAR_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo/Address[1]";
	public static final String MIXCONTENT_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/MixContent";
	public static final String HASCHILDREN_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo";
	    
    public static String getFileContentFromResource(String filePath) {
		List<String> content = null;
		try {
			content = Files.readAllLines(Paths.get(
					new org.springframework.core.io.ClassPathResource(filePath).getURI()), Charset.defaultCharset());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
                        
		return content.stream().map(e -> e.toString()).reduce("", String::concat);
    }
    
    public static byte[] getFileBytesFromResource(String filePath) {
		byte[] content = null;
		try {
			content = Files.readAllBytes(Paths.get(new org.springframework.core.io.ClassPathResource(filePath).getURI()));			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
                        
		return content;
    }
    
    public static InputStream getFileFromResource(String filePath) {
    	InputStream is = null;
		try {
			is = new org.springframework.core.io.ClassPathResource(filePath).getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return is;
    	
    } 
}
