package com._4point.aem.formspipeline.chunks;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;

public sealed abstract class AbstractDocumentOutputChunk <D extends Context, U extends Context> extends AbstractInMemoryChunkImpl implements OutputChunk<D, U> permits PclOutputChunk<D>, PdfOutputChunk<D>, PsOutputChunk<D> 
{
	private final D dataContext;
	
	protected AbstractDocumentOutputChunk(D dataContext, byte[] bytes) {
		super(bytes);
		this.dataContext = dataContext;
	}
	
	public D dataContext() {
		return dataContext;
	}
}
