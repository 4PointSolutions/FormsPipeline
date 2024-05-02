package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.chunks.PsOutputChunk;
import com._4point.aem.formspipeline.chunks.SimpleChunk;
import com._4point.aem.formspipeline.transformations.AemConvertPdfToPsService.AemConvertPdfToPsServiceContext;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@WireMockTest
class AemConvertPdfToPsServiceTest {

	private static final String TEST_CHUNK_DATA_STRING = "<root>data bytes</root>";

	private AemConvertPdfToPsService<Context> underTest;
	
	private static final boolean WIREMOCK_RECORDING = false;
	
	@BeforeEach
	void setUp(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
		underTest = AemConvertPdfToPsService.builder()
				.machineName("localhost")
				.basicAuthentication("admin", "admin")
				.port(wmRuntimeInfo.getHttpPort())
				.build(); 
				
		if (WIREMOCK_RECORDING) {
			String realServiceBaseUri = new URI("http://localhost:4502").toString();
			WireMock.startRecording(realServiceBaseUri);
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		if (WIREMOCK_RECORDING) {
			SnapshotRecordResult recordings = WireMock.stopRecording();
			List<StubMapping> mappings = recordings.getStubMappings();
			System.out.println("Found " + mappings.size() + " recordings.");
			for (StubMapping mapping : mappings) {
				ResponseDefinition response = mapping.getResponse();
				var jsonBody = response.getJsonBody();
				System.out.println(jsonBody == null ? "JsonBody is null" : jsonBody.toPrettyString());
			}
		}
	}
	
	@Test
	void testProcess() throws Exception {
		Context context = AemConvertPdfToPsServiceContext.contextWriter()
														 .psLevel(AemConvertPdfToPsServiceContext.PSLEVEL__LEVEL_3)
														 .fontInclusion(AemConvertPdfToPsServiceContext.FONT_INCLUSION__EMBEDDED_AND_REFERENCED_FONTS)
														 .build();

		PsOutputChunk<Context> result = underTest.process(PdfOutputChunk.createSimple(context, TEST_CHUNK_DATA_STRING.getBytes()));
		
		assertNotNull(result);
	}

	@Nested
	static class ContextReaderWriterTests {
		// Make sure we can write and then read everything
		@Test
		void testContextReaderWriter_populatedValues() {
			
			String color = AemConvertPdfToPsServiceContext.COLOR__COMPOSITE;
			String fontInclusion = AemConvertPdfToPsServiceContext.FONT_INCLUSION__EMBEDDED_AND_REFERENCED_FONTS;
			String lineWeight = AemConvertPdfToPsServiceContext.LINE_WEIGHT__POINT_25;
			String stringValue = "12";
			String pageSize = AemConvertPdfToPsServiceContext.PAGE_SIZE__LETTER;
			String psLevel = AemConvertPdfToPsServiceContext.PSLEVEL__LEVEL_3;
			String style = AemConvertPdfToPsServiceContext.STYLE__DEFAULT;
			Boolean boolValue = true;
			
			Context context = AemConvertPdfToPsServiceContext.contextWriter()
					 .color(color)
					 .fontInclusion(fontInclusion)
					 .lineWeight(lineWeight)
					 .pageRange(stringValue)
					 .pageSize(pageSize)
					 .pageSizeHeight(stringValue)
					 .pageSizeWidth(stringValue)
					 .psLevel(psLevel)
					 .style(style)
					 .allowBinaryContent(boolValue)
					 .bleedMarks(boolValue)
					 .colorBars(boolValue)
					 .convertTrueTypeToType1(boolValue)
					 .emitCIDFontType2(boolValue)
					 .emitPSFormObjects(boolValue)
					 .expandToFit(boolValue)
					 .includeComments(boolValue)
					 .legacyToSimplePSFlag(boolValue)
					 .pageInformation(boolValue)
					 .registrationMarks(boolValue)
					 .reverse(boolValue)
					 .rotateAndCenter(boolValue)
					 .shrinkToFit(boolValue)
					 .trimMarks(boolValue)
					 .useMaxJPEGImageResolution(boolValue)
					 .build();

			var reader = AemConvertPdfToPsServiceContext.contextReader(context);
			
			assertAll(
				()->assertEquals(color, reader.color().orElseThrow()),
				()->assertEquals(fontInclusion, reader.fontInclusion().orElseThrow()),
				()->assertEquals(lineWeight, reader.lineWeight().orElseThrow()),
				()->assertEquals(stringValue, reader.pageRange().orElseThrow()),
				()->assertEquals(pageSize, reader.pageSize().orElseThrow()),
				()->assertEquals(stringValue, reader.pageSizeHeight().orElseThrow()),
				()->assertEquals(stringValue, reader.pageSizeWidth().orElseThrow()),
				()->assertEquals(psLevel, reader.psLevel().orElseThrow()),
				()->assertEquals(style, reader.style().orElseThrow()),
				()->assertEquals(boolValue, reader.allowBinaryContent().orElseThrow()),
				()->assertEquals(boolValue, reader.bleedMarks().orElseThrow()),
				()->assertEquals(boolValue, reader.colorBars().orElseThrow()),
				()->assertEquals(boolValue, reader.convertTrueTypeToType1().orElseThrow()),
				()->assertEquals(boolValue, reader.emitCIDFontType2().orElseThrow()),
				()->assertEquals(boolValue, reader.emitPSFormObjects().orElseThrow()),
				()->assertEquals(boolValue, reader.expandToFit().orElseThrow()),
				()->assertEquals(boolValue, reader.includeComments().orElseThrow()),
				()->assertEquals(boolValue, reader.legacyToSimplePSFlag().orElseThrow()),
				()->assertEquals(boolValue, reader.pageInformation().orElseThrow()),
				()->assertEquals(boolValue, reader.registrationMarks().orElseThrow()),
				()->assertEquals(boolValue, reader.reverse().orElseThrow()),
				()->assertEquals(boolValue, reader.rotateAndCenter().orElseThrow()),
				()->assertEquals(boolValue, reader.shrinkToFit().orElseThrow()),
				()->assertEquals(boolValue, reader.trimMarks().orElseThrow()),
				()->assertEquals(boolValue, reader.useMaxJPEGImageResolution().orElseThrow())
			);
		}
	
		@Test
		void testContextReaderWriter_unpopulatedValues() {
			Context context = AemConvertPdfToPsServiceContext.contextWriter()
															 .build();
			
			var reader = AemConvertPdfToPsServiceContext.contextReader(context);
			
			assertAll(
				()->assertTrue(reader.color().isEmpty(), "color is expected to be empty but is not empty"),
				()->assertTrue(reader.fontInclusion().isEmpty(), "fontInclusion is expected to be empty but is not empty"),
				()->assertTrue(reader.lineWeight().isEmpty(), "lineWeight is expected to be empty but is not empty"),
				()->assertTrue(reader.pageRange().isEmpty(), "pageRange is expected to be empty but is not empty"),
				()->assertTrue(reader.pageSize().isEmpty(), "pageSize is expected to be empty but is not empty"),
				()->assertTrue(reader.pageSizeHeight().isEmpty(), "pageSizeHeight is expected to be empty but is not empty"),
				()->assertTrue(reader.pageSizeWidth().isEmpty(), "pageSizeWidth is expected to be empty but is not empty"),
				()->assertTrue(reader.psLevel().isEmpty(), "psLevel is expected to be empty but is not empty"),
				()->assertTrue(reader.style().isEmpty(), "style is expected to be empty but is not empty"),
				()->assertTrue(reader.allowBinaryContent().isEmpty(), "allowBinaryContent is expected to be empty but is not empty"),
				()->assertTrue(reader.bleedMarks().isEmpty(), "bleedMarks is expected to be empty but is not empty"),
				()->assertTrue(reader.colorBars().isEmpty(), "colorBars is expected to be empty but is not empty"),
				()->assertTrue(reader.convertTrueTypeToType1().isEmpty(), "convertTrueTypeToType1 is expected to be empty but is not empty"),
				()->assertTrue(reader.emitCIDFontType2().isEmpty(), "emitCIDFontType2 is expected to be empty but is not empty"),
				()->assertTrue(reader.emitPSFormObjects().isEmpty(), "emitPSFormObjects is expected to be empty but is not empty"),
				()->assertTrue(reader.expandToFit().isEmpty(), "expandToFit is expected to be empty but is not empty"),
				()->assertTrue(reader.includeComments().isEmpty(), "includeComments is expected to be empty but is not empty"),
				()->assertTrue(reader.legacyToSimplePSFlag().isEmpty(), "legacyToSimplePSFlag is expected to be empty but is not empty"),
				()->assertTrue(reader.pageInformation().isEmpty(), "pageInformation is expected to be empty but is not empty"),
				()->assertTrue(reader.registrationMarks().isEmpty(), "registrationMarks is expected to be empty but is not empty"),
				()->assertTrue(reader.reverse().isEmpty(), "reverse is expected to be empty but is not empty"),
				()->assertTrue(reader.rotateAndCenter().isEmpty(), "rotateAndCenter is expected to be empty but is not empty"),
				()->assertTrue(reader.shrinkToFit().isEmpty(), "shrinkToFit is expected to be empty but is not empty"),
				()->assertTrue(reader.trimMarks().isEmpty(), "trimMarks is expected to be empty but is not empty"),
				()->assertTrue(reader.useMaxJPEGImageResolution().isEmpty(), "useMaxJPEGImageResolution is expected to be empty but is not empty")
			);
		}
	}
}
