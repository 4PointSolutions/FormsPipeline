package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.chunks.DocumentOutputChunk.DocumentOutputContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;

public class DocumentOutputChunk <D extends Context> extends AbstractInMemoryChunkImpl implements OutputChunk<D, DocumentOutputContext> 
{
	private final D dataContext;
	private final DocumentOutputContext outputContext;
	
	private DocumentOutputChunk(D dataContext, DocumentOutputContext outputContext, byte[] bytes) {
		super(bytes);
		this.dataContext = dataContext;
		this.outputContext = outputContext;
	}
	
	@Override
	public D dataContext() {
		return dataContext;
	}

	@Override
	public DocumentOutputContext outputContext() {
		return outputContext;
	}
	
	public static <D extends Context> DocumentOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new DocumentOutputChunk<D>(dataContext, new SimpleDocumentOutputContext(), bytes);
	}
	
	public static <D extends Context> DocumentOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new DocumentOutputChunk<D>(dataContext, new SimpleDocumentOutputContext(numPages), bytes);
	}
	
	public static interface DocumentOutputContext extends PagedContext {
	}
	
	/**
	 * Simple Print context that provides a minimal amount of information about the PCL.
	 * 
	 * Mainly just page number. 
	 * 
	 */
	public static class SimpleDocumentOutputContext extends AbstractPagedContext implements DocumentOutputContext {

		private SimpleDocumentOutputContext() {
			super();
		}

		private SimpleDocumentOutputContext(int numPages) {
			super(numPages);
		}

		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return Optional.empty();
		}
	}


}
