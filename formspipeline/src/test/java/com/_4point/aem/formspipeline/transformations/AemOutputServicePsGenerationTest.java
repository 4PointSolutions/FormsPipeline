package com._4point.aem.formspipeline.transformations;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com._4point.aem.fluentforms.api.PathOrUrl;
import com._4point.aem.fluentforms.api.output.PrintConfig;
import com._4point.aem.fluentforms.impl.SimpleDocumentFactoryImpl;
import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.chunks.PsPayload;
import com._4point.aem.formspipeline.chunks.XmlPayload;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.transformations.AemOutputServicePsGeneration.AemOutputServicePsGenerationContext;
import com.adobe.fd.output.api.PaginationOverride;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@WireMockTest
class AemOutputServicePsGenerationTest {
	
	private static final String TEST_CHUNK_DATA_STRING = "<root>data bytes</root>";
	private static final Path RESOURCES_DIR = Path.of("src", "test", "resources");

	private AemOutputServicePsGeneration underTest;
	
	private static final boolean WIREMOCK_RECORDING = false;
	
	@BeforeEach
	void setUp(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
		underTest = AemOutputServicePsGeneration.builder()
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
	void testProcess() {
		Context context = AemOutputServicePsGenerationContext.contextWriter()
															  .template(RESOURCES_DIR.resolve("sampleForms").resolve("SampleForPs.xdp").toAbsolutePath())
															  .build();
		
		Message<PsPayload> result = underTest.process(MessageBuilder.createMessage(new XmlPayload(TEST_CHUNK_DATA_STRING), context));
		
		assertNotNull(result);
		assertNotNull(result.payload());
	}
	
	@Test
	void testProcess_throwsException() {
		String template = "ThrowsException.xdp";
		Context context = AemOutputServicePsGenerationContext.contextWriter()
															  .template(template)
															  .build();
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(MessageBuilder.createMessage(new XmlPayload(TEST_CHUNK_DATA_STRING), context)));
		
		String msg = ex.getMessage();
		assertNotNull(msg);
		
		assertThat(msg, allOf(containsStringIgnoringCase("Error while generating PS document from template"), containsString(template)));
	}
	
	@Test
	void testProcess_MissingTemplate() {
		// No template argument in the context.
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()->underTest.process(MessageBuilder.createMessage(new XmlPayload(TEST_CHUNK_DATA_STRING), EmptyContext.emptyInstance())));
		
		String msg = ex.getMessage();
		assertNotNull(msg);
		assertThat(msg, allOf(containsStringIgnoringCase("Template parameter"), 
							  containsString("formspipeline.aem_output_ps_gen.template"),
							  containsStringIgnoringCase("not found")
							  ));
	}

	@Nested
	static class ContextReaderWriterTests {
		// Make sure we can write and then read everything
		
		@Test
		void testContextReaderWriter_populatedValues() {
			var contentRoot = PathOrUrl.from("/foo/bar");
			var local = Locale.CANADA_FRENCH;
			var copies = Integer.parseInt("8");
			var debugDirectory = Path.of("C:\\");
			var paginationOverride = PaginationOverride.duplexLongEdge;
			var printConfig = PrintConfig.DPL300;
			var template = PathOrUrl.from("SomeXdp.xdp");
			var xciDocument = SimpleDocumentFactoryImpl.getFactory().create("TestData".getBytes());

			Context context = AemOutputServicePsGenerationContext.contextWriter()
					.contentRoot(contentRoot)
					.locale(local)
					.copies(copies)
					.debugDirectory(debugDirectory)
					.paginationOverride(paginationOverride)
					.printConfig(printConfig)
					.template(template)
					.xci(xciDocument)
					.build();
			
			var reader = AemOutputServicePsGenerationContext.contextReader(context);
			assertAll(
					()->assertEquals(contentRoot, reader.contentRoot().orElseThrow()),
					()->assertEquals(local, reader.locale().orElseThrow()),
					()->assertEquals(copies, reader.copies().orElseThrow()),
					()->assertEquals(debugDirectory, reader.debugDirectory().orElseThrow()),
					()->assertEquals(paginationOverride, reader.paginationOverride().orElseThrow()),
					()->assertEquals(printConfig, reader.printConfig().orElseThrow()),
					()->assertEquals(template, reader.template()),
					()->assertEquals(xciDocument, reader.xci().orElseThrow())
					);			
			}
		
		@Test
		void testContextReaderWriter_unpopulatedValues() {
			Context context = AemOutputServicePsGenerationContext.contextWriter()
					.build();
			
			var reader = AemOutputServicePsGenerationContext.contextReader(context);
			assertAll(
					()->assertTrue(reader.contentRoot().isEmpty(),"contentRoot is expected to be empty but is not empty"),
					()->assertTrue(reader.copies().isEmpty(),"copies is expected to be empty but is not empty"),
					()->assertTrue(reader.debugDirectory().isEmpty(),"debugDirectory is expected to be empty but is not empty"),
					()->assertTrue(reader.paginationOverride().isEmpty(),"paginationOverride is expected to be empty but is nott empty"),
					()->assertTrue(reader.locale().isEmpty(),"locale is expected to be empty but is not empty"),
					()->assertTrue(reader.debugDirectory().isEmpty(),"debugDirectory is expected to be empty but is not empty"),
					()->assertEquals(PrintConfig.Generic_PS_L3 , reader.printConfig().orElseThrow(), "PrintConfig is not the default"),
					()->assertTrue(reader.xci().isEmpty(), "XCI Document is not empty")  
					);
		}
		
		// Test the convenience setters that convert to PathOrUrl
		@Test
		void testContextReaderWriter_convertedValues() throws Exception {
			String filePrefix = "file:";
			
			String contentRootStr = "/foo/bar";
			Path contentRootPath = Path.of(contentRootStr);
			URL contentRootUrl = new URL(filePrefix + contentRootStr);
			
			String templateStr = "SomeXdp.xdp";			
			URL templateUrl = new URL(filePrefix + templateStr);
			PathOrUrl templatePath = PathOrUrl.from("SomeXdp.xdp");
			
			var contentRoot = PathOrUrl.from(contentRootStr);
			var template = PathOrUrl.from(templateStr);			
			var xciDocument = SimpleDocumentFactoryImpl.getFactory().create("TestData".getBytes());
				
			Context contextStr = AemOutputServicePsGenerationContext.contextWriter()
					.contentRoot(contentRootStr)
					.template(templateStr)
					.build();
			var readerStr = AemOutputServicePsGenerationContext.contextReader(contextStr);

			Context contextPath = AemOutputServicePsGenerationContext.contextWriter()
					.contentRoot(contentRootPath)
					.template(templatePath)
					.build();
			var readerPath = AemOutputServicePsGenerationContext.contextReader(contextPath);

			Context contextUrl = AemOutputServicePsGenerationContext.contextWriter()
					.contentRoot(contentRootUrl)
					.template(templateUrl)
					.build();
			var readerUrl = AemOutputServicePsGenerationContext.contextReader(contextUrl);

			assertAll(
					()->assertEquals(contentRoot, readerStr.contentRoot().orElseThrow()),
					()->assertEquals(template, readerStr.template()),
					()->assertEquals(contentRoot, readerPath.contentRoot().orElseThrow()),
					()->assertEquals(template, readerPath.template()),
					()->assertEquals(PathOrUrl.from(contentRootUrl), readerUrl.contentRoot().orElseThrow()),
					()->assertEquals(PathOrUrl.from(templateUrl), readerUrl.template())
					);			
			}

	}
}