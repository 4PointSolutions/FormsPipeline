package com._4point.aem.formspipeline.chunks;

public final class XmlPayload extends AbstractInMemoryTypedDataImpl {
	public static final String CONTENT_TYPE = "application/xml";
	
	public XmlPayload(byte[] bytes) {
		super(bytes, CONTENT_TYPE);
	}
	
	public XmlPayload(String xml) {
		this(xml.getBytes());
	}
}
