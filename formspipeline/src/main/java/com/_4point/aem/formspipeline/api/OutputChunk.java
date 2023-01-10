package com._4point.aem.formspipeline.api;

/**
 * OutputChunks are generic objects that contain data (i.e. a set of bytes) and two Contexts.  They are used to transfer
 * data between OutputTransformation steps and/or between the OutputGeneration step and the first OutputTransformation step.
 * 
 * @param <D>
 * @param <O>
 */
public interface OutputChunk<D extends Context, O extends Context> extends Chunk {
	/**
	 * Retrieve the Context from the last DataTransformation step.
	 * 
	 * @return
	 */
	public D dataContext();

	/**
	 * Retrieve the Context from the OutputTransformation step that created this chunk.
	 * 
	 * @return
	 */
	public O outputContext();
}
