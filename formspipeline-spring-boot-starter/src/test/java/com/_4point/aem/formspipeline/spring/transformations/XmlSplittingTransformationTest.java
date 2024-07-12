package com._4point.aem.formspipeline.spring.transformations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.xmlunit.matchers.CompareMatcher.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.payloads.XmlPayload;

class XmlSplittingTransformationTest {

	private final XmlSplittingTransformation underTest = new XmlSplittingTransformation(2);
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcess() {
		EmptyContext emptyContext = EmptyContext.emptyInstance();
		Message<XmlPayload> chunk = MessageBuilder.createMessage(new XmlPayload(XmlEventManipulationTest.FULL_TRANSACTION), emptyContext);
		List<Message<XmlPayload>> result = underTest.process(chunk).toList();
		
		assertAll(
				()->assertEquals(3, result.size()),
				()->assertThat(Input.fromByteArray(result.get(0).payload().bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_1)).ignoreWhitespace()),
				()->assertThat(Input.fromByteArray(result.get(1).payload().bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_2)).ignoreWhitespace()),
				()->assertThat(Input.fromByteArray(result.get(2).payload().bytes()), isIdenticalTo(Input.fromByteArray(XmlEventManipulationTest.TRANSACTION_3)).ignoreWhitespace()),
				()->assertSame(emptyContext, result.get(0).context()),
				()->assertSame(emptyContext, result.get(1).context()),
				()->assertSame(emptyContext, result.get(2).context())
				);
		
	}

}
