package com._4point.aem.formspipeline.spring.payloads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.contexts.AggregateContext;
import com._4point.aem.formspipeline.payloads.XmlPayload;

public class ParsedXmlPayload extends XmlPayload {	
	private static final Logger logger = LoggerFactory.getLogger(ParsedXmlPayload.class);

	private final ParsedXmlContextImpl xmlContext;

	public ParsedXmlPayload(byte[] bytes) {
		super(bytes);
		this.xmlContext = ParsedXmlContextImpl.initializeXmlDoc(this.bytes());
	}

	public ParsedXmlPayload(String xml) {
		super(xml);
		this.xmlContext = ParsedXmlContextImpl.initializeXmlDoc(this.bytes());
	}

	public Context xmlContext() {
		return xmlContext;
	}

	private static class ParsedXmlContextImpl implements Context {
		private final Document xmlDoc;
		private final XPath xpathFactory;
		
		private ParsedXmlContextImpl(Document pXmlDoc, XPath pXPathF) {
			xpathFactory = pXPathF;
			xmlDoc = pXmlDoc;
		}
		
		public Document getXmlDoc() {
			return xmlDoc;
		}
		
		public static ParsedXmlContextImpl initializeXmlDoc(byte[] bytes) throws XmlDataException {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.parse(new ByteArrayInputStream(bytes));
			    doc.getDocumentElement().normalize();
				return new ParsedXmlContextImpl(doc,xPath);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.atTrace().addArgument(()->new String(bytes, StandardCharsets.UTF_8)).log("XMLChunk Data = '{}'.");
				throw new XmlDataException(String.format("Failed to create XmlDataContext ... %s", e.getMessage()),e);
			} 
		}

		private Optional<String> internalGetString(String xpath) {
			List<String> strings = internalGetStrings(xpath);
			if (strings.size() > 1) {
	        	//Multiple matches for the same xpath (i.e. repeated sections)9i
	        	throw new IllegalArgumentException(String.format("Failed to parse xml path %s to a single entry (Found %d entries).", xpath, strings.size()));
	        }
			return strings.size() == 1 ? Optional.of(strings.get(0)) : Optional.empty();
		}	

		private List<String> internalGetStrings(String xpath) {			
			try {
				NodeList nodes = getNodeListByXpath(xpath);	        
		        return IntStream.range(0, nodes.getLength())
		        	     		.mapToObj(nodes::item)
		        	     		.mapMulti(this::mapNodeToString)
		        	     		.toList();
			} catch (XPathExpressionException e) {			
				throw new IllegalArgumentException(String.format("Failed to parse xml path %s. Error message: %s", xpath, e.getMessage()),e);
			} 		
		}

		private void mapNodeToString(Node node, Consumer<String> consumer) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				consumer.accept(((Element)node).getTextContent());	
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				consumer.accept(((Attr)node).getTextContent());
			}
		}

		private NodeList getNodeListByXpath(String xpath) throws XPathExpressionException {
			XPathExpression expr = xpathFactory.compile(xpath);
			return (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> type) {
			if (type.equals(String.class)) {
				return (Optional<T>) internalGetString(key);
			}
			return Optional.empty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> getMulti(String key, Class<T> type) {
			if (type.equals(String.class)) {
				return (List<T>)internalGetStrings(key);
			}
			return List.of();
		}
	}
	
	@SuppressWarnings("serial")
	public static class XmlDataException extends RuntimeException {

		public XmlDataException() {
			super();
		}

		public XmlDataException(String message, Throwable cause) {
			super(message, cause);
		}

		public XmlDataException(String message) {
			super(message);
		}

		public XmlDataException(Throwable cause) {
			super(cause);
		}
	}

}
