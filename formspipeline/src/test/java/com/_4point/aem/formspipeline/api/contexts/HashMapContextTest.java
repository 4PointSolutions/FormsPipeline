package com._4point.aem.formspipeline.api.contexts;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HashMapContextTest {

	private static final String KEY1 = "key1";
	private static final String VALUE1 = "value1";
	private static final String KEY2 = "key2";
	private static final int VALUE2 = 4;

	private final static Map<String, Object> testData = Map.of(KEY1, VALUE1, KEY2, VALUE2);
	
	private final HashMapContext underTest = new HashMapContext(testData);
	
	@Test
	void testGetString() {
		String result = underTest.get(KEY1, String.class);
		assertEquals(VALUE1, result);
	}

	@Test
	void testGetInteger() {
		int result = underTest.get(KEY2, Integer.class);
		assertEquals(VALUE2, result);
	}

	@Test
	void testGetNotAssignable() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()->underTest.get(KEY1, Integer.class));
		String msg = ex.getMessage();
		assertNotNull(msg);
		assertThat(msg, allOf(containsString(String.class.getName()), containsString(Integer.class.getName()), containsString("Unable to convert object")));
	}

	@Test
	void testGetString2() {
		String result = underTest.getString(KEY1);
		assertEquals(VALUE1, result);
	}

	@Test
	void testGetStringNotAssignable() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()->underTest.getString(KEY2));
		String msg = ex.getMessage();
		assertNotNull(msg);
		assertThat(msg, allOf(containsString(String.class.getName()), containsString(Integer.class.getName()), containsString("Unable to convert object")));
	}


}
