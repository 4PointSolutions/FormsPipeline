package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;

public abstract class AbstractDocumentOutputChunk <D extends Context, U extends Context> extends AbstractInMemoryChunkImpl implements OutputChunk<D, U> 
{
	private final D dataContext;
	private final U outputContext;
	
	protected AbstractDocumentOutputChunk(D dataContext, U outputContext, byte[] bytes) {
		super(bytes);
		this.dataContext = dataContext;
		this.outputContext = outputContext;
	}
	
	public D dataContext() {
		return dataContext;
	}

	public U outputContext() {
		return outputContext;
	}
}
