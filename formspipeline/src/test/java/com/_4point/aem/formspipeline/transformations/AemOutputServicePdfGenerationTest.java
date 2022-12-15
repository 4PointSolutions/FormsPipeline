package com._4point.aem.formspipeline.transformations;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk;
import com._4point.aem.formspipeline.chunks.SimpleChunk;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.transformations.AemOutputServicePdfGeneration.AemOutputServicePdfGenerationContext;

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

	
	private final AemOutputServicePdfGeneration<Context, CustomDataChunk> underTest = AemOutputServicePdfGeneration.builder()
															.machineName("localhost")
															.port(4502)
															.build();
			
	@BeforeEach
	void setUp() throws Exception {
	}

	@Disabled
	@Test
	void testProcess() {
		Context context = AemOutputServicePdfGenerationContext.contextWriter()
															  .template("Foo.xdp")
															  .build();
		
		PdfOutputChunk<Context> result = underTest.process(new CustomDataChunk(TEST_CHUNK_DATA_STRING, context));
		
//		assertresult.bytes()
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

}
