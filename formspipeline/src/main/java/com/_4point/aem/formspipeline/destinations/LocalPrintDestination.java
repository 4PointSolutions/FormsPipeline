package com._4point.aem.formspipeline.destinations;

import java.util.Optional;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Context.ContextBuilder;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.PagedContext;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.chunks.AbstractDocumentOutputChunk;
import com._4point.aem.formspipeline.contexts.AggregateContext;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.contexts.MapContext;
import com._4point.aem.formspipeline.results.SimpleResult;
import com._4point.aem.formspipeline.utils.JavaPrinterService;

public class LocalPrintDestination<DC extends Context, OC extends PagedContext,
								  O extends AbstractDocumentOutputChunk<DC, OC>
								  > implements OutputDestination<O, Result<DC, OC, Context>> {
	private static final String LOCAL_PRINT_DESTINATION_PREFIX = "com._4point.aem.formspipeline.destinations.local_print_destination.";
	private static final String PRINTJOB_NAME_KEY = LOCAL_PRINT_DESTINATION_PREFIX + "print_job_key";
	private static final String DEFAULT_PRINT_JOB_NAME = "AEM Print Job";

	private final JavaPrinterService printerService;
	
	// Package private for unit testing
	LocalPrintDestination(JavaPrinterService printerService) {
		this.printerService = printerService;
	}

	public LocalPrintDestination(String printerName, String contentType) {
		this(new JavaPrinterService(printerName, contentType));
	}

	public LocalPrintDestination(String printerName) {
		this(new JavaPrinterService(printerName));
	}

	@Override
	public Result<DC, OC, Context> process(O outputChunk) {
		DC dataContext = outputChunk.dataContext();
		OC outputContext = outputChunk.outputContext();
		Context combinedContext = new AggregateContext(dataContext, outputContext);
		
		ContextReader reader = new ContextReader(combinedContext);
		String printJobName = reader.printJobName().orElse(DEFAULT_PRINT_JOB_NAME);
		
		printerService.print(outputChunk.bytes(), printJobName);
		
		return new SimpleResult<DC, OC, Context>(dataContext, outputContext, EmptyContext.emptyInstance());
	}

	public static ContextReader reader(Context c) { return new ContextReader(c); }
	
	public static class ContextReader {
		private final Optional<String> printJobName;

		public ContextReader(Context context) {
			this.printJobName = context.getString(PRINTJOB_NAME_KEY);
		}

		public Optional<String> printJobName() {
			return printJobName;
		}
	}

	public static class ContextWriter {
		private final ContextBuilder builder;
		
		private ContextWriter() 											{ this(MapContext.builder());}
		private ContextWriter(ContextBuilder builder) 						{ this.builder = builder;}
		
		public ContextWriter printJobName(String printJobName) 				{ builder.put(PRINTJOB_NAME_KEY, printJobName); return this;}
		
		public Context build() 	{ return builder.build(); }
	}
}
