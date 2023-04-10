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
		   </statement>""";
	private static final String STATEMENT2 =
			"""
		   <statement account="456">
		      <nested1>
			     <nested2>
			        <nested3_1>Some Data</nested3_1>
			        <nested3_2/>
			        <nested3_3>Some More Data</nested3_3><nested3_4></nested3_4>
			     </nested2>
		      </nested1>
		      ...stuff...
		   </statement>
		   """;
	private static final String STATEMENT3 =
			"""
		   <statement account="789">
		      <nested1>
			     <nested2>
			        <nested3_1>Some Data</nested3_1>
			        <nested3_2/>
			        <nested3_3>Some More Data</nested3_3><nested3_4></nested3_4>
			     </nested2>
		      </nested1>
		      ...stuff...
		   </statement>""";
	private static final String XML_WRAPPER_FMT_STRING = 
			"<output><statements>%s</statements></output>\n";

	private static final byte[] TEST_BYTES = XML_WRAPPER_FMT_STRING.formatted(STATEMENT1 + STATEMENT2 + STATEMENT3).getBytes(StandardCharsets.UTF_8);

	private final XmlSplittingTransformation underTest = new XmlSplittingTransformation(2);
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess() {
		XmlDataChunk chunk = XmlDataChunk.create(TEST_BYTES);
		List<XmlDataChunk> result = underTest.process(chunk).toList();
		
		assertEquals(3, result.size());
		assertThat(Input.fromByteArray(result.get(0).bytes()), isIdenticalTo(Input.fromString(XML_WRAPPER_FMT_STRING.formatted(STATEMENT1))).ignoreWhitespace());
		assertThat(Input.fromByteArray(result.get(1).bytes()), isIdenticalTo(Input.fromString(XML_WRAPPER_FMT_STRING.formatted(STATEMENT2))).ignoreWhitespace());
		assertThat(Input.fromByteArray(result.get(2).bytes()), isIdenticalTo(Input.fromString(XML_WRAPPER_FMT_STRING.formatted(STATEMENT3))).ignoreWhitespace());
	}

}
