package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.chunks.PsPayload.PsOutputContext;
import com._4point.aem.formspipeline.contexts.EmptyContext;

class PsOutputChunkTest {

	private static class CustomContext extends EmptyContext{
		
	}
	
	private static final CustomContext DATA_CONTEXT = new CustomContext();
	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	private static final int NUM_PAGES = 23;
	
	private final PsPayload<CustomContext> underTestNoPages = PsPayload.createSimple(DATA_CONTEXT, TEST_BYTES);
	private final PsPayload<CustomContext> underTestPages = PsPayload.createSimple(DATA_CONTEXT, TEST_BYTES, NUM_PAGES);
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testBytes() {
		assertArrayEquals(TEST_BYTES, underTestNoPages.bytes());
		assertArrayEquals(TEST_BYTES, underTestPages.bytes());
	}

	@Test
	void testDataContext() {
		assertSame(DATA_CONTEXT, underTestNoPages.dataContext());
		assertSame(DATA_CONTEXT, underTestPages.dataContext());
	}

	@Test
	void testOutputContext() {
		PsOutputContext outputContextNoPages = underTestNoPages.outputContext();
		assertTrue(outputContextNoPages.numPages().isEmpty());
		assertTrue(outputContextNoPages.get("Key", String.class).isEmpty());
		PsOutputContext outputContextPages = underTestPages.outputContext();
		assertEquals(NUM_PAGES, outputContextPages.numPages().getAsInt());
		assertTrue(outputContextPages.get("Key", String.class).isEmpty());
	}
}
