package com._4point.aem.formspipeline.spring.transformations;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunkImpl;

/**
 * This class has minimal tests because the main tests are in XsltXmlDataTransformationTest since the class it tests
 * relies on the XsltNonXmlDataTransformation (which this class tests).
 * 
 * Most of the time XsltXmlDataTransformation will be the class used.  XsltNonXmlDataTransformation is only used for specialized
 * use cases.  As such, we only make "sanity check" tests here.  The bulk of more extensive testing is carrid out in
 * XsltXmlDataTransformationTest.
 *
 */
class XsltNonXmlDataTransformationTest {
	private static final String XML_DATA = 
			"""
			<?xml version="1.0" encoding="UTF-8"?>
			<DriverSection>
				<DocumentType_4>INVS01</DocumentType_4>
				<DocumentType_Desc>INVOICE                       </DocumentType_Desc>
				<SendFax_6>0</SendFax_6>
				<SendEMail_8>1</SendEMail_8>
				<Archive_16>1</Archive_16>
				<FaxCoverPage_18 />
				<PrintTC_20>1</PrintTC_20>
				<CountryOM_28>BRA</CountryOM_28>
				<LanguageOM_30 />
				<ProductCode>42</ProductCode>
			</DriverSection>
			""";
	
	private static final String XSLT_STR = 
			"""
			<?xml version="1.0" encoding="UTF-8"?>
			<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
			    xmlns:xs="http://www.w3.org/2001/XMLSchema"
			    exclude-result-prefixes="xs"
			    version="2.0">
			    
			    <xsl:output method="text" encoding="utf-8" />
			    
			    <xsl:template match="/">
			        <xsl:apply-templates select="DriverSection" />
			    </xsl:template>
			    <xsl:template match="DocumentType_4">
			        <xsl:value-of select="normalize-space(text())"/>
			        <xsl:text>,</xsl:text>
			    </xsl:template>
			    <xsl:template match="DocumentType_Desc">
			        <xsl:value-of select="normalize-space(text())"/>
			        <xsl:text>,</xsl:text>
			    </xsl:template>
			    <xsl:template match="SendFax_6">
			        <xsl:value-of select="normalize-space(text())"/>
			        <xsl:text>,</xsl:text>
			    </xsl:template>
			    <xsl:template match="SendEMail_8">
			        <xsl:value-of select="normalize-space(text())"/>
			        <xsl:text>,</xsl:text>
			    </xsl:template>
			    <xsl:template match="Archive_16">
			        <xsl:value-of select="normalize-space(text())"/>
			        <xsl:value-of>
			Count = 1</xsl:value-of>
			    </xsl:template>
			    <xsl:template match="text()" /> <!-- Eat all the fields that are not listed above -->
			</xsl:stylesheet>			
			""";
	
	private static final String EXPECTED_RESULT = 
			"""
			INVS01,INVOICE,0,1,1
			Count = 1""";
	@Test
	void testProcess() {
		XsltNonXmlDataTransformation underTest = new XsltNonXmlDataTransformation(XSLT_STR.getBytes(StandardCharsets.UTF_8));
		DataChunk<Context> result = underTest.process(new XmlDataChunkImpl(XML_DATA.getBytes(StandardCharsets.UTF_8)));
		assertEquals(EXPECTED_RESULT, result.asString());
	}

}
