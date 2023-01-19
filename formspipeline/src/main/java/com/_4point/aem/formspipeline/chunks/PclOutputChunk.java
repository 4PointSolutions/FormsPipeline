package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.chunks.PclOutputChunk.PclOutputContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;

public class PclOutputChunk <D extends Context> extends AbstractDocumentOutputChunk<D, PclOutputContext> {

	private final PclOutputContext outputContext;
	
	private PclOutputChunk(D dataContext, PclOutputContext outputContext, byte[] bytes) {
		super(dataContext, bytes);
		this.outputContext = outputContext;
	}

	public PclOutputContext outputContext() {
		return outputContext;
	}
	
	public static <D extends Context> PclOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PclOutputChunk<>(dataContext, new SimpleDocumentOutputContext(), bytes);
	}
	
	public static <D extends Context> PclOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PclOutputChunk<>(dataContext, new SimpleDocumentOutputContext(numPages), bytes);
	}
	
	public static interface PclOutputContext extends PagedContext {
	}
	
	/**
	 * Simple Print context that provides a minimal amount of information about the PCL.
	 * 
	 * Mainly just page number. 
	 * 
	 */
	public static class SimpleDocumentOutputContext extends AbstractPagedContext implements PclOutputContext {

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
