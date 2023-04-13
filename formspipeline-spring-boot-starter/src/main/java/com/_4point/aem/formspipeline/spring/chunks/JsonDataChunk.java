package com._4point.aem.formspipeline.spring.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.contexts.AggregateContext;
import com.fasterxml.jackson.databind.JsonNode;

public interface JsonDataChunk extends DataChunk<Context>{

	byte[] bytes();
	Context dataContext();

	/**
	 * Create a new XmlDataChunk from the xmlBytes parameter
	 * 
	 * @param xmlBytes
	 * @return
	 */
	public static JsonDataChunk create(byte[] xmlBytes) { return new JsonDataChunkImpl(xmlBytes); }
	
	/**
	 * Create a new XmlDataChunk from the xmlBytes parameter and a context.  The xmlDataContext resulting from
	 * reading the xmlBytes is combined with the prevContext parameter.  New entries from the xmlBytes take
	 * precedence over entries in the prevContext.
	 * 
	 * @param xmlBytes
	 * @param prevContext
	 * @return
	 */
	public static JsonDataChunk create(byte[] xmlBytes, Context prevContext) { return new JsonDataChunkImpl(xmlBytes, prevContext); }

	/**
	 * Update the context with additional data. 
	 * 
	 * This routine produces a new XmlDataChunk that incorporates the new context while retaining the same XML data.
	 * 
	 * @param newContexts - one or more additional contexts.
	 * @return
	 */
	public default JsonDataChunk updateContext(Context... newContexts) { return new JsonDataChunkImpl(this, AggregateContext.aggregate(newContexts)); }
	
	public interface JsonDataContext extends Context {
		public JsonNode getJsonDoc();
		
//		public static XmlDataContext create(Document pXmlDoc, XPath pXPathF) { return new XmlDataChunkImpl.XmlDataContextImpl(pXmlDoc, pXPathF); }
	}


	@SuppressWarnings("serial")
	public static class JsonDataException extends RuntimeException {

		public JsonDataException() {
			super();
		}

		public JsonDataException(String message, Throwable cause) {
			super(message, cause);
		}

		public JsonDataException(String message) {
			super(message);
		}

		public JsonDataException(Throwable cause) {
			super(cause);
		}
	}
}