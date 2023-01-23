package com._4point.aem.formspipeline.contexts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;

class NamespacedContextTest {

	private static final String TEST_NAMESPACE = "test.namespace";
	private static final String ORIGINAL_KEY = "originalKey";
	private static final String ORIGINAL_VALUE = "originalValue";
	private static final Context ORIGINAL_CONTEXT = new MapContext(Map.of(ORIGINAL_KEY, ORIGINAL_VALUE));
	
	private final Context underTest = new NamespacedContext(TEST_NAMESPACE, ORIGINAL_CONTEXT);
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@DisplayName("Get of properly namespaced key should return original value.")
	@Test
	void testGet_Found() {
		// Precondition
		assertTrue(ORIGINAL_CONTEXT.get(ORIGINAL_KEY, String.class).isPresent(), "ORIGINAL_CONTEXT should not return an empty value but did");
		
		// Operation
		Optional<String> result = underTest.get(TEST_NAMESPACE + "." + ORIGINAL_KEY, String.class);
		// Validation of result
		assertTrue(result.isPresent(), "underTest.get() should not return an empty value but did");
		assertEquals(ORIGINAL_VALUE, result.get());
	}

	@DisplayName("Get of original key should not return a value.")
	@Test
	void testGet_Original_NotFound() {
		// Precondition
		assertTrue(ORIGINAL_CONTEXT.get(ORIGINAL_KEY, String.class).isPresent(), "ORIGINAL_CONTEXT should not return an empty value but did");

		// Operation & Validation
		assertTrue(underTest.get(ORIGINAL_KEY, String.class).isEmpty(), "Should return an empty value but didn't");
	}

	@DisplayName("Get of non-existent key should not return a value.")
	@Test
	void testGet_Other_NotFound() {
		final String key = "NonexistentKey";
		// Precondition
		assertTrue(ORIGINAL_CONTEXT.get(key, String.class).isEmpty(), "ORIGINAL_CONTEXT should return an empty value but didn't");

		// Operation & Validation
		assertTrue(underTest.get(key, Integer.class).isEmpty(), "Should return an empty value but didn't");
	}

	@DisplayName("Get of non-existent key should not return a value.")
	@Test
	void testGet_NamespacedOther_NotFound() {
		final String key = "NonexistentKey";
		// Precondition
		assertTrue(ORIGINAL_CONTEXT.get(key, String.class).isEmpty(), "ORIGINAL_CONTEXT should return an empty value but didn't");

		// Operation & Validation
		assertTrue(underTest.get(TEST_NAMESPACE + "." + key, Integer.class).isEmpty(), "Should return an empty value but didn't");
	}
}
