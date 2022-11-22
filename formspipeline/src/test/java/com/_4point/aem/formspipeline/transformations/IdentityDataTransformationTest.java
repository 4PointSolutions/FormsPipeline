package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;

class IdentityDataTransformationTest {

	private final TestChunk testChunk1 = new TestChunk();
	private final TestChunk testChunk2 = new TestChunk();
	
	private final IdentityDataTransformation<TestChunk> underTest = new IdentityDataTransformation<>();
	
	@Test
	void testProcessT() {
		TestChunk testChunk = testChunk1;
		assertSame(testChunk, underTest.process(testChunk));
	}

	@Test
	void testProcessStreamOfQextendsDataChunk() {
		Stream<TestChunk> testStream = Stream.of(testChunk1, testChunk2);
		assertSame(testStream, underTest.process(testStream));
	}

	private static class TestChunk implements DataChunk {

		@Override
		public byte[] bytes() {
			return null;
		}

		@Override
		public Context dataContext() {
			return null;
		}
		
	}
}
