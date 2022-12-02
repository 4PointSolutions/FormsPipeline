package com._4point.aem.formspipeline.spring.chunks;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;

public class XmlDataChunk<T extends Context> implements DataChunk<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(XmlDataChunk.class);
	
	private Document xmlDoc;
	private String xmlString;
	private XPath xpathFactory;
	
	public XmlDataChunk() {
		XPathFactory xPathF = XPathFactory.newInstance();
		xpathFactory = xPathF.newXPath();	
	}

	@Override
	public byte[] bytes() 
	{
		return xmlString.getBytes();		
	}

	@Override
	public T dataContext() {
		return null;
	}
	
	public void loadXMLFromString(String xml) throws Exception
	{		
		xmlString = xml;
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(this.asInputStream());
	    doc.getDocumentElement().normalize();
	    xmlDoc = doc;	  	    
	}
	
	public List<String> getXmlDataValue(String xpath) {
		List<String> list = new ArrayList<>();
		try {
	        //create XPathExpression object
			XPathExpression expr = xpathFactory.compile(xpath);			
	        //evaluate expression result on XML document
	        NodeList nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
	        for (int i = 0; i < nodes.getLength(); i++) {
	        	Node node = nodes.item(i);
	        	if (node.getNodeType() == Node.ELEMENT_NODE) {
	        		Element childElementDetails = (Element)node;
	        		if(childElementDetails.getAttribute("value").isBlank()) {
        				list.add(childElementDetails.getTextContent());
	        		} else {
	        			list.add(childElementDetails.getAttribute("value"));	
	        		}
	        	}	            
	        }
		} catch (Exception e) {
			
			Formatter fmt = new Formatter();
			logger.error(fmt.format("Failed to parse xml path (%s). Error message %s", xpath, e.getMessage()).toString());
		}
		
		if(list.isEmpty()) {
			list.add(xpath);  	        //If it couldn't find the value (i.e pointing to something that isn't text)
		}
		return list;		
	}
	
	public Element getXmlElement(String xpath) {
		return xmlDoc.getElementById(xpath);
	}
}
