package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.chunks.SimpleChunk;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@Disabled("No completed yet")
class AemOutputServicePdfGenerationTest {
	
	private class CustomDataContext extends EmptyContext {
		
	}
	
	private class CustomDataChunk implements DataChunk<CustomDataContext> {
		private final SimpleChunk chunk;
		private final CustomDataContext context;

		public CustomDataChunk(String chunkData) {
			this.chunk = new SimpleChunk(chunkData);
			this.context = new CustomDataContext();
		}

		@Override
		public byte[] bytes() {
			return chunk.bytes();
		}

		@Override
		public CustomDataContext dataContext() {
			return this.context;
		}
	}

	
	private final AemOutputServicePdfGeneration<CustomDataContext, CustomDataChunk> underTest = AemOutputServicePdfGeneration.builder()
															.machineName("localhost")
															.port(4502)
															.build();
			
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess() {
		fail("Not yet implemented");
	}

}
