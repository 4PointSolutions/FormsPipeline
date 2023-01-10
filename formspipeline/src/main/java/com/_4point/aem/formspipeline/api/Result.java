package com._4point.aem.formspipeline.api;

/**
 * Results are objects that contain three Context objects.  They are used to provide post pipeline processes with 
 * data from the pipeline.   
 *
 * @param <D>
 * @param <O>
 * @param <R>
 */
public interface Result<D extends Context, O extends Context, R extends Context> {
	/**
	 * Retrieve the Context from the last DataTransformation step.
	 * 
	 * @return
	 */
	public D dataContext();

	/**
	 * Retrieve the Context from the last OutputTransformation step.
	 * 
	 * @return
	 */
	public O outputContext();
	
	/**
	 * Retrieve the Context from the OutputDestination step.
	 * 
	 * @return
	 */
	public R resultContext();
}
