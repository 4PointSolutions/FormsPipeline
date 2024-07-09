package com._4point.aem.formspipeline.chunks;

public final class PclPayload extends AbstractInMemoryPagedDataImpl  {
	public static final String CONTENT_TYPE = "application/vnd.hp-pcl";

	public PclPayload(byte[] bytes) {
		super(bytes, CONTENT_TYPE);
	}

	public PclPayload(byte[] bytes, int numPages) {
		super(bytes, CONTENT_TYPE, numPages);
	}
}
