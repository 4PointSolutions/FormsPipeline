package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;
import com._4point.aem.formspipeline.chunks.PsOutputChunk.PsOutputContext;

public class PsOutputChunk <D extends Context> extends AbstractDocumentOutputChunk<D, PsOutputContext> 
{
	private final D dataContext;
	private final PsOutputContext outputContext;
	
	private PsOutputChunk(D dataContext, PsOutputContext outputContext, byte[] bytes) {
		super(dataContext, outputContext, bytes);
		this.dataContext = dataContext;
		this.outputContext = outputContext;
	}
	
	@Override
	public D dataContext() {
		return dataContext;
	}

	@Override
	public PsOutputContext outputContext() {
		return outputContext;
	}
	
	public static <D extends Context> PsOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PsOutputChunk<>(dataContext, new SimpleDocumentOutputContext(), bytes);
	}
	
	public static <D extends Context> PsOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PsOutputChunk<>(dataContext, new SimpleDocumentOutputContext(numPages), bytes);
	}
	
	public static interface PsOutputContext extends PagedContext {
	}
	
	/**
	 * Simple Print context that provides a minimal amount of information about the PCL.
	 * 
	 * Mainly just page number. 
	 * 
	 */
	public static class SimpleDocumentOutputContext extends AbstractPagedContext implements PsOutputContext {

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
