package com._4point.aem.formspipeline.spring.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk.XmlDataContext;


public class XmlDataChunk implements DataChunk<XmlDataContext> {	
	private static final Logger logger = LoggerFactory.getLogger(XmlDataChunk.class);
	private final byte[] xmlBytes;  
	
	public XmlDataChunk(byte[] pXmlBytes) {
		xmlBytes = pXmlBytes;
	}
	
	@Override
	public byte[] bytes() {
		return xmlBytes;		
	}
	
	@Override
	public XmlDataContext dataContext() {
		return XmlDataContext.initializeXmlDoc(this.asInputStream());
	}
		
	public static class XmlDataContext implements Context {
		private final Document xmlDoc;
		private final XPath xpathFactory;
		
		XmlDataContext(Document pXmlDoc, XPath pXPathF) {
			xpathFactory = pXPathF;
			xmlDoc = pXmlDoc;
		}
		
		public Document getXmlDoc() {
			return xmlDoc;
		}
		
		public static XmlDataContext initializeXmlDoc(InputStream inputStream) throws XmlDataException {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.parse(inputStream);
			    doc.getDocumentElement().normalize();
				return new XmlDataContext(doc,xPath);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new XmlDataException(String.format("Failed to create XmlDataContext ... %s", e.getMessage()),e);
			} 
		}
	
		@Override
		public Optional<String> getString(String xpath) {			
			String value = ""; 
			try {
				NodeList nodes = getNodeListByXpath(xpath);	        
		        if (nodes.getLength() > 1) {
		        	//Multiple matches for the same xpath (i.e. repeated sections)9i
		        	throw new IllegalArgumentException(String.format("Failed to parse xml path %s.", xpath));
		        }
		        
		        for (int i = 0; i < nodes.getLength(); i++) {
		        	Node node = nodes.item(i);
		        	if (node.getNodeType() == Node.ELEMENT_NODE) {
	        			value = ((Element)node).getTextContent();	
		        	}	            
		        }
			} catch (XPathExpressionException e) {			
				throw new IllegalArgumentException(String.format("Failed to parse xml path %s. Error message: %s", xpath, e.getMessage()));
			} 		
			return Optional.of(value);	 
		}

		private NodeList getNodeListByXpath(String xpath) throws XPathExpressionException {
			XPathExpression expr = xpathFactory.compile(xpath);
			return (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> type) {
			if (type.getClass().isInstance(String.class)) {
				return (Optional<T>) getString(key);
			}
			return Optional.empty();
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
