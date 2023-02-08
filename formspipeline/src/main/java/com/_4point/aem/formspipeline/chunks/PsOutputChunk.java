package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;
import com._4point.aem.formspipeline.chunks.PsOutputChunk.PsOutputContext;

public final class PsOutputChunk <D extends Context> extends AbstractDocumentOutputChunk<D, PsOutputContext> {
	public static final String CONTENT_TYPE = "application/postscript";
	
	private final PsOutputContext outputContext;
	
	private PsOutputChunk(D dataContext, PsOutputContext outputContext, byte[] bytes) {
		super(dataContext, bytes);
		this.outputContext = outputContext;
	}

	public PsOutputContext outputContext() {
		return outputContext;
	}
	
	public static <D extends Context> PsOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PsOutputChunk<>(dataContext, new SimplePsOutputContext(), bytes);
	}
	
	public static <D extends Context> PsOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PsOutputChunk<>(dataContext, new SimplePsOutputContext(numPages), bytes);
	}
	
	public static interface PsOutputContext extends PagedContext {
	}
	
	/**
	 * Simple Print context that provides a minimal amount of information about the PCL.
	 * 
	 * Mainly just page number. 
	 * 
	 */
	public static class SimplePsOutputContext extends AbstractPagedContext implements PsOutputContext {

		private SimplePsOutputContext() {
			super();
		}

		private SimplePsOutputContext(int numPages) {
			super(numPages);
		}

		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return Optional.empty();
		}

		@Override
		public String contentType() {
			return CONTENT_TYPE;
		}
	}

}
