package com._4point.aem.formspipeline.spring.chunks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.contexts.AggregateContext;

public class XmlDataChunkImpl implements XmlDataChunk {	
	private static final Logger logger = LoggerFactory.getLogger(XmlDataChunkImpl.class);

	private final byte[] xmlBytes;
	private final Context context;
	
	public XmlDataChunkImpl(byte[] pXmlBytes) {
		this.xmlBytes = pXmlBytes;
		this.context = XmlDataContextImpl.initializeXmlDoc(pXmlBytes);
	}
	
	public XmlDataChunkImpl(byte[] pXmlBytes, Context prevContext) {
		this.xmlBytes = pXmlBytes;
		this.context = new AggregateContext(XmlDataContextImpl.initializeXmlDoc(pXmlBytes), prevContext);
	}
	
	public XmlDataChunkImpl(XmlDataChunk prevChunk, Context newContext) {
		this.xmlBytes = prevChunk.bytes();
		this.context = new AggregateContext(newContext, prevChunk.dataContext());
	}
	
	@Override
	public byte[] bytes() {
		return xmlBytes;		
	}
	
	@Override
	public Context dataContext() {
		return context;
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
		
		public static XmlDataContextImpl initializeXmlDoc(byte[] bytes) throws XmlDataException {
			try {
				XPath xPath = XPathFactory.newInstance().newXPath();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.parse(new ByteArrayInputStream(bytes));
			    doc.getDocumentElement().normalize();
				return new XmlDataContextImpl(doc,xPath);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.atTrace().addArgument(()->new String(bytes, StandardCharsets.UTF_8)).log("XMLChunk Data = '{}'.");
				throw new XmlDataException(String.format("Failed to create XmlDataContext ... %s", e.getMessage()),e);
			} 
		}
	
		private Optional<String> internalGetString(String xpath) {			
			try {
				NodeList nodes = getNodeListByXpath(xpath);	        
		        int length = nodes.getLength();
				if (length > 1) {
		        	//Multiple matches for the same xpath (i.e. repeated sections)9i
		        	throw new IllegalArgumentException(String.format("Failed to parse xml path %s to a single entry (Found %d entries).", xpath, length));
		        }
		        
		        if (length == 1) {	// If it's exactly one result, then that's what we're looking for.
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
				return (Optional<T>) internalGetString(key);
			}
			return Optional.empty();
		}
	}
}
