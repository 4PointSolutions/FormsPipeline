package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.chunks.SimpleDataChunk;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

import net.sf.saxon.TransformerFactoryImpl;


/**
 * 
 * This class is for transforming XML with an XSLT transformation into a non-XML format.  
 * Receiving one xslt and one xml document and perform the transformation using that data.
 *
 */
public class XsltNonXmlDataTransformation implements DataTransformationOneToOne<XmlDataChunk, DataChunk<Context>> {
	
	private final Transformer transformer;
	
	public XsltNonXmlDataTransformation(byte[] xsltBytes, TransformerFactoryImpl transformerFactory) throws IllegalArgumentException {
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            this.transformer = transformerFactory.newTransformer(new StreamSource(new ByteArrayInputStream(xsltBytes)));
		} catch (TransformerConfigurationException e) {
			throw new IllegalArgumentException(String.format("Failed to instantiate XsltXmlDataTransformation.  %s", e.getMessage()),e);
		}		        
	}
	
	public XsltNonXmlDataTransformation(byte[] xsltBytes) throws IllegalArgumentException {
		this(xsltBytes, new TransformerFactoryImpl());
	}
	
	@Override
	public DataChunk<Context> process(XmlDataChunk dataChunk) {
		try {
			//Add correlation ID, processing time, size of dataChunk, which transformer is used
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			transformer.transform(new StreamSource(dataChunk.asInputStream()), new StreamResult(output));
			return new SimpleDataChunk(dataChunk.dataContext(), output.toByteArray());	// Include previous context in this one.
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