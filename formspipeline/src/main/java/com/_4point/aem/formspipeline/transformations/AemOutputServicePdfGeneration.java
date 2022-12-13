package com._4point.aem.formspipeline.transformations;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.docservices.rest_services.client.helpers.AemServerType;
import com._4point.aem.docservices.rest_services.client.output.RestServicesOutputServiceAdapter;
import com._4point.aem.fluentforms.api.output.OutputService;
import com._4point.aem.fluentforms.api.output.OutputService.GeneratePdfOutputArgumentBuilder;
import com._4point.aem.fluentforms.api.output.OutputService.OutputServiceException;
import com._4point.aem.fluentforms.impl.UsageContext;
import com._4point.aem.fluentforms.impl.output.OutputServiceImpl;
import com._4point.aem.formspipeline.aem.AemConfigBuilder;
import com._4point.aem.formspipeline.api.Constants;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.OutputGeneration;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk.PdfOutputContext;

public class AemOutputServicePdfGeneration<D extends Context, T extends DataChunk<D>> implements OutputGeneration<T, PdfOutputChunk<D>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePdfGeneration.class);

	public static final String AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX = Constants.PROPERTY_PREFIX + "aem_output_pdf_gen.";
	public static final String CONTENT_ROOT = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "content_root";
	public static final String EMBED_FONTS = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "embed_fonts";
	public static final String TAGGED_PDF = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "tagged_pdf";
	public static final String LINEARIZED_PDF = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "linearized_pdf";
	public static final String RETAIN_PFD_FORM_STATE = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "retain_pdf_form_state";
	public static final String RETAIN_UNSIGNED_SIGANTURE_FIELDS = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "retain_unsigned_signature_fields";
	public static final String ACROBAT_VERSION = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "acrobat_version";
	public static final String TEMPLATE = AEM_OUTPUT_SERVICE_PDF_GEN_PREFIX + "template";
	
	private final OutputService outputService;
	
	private AemOutputServicePdfGeneration(OutputService outputService) {
		this.outputService = outputService;
	}

	@Override
	public PdfOutputChunk<D> process(T dataChunk) {
		D dataContext = dataChunk.dataContext();
		String template = dataContext.getString(TEMPLATE).orElseThrow(()->new IllegalArgumentException("Template parameter (" + TEMPLATE + ") not found."));
		GeneratePdfOutputArgumentBuilder builder = outputService.generatePDFOutput();

		try {
			builder.executeOn(Path.of(template), dataChunk.asInputStream());
		} catch (FileNotFoundException | OutputServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Builder builder() {
		return new Builder();
	}
	
	private GeneratePdfOutputArgumentBuilder setArguments(GeneratePdfOutputArgumentBuilder builder) {
		
		return builder;
	}
	
	public class Builder extends AemConfigBuilder {

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
		
		public AemOutputServicePdfGeneration<D, T> build() {
			RestServicesOutputServiceAdapter adapter = setBuilderFields(RestServicesOutputServiceAdapter.builder()).build();
			return new AemOutputServicePdfGeneration<>(new OutputServiceImpl(adapter, UsageContext.CLIENT_SIDE));
		}
	}
}
