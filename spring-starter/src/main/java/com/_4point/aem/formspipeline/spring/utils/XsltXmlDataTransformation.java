package com._4point.aem.formspipeline.spring.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

import net.sf.saxon.TransformerFactoryImpl;


/**
 * 
 * This class is for transforming XML with an XSLT transformation.  
 * Receiving one xslt and one xml document and perform the transformation using that data.
 * @author lien.ly
 *
 */
public class XsltXmlDataTransformation implements DataTransformationOneToOne<XmlDataChunk, XmlDataChunk> {
	
	private final Transformer transformer;
	
	public XsltXmlDataTransformation(byte[] xsltBytes, TransformerFactoryImpl transformerFactory) throws IllegalArgumentException {
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            this.transformer = transformerFactory.newTransformer(new StreamSource(new ByteArrayInputStream(xsltBytes)));
		} catch (TransformerConfigurationException e) {
			throw new IllegalArgumentException(String.format("Failed to instantiate XsltXmlDataTransformation.  %s", e.getMessage()));
		}		        
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes) throws IllegalArgumentException {
		this(xsltBytes, new TransformerFactoryImpl());
	}

	@Override
	public XmlDataChunk process(XmlDataChunk dataChunk) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(dataChunk.asInputStream()), new StreamResult(output));
			return new XmlDataChunk(output.toByteArray());
		} catch (TransformerException e) {
			throw new XmlTransformationException(e);
		}
	}
    
    @SuppressWarnings("serial")
    public static class XmlTransformationException extends RuntimeException {

    	public XmlTransformationException() {
    		super();
    	}

    	public XmlTransformationException(String message, Throwable cause) {
    		super(message, cause);
    	}

    	public XmlTransformationException(String message) {
    		super(message);
    	}

    	public XmlTransformationException(Throwable cause) {
    		super(cause);
    	}
    }
}
