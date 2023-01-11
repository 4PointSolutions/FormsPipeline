package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.contexts.EmptyContext;

class SimpleOutputChunkTest {
	private static class CustomContext extends EmptyContext{		
	}

	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	private static final CustomContext DATA_CONTEXT = new CustomContext();
	private static final EmptyContext OUTPUDATA_CONTEXT = new EmptyContext();
	
	
	private final SimpleOutputChunk underTest = new SimpleOutputChunk(DATA_CONTEXT, OUTPUDATA_CONTEXT, TEST_BYTES);
	
	@Test
	void testBytes() {
		assertArrayEquals(TEST_BYTES, underTest.bytes());		
	}

	@Test
	void testDataContext() {
		assertSame(DATA_CONTEXT, underTest.dataContext());
	}
	
	@Test
	void testOutputDataContext() {
		assertSame(OUTPUDATA_CONTEXT, underTest.outputContext());
	}
}
