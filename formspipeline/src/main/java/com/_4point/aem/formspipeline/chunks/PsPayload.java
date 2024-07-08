package com._4point.aem.formspipeline.chunks;

public final class PsPayload extends AbstractInMemoryPagedDataImpl {
	public static final String CONTENT_TYPE = "application/postscript";
	
	public PsPayload(byte[] bytes) {
		super(bytes, CONTENT_TYPE);
	}
	public PsPayload(byte[] bytes, int numPages) {
		super(bytes, CONTENT_TYPE, numPages);
	}
}
