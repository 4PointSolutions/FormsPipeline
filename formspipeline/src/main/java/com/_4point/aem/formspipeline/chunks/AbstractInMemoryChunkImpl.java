package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.Chunk;

public abstract class AbstractInMemoryChunkImpl implements Chunk {
	
	private final byte[] bytes;
	
	public AbstractInMemoryChunkImpl(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public byte[] bytes() {
		return bytes;
	}
}
