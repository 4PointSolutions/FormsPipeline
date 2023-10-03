package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.chunks.PdfOutputChunk.PdfOutputContext;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.contexts.SingletonContext;

class PdfOutputChunkTest {
	

	private static class CustomContext extends EmptyContext{
		
	}
	
	private static final CustomContext DATA_CONTEXT = new CustomContext();
	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	private static final int NUM_PAGES = 23;
	
	private final PdfOutputChunk<CustomContext> underTestNoPages = PdfOutputChunk.createSimple(DATA_CONTEXT, TEST_BYTES);
	private final PdfOutputChunk<CustomContext> underTestPages = PdfOutputChunk.createSimple(DATA_CONTEXT, TEST_BYTES, NUM_PAGES);
	
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
		PdfOutputContext outputContextNoPages = underTestNoPages.outputContext();
		assertTrue(outputContextNoPages.numPages().isEmpty());
		assertTrue(outputContextNoPages.get("Key", String.class).isEmpty());
		PdfOutputContext outputContextPages = underTestPages.outputContext();
		assertEquals(NUM_PAGES, outputContextPages.numPages().getAsInt());
		assertTrue(outputContextPages.get("Key", String.class).isEmpty());
	}

	@Test
	void testUpdateOutputContext() {
		String NEW_CONTEXT_KEY = "newkey";
		String NEW_CONTEXT_VALUE = "value";
		Context newContext = SingletonContext.of(NEW_CONTEXT_KEY, NEW_CONTEXT_VALUE);
		
		PdfOutputContext outputContextNoPages = underTestNoPages.updateContext(newContext).outputContext();
		assertTrue(outputContextNoPages.numPages().isEmpty());
		assertTrue(outputContextNoPages.get("Key", String.class).isEmpty());
		assertEquals(NEW_CONTEXT_VALUE, outputContextNoPages.get(NEW_CONTEXT_KEY, String.class).orElseThrow());

		PdfOutputContext outputContextPages = underTestPages.updateContext(newContext).outputContext();
		assertEquals(NUM_PAGES, outputContextPages.numPages().getAsInt());
		assertTrue(outputContextPages.get("Key", String.class).isEmpty());
		assertEquals(NEW_CONTEXT_VALUE, outputContextNoPages.get(NEW_CONTEXT_KEY, String.class).orElseThrow());
	}

}
