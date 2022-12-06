package com._4point.aem.formspipeline.spring.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.XmlTransformationException;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is for transforming XML with an XSLT transformation.  
 * Receiving one xslt and one xml document and perform the transformation using that data.
 * @author lien.ly
 *
 */
public class XsltXmlDataTransformation implements DataTransformationOneToOne<XmlDataChunk, XmlDataChunk> {
	
	private static final Logger logger = LoggerFactory.getLogger(XsltXmlDataTransformation.class);	
	private final byte[] xsltBytes;

	public XsltXmlDataTransformation(byte[] xsltBytes){
		this.xsltBytes = xsltBytes;
	}

	@Override
	public XmlDataChunk process(XmlDataChunk dataChunk) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transform(new StreamSource(dataChunk.asInputStream()), output);
			return new XmlDataChunk(output.toByteArray());
		} catch (TransformerException e) {
			logger.error(String.format("Failed to execute process error: %s.", e.getMessage()));
			throw new XmlTransformationException(e);
		}
	}

    public void transform(Source xmlDoc, OutputStream output)
            throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // add XSLT in Transformer
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(new ByteArrayInputStream(xsltBytes)));
        transformer.transform(xmlDoc, new StreamResult(output));
    }


}
