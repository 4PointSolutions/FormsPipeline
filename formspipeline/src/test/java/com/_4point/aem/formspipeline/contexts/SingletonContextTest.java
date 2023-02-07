package com._4point.aem.formspipeline.contexts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;

class SingletonContextTest {

	private static final String VALUE = "5";
	private static final String KEY = "KEY";
	private final Context underTest = SingletonContext.of(KEY, VALUE);

	@DisplayName("get() with the correct class should return the value.")
	@Test
	void testGet_Simple() {
		assertEquals(VALUE, underTest.get(KEY, String.class).get());
	}

	@DisplayName("get() with with a super class should return the value.")
	@Test
	void testGet_Super() {
		assertSame(VALUE, underTest.get(KEY, Object.class).get());
	}

	@DisplayName("getString() should return the value as a String.")
	@Test
	void testGetString_Simple() {
		assertEquals(VALUE, underTest.getString(KEY).get());
	}

	@DisplayName("getInteger() should return the value as an Integer.")
	@Test
	void testGetInteger_Simple() {
		assertEquals(5, underTest.getInteger(KEY).get());
	}

	@DisplayName("Using a different key should not return a result.")
	@Test
	void testGet_KeyNotFound() {
		assertTrue(underTest.getString(KEY+"NotFound").isEmpty());
	}

	@DisplayName("Using a different class should not return a result.")
	@Test
	void testGet_DifferentClass() {
		assertTrue(underTest.get(KEY, Integer.class).isEmpty());
	}
}
