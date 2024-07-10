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
import com._4point.aem.formspipeline.payloads.PclPayload;
import com._4point.aem.formspipeline.payloads.XmlPayload;
import com.adobe.fd.output.api.PaginationOverride;

import jakarta.ws.rs.client.Client;

public class AemOutputServicePclGeneration implements DataTransformationOneToOne<Message<XmlPayload>, Message<PclPayload>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePclGeneration.class);

	private final OutputService outputService;
	
	private AemOutputServicePclGeneration(OutputService outputService) {
		this.outputService = outputService;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	@Override
	public Message<PclPayload> process(Message<XmlPayload> msg) {
		Context dataContext = msg.context();
		var myContext = new AemOutputServicePclGenerationContext.ContextReader(dataContext);
		PathOrUrl template = myContext.template();
		try {
			Document result = myContext.transferAllSettings(outputService.generatePrintedOutput())
											  .executeOn(template, msg.payload().asInputStream());
			Optional<Long> pageCount = result.getPageCount();
			var pdfPayload = pageCount.isPresent() ? new PclPayload(result.getInputStream().readAllBytes(), pageCount.get().intValue())
												   : new PclPayload(result.getInputStream().readAllBytes());
			return MessageBuilder.createMessage(pdfPayload, msg.context());
		} catch (IOException | OutputServiceException  e) {
			throw new IllegalStateException("Error while generating PCL document from template (" + template.toString() + ").", e);
		}
	}
	
	/**
	 * Class that allows for setting and retrieving AemOutputServicePdfGeneration parameters to and from the Context.
	 *
	 */
	public static class AemOutputServicePclGenerationContext {
		
		private static final String AEM_OUTPUT_SERVICE_PRINT_GEN_PREFIX = Context.FORMSPIPELINE_PROPERTY_PREFIX + "aem_output_pcl_gen.";
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
					return Optional.ofNullable(PrintConfig.GenericColor_PCL_5c);
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
						.map(b->transferOneSetting(b, xci(), b::setXci))
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
		
		public AemOutputServicePclGeneration build() {
			RestServicesOutputServiceAdapter adapter = setBuilderFields(RestServicesOutputServiceAdapter.builder()).build();
			return new AemOutputServicePclGeneration(new OutputServiceImpl(adapter, UsageContext.CLIENT_SIDE));
		}
	}

}

