package com._4point.aem.formspipeline.samples;

import com._4point.aem.formspipeline.api.OutputGeneration;

public class SampleOutputGeneration1 implements OutputGeneration<SampleDataChunk2, SampleOutputChunk1> {

	@Override
	public SampleOutputChunk1 process(SampleDataChunk2 dataChunk) {
		System.out.println("Generating SampleOutputChunk1 from SampleDataChunk2");
		return new SampleOutputChunk1(dataChunk.dataContext());
	}
}
