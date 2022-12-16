package com._4point.aem.formspipeline.transformations;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com._4point.aem.fluentforms.api.PathOrUrl;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.chunks.SimpleChunk;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.transformations.AemOutputServicePdfGeneration.AemOutputServicePdfGenerationContext;
import com.adobe.fd.output.api.AcrobatVersion;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@WireMockTest
class AemOutputServicePdfGenerationTest {
	
	private static final String TEST_CHUNK_DATA_STRING = "<root>data bytes</root>";
	private static final byte[] TEST_CHUNK_DATA_BYTES = TEST_CHUNK_DATA_STRING.getBytes(StandardCharsets.UTF_8);

	private class CustomDataChunk implements DataChunk<Context> {
		private final SimpleChunk chunk;
		private final Context context;

		public CustomDataChunk(String chunkData, Context context) {
			this.chunk = new SimpleChunk(chunkData);
			this.context = context;
		}

		@Override
		public byte[] bytes() {
			return chunk.bytes();
		}

		@Override
		public Context dataContext() {
			return this.context;
		}
	}

	
	private AemOutputServicePdfGeneration<Context, CustomDataChunk> underTest;
			
	private static final boolean WIREMOCK_RECORDING = false;

	@BeforeEach
	void setUp(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
		underTest = AemOutputServicePdfGeneration.builder()
				.machineName("localhost")
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

//	@Disabled("This test is not yet complete.")
	@Test
	void testProcess() {
		Context context = AemOutputServicePdfGenerationContext.contextWriter()
															  .template("Foo.xdp")
															  .build();
		
		PdfOutputChunk<Context> result = underTest.process(new CustomDataChunk(TEST_CHUNK_DATA_STRING, context));
		
		assertNotNull(result);
//		assertresult.bytes()
	}

	@Test
	void testProcess_throwsException() {
		String template = "ThrowsException.xdp";
		Context context = AemOutputServicePdfGenerationContext.contextWriter()
															  .template(template)
															  .build();
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(new CustomDataChunk(TEST_CHUNK_DATA_STRING, context)));
		
		String msg = ex.getMessage();
		assertNotNull(msg);
		assertThat(msg, allOf(containsStringIgnoringCase("Error while generating PDF from template"), containsString(template)));
	}

	@Test
	void testProcess_MissingTemplate() {
		// No template argument in the context.
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()->underTest.process(new CustomDataChunk(TEST_CHUNK_DATA_STRING, EmptyContext.emptyInstance())));
		
		String msg = ex.getMessage();
		assertNotNull(msg);
		assertThat(msg, allOf(containsStringIgnoringCase("Template parameter"), 
							  containsString("formspipeline.aem_output_pdf_gen.template"),
							  containsStringIgnoringCase("not found")
							  ));
	}
	
	@Nested
	class ContextReaderWriterTests {
		// Make sure we can write and then read everything
		
		@Test
		void testContextReaderWriter_populatedValues() {
			var contentRoot = PathOrUrl.from("/foo/bar");
			var embedFonts = true;
			var taggedPdf = false;
			var linearizedPdf = true;
			var retainPdfFormState = false;
			var retainUnsignedSignatureFields = true;
			var acrobatVersion = AcrobatVersion.Acrobat_10_1;
			var template = PathOrUrl.from("SomeXdp.xdp");

			Context context = AemOutputServicePdfGenerationContext.contextWriter()
					.contentRoot(contentRoot)
					.embedFonts(embedFonts)
					.taggedPdf(taggedPdf)
					.linearizedPdf(linearizedPdf)
					.retainPdfFormState(retainPdfFormState)
					.retainUnsignedSignatureFields(retainUnsignedSignatureFields)
					.acrobatVersion(acrobatVersion)
					.template(template)
					.build();
			
			var reader = AemOutputServicePdfGenerationContext.contextReader(context);
			assertAll(
					()->assertEquals(contentRoot, reader.contentRoot().orElseThrow()),
					()->assertEquals(embedFonts, reader.embedFonts().orElseThrow()),
					()->assertEquals(taggedPdf, reader.taggedPdf().orElseThrow()),
					()->assertEquals(linearizedPdf, reader.linearizedPdf().orElseThrow()),
					()->assertEquals(retainPdfFormState, reader.retainPdfFormState().orElseThrow()),
					()->assertEquals(retainUnsignedSignatureFields, reader.retainUnsignedSignatureFields().orElseThrow()),
					()->assertEquals(acrobatVersion, reader.acrobatVersion().orElseThrow()),
					()->assertEquals(template, reader.template())
					);
			
			}

		@Test
		void testContextReaderWriter_umpopulatedValues() {
			Context context = AemOutputServicePdfGenerationContext.contextWriter()
					.build();
			
			var reader = AemOutputServicePdfGenerationContext.contextReader(context);
			assertAll(
					()->assertTrue(reader.contentRoot().isEmpty()),
					()->assertTrue(reader.embedFonts().isEmpty()),
					()->assertTrue(reader.taggedPdf().isEmpty()),
					()->assertTrue(reader.linearizedPdf().isEmpty()),
					()->assertTrue(reader.retainPdfFormState().isEmpty()),
					()->assertTrue(reader.retainUnsignedSignatureFields().isEmpty()),
					()->assertTrue(reader.acrobatVersion().isEmpty()),
					()->{
						IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()->reader.template());
						String msg = ex.getMessage();
						assertNotNull(msg);
						assertThat(msg, allOf(containsStringIgnoringCase("Template parameter"), containsStringIgnoringCase("not found")));
						}
					);
			}

		// Test the convenience setters that convert to PathOrUrl
		@Test
		void testContextReaderWriter_convertedValues() throws Exception {
			String filePrefix = "file:";
			String contentRootStr = "/foo/bar";
			Path contentRootPath = Path.of(contentRootStr);
			URL contentRootUrl = new URL(filePrefix + contentRootStr);
			var contentRoot = PathOrUrl.from(contentRootStr);
			String templateStr = "SomeXdp.xdp";
			Path templatePath = Path.of(templateStr);
			URL templateUrl = new URL(filePrefix + templateStr);
			var template = PathOrUrl.from(templateStr);
			AcrobatVersion acroVersion = AcrobatVersion.Acrobat_11;
			String acroVersionStr = acroVersion.toString();
					

			Context contextStr = AemOutputServicePdfGenerationContext.contextWriter()
					.contentRoot(contentRootStr)
					.template(templateStr)
					.acrobatVersion(acroVersionStr)
					.build();
			var readerStr = AemOutputServicePdfGenerationContext.contextReader(contextStr);

			Context contextPath = AemOutputServicePdfGenerationContext.contextWriter()
					.contentRoot(contentRootPath)
					.template(templatePath)
					.build();
			var readerPath = AemOutputServicePdfGenerationContext.contextReader(contextPath);

			Context contextUrl = AemOutputServicePdfGenerationContext.contextWriter()
					.contentRoot(contentRootUrl)
					.template(templateUrl)
					.build();
			var readerUrl = AemOutputServicePdfGenerationContext.contextReader(contextUrl);

			assertAll(
					()->assertEquals(contentRoot, readerStr.contentRoot().orElseThrow()),
					()->assertEquals(template, readerStr.template()),
					()->assertEquals(acroVersion, readerStr.acrobatVersion().orElseThrow()),
					()->assertEquals(contentRoot, readerPath.contentRoot().orElseThrow()),
					()->assertEquals(template, readerPath.template())
					);
			
			}
	}

}
