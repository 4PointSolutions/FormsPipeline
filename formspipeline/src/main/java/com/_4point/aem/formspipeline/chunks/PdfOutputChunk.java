package com._4point.aem.formspipeline.chunks;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk.PdfOutputContext;

public class PdfOutputChunk<D extends Context> extends AbstractInMemoryChunkImpl 
											   implements OutputChunk<D, PdfOutputContext> 
											    {
	private final D dataContext;
	private final PdfOutputContext outputContext;
	
	private PdfOutputChunk(D dataContext, PdfOutputContext outputContext, byte[] bytes) {
		super(bytes);
		this.dataContext = dataContext;
		this.outputContext = outputContext;
	}

	@Override
	public D dataContext() {
		return dataContext;
	}

	@Override
	public PdfOutputContext outputContext() {
		return outputContext;
	}

	public static <D extends Context> PdfOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PdfOutputChunk<D>(dataContext, new SimplePdfOutputContext(), bytes);
	}
	
	public static <D extends Context> PdfOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PdfOutputChunk<D>(dataContext, new SimplePdfOutputContext(numPages), bytes);
	}
	
	public static interface PdfOutputContext extends PagedContext {
	}

	/**
	 * Simple PDF context that provides a minimal amount of information about the PDF.
	 * 
	 * Mainly just page number. 
	 *
	 * Down the road, we may implement a more complicated PDF context that uses a PDF library to parse the
	 * PDF and make additional information information available.
	 * 
	 */
	public static class SimplePdfOutputContext extends AbstractPagedContext implements PdfOutputContext {

		private SimplePdfOutputContext() {
			super();
		}

		private SimplePdfOutputContext(int numPages) {
			super(numPages);
		}

		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return Optional.empty();
		}
	}
	
	/*
	 */
}