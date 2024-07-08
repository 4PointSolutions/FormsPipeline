package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.RawData;

public abstract class AbstractInMemoryRawDataImpl implements RawData {
	
	private final byte[] bytes;
	
	protected AbstractInMemoryRawDataImpl(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public byte[] bytes() {
		return bytes;
	}
}
