package com._4point.aem.formspipeline.api;

public interface DataChunk<D extends Context> extends Chunk {
	D dataContext();
}
