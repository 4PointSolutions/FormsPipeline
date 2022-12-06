package com._4point.aem.formspipeline;

public class XmlDataException extends Exception {
	Exception originalException;
	
	public XmlDataException(String pMessage, Exception pException) {
		super(pMessage);
		originalException = pException;
	}
}
