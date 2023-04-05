package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.*;


import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

class XmlSplittingTransformationTest {
	private static final String STATEMENT1 =
			"""
		   <statement account="123">
		      ...stuff...
		      <foo>bar</foo>
		      <emptyTag />
		      ...stuff...
		   </statement>
			""";
	private static final String STATEMENT2 =
			"""
		   <statement account="456">
		      <nested1>
			     <nested2>
			        <nested3_1>Some Data</nested3_1>
			        <nested3_2/>
			        <nested3_3>Some More Data</nested3_3>
			     </nested2>
		      </nested1>
		      ...stuff...
		   </statement>
			""";
	private static final String TEST_INPUT = 
			"<statements>" + STATEMENT1 + STATEMENT2 + "</statements>";

	private static final byte[] TEST_BYTES = TEST_INPUT.getBytes(StandardCharsets.UTF_8);

	private final XmlSplittingTransformation underTest = new XmlSplittingTransformation();
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess() {
		List<XmlDataChunk> result = underTest.process(XmlDataChunk.create(TEST_BYTES)).toList();
		
		assertEquals(2, result.size());
		assertThat(Input.fromByteArray(result.get(0).bytes()), isIdenticalTo(Input.fromString(STATEMENT1)));
		assertThat(Input.fromByteArray(result.get(1).bytes()), isIdenticalTo(Input.fromString(STATEMENT2)));
	}

}
