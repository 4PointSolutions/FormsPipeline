package com._4point.aem.formspipeline.transformations;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com._4point.aem.docservices.rest_services.client.convertPdf.RestServicesConvertPdfServiceAdapter;
import com._4point.aem.docservices.rest_services.client.helpers.AemServerType;
import com._4point.aem.fluentforms.api.Document;
import com._4point.aem.fluentforms.api.convertPdf.ConvertPdfService;
import com._4point.aem.fluentforms.api.convertPdf.ConvertPdfService.ConvertPdfServiceException;
import com._4point.aem.fluentforms.api.convertPdf.ConvertPdfService.ToPSArgumentBuilder;
import com._4point.aem.fluentforms.impl.convertPdf.ConvertPdfServiceImpl;
import com._4point.aem.formspipeline.aem.AemConfigBuilder;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Context.ContextBuilder;
import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.chunks.PdfPayload;
import com._4point.aem.formspipeline.chunks.PsPayload;
import com._4point.aem.formspipeline.contexts.MapContext;
import com.adobe.fd.cpdf.api.enumeration.Color;
import com.adobe.fd.cpdf.api.enumeration.FontInclusion;
import com.adobe.fd.cpdf.api.enumeration.LineWeight;
import com.adobe.fd.cpdf.api.enumeration.PSLevel;
import com.adobe.fd.cpdf.api.enumeration.PageSize;
import com.adobe.fd.cpdf.api.enumeration.Style;

import jakarta.ws.rs.client.Client;

public class AemConvertPdfToPsService implements DataTransformationOneToOne<Message<PdfPayload>, Message<PsPayload>> {
	private final ConvertPdfService convertPdfService;
	
	public AemConvertPdfToPsService(ConvertPdfService convertPdfService) {
		this.convertPdfService = convertPdfService;
	}
	
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Message<PsPayload> process(Message<PdfPayload> msg) {
		OptionalInt pageCount = msg.payload().numPages();
		var myContext = new AemConvertPdfToPsServiceContext.ContextReader(msg.context());
		try {
			Document result = myContext.transferAllSettings(convertPdfService.toPS())
									   .executeOn(msg.payload().bytes());
			
			var psPayload = pageCount.isPresent() ? new PsPayload(result.getInputStream().readAllBytes(), pageCount.getAsInt())
										 		  : new PsPayload(result.getInputStream().readAllBytes());
			
			return MessageBuilder.fromMessageReplacingPayload(msg, psPayload);
		} catch (ConvertPdfServiceException | IOException e) {
			throw new IllegalStateException("Error while converting PDF document to PostScript.", e);
		}
	}

	public static class AemConvertPdfToPsServiceContext {
		private static final String AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX = Context.FORMSPIPELINE_PROPERTY_PREFIX + "aem_convert_pdf_to_ps.";
		private static final String COLOR = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "color";
		private static final String FONT_INCLUSION = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "fontInclusion";
		private static final String LINE_WEIGHT = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "lineWeight";
		private static final String PAGE_RANGE = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "pageRange";
		private static final String PAGE_SIZE = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "pageSize";
		private static final String PAGE_SIZE_HEIGHT = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "pageSizeHeight";
		private static final String PAGE_SIZE_WIDTH = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "pageSizeWidth";
		private static final String PS_LEVEL = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "psLevel";
		private static final String STYLE = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "style";
		private static final String ALLOW_BINARY_CONTENT = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "allowBinaryContent";
		private static final String BLEED_MARKS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "bleedMarks";
		private static final String COLOR_BARS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "colorBars";
		private static final String CONVERT_TRUE_TYPE = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "convertTrueTypeToType1";
		private static final String EMIT_CID_FONTS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "emitCIDFontType2";
		private static final String EMIT_PS_FORM_OBJS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "emitPSFormObjects";
		private static final String EXPAND_TO_FIT = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "expandToFit";
		private static final String INCLUDE_COMMENTS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "includeComments";
		private static final String SIMPLE_PS_FLAG = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "legacyToSimplePSFlag";
		private static final String PAGE_INFORMATION = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "pageInformation";
		private static final String REGISTRATION_MARKS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "registrationMarks";
		private static final String REVERSE = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "reverse";
		private static final String ROTATE_AND_CENTER = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "rotateAndCenter";
		private static final String SHRINK_TO_FIT = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "shrinkToFit";
		private static final String TRIM_MARKS = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "trimMarks";
		private static final String USE_MAX_JPG_RESOLUTION = AEM_CONVERT_PDF_TO_PS_SERVICE_PREFIX + "useMaxJPEGImageResolution";
		
		public static final String COLOR__COMPOSITE = Color.composite.toString();
		public static final String COLOR__COMPOSITE_GRAY = Color.compositeGray.toString();
		public static final String FONT_INCLUSION__EMBEDDED_AND_REFERENCED_FONTS = FontInclusion.embeddedAndReferencedFonts.toString();
		public static final String FONT_INCLUSION__EMBEDDED_FONTS = FontInclusion.embeddedFonts.toString();
		public static final String FONT_INCLUSION__NONE = FontInclusion.none.toString();
		public static final String LINE_WEIGHT__POINT_5 = LineWeight.point5.toString();
		public static final String LINE_WEIGHT__POINT_25 = LineWeight.point25.toString();
		public static final String LINE_WEIGHT__POINT_125 = LineWeight.point125.toString();
		public static final String PAGE_SIZE__A2 = PageSize.A2.toString();
		public static final String PAGE_SIZE__A3 = PageSize.A3.toString();
		public static final String PAGE_SIZE__A4 = PageSize.A4.toString();
		public static final String PAGE_SIZE__A5 = PageSize.A5.toString();
		public static final String PAGE_SIZE__CUSTOM = PageSize.Custom.toString();
		public static final String PAGE_SIZE__DETERMINE_AUTOMATICALLY = PageSize.DetermineAutomatically.toString();
		public static final String PAGE_SIZE__ENVELOPE = PageSize.Envelope.toString();
		public static final String PAGE_SIZE__EXECUTIVE = PageSize.Executive.toString();
		public static final String PAGE_SIZE__FOLIO = PageSize.Folio.toString();
		public static final String PAGE_SIZE__LEGAL = PageSize.Legal.toString();
		public static final String PAGE_SIZE__LETTER = PageSize.Letter.toString();
		public static final String PAGE_SIZE__TABLOID = PageSize.Tabloid.toString();
		public static final String PSLEVEL__LEVEL_2 = PSLevel.LEVEL_2.toString();
		public static final String PSLEVEL__LEVEL_3 = PSLevel.LEVEL_3.toString();
		public static final String STYLE__DEFAULT = Style.Default.toString();
		public static final String STYLE__ILLUSTRATOR = Style.Illustrator.toString();
		public static final String STYLE__ILLUSTRATOR_J = Style.IllustratorJ.toString();
		public static final String STYLE__INDESIGN_J1 = Style.InDesignJ1.toString();
		public static final String STYLE__INDESIGN_J2 = Style.InDesignJ2.toString();
		public static final String STYLE_QUARK_X_PRESS = Style.QuarkXPress.toString();
		

		public static ContextWriter contextWriter() 				{ return new ContextWriter(); }
		public static ContextReader contextReader(Context context) 	{ return new ContextReader(context); }

		public static class ContextReader {
			private final Context context;

			private ContextReader(Context context) { this.context = context; }

			public Optional<String> color() 									{ return context.getString(COLOR); }
			public Optional<String> fontInclusion() 							{ return context.getString(FONT_INCLUSION); }
			public Optional<String> lineWeight() 								{ return context.getString(LINE_WEIGHT); }
			public Optional<String> pageRange() 								{ return context.getString(PAGE_RANGE); }
			public Optional<String> pageSize() 									{ return context.getString(PAGE_SIZE); }
			public Optional<String> pageSizeHeight() 							{ return context.getString(PAGE_SIZE_HEIGHT); }
			public Optional<String> pageSizeWidth() 							{ return context.getString(PAGE_SIZE_WIDTH); }
			public Optional<String> psLevel() 									{ return context.getString(PS_LEVEL); }
			public Optional<String> style() 									{ return context.getString(STYLE); }
			public Optional<Boolean> allowBinaryContent() 						{ return context.getBoolean(ALLOW_BINARY_CONTENT); }
			public Optional<Boolean> bleedMarks() 								{ return context.getBoolean(BLEED_MARKS); }
			public Optional<Boolean> colorBars() 								{ return context.getBoolean(COLOR_BARS); }
			public Optional<Boolean> convertTrueTypeToType1() 					{ return context.getBoolean(CONVERT_TRUE_TYPE); }
			public Optional<Boolean> emitCIDFontType2() 						{ return context.getBoolean(EMIT_CID_FONTS); }
			public Optional<Boolean> emitPSFormObjects() 						{ return context.getBoolean(EMIT_PS_FORM_OBJS); }
			public Optional<Boolean> expandToFit() 								{ return context.getBoolean(EXPAND_TO_FIT); }
			public Optional<Boolean> includeComments() 							{ return context.getBoolean(INCLUDE_COMMENTS); }
			public Optional<Boolean> legacyToSimplePSFlag() 					{ return context.getBoolean(SIMPLE_PS_FLAG); }
			public Optional<Boolean> pageInformation() 							{ return context.getBoolean(PAGE_INFORMATION); }
			public Optional<Boolean> registrationMarks() 						{ return context.getBoolean(REGISTRATION_MARKS); }
			public Optional<Boolean> reverse() 									{ return context.getBoolean(REVERSE); }
			public Optional<Boolean> rotateAndCenter()		 					{ return context.getBoolean(ROTATE_AND_CENTER); }
			public Optional<Boolean> shrinkToFit() 								{ return context.getBoolean(SHRINK_TO_FIT); }
			public Optional<Boolean> trimMarks() 								{ return context.getBoolean(TRIM_MARKS); }
			public Optional<Boolean> useMaxJPEGImageResolution() 				{ return context.getBoolean(USE_MAX_JPG_RESOLUTION); }

			// Transfer all the settings that are present over to the builder.
			private ToPSArgumentBuilder transferAllSettings(ToPSArgumentBuilder builder) {
				return Stream.of(builder)
						.map(b->transferOneSetting(b, color(), v->b.setColor(Color.valueOf(v))))
						.map(b->transferOneSetting(b, fontInclusion(), v->b.setFontInclusion(FontInclusion.valueOf(v))))
						.map(b->transferOneSetting(b, lineWeight(), v->b.setLineWeight(LineWeight.valueOf(v))))
						.map(b->transferOneSetting(b, pageRange(), v->b.setPageRange(v)))
						.map(b->transferOneSetting(b, pageSize(), v->b.setPageSize(PageSize.valueOf(v))))
						.map(b->transferOneSetting(b, pageSizeHeight(), v->b.setPageSizeHeight(v)))
						.map(b->transferOneSetting(b, pageSizeWidth(), v->b.setPageSizeWidth(v)))
						.map(b->transferOneSetting(b, psLevel(), v->b.setPsLevel(PSLevel.valueOf(v))))
						.map(b->transferOneSetting(b, style(), v->b.setStyle(Style.valueOf(v))))
						.map(b->transferOneSetting(b, allowBinaryContent(), v->b.setAllowedBinaryContent(v)))
						.map(b->transferOneSetting(b, bleedMarks(), v->b.setBleedMarks(v)))
						.map(b->transferOneSetting(b, colorBars(), v->b.setColorBars(v)))
						.map(b->transferOneSetting(b, convertTrueTypeToType1(), v->b.setConvertTrueTypeToType1(v)))
						.map(b->transferOneSetting(b, emitCIDFontType2(), v->b.setEmitCIDFontType2(v)))
						.map(b->transferOneSetting(b, emitPSFormObjects(), v->b.setEmitPSFormObjects(v)))
						.map(b->transferOneSetting(b, expandToFit(), v->b.setExpandToFit(v)))
						.map(b->transferOneSetting(b, includeComments(), v->b.setIncludeComments(v)))
						.map(b->transferOneSetting(b, legacyToSimplePSFlag(), v->b.setLegacyToSimplePSFlag(v)))
						.map(b->transferOneSetting(b, pageInformation(), v->b.setPageInformation(v)))
						.map(b->transferOneSetting(b, registrationMarks(), v->b.setRegistrationMarks(v)))
						.map(b->transferOneSetting(b, reverse(), v->b.setReverse(v)))
						.map(b->transferOneSetting(b, rotateAndCenter(), v->b.setRotateAndCenter(v)))
						.map(b->transferOneSetting(b, shrinkToFit(), v->b.setShrinkToFit(v)))
						.map(b->transferOneSetting(b, trimMarks(), v->b.setTrimMarks(v)))
						.map(b->transferOneSetting(b, useMaxJPEGImageResolution(), v->b.setUseMaxJPEGImageResolution(v)))
						.findFirst().get();
			}
			
			// Transfer one setting over to the builder if and only if that setting is present
			private <T> ToPSArgumentBuilder transferOneSetting(ToPSArgumentBuilder b, Optional<T> v, Function<T, ToPSArgumentBuilder> s) {
				return v.map(s).orElse(b);
			}
		}

		public static class ContextWriter {
			private final ContextBuilder builder;
			
			private ContextWriter() 											{ this(MapContext.builder());}
			private ContextWriter(ContextBuilder builder) 						{ this.builder = builder;}
			
			public ContextWriter color(String value) 							{ builder.put(COLOR , value); return this; };
			public ContextWriter fontInclusion(String value) 					{ builder.put(FONT_INCLUSION , value); return this; };
			public ContextWriter lineWeight(String value) 						{ builder.put(LINE_WEIGHT , value); return this; };
			public ContextWriter pageRange(String value) 						{ builder.put(PAGE_RANGE , value); return this; };
			public ContextWriter pageSize(String value) 						{ builder.put(PAGE_SIZE , value); return this; };
			public ContextWriter pageSizeHeight(String value) 					{ builder.put(PAGE_SIZE_HEIGHT , value); return this; };
			public ContextWriter pageSizeWidth(String value) 					{ builder.put(PAGE_SIZE_WIDTH , value); return this; };
			public ContextWriter psLevel(String value) 							{ builder.put(PS_LEVEL , value); return this; };
			public ContextWriter style(String value) 							{ builder.put(STYLE , value); return this; };
			public ContextWriter allowBinaryContent(Boolean value) 				{ builder.put(ALLOW_BINARY_CONTENT , value); return this; };
			public ContextWriter bleedMarks(Boolean value) 						{ builder.put(BLEED_MARKS , value); return this; };
			public ContextWriter colorBars(Boolean value) 						{ builder.put(COLOR_BARS , value); return this; };
			public ContextWriter convertTrueTypeToType1(Boolean value) 			{ builder.put(CONVERT_TRUE_TYPE , value); return this; };
			public ContextWriter emitCIDFontType2(Boolean value) 				{ builder.put(EMIT_CID_FONTS , value); return this; };
			public ContextWriter emitPSFormObjects(Boolean value) 				{ builder.put(EMIT_PS_FORM_OBJS , value); return this; };
			public ContextWriter expandToFit(Boolean value) 					{ builder.put(EXPAND_TO_FIT , value); return this; };
			public ContextWriter includeComments(Boolean value) 				{ builder.put(INCLUDE_COMMENTS , value); return this; };
			public ContextWriter legacyToSimplePSFlag(Boolean value) 			{ builder.put(SIMPLE_PS_FLAG , value); return this; };
			public ContextWriter pageInformation(Boolean value) 				{ builder.put(PAGE_INFORMATION , value); return this; };
			public ContextWriter registrationMarks(Boolean value) 				{ builder.put(REGISTRATION_MARKS , value); return this; };
			public ContextWriter reverse(Boolean value) 						{ builder.put(REVERSE , value); return this; };
			public ContextWriter rotateAndCenter(Boolean value) 				{ builder.put(ROTATE_AND_CENTER , value); return this; };
			public ContextWriter shrinkToFit(Boolean value) 					{ builder.put(SHRINK_TO_FIT , value); return this; };
			public ContextWriter trimMarks(Boolean value) 						{ builder.put(TRIM_MARKS , value); return this; };
			public ContextWriter useMaxJPEGImageResolution(Boolean value) 		{ builder.put(USE_MAX_JPG_RESOLUTION , value); return this; };
			
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
		
		public AemConvertPdfToPsService build() {
			RestServicesConvertPdfServiceAdapter adapter = setBuilderFields(RestServicesConvertPdfServiceAdapter.builder()).build();
			return new AemConvertPdfToPsService(new ConvertPdfServiceImpl(adapter));
		}
	}
}
