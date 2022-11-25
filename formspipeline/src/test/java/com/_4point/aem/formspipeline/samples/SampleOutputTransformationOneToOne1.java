package com._4point.aem.formspipeline.samples;

import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationOneToOne;

public class SampleOutputTransformationOneToOne1 implements OutputTransformationOneToOne<SampleOutputChunk1, SampleOutputChunk2> {

	@Override
	public SampleOutputChunk2 process(SampleOutputChunk1 outputChunk) {
		return new SampleOutputChunk2(outputChunk.dataContext());
	}

}
