package com._4point.aem.formspipeline.payloads;

public final class PdfPayload extends AbstractInMemoryPagedDataImpl {
	public static final String CONTENT_TYPE = "application/pdf";
	
	public PdfPayload(byte[] bytes) {
		super(bytes, CONTENT_TYPE);
	}

	public PdfPayload(byte[] bytes, int numPages) {
		super(bytes, CONTENT_TYPE, numPages);
	}
}
