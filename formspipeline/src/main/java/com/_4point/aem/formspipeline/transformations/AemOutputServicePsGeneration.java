package com._4point.aem.formspipeline.transformations;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.docservices.rest_services.client.helpers.AemServerType;
import com._4point.aem.docservices.rest_services.client.output.RestServicesOutputServiceAdapter;
import com._4point.aem.fluentforms.api.Document;
import com._4point.aem.fluentforms.api.PathOrUrl;
import com._4point.aem.fluentforms.api.output.OutputService;
import com._4point.aem.fluentforms.api.output.OutputService.GeneratePrintedOutputArgumentBuilder;
import com._4point.aem.fluentforms.api.output.OutputService.OutputServiceException;
import com._4point.aem.fluentforms.api.output.PrintConfig;
import com._4point.aem.fluentforms.impl.UsageContext;
import com._4point.aem.fluentforms.impl.output.OutputServiceImpl;
import com._4point.aem.formspipeline.aem.AemConfigBuilder;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Context.ContextBuilder;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.MapContext;
import com._4point.aem.formspipeline.payloads.PsPayload;
import com._4point.aem.formspipeline.payloads.XmlPayload;
import com.adobe.fd.output.api.PaginationOverride;

import jakarta.ws.rs.client.Client;

/**
 * This class is used to call AEM to generate a Print documents.
 * 
 * The AEM Print generation has many parameters.  This class looks for them in the incoming data chunk's context.  Typically,
 * these parameters will be supplied from a process upstream in the pipeline or in a context that exposes the environment
 * parameters (usually, the Spring environent will be made available in a Context that is added at the start of the
 * pipeline - so all requests for parameters will default to the Spring environment if no steps in the pipeline otherwise
 * provide a parameter).
 * 
 * This class provides helper classes for getting parameters it needs into and out of a Context.  These are available
 * through the methods on the AemOutputServicePrintGenerationContext object.  contextWriter() (which produces a context with 
 * AEMOutputServicePrintGenerator parameters in it) and contextReader() (which provides a view on an existing context).
 * 
 * Typically, some process upstream of the AemOutputServicePdfGeneration step will use the contextWriter() to create a context 
 * with the parameters it knows about and then it will combine that context with the existing context using an AggregateContext object.
 *
 * When building an AemOutputServicePrintGeneration object, the client application will need to supply parameters (like the
 * location of an AEM server, credentials to talk to that server, etc.).  This is done using a Builder object retrieved 
 * by calling the static builder() method.
 *
 */
public class AemOutputServicePsGeneration implements DataTransformationOneToOne<Message<XmlPayload>, Message<PsPayload>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePsGeneration.class);

	private final OutputService outputService;
	
	private AemOutputServicePsGeneration(OutputService outputService) {
		this.outputService = outputService;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	@Override
	public Message<PsPayload> process(Message<XmlPayload> msg) {
		Context dataContext = msg.context();
		var myContext = new AemOutputServicePsGenerationContext.ContextReader(dataContext);
		PathOrUrl template = myContext.template();
		try {
			Document psResult = myContext.transferAllSettings(outputService.generatePrintedOutput())
											  .executeOn(template, msg.payload().asInputStream());
			Optional<Long> pageCount = psResult.getPageCount();
			var result = pageCount.isPresent() ? new PsPayload(psResult.getInputStream().readAllBytes(), pageCount.get().intValue())
											   : new PsPayload(psResult.getInputStream().readAllBytes());
			return MessageBuilder.createMessage(result, msg.context());
		} catch (IOException | OutputServiceException  e) {
			throw new IllegalStateException("Error while generating PS document from template (" + template.toString() + ").", e);
		}
	}
	
	/**
	 * Class that allows for setting and retrieving AemOutputServicePdfGeneration parameters to and from the Context.
	 *
	 */
	public static class AemOutputServicePsGenerationContext {
		
		private static final String AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX = Context.FORMSPIPELINE_PROPERTY_PREFIX + "aem_output_ps_gen.";
		private static final String CONTENT_ROOT = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "content_root";
		private static final String LOCALE = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "locale";
		private static final String COPIES = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "copies";
		private static final String DEBUG_DIRECTORY = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "debug_directory";
		private static final String PAGINATION_OVERRIDE = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "pagination_override";
		private static final String PRINT_CONFIG = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "print_config";
		private static final String DOCUMENT_XCI = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "xci";
	
		private static final String TEMPLATE = AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX + "template";
		
		public static ContextWriter contextWriter() 				{ return new ContextWriter(); }
		public static ContextReader contextReader(Context context) 	{ return new ContextReader(context); }
		
		public static class ContextReader {
			private final Context context;

			private ContextReader(Context context) { this.context = context; }
			
			public Optional<PathOrUrl> contentRoot() 					{ return context.get(CONTENT_ROOT, PathOrUrl.class); }			
			public Optional<Locale> locale() 							{ return context.get(LOCALE, Locale.class); }
			public Optional<Integer> copies() 							{ return context.get(COPIES, Integer.class); }			
			public Optional<Path> debugDirectory() 						{ return context.get(DEBUG_DIRECTORY, Path.class); }			
			public Optional<PaginationOverride> paginationOverride() 	{ return context.get(PAGINATION_OVERRIDE, PaginationOverride.class); }
			public Optional<PrintConfig> printConfig()	 				{				
				if(context.get(PRINT_CONFIG, PrintConfig.class).isEmpty()) {
					return Optional.ofNullable(PrintConfig.Generic_PS_L3);
				}
				return context.get(PRINT_CONFIG, PrintConfig.class); }
			public Optional<Document> xci()	 							{ return context.get(DOCUMENT_XCI, Document.class); }
			public PathOrUrl template() 								{ return context.get(TEMPLATE, PathOrUrl.class)
																						.orElseThrow(()->new IllegalArgumentException("Template parameter (" + TEMPLATE + ") not found.")); }
			
			// Transfer all the settings that are present over to the builder.
			private GeneratePrintedOutputArgumentBuilder transferAllSettings(GeneratePrintedOutputArgumentBuilder builder) {
				return Stream.of(builder)
						.map(b->transferOneSetting(b, contentRoot(), b::setContentRoot))
						.map(b->transferOneSetting(b, locale(), b::setLocale))
						.map(b->transferOneSetting(b, copies(), b::setCopies))
						.map(b->transferOneSetting(b, debugDirectory(), b::setDebugDir))
						.map(b->transferOneSetting(b, paginationOverride(), b::setPaginationOverride))
						.map(b->transferOneSetting(b, printConfig(), b::setPrintConfig))
						.findFirst().get();
			}
						
			// Transfer one setting over to the builder if and only if that setting is present
			private <T> GeneratePrintedOutputArgumentBuilder transferOneSetting(GeneratePrintedOutputArgumentBuilder b, Optional<T> v, Function<T, GeneratePrintedOutputArgumentBuilder> s) {
				return v.map(s).orElse(b);
			}
		}
		
		public static class ContextWriter {
			private final ContextBuilder builder;
			
			private ContextWriter() 											{ this(MapContext.builder());}
			private ContextWriter(ContextBuilder builder) 						{ this.builder = builder;}
	
			public ContextWriter contentRoot(PathOrUrl value) 					{ builder.put(CONTENT_ROOT , value); return this;}
			public ContextWriter contentRoot(String value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
			public ContextWriter contentRoot(Path value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
			public ContextWriter contentRoot(URL value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
						
			public ContextWriter locale(Locale value)							{ builder.put(LOCALE , value); return this; }
			public ContextWriter copies(Integer value)							{ builder.put(COPIES , value); return this; }			
			public ContextWriter debugDirectory(Path value)						{ builder.put(DEBUG_DIRECTORY , value); return this; }
			public ContextWriter paginationOverride(PaginationOverride value)	{ builder.put(PAGINATION_OVERRIDE , value); return this; }
			public ContextWriter printConfig(PrintConfig value)					{ builder.put(PRINT_CONFIG , value); return this; }
			public ContextWriter xci(Document value)							{ builder.put(DOCUMENT_XCI , value); return this; }
						
			public ContextWriter template(PathOrUrl value) 						{ builder.put(TEMPLATE , value); return this;}
			public ContextWriter template(String value) 						{ template(PathOrUrl.from(value)); return this;}
			public ContextWriter template(Path value) 							{ template(PathOrUrl.from(value)); return this;}
			public ContextWriter template(URL value) 							{ template(PathOrUrl.from(value)); return this;}
			
			public Context build() 	{ return builder.build(); }
		}
	}
	
	/**
	 * Builder is a class that is used to build an AemOutputServicePrintGeneration object.  It allows a user to specify 
	 * all the various parameters that may be required to instantiate a AemOutputServicePrintGeneration object.
	 *
	 */
	public static class Builder extends AemConfigBuilder {

		private Builder() {}

		@Override
		public Builder machineName(String machineName) {
			super.machineName(machineName);
			return this;
		}

		@Override
		public Builder port(Integer port) {
			super.port(port);
			return this;
		}

		@Override
		public Builder useSsl(Boolean useSsl) {
			super.useSsl(useSsl);
			return this;
		}

		@Override
		public Builder clientFactory(Supplier<Client> clientFactory) {
			super.clientFactory(clientFactory);
			return this;
		}

		@Override
		public Builder basicAuthentication(String username, String password) {
			super.basicAuthentication(username, password);
			return this;
		}

		@Override
		public Builder correlationId(Supplier<String> correlationIdFn) {
			super.correlationId(correlationIdFn);
			return this;
		}

		@Override
		public Builder aemServerType(AemServerType serverType) {
			super.aemServerType(serverType);
			return this;
		}
		
		public AemOutputServicePsGeneration build() {
			RestServicesOutputServiceAdapter adapter = setBuilderFields(RestServicesOutputServiceAdapter.builder()).build();
			return new AemOutputServicePsGeneration(new OutputServiceImpl(adapter, UsageContext.CLIENT_SIDE));
		}
	}
}
