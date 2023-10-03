package com._4point.aem.formspipeline.chunks;

import java.util.Optional;
import java.util.OptionalInt;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk.PdfOutputContext;
import com._4point.aem.formspipeline.contexts.AbstractPagedContext;
import com._4point.aem.formspipeline.contexts.AggregateContext;
import com._4point.aem.formspipeline.contexts.EmptyContext;

public final class PdfOutputChunk<D extends Context> extends AbstractDocumentOutputChunk<D, PdfOutputContext> {
	public static final String CONTENT_TYPE = "application/pdf";
	
	private final PdfOutputContext outputContext;
	
	private PdfOutputChunk(D dataContext, PdfOutputContext outputContext, byte[] bytes) {
		super(dataContext, bytes);
		this.outputContext = outputContext;
	}
	
	private PdfOutputChunk(PdfOutputChunk<D> origChunk, Context newContext) {
		super(origChunk.dataContext(), origChunk.bytes());
		OptionalInt numPages = origChunk.outputContext.numPages();
		this.outputContext = numPages.isPresent() ? new SimplePdfOutputContext(new AggregateContext(newContext, origChunk.outputContext), numPages.getAsInt())
												  : new SimplePdfOutputContext(new AggregateContext(newContext, origChunk.outputContext));
	}
	
	public PdfOutputContext outputContext() {
		return outputContext;
	}

	public static <D extends Context> PdfOutputChunk<D> createSimple(D dataContext, byte[] bytes) {
		return new PdfOutputChunk<D>(dataContext, new SimplePdfOutputContext(), bytes);
	}
	
	public static <D extends Context> PdfOutputChunk<D> createSimple(D dataContext, byte[] bytes, int numPages) {
		return new PdfOutputChunk<D>(dataContext, new SimplePdfOutputContext(numPages), bytes);
	}
	
	/**
	 * Update the context with additional data. 
	 * 
	 * This routine produces a new pdfOutputChunk that incorporates the new context while retaining the same Pdf data.
	 * 
	 * @param newContexts - one or more additional contexts.
	 * @return
	 */
	public PdfOutputChunk<D> updateContext(Context... newContexts) { return new PdfOutputChunk<>(this, AggregateContext.aggregate(newContexts)); }
	

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

		private final Context parentContext;
		
		private SimplePdfOutputContext(Context parentContext, int numPages) {
			super(numPages);
			this.parentContext = parentContext;
		}

		private SimplePdfOutputContext(Context parentContext) {
			super();
			this.parentContext = parentContext;
		}

		private SimplePdfOutputContext() {
			super();
			this.parentContext = EmptyContext.emptyInstance();
		}

		private SimplePdfOutputContext(int numPages) {
			super(numPages);
			this.parentContext = EmptyContext.emptyInstance();
		}

		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return this.parentContext.get(key, target);
		}

		@Override
		public String contentType() {
			return CONTENT_TYPE;
		}
	}
	
	/*
	 */
}
