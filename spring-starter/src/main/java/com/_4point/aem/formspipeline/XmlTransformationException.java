package com._4point.aem.formspipeline;

public class XmlTransformationException extends Exception {
	Exception originalException;
	
	public XmlTransformationException(String pMessage, Exception pException) {
		super(pMessage);
		originalException = pException;
	}

}
