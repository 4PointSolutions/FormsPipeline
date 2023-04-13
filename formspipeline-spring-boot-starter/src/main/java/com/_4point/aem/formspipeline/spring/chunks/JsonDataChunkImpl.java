package com._4point.aem.formspipeline.spring.chunks;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.contexts.AggregateContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDataChunkImpl implements JsonDataChunk {	
	private static final Logger logger = LoggerFactory.getLogger(JsonDataChunkImpl.class);

	private final byte[] jsonBytes;
	private final Context context;
	
	public JsonDataChunkImpl(byte[] pJsonBytes) {
		this.jsonBytes = pJsonBytes;
		this.context = JsonDataContextImpl.initializeJsonDoc(pJsonBytes);
	}
	
	public JsonDataChunkImpl(byte[] pJsonBytes, Context prevContext) {
		this.jsonBytes = pJsonBytes;
		this.context = new AggregateContext(JsonDataContextImpl.initializeJsonDoc(pJsonBytes), prevContext);
	}
	
	public JsonDataChunkImpl(JsonDataChunk prevChunk, Context newContext) {
		this.jsonBytes = prevChunk.bytes();
		this.context = new AggregateContext(newContext, prevChunk.dataContext());
	}
	
	@Override
	public byte[] bytes() {
		return jsonBytes;		
	}
	
	@Override
	public Context dataContext() {
		return context;
	}

	public static class JsonDataContextImpl implements JsonDataContext {
		private final JsonNode jsonDoc;
//		private final XPath xpathFactory;

		public JsonDataContextImpl(JsonNode jsonDoc) {
			this.jsonDoc = jsonDoc;
		}
		
		//		private JsonDataContextImpl(Document pJsonDoc, XPath pXPathF) {
//			xpathFactory = pXPathF;
//			jsonDoc = pJsonDoc;
//		}
//		
		public JsonNode getJsonDoc() {
			return jsonDoc;
		}
		
//		
		private static JsonNode convertToJson(byte[] data) throws JsonDataException {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode invoiceData;
			try {
				invoiceData = mapper.readValue(data, JsonNode.class);
				// TODO: Add optional validation against a JSON Schema using com.github.fge.jsonschema.main.JsonValidator
				// 		http://java-json-tools.github.io/json-schema-validator/2.2.x/index.html
				// 
			} catch (IOException e) {
				throw new JsonDataException("Error reading from Invoice Source.", e);
			}
			return invoiceData;
		}

		public static JsonDataContextImpl initializeJsonDoc(byte[] bytes) throws JsonDataException {
//			try {
//				XPath xPath = XPathFactory.newInstance().newXPath();
//				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//			    DocumentBuilder builder = factory.newDocumentBuilder();
//			    Document doc = builder.parse(new ByteArrayInputStream(bytes));
//			    doc.getDocumentElement().normalize();
//				return new JsonDataContextImpl(doc,xPath);
//			} catch (ParserConfigurationException | SAXException | IOException e) {
//				logger.atTrace().addArgument(()->new String(bytes, StandardCharsets.UTF_8)).log("XMLChunk Data = '{}'.");
//				throw new JsonDataException(String.format("Failed to create JsonDataContext ... %s", e.getMessage()),e);
//			}
			return new JsonDataContextImpl(convertToJson(bytes));
		}
//	
//		private Optional<String> internalGetString(String xpath) {			
//			try {
//				NodeList nodes = getNodeListByXpath(xpath);	        
//		        int length = nodes.getLength();
//				if (length > 1) {
//		        	//Multiple matches for the same xpath (i.e. repeated sections)9i
//		        	throw new IllegalArgumentException(String.format("Failed to parse json path %s to a single entry (Found %d entries).", xpath, length));
//		        }
//		        
//		        if (length == 1) {	// If it's exactly one result, then that's what we're looking for.
//		        	Node node = nodes.item(0);
//		        	if (node.getNodeType() == Node.ELEMENT_NODE) {
//	        			return Optional.of(((Element)node).getTextContent());	
//		        	} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
//		        		return Optional.of(((Attr)node).getTextContent());
//		        	}
//		        }
//		        
//		        return Optional.empty();	 
//			} catch (XPathExpressionException e) {			
//				throw new IllegalArgumentException(String.format("Failed to parse json path %s. Error message: %s", xpath, e.getMessage()),e);
//			} 		
//		}
//
//		private NodeList getNodeListByXpath(String xpath) throws XPathExpressionException {
//			XPathExpression expr = xpathFactory.compile(xpath);
//			return (NodeList) expr.evaluate(jsonDoc, XPathConstants.NODESET);
//		}
//
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> type) {
//			if (type.equals(String.class)) {
//				return (Optional<T>) internalGetString(key);
//			}
			return Optional.empty();
		}
	}
}
