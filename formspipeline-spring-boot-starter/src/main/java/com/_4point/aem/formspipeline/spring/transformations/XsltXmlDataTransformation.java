package com._4point.aem.formspipeline.spring.transformations;

import java.nio.file.Path;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.payloads.XmlPayload;

import net.sf.saxon.TransformerFactoryImpl;


/**
 * 
 * This class is for transforming XML with an XSLT transformation.  
 * Receiving one xslt and one xml document and perform the transformation using that data.
 * @author lien.ly
 *
 */
public class XsltXmlDataTransformation implements DataTransformationOneToOne<Message<XmlPayload>, Message<XmlPayload>> {
	
	private final XsltNonXmlDataTransformation transformer;
	
	private XsltXmlDataTransformation(XsltNonXmlDataTransformation transformer) {
		this.transformer = transformer;
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes, TransformerFactoryImpl transformerFactory, Path xsltLocation) throws IllegalArgumentException {
		this(new XsltNonXmlDataTransformation(xsltBytes, transformerFactory, xsltLocation));
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes, TransformerFactoryImpl transformerFactory) throws IllegalArgumentException {
		this(new XsltNonXmlDataTransformation(xsltBytes, transformerFactory));
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes, Path xsltLocation) throws IllegalArgumentException {
		this(xsltBytes, new TransformerFactoryImpl(), xsltLocation);
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes) throws IllegalArgumentException {
		this(xsltBytes, new TransformerFactoryImpl());
	}
	
	@Override
	public Message<XmlPayload> process(Message<XmlPayload> msg) {
		Message<byte[]> result = transformer.process(msg);
		return MessageBuilder.createMessage(new XmlPayload(result.payload()), result.context());	// Convert from regular DataChunk to XmlDataChunk
	}
}
