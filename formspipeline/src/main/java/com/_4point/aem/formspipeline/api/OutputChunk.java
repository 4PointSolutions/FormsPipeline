package com._4point.aem.formspipeline.api;

public interface OutputChunk extends Chunk {
	public Context dataContext();
	public Context outputContext();
}
