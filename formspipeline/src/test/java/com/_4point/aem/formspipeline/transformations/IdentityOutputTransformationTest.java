package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;

class IdentityOutputTransformationTest {

	private final TestChunk testChunk1 = new TestChunk();
	private final TestChunk testChunk2 = new TestChunk();
	
	private final IdentityOutputTransformation<TestChunk> underTest = new IdentityOutputTransformation<>();
	
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

	private static class TestChunk implements OutputChunk {

		@Override
		public byte[] bytes() {
			return null;
		}

		@Override
		public Context outputContext() {
			return null;
		}

		@Override
		public Context dataContext() {
			return null;
		}
		
	}

}
