package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.chunks.PclOutputChunk.PclOutputContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;

public final class PclOutputChunk <D extends Context> extends AbstractDocumentOutputChunk<D, PclOutputContext> {
	public static final String CONTENT_TYPE = "application/vnd.hp-pcl";

	private final PclOutputContext outputContext;
	
	private PclOutputChunk(D dataContext, PclOutputContext outputContext, byte[] bytes) {
		super(dataContext, bytes);
		this.outputContext = outputContext;
	}

	public PclOutputContext outputContext() {
		return outputContext;
	}
	
	public static <D extends Context> PclOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PclOutputChunk<>(dataContext, new SimplePclOutputContext(), bytes);
	}
	
	public static <D extends Context> PclOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PclOutputChunk<>(dataContext, new SimplePclOutputContext(numPages), bytes);
	}
	
	public static interface PclOutputContext extends PagedContext {
	}
	
	/**
	 * Simple Print context that provides a minimal amount of information about the PCL.
	 * 
	 * Mainly just page number. 
	 * 
	 */
	public static class SimplePclOutputContext extends AbstractPagedContext implements PclOutputContext {

		private SimplePclOutputContext() {
			super();
		}

		private SimplePclOutputContext(int numPages) {
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
