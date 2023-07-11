package com._4point.aem.formspipeline.spring.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {
	public static final String BAD_XML_DATA_FILE = "/transformations/InvalidXml.xml";
	public static final String SIMPLE_XML_DATA_FILE = "/transformations/simpleXmlSample.xml";
	public static final String COMPLEX_XML_DATA_FILE = "/transformations/English/complexXmlSample.xml";
	public static final String COMPLEX_XML_DATA_FILE_ASIAN = "/transformations/Asian/complexXmlSample.xml";
	
	public static final String SIMPLE_XSLT_DATA_FILE = "/transformations/simpleXmlSample.xslt";
	
	public static final String XSLTV21_DATA_FILE = "/transformations/transformation2_1.xslt";
	//public static final String XSLTV3_DATA_FILE = "/transformations/transformation3_0.xslt";
	public static final String SIMPLE_XSLTV20_DATA_FILE = "/transformations/transformation2_0.xslt";
	public static final String XSLTV20_DATA_FILE = "/transformations/country.xml";
	public static final String SIMPLE_XSLT_INCLUDE_FILE = "/transformations/include.xslt";

	public static final String INVALID_XSLT_DATA_FILE = "/transformations/invalidXmlSample.xslt";
	
	public static final String BAD_XPATH_EXPRESSION = "*laptops*laptop?price";
	public static final String REPEAT_SECTION_XPATH_EXP_FOR_SIMPLE_XML = "/laptops/laptop/price";
	public static final String VALID_XPATH_FOR_SIMPLE_XML = "/laptops/laptop[@name='Dell']/price";
	public static final String NOTFOUND_XPATH_EXP_FOR_SIMPLE_XML = "/laptops/laptop/notavalidtag";
	
	public static final String REPEAT_SECTION_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice/DriverSection/DocumentType";
	public static final String CHINESECHAR_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo/Address[1]";
	public static final String MIXCONTENT_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DriverSection/MixContent";
	public static final String HASCHILDREN_XPATH_EXP_FOR_COMPLEX_XML = "/Output/XMLInvoices/XMLInvoice[1]/DataSection/ShipTo";
	    
    public static Path getPathFromResource(String filePath) throws IOException {
    	return Paths.get(new org.springframework.core.io.ClassPathResource(filePath).getURI());
    }
    
    public static byte[] getFileBytesFromResource(String filePath) {
		try {
			return Files.readAllBytes(getPathFromResource(filePath));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read bytes from %s".formatted(filePath), e);
		}			
    }
}
