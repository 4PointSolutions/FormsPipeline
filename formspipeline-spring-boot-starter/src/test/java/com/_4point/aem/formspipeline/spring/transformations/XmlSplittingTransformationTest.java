package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.*;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

class XmlSplittingTransformationTest {

	private final XmlSplittingTransformation underTest = new XmlSplittingTransformation(2);
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess() {
		XmlDataChunk chunk = XmlDataChunk.create(XmlEventManipulationTest.FULL_TRANSACTION);
		List<XmlDataChunk> result = underTest.process(chunk).toList();
		
		assertEquals(3, result.size());
		assertThat(Input.fromByteArray(result.get(0).bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_1)).ignoreWhitespace());
		assertThat(Input.fromByteArray(result.get(1).bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_2)).ignoreWhitespace());
		assertThat(Input.fromByteArray(result.get(2).bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_3)).ignoreWhitespace());
	}

}
