package com._4point.aem.formspipeline.api;

public interface OutputChunk extends Chunk {
	Context dataContext();
	Context outputContext();
}
