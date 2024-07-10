package com._4point.aem.formspipeline.payloads;

import com._4point.aem.formspipeline.api.TypedData;

public abstract class AbstractInMemoryTypedDataImpl extends AbstractInMemoryRawDataImpl implements TypedData {
	private final String contentType;

	protected AbstractInMemoryTypedDataImpl(byte[] bytes, String contentType) {
		super(bytes);
		this.contentType = contentType;
	}

	@Override
	public String contentType() {
		return contentType;
	}
}
