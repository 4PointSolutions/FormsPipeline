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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;

public interface XmlDataChunk extends DataChunk<XmlDataChunk.XmlDataContext>{

	byte[] bytes();

	XmlDataContext dataContext();

	/**
	 * Create a new XmlDataChunk from the xmlBytes parameter
	 * 
	 * @param xmlBytes
	 * @return
	 */
	public static XmlDataChunk create(byte[] xmlBytes) { return new XmlDataChunkImpl(xmlBytes); }
	
	/**
	 * Create a new XmlDataChunk from the xmlBytes parameter and a context.  The xmlDataContext resulting from
	 * reading the xmlBytes is combined with the prevContext parameter.  New entries from the xmlBytes take
	 * precedence over entries in the prevContext.
	 * 
	 * @param xmlBytes
	 * @param prevContext
	 * @return
	 */
	public static XmlDataChunk create(byte[] xmlBytes, Context prevContext) { return new XmlDataChunkImpl(xmlBytes, prevContext); }

	public interface XmlDataContext extends Context {
		public Document getXmlDoc();
		
		public static XmlDataContext create(Document pXmlDoc, XPath pXPathF) { return new XmlDataContextImpl(pXmlDoc, pXPathF); }
	}

	public class XmlDataChunkImpl implements XmlDataChunk {	
		private final byte[] xmlBytes;
		private final Context prevContext;
		
		public XmlDataChunkImpl(byte[] pXmlBytes) {
			this.xmlBytes = pXmlBytes;
			this.prevContext = null;
		}
		
		public XmlDataChunkImpl(byte[] pXmlBytes, Context prevContext) {
			this.xmlBytes = pXmlBytes;
			this.prevContext = prevContext;
		}
		
		@Override
		public byte[] bytes() {
			return xmlBytes;		
		}
		
		@Override
		public XmlDataContext dataContext() {
			XmlDataContextImpl newContext = XmlDataContextImpl.initializeXmlDoc(this.asInputStream());
			return prevContext == null ? newContext : new XmlDataContextWithPrevContext(newContext, prevContext);
		}
	}

	public static class XmlDataContextImpl implements XmlDataContext {
		private final Document xmlDoc;
		private final XPath xpathFactory;
		
		private XmlDataContextImpl(Document pXmlDoc, XPath pXPathF) {
			xpathFactory = pXPathF;
			xmlDoc = pXmlDoc;
		}
		
		public Document getXmlDoc() {
			return xmlDoc;
		}
		
		public static XmlDataContextImpl initializeXmlDoc(InputStream inputStream) throws XmlDataException {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.parse(inputStream);
			    doc.getDocumentElement().normalize();
				return new XmlDataContextImpl(doc,xPath);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new XmlDataException(String.format("Failed to create XmlDataContext ... %s", e.getMessage()),e);
			} 
		}
	
		@Override
		public Optional<String> getString(String xpath) {			
			try {
				NodeList nodes = getNodeListByXpath(xpath);	        
		        if (nodes.getLength() > 1) {
		        	//Multiple matches for the same xpath (i.e. repeated sections)9i
		        	throw new IllegalArgumentException(String.format("Failed to parse xml path %s to a single entry (Found %d entries).", xpath, nodes.getLength()));
		        }
		        
		        if (nodes.getLength() == 1) {	// If it's exactly one result, then that's what we're looking for.
		        	Node node = nodes.item(0);
		        	if (node.getNodeType() == Node.ELEMENT_NODE) {
	        			return Optional.of(((Element)node).getTextContent());	
		        	} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
		        		return Optional.of(((Attr)node).getTextContent());
		        	}
		        }
		        
		        return Optional.empty();	 
			} catch (XPathExpressionException e) {			
				throw new IllegalArgumentException(String.format("Failed to parse xml path %s. Error message: %s", xpath, e.getMessage()),e);
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
				return (Optional<T>) getString(key);
			}
			return Optional.empty();
		}
	}

	public static class XmlDataContextWithPrevContext implements XmlDataContext {
		private final XmlDataContext xmlContext;
		private final Context combinedContext;

		public XmlDataContextWithPrevContext(XmlDataContext xmlContext, Context prevContext) {
			this.xmlContext = xmlContext;
			this.combinedContext = xmlContext.incorporate(prevContext);
		}

		@Override
		public <T> Optional<T> get(String key, Class<T> target) { return combinedContext.get(key, target); }

		@Override
		public Document getXmlDoc() { return xmlContext.getXmlDoc(); }
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