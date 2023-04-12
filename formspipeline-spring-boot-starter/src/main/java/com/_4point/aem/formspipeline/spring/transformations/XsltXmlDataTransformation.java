package com._4point.aem.formspipeline.spring.transformations;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
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
	
	private final XsltNonXmlDataTransformation transformer;
	
	private XsltXmlDataTransformation(XsltNonXmlDataTransformation transformer) {
		this.transformer = transformer;
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes, TransformerFactoryImpl transformerFactory) throws IllegalArgumentException {
		this(new XsltNonXmlDataTransformation(xsltBytes, transformerFactory));
	}
	
	public XsltXmlDataTransformation(byte[] xsltBytes) throws IllegalArgumentException {
		this(xsltBytes, new TransformerFactoryImpl());
	}
	
	@Override
	public XmlDataChunk process(XmlDataChunk dataChunk) {
		DataChunk<Context> result = transformer.process(dataChunk);
		return XmlDataChunk.create(result.bytes(), result.dataContext());	// Convert from regular DataChunk to XmlDataChunk
	}
}
