package com._4point.aem.formspipeline.contexts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;

class AggregateContextTest {
	private static final String KEY1 = "key1";
	private static final String VALUE1 = "value1";
	private static final String KEY2 = "key2";
	private static final String VALUE2 = "value2";
	private static final String KEY3 = "key3";		// No associatedValue

	private final Context context1 = new Context() {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return key.equalsIgnoreCase(KEY1) ? Optional.of((T)VALUE1) : Optional.empty();
		}
	};
	
	private final Context context2 = new Context() {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return key.equalsIgnoreCase(KEY2) ? Optional.of((T)VALUE2) : Optional.empty();
		}
	};

	private final Context underTest = new AggregateContext(context1, context2);
	
	@Test
	void testGet_Context1() {
		assertEquals(VALUE1, underTest.getString(KEY1).orElseThrow());
	}

	@Test
	void testGet_Context2() {
		assertEquals(VALUE2, underTest.getString(KEY2).orElseThrow());
	}

	@Test
	void testGet_NotFound() {
		assertTrue(underTest.getString(KEY3).isEmpty(), ()->"Expected get{" + KEY3 + ") to be empty.");
	}

}
