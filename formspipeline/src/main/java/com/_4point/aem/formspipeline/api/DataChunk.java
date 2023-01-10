package com._4point.aem.formspipeline.api;

/**
 * DataChunks are generic objects that contain data (i.e. a set of bytes) and a Context.  They are used to transfer
 * data between DataTransformation steps and/or between the last DataTransformation step and the OutputGeneration step.   
 *
 * @param <D>
 */
public interface DataChunk<D extends Context> extends Chunk {
	/**
	 * Retrieve the context for this chunk.  It is typically the context created by the DataTransformation step that
	 * created this DataChunk.
	 * 
	 * @return
	 */
	D dataContext();
}
