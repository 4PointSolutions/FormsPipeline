package com._4point.aem.formspipeline.samples;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;

public class SampleDataTransformationOneToOne1 implements DataTransformationOneToOne<SampleDataChunk1, SampleDataChunk2> {

	@Override
	public SampleDataChunk2 process(SampleDataChunk1 dataChunk) {
		return new SampleDataChunk2();
	}
}
