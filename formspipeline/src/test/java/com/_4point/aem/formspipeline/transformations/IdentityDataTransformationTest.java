package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.EmptyContext;

class IdentityDataTransformationTest {

	private final Message<String> TEST_MSG_1 = MessageBuilder.createMessage("String 1", EmptyContext.emptyInstance());
	private final Message<String> TEST_MSG_2 = MessageBuilder.createMessage("String 2", EmptyContext.emptyInstance());
	
	private final IdentityDataTransformation<String> underTest = new IdentityDataTransformation<>();
	
	@Test
	void testProcessT() {
		assertSame(TEST_MSG_1, underTest.process(TEST_MSG_1));
	}

	@Test
	void testProcessStreamOfQextendsDataChunk() {
		Stream<Message<String>> testStream = Stream.of(TEST_MSG_1, TEST_MSG_2);
		assertSame(testStream, underTest.process(testStream));
	}
}
