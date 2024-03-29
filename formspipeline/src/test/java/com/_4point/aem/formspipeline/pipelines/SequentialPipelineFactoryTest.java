package com._4point.aem.formspipeline.pipelines;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com._4point.aem.formspipeline.api.Pipeline;
import com._4point.aem.formspipeline.samples.SampleDataChunk1;
import com._4point.aem.formspipeline.samples.SampleDataTransformationOneToOne1;
import com._4point.aem.formspipeline.samples.SampleOutputDestination1;
import com._4point.aem.formspipeline.samples.SampleOutputGeneration1;
import com._4point.aem.formspipeline.samples.SampleOutputTransformationOneToOne1;
import com._4point.aem.formspipeline.samples.SampleResult1;

class SequentialPipelineFactoryTest {

	@Test
	void testBuilder() {
		Pipeline pipeline = SequentialPipelineFactory.builder2()
				.setDataTransformation(new SampleDataTransformationOneToOne1())
				.setOutputGeneration(new SampleOutputGeneration1())
				.setOutputTransformation(new SampleOutputTransformationOneToOne1())
				.setOutputDestination(new SampleOutputDestination1())
				.build()
				;
		Stream result = pipeline.process(new SampleDataChunk1());
		List list = result.toList();
		assertEquals(1, list.size());
		Object resultObj = list.get(0);
		assertTrue(SampleResult1.class.isInstance(resultObj));
//		SampleResult1 sampleResult = (SampleResult1) resultObj;
//		assertEquals("")
	}

}
