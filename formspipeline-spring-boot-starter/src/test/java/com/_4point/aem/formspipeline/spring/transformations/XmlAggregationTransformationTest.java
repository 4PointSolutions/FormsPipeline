package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.payloads.XmlPayload;

class XmlAggregationTransformationTest {

	private final XmlAggregationTransformation underTest = new XmlAggregationTransformation(2);
	
	@Test
	void testProcess() {
		Stream<Message<XmlPayload>> chunks = Stream.of(XmlEventManipulationTest.TRANSACTION_1, XmlEventManipulationTest.TRANSACTION_2, XmlEventManipulationTest.TRANSACTION_3)
												   .map(XmlPayload::new)
												   .map(p->MessageBuilder.createMessage(p, EmptyContext.emptyInstance()));
		Message<XmlPayload> result = underTest.process(chunks);

		assertNotNull(result);
//		System.out.println("-----");
//		System.out.println(new String(XmlEventManipulationTest.FULL_TRANSACTION));
//		System.out.println("-----");
//		System.out.println("-----");
//		System.out.println(new String(result.bytes()));
//		System.out.println("-----");
		assertAll(
				()->assertThat(Input.fromByteArray(result.payload().bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.FULL_TRANSACTION)).ignoreWhitespace()),
				()->assertSame(EmptyContext.emptyInstance(), result.context())
				);
	}
}