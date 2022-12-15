package com._4point.aem.formspipeline.transformations;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.docservices.rest_services.client.helpers.AemServerType;
import com._4point.aem.docservices.rest_services.client.output.RestServicesOutputServiceAdapter;
import com._4point.aem.fluentforms.api.Document;
import com._4point.aem.fluentforms.api.PathOrUrl;
import com._4point.aem.fluentforms.api.output.OutputService;
import com._4point.aem.fluentforms.api.output.OutputService.GeneratePdfOutputArgumentBuilder;
import com._4point.aem.fluentforms.api.output.OutputService.OutputServiceException;
import com._4point.aem.fluentforms.impl.UsageContext;
import com._4point.aem.fluentforms.impl.output.OutputServiceImpl;
import com._4point.aem.formspipeline.aem.AemConfigBuilder;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Context.ContextBuilder;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.OutputGeneration;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.contexts.MapContext;
import com.adobe.fd.output.api.AcrobatVersion;

public class AemOutputServicePdfGeneration<D extends Context, T extends DataChunk<D>> implements OutputGeneration<T, PdfOutputChunk<D>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePdfGeneration.class);

	private final OutputService outputService;
	
	private AemOutputServicePdfGeneration(OutputService outputService) {
		this.outputService = outputService;
	}

	@Override
	public PdfOutputChunk<D> process(T dataChunk) {
		D dataContext = dataChunk.dataContext();
		var myContext = new AemOutputServicePdfGenerationContext.ContextReader(dataContext);
		PathOrUrl template = myContext.template();
		try {
			Document pdfResult = myContext.transferAllSettings(outputService.generatePDFOutput())
										  .executeOn(template, dataChunk.asInputStream());
			
			
			return PdfOutputChunk.createSimple(dataContext, pdfResult.getInputStream().readAllBytes());
		} catch (IOException | OutputServiceException e) {
			throw new IllegalStateException("Error while generating PDF from template (" + template.toString() + ").", e);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Class that allows for setting and retrieving AemOutputServicePdfGeneration parameters to and from the Context.
	 *
	 */
	public static class AemOutputServicePdfGenerationContext {
		private static final String AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX = Context.FORMSPIPELINE_PROPERTY_PREFIX + "aem_output_pdf_gen.";
		private static final String CONTENT_ROOT = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "content_root";
		private static final String EMBED_FONTS = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "embed_fonts";
		private static final String TAGGED_PDF = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "tagged_pdf";
		private static final String LINEARIZED_PDF = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "linearized_pdf";
		private static final String RETAIN_PDF_FORM_STATE = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "retain_pdf_form_state";
		private static final String RETAIN_UNSIGNED_SIGANTURE_FIELDS = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "retain_unsigned_signature_fields";
		private static final String ACROBAT_VERSION = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "acrobat_version";
		private static final String TEMPLATE = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "template";
		
		public static ContextWriter contextWriter() 				{ return new ContextWriter(); }
		public static ContextReader contextReader(Context context) 	{ return new ContextReader(context); }
		
		public static class ContextReader {
			private final Context context;

			private ContextReader(Context context) { this.context = context; }
			
			public Optional<PathOrUrl> contentRoot() 					{ return context.get(CONTENT_ROOT, PathOrUrl.class); }
			public Optional<Boolean> embedFonts() 						{ return context.getBoolean(EMBED_FONTS);}
			public Optional<Boolean> taggedPdf()						{ return context.getBoolean(TAGGED_PDF);}
			public Optional<Boolean> linearizedPdf()					{ return context.getBoolean(LINEARIZED_PDF);}
			public Optional<Boolean> retainPdfFormState()				{ return context.getBoolean(RETAIN_PDF_FORM_STATE);}
			public Optional<Boolean> retainUnsignedSignatureFields()	{ return context.getBoolean(RETAIN_UNSIGNED_SIGANTURE_FIELDS);}
			public Optional<AcrobatVersion> acrobatVersion()			{ return context.get(ACROBAT_VERSION, AcrobatVersion.class);}
			public PathOrUrl template() 								{ return context.get(TEMPLATE, PathOrUrl.class)
																						.orElseThrow(()->new IllegalArgumentException("Template parameter (" + TEMPLATE + ") not found.")); }
			
			// Transfer all the settings that are present over to the builder.
			private GeneratePdfOutputArgumentBuilder transferAllSettings(GeneratePdfOutputArgumentBuilder builder) {
				return Stream.of(builder)
							 .map(b->transferOneSetting(b, contentRoot(), b::setContentRoot))
							 .map(b->transferOneSetting(b, embedFonts(), b::setEmbedFonts))
							 .map(b->transferOneSetting(b, taggedPdf(), b::setTaggedPDF))
							 .map(b->transferOneSetting(b, linearizedPdf(), b::setLinearizedPDF))
							 .map(b->transferOneSetting(b, retainPdfFormState(), b::setRetainPDFFormState))
							 .map(b->transferOneSetting(b, retainUnsignedSignatureFields(), b::setRetainUnsignedSignatureFields))
							 .map(b->transferOneSetting(b, acrobatVersion(), b::setAcrobatVersion))
							 .findFirst().get();
			}
			
			// Transfer one setting over to the builder if and only if that setting is present
			private <T> GeneratePdfOutputArgumentBuilder transferOneSetting(GeneratePdfOutputArgumentBuilder b, Optional<T> v, Function<T, GeneratePdfOutputArgumentBuilder> s) {
				return v.map(s).orElse(b);
			}
		}

		public static class ContextWriter {
			private final ContextBuilder builder;
			
			private ContextWriter() 						{ this(MapContext.builder());}
			private ContextWriter(ContextBuilder builder) 	{ this.builder = builder;}

			public ContextWriter contentRoot(PathOrUrl value) 					{ builder.put(CONTENT_ROOT , value); return this;}
			public ContextWriter contentRoot(String value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
			public ContextWriter contentRoot(Path value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
			public ContextWriter contentRoot(URL value) 						{ contentRoot(PathOrUrl.from(value)); return this;}
			public ContextWriter embedFonts(Boolean value) 						{ builder.put(EMBED_FONTS , value); return this;}
			public ContextWriter taggedPdf(Boolean value) 						{ builder.put(TAGGED_PDF , value); return this;}
			public ContextWriter linearizedPdf(Boolean value) 					{ builder.put(LINEARIZED_PDF , value); return this;}
			public ContextWriter retainPdfFormState(Boolean value) 				{ builder.put(RETAIN_PDF_FORM_STATE , value); return this;}
			public ContextWriter retainUnsignedSignatureFields(Boolean value)	{ builder.put(RETAIN_UNSIGNED_SIGANTURE_FIELDS , value); return this;}
			public ContextWriter acrobatVersion(AcrobatVersion value) 			{ builder.put(ACROBAT_VERSION , value); return this;}
			public ContextWriter acrobatVersion(String value) 					{ acrobatVersion(AcrobatVersion.valueOf(value)); return this;}
			public ContextWriter template(PathOrUrl value) 						{ builder.put(TEMPLATE , value); return this;}
			public ContextWriter template(String value) 						{ template(PathOrUrl.from(value)); return this;}
			public ContextWriter template(Path value) 							{ template(PathOrUrl.from(value)); return this;}
			public ContextWriter template(URL value) 							{ template(PathOrUrl.from(value)); return this;}
			
			public Context build() 	{ return builder.build(); }
		}
		
	}
	
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
		
		public <D extends Context, T extends DataChunk<D>> AemOutputServicePdfGeneration<D,T> build() {
			RestServicesOutputServiceAdapter adapter = setBuilderFields(RestServicesOutputServiceAdapter.builder()).build();
			return new AemOutputServicePdfGeneration<>(new OutputServiceImpl(adapter, UsageContext.CLIENT_SIDE));
		}
	}
}
