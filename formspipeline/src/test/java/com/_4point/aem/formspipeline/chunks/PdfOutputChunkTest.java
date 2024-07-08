package com._4point.aem.formspipeline.chunks;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.chunks.PdfPayload.PdfOutputContext;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.contexts.SingletonContext;

class PdfOutputChunkTest {
	

	private static class CustomContext extends EmptyContext{
		
	}
	
	private static final CustomContext DATA_CONTEXT = new CustomContext();
	private static final String TEST_STRING = "Test Data";
	private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);
	private static final int NUM_PAGES = 23;
	
	private final PdfPayload<CustomContext> underTestNoPages = PdfPayload.createSimple(DATA_CONTEXT, TEST_BYTES);
	private final PdfPayload<CustomContext> underTestPages = PdfPayload.createSimple(DATA_CONTEXT, TEST_BYTES, NUM_PAGES);
	
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

	@Test
	void testUpdateDataContext() {
		String NEW_CONTEXT_KEY = "newkey";
		String NEW_CONTEXT_VALUE = "value";
		Context newContext = SingletonContext.of(NEW_CONTEXT_KEY, NEW_CONTEXT_VALUE);
		
		PdfPayload<CustomContext> updatedNoPagesChunk = underTestNoPages.updateDataContext(newContext);
		Context dataContextNoPages = updatedNoPagesChunk.dataContext();
		assertTrue(updatedNoPagesChunk.outputContext().numPages().isEmpty());	// Make sure this is still the same
		assertTrue(dataContextNoPages.get("Key", String.class).isEmpty());
		assertEquals(NEW_CONTEXT_VALUE, dataContextNoPages.get(NEW_CONTEXT_KEY, String.class).orElseThrow());

		PdfPayload<CustomContext> updatedPagesChunk = underTestPages.updateContext(newContext);
		Context dataContextPages = updatedPagesChunk.dataContext();
		assertEquals(NUM_PAGES, updatedPagesChunk.outputContext().numPages().getAsInt());
		assertTrue(dataContextPages.get("Key", String.class).isEmpty());
		assertEquals(NEW_CONTEXT_VALUE, dataContextNoPages.get(NEW_CONTEXT_KEY, String.class).orElseThrow());
	}
}
