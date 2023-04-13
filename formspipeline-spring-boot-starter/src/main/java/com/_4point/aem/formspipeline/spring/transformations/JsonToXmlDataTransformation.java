package com._4point.aem.formspipeline.spring.transformations;

import java.util.function.Function;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.spring.chunks.JsonDataChunk;
import com._4point.aem.formspipeline.spring.chunks.JsonDataChunk.JsonDataContext;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class JsonToXmlDataTransformation implements DataTransformationOneToOne<JsonDataChunk, XmlDataChunk> {

	private final Function<Context, String> xmlRootElementNamingFn;
	private final Function<Context, String> jsonPointerFn;
	
	public JsonToXmlDataTransformation(Function<Context, String> xmlRootElementNamingFn, Function<Context, String> jsonPointerFn) {
		this.xmlRootElementNamingFn = xmlRootElementNamingFn;
		this.jsonPointerFn = jsonPointerFn;
	}

	public JsonToXmlDataTransformation(Function<Context, String> xmlRootElementNamingFn) {
		this(xmlRootElementNamingFn, null);
	}

	public JsonToXmlDataTransformation(String xmlRootElementName) {
		this(__->xmlRootElementName);
	}

	@Override
	public XmlDataChunk process(JsonDataChunk dataChunk) {
		JsonDataContext jsonDataContext = (JsonDataContext)dataChunk.dataContext();
		byte[] xmlBytes = convertToXml(xmlRootElementNamingFn.apply(jsonDataContext), jsonDataContext.getJsonDoc(), jsonPointerFn != null ? jsonPointerFn.apply(jsonDataContext) : null);
		return XmlDataChunk.create(xmlBytes, jsonDataContext);
	}
	
	protected static byte[] convertToXml(String xmlRootElement, JsonNode json, String jsonRootPointer) {
		try {
			XmlMapper xmlMapper = new XmlMapper();
			xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
			ObjectWriter ow = xmlMapper.writer().withRootName(xmlRootElement);	// Set the root element name
			JsonNode jsonAtNode = jsonRootPointer == null ? json : json.at(jsonRootPointer);						// grab the JSON from wherever the pointer points
			return ow.writeValueAsBytes(jsonAtNode);
		} catch (JsonProcessingException e) {
			throw new JsonToXmlDataTransformationException("Error converting Invoice JSON to XML.", e);
		}
	}

	@SuppressWarnings("serial")
	public static class JsonToXmlDataTransformationException extends RuntimeException {

		public JsonToXmlDataTransformationException() {
		}

		public JsonToXmlDataTransformationException(String message, Throwable cause) {
			super(message, cause);
		}

		public JsonToXmlDataTransformationException(String message) {
			super(message);
		}

		public JsonToXmlDataTransformationException(Throwable cause) {
			super(cause);
		}
	}

}
