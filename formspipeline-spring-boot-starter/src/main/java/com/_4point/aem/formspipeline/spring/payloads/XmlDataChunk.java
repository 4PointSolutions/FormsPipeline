//package com._4point.aem.formspipeline.spring.payloads;
//
//import org.w3c.dom.Document;
//
//import com._4point.aem.formspipeline.api.Context;
//import com._4point.aem.formspipeline.api.DataChunk;
//import com._4point.aem.formspipeline.contexts.AggregateContext;
//
//public interface XmlDataChunk extends DataChunk<Context>{
//
//	byte[] bytes();
//	Context dataContext();
//
//	/**
//	 * Create a new XmlDataChunk from the xmlBytes parameter
//	 * 
//	 * @param xmlBytes
//	 * @return
//	 */
//	public static XmlDataChunk create(byte[] xmlBytes) { return new XmlDataChunkImpl(xmlBytes); }
//	
//	/**
//	 * Create a new XmlDataChunk from the xmlBytes parameter and a context.  The xmlDataContext resulting from
//	 * reading the xmlBytes is combined with the prevContext parameter.  New entries from the xmlBytes take
//	 * precedence over entries in the prevContext.
//	 * 
//	 * @param xmlBytes
//	 * @param prevContext
//	 * @return
//	 */
//	public static XmlDataChunk create(byte[] xmlBytes, Context prevContext) { return new XmlDataChunkImpl(xmlBytes, prevContext); }
//
//	/**
//	 * Update the context with additional data. 
//	 * 
//	 * This routine produces a new XmlDataChunk that incorporates the new context while retaining the same XML data.
//	 * 
//	 * @param newContexts - one or more additional contexts.
//	 * @return
//	 */
//	public default XmlDataChunk updateContext(Context... newContexts) { return new XmlDataChunkImpl(this, AggregateContext.aggregate(newContexts)); }
//	
//	public interface XmlDataContext extends Context {
//		public Document getXmlDoc();
//		
////		public static XmlDataContext create(Document pXmlDoc, XPath pXPathF) { return new XmlDataChunkImpl.XmlDataContextImpl(pXmlDoc, pXPathF); }
//	}
//
//}