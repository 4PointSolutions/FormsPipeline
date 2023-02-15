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

/**
 * Destination step used to send output to a local printer queue.
 *
 * @param <DC>
 * @param <OC>
 * @param <O>
 */
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

	/**
	 * Write a Print Job Name to a context.  If this is added to the context before the LocalPrintDestination step is
	 * invoked, then the LocalPrintDestination step will set the name of the job in the print queue to the Print Job Name. 
	 * 
	 * @param context
	 * @param printJobName
	 * @return
	 */
	public static Context addPrintJobNameToContext(Context context, String printJobName) { return context.incorporate(contextWriter().printJobName(printJobName).build()); }
	
	/**
	 * Create a ContextWriter to write values to a context that will be used by the LOcalPrintDestination.
	 * 
	 * This exists for standardization (all steps have ContextReader/Writer implementations to communicate with them however
	 * since there is only one value, addPrintJobNameToContext is a more concise choice.
	 * 
	 * @return
	 */
	public static ContextWriter contextWriter() { return new ContextWriter(); }
	
	/**
	 * Retrieve a LocalPrintDestination print job name from a context.
	 * 
	 * This unlikely to be used outside of LocalPrintDestination, however just in case someone wants to read the Print Job Name
	 * set in a previous step, this is available.
	 * 
	 * @param context - Context that will be searched for the print job name.
	 * @return
	 */
	public static Optional<String> getPrintJobName(Context context) { return context.getString(PRINTJOB_NAME_KEY); }
	
	/**
	 * Retrieve values used by the LocalPrintDestination.
	 * 
	 * This currently consists of only the Printer Job Name, so using getPrintJobName() is a more concise choice.
	 * 
	 * @param context
	 * @return
	 */
	public static ContextReader contextReader(Context context) { return new ContextReader(context); }

	/**
	 * Class for reading values used by LocalPrintDestination from a Context object.
	 *
	 */
	public static class ContextReader {
		private final Optional<String> printJobName;

		private ContextReader(Context context) {
			this.printJobName = getPrintJobName(context);
		}

		public Optional<String> printJobName() {
			return printJobName;
		}
	}

	/**
	 * Class for creating a context containing values used by LocalPrintDestination.
	 *
	 */
	public static class ContextWriter {
		private final ContextBuilder builder;
		
		private ContextWriter() 											{ this(MapContext.builder());}
		private ContextWriter(ContextBuilder builder) 						{ this.builder = builder;}
		
		public ContextWriter printJobName(String printJobName) 				{ builder.put(PRINTJOB_NAME_KEY, printJobName); return this;}
		
		public Context build() 	{ return builder.build(); }
	}
	
}
