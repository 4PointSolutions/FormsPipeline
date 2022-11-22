package com._4point.aem.formspipeline.api;

public interface OutputChunk<D extends Context, O extends Context> extends Chunk {
	public D dataContext();
	public O outputContext();
}
