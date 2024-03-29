package com._4point.aem.formspipeline.contexts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;

class AggregateContextTest {
	private static final String KEY1 = "key1";
	private static final String VALUE1 = "value1";
	private static final String KEY2 = "key2";
	private static final String VALUE2 = "value2";
	private static final String KEY3 = "key3";		// No associatedValue in context1 and context2
	private static final String VALUE3 = "value3";
	private static final String KEY4 = "key4";		// Occurs in all contexts with different values
	private static final String KEY5 = "key5";		//  No associatedValue in any context

	private final Context context1 = new Context() {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return key.equalsIgnoreCase(KEY1) || key.equalsIgnoreCase(KEY4) ? Optional.of((T)VALUE1) : Optional.empty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> getMulti(String key, Class<T> target) {
			// TODO Auto-generated method stub
			return key.equalsIgnoreCase(KEY1) || key.equalsIgnoreCase(KEY4) ? List.of((T)VALUE1, (T)VALUE1) : List.of();
		}
	};
	
	private final Context context2 = new Context() {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return key.equalsIgnoreCase(KEY2) || key.equalsIgnoreCase(KEY4) ? Optional.of((T)VALUE2) : Optional.empty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> getMulti(String key, Class<T> target) {
			// TODO Auto-generated method stub
			return key.equalsIgnoreCase(KEY2) || key.equalsIgnoreCase(KEY4) ? List.of((T)VALUE2, (T)VALUE2) : List.of();
		}
	};

	private final Context context3 = new Context() {
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> Optional<T> get(String key, Class<T> target) {
			return key.equalsIgnoreCase(KEY3) || key.equalsIgnoreCase(KEY4) ? Optional.of((T)VALUE3) : Optional.empty();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> getMulti(String key, Class<T> target) {
			// TODO Auto-generated method stub
			return key.equalsIgnoreCase(KEY3) || key.equalsIgnoreCase(KEY4) ? List.of((T)VALUE3, (T)VALUE3) : List.of();
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

	@Test
	void testGet_Both() {
		assertEquals(VALUE1, underTest.getString(KEY4).orElseThrow());
	}

	@Test
	void testGetMulti_Context1() {
		assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY1)));
	}

	@Test
	void testGetMulti_Context2() {
		assertArrayEquals(new String[] {VALUE2, VALUE2}, asArray(underTest.getStrings(KEY2)));
	}

	@Test
	void testGetMulti_NotFound() {
		assertTrue(underTest.getStrings(KEY3).isEmpty(), ()->"Expected get{" + KEY3 + ") to be empty.");
	}

	@Test
	void testGetMulti_Both() {
		assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY4)));
	}

	@Test
	void testAggregate_0args() {
		Context underTest = AggregateContext.aggregate();
		assertSame(EmptyContext.emptyInstance(), underTest);
	}

	@Test
	void testAggregate_1args() {
		Context underTest = AggregateContext.aggregate(context1);
		assertSame(context1, underTest);
	}

	@Test
	void testAggregate_2args() {
		Context underTest = AggregateContext.aggregate(context2, context1);
		assertAll(
				()->assertEquals(VALUE1, underTest.getString(KEY1).orElseThrow()),
				()->assertEquals(VALUE2, underTest.getString(KEY2).orElseThrow()),
				()->assertTrue(underTest.getString(KEY3).isEmpty(), ()->"Expected get{" + KEY3 + ") to be empty."),
				()->assertEquals(VALUE2, underTest.getString(KEY4).orElseThrow()),
				()->assertTrue(underTest.getString(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty."),
				()->assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY1))),
				()->assertArrayEquals(new String[] {VALUE2, VALUE2}, asArray(underTest.getStrings(KEY2))),
				()->assertTrue(underTest.getStrings(KEY3).isEmpty(), ()->"Expected get{" + KEY3 + ") to be empty."),
				()->assertArrayEquals(new String[] {VALUE2, VALUE2}, asArray(underTest.getStrings(KEY4))),
				()->assertTrue(underTest.getStrings(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty.")
				);
	}

	@Test
	void testAggregate_3args() {
		Context underTest = AggregateContext.aggregate(context1, context2, context3);
		assertAll(
				()->assertEquals(VALUE1, underTest.getString(KEY1).orElseThrow()),
				()->assertEquals(VALUE2, underTest.getString(KEY2).orElseThrow()),
				()->assertEquals(VALUE3, underTest.getString(KEY3).orElseThrow()),
				()->assertEquals(VALUE1, underTest.getString(KEY4).orElseThrow()),
				()->assertTrue(underTest.getString(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty."),
				()->assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY1))),
				()->assertArrayEquals(new String[] {VALUE2, VALUE2}, asArray(underTest.getStrings(KEY2))),
				()->assertArrayEquals(new String[] {VALUE3, VALUE3}, asArray(underTest.getStrings(KEY3))),
				()->assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY4))),
				()->assertTrue(underTest.getStrings(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty.")
				);
	}

	@Test
	void testAggregate_ListPlusArg() {
		Context underTest = AggregateContext.aggregate(List.of(context1, context2), context3);
		assertAll(
				()->assertEquals(VALUE1, underTest.getString(KEY1).orElseThrow()),
				()->assertEquals(VALUE2, underTest.getString(KEY2).orElseThrow()),
				()->assertEquals(VALUE3, underTest.getString(KEY3).orElseThrow()),
				()->assertEquals(VALUE1, underTest.getString(KEY4).orElseThrow()),
				()->assertTrue(underTest.getString(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty."),
				()->assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY1))),
				()->assertArrayEquals(new String[] {VALUE2, VALUE2}, asArray(underTest.getStrings(KEY2))),
				()->assertArrayEquals(new String[] {VALUE3, VALUE3}, asArray(underTest.getStrings(KEY3))),
				()->assertArrayEquals(new String[] {VALUE1, VALUE1}, asArray(underTest.getStrings(KEY4))),
				()->assertTrue(underTest.getStrings(KEY5).isEmpty(), ()->"Expected get{" + KEY5 + ") to be empty.")
				);
	}
	
	private static String[] asArray(List<String> list) {
		return list.toArray(new String[list.size()]);
	}
}
