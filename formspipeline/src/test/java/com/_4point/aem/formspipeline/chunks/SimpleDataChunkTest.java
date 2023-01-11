package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import com._4point.aem.formspipeline.contexts.EmptyContext;

class SimpleDataChunkTest {
	
	private static class CustomContext extends EmptyContext{		
	}

	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	private static final CustomContext DATA_CONTEXT = new CustomContext();
	
	private final SimpleDataChunk underTest = new SimpleDataChunk(DATA_CONTEXT,TEST_BYTES);
	
	@Test
	void testBytes() {
		assertArrayEquals(TEST_BYTES, underTest.bytes());
	}
	
	@Test
	void testContext() {
		assertEquals(DATA_CONTEXT, underTest.dataContext());
	}
}
