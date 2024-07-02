package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

class XmlAggregationTransformationTest {

	private final XmlAggregationTransformation underTest = new XmlAggregationTransformation(2);
	
	@Test
	void testProcess() {
		Stream<XmlDataChunk> chunks = Stream.of(XmlDataChunk.create(XmlEventManipulationTest.TRANSACTION_1), XmlDataChunk.create(XmlEventManipulationTest.TRANSACTION_2), XmlDataChunk.create(XmlEventManipulationTest.TRANSACTION_3));
		XmlDataChunk result = underTest.process(chunks);

		assertNotNull(result);
//		System.out.println("-----");
//		System.out.println(new String(XmlEventManipulationTest.FULL_TRANSACTION));
//		System.out.println("-----");
//		System.out.println("-----");
//		System.out.println(new String(result.bytes()));
//		System.out.println("-----");
		assertThat(Input.fromByteArray(result.bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.FULL_TRANSACTION)).ignoreWhitespace());
	}

}
