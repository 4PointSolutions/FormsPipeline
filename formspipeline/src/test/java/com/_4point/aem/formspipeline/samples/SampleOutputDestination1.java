package com._4point.aem.formspipeline.samples;

import com._4point.aem.formspipeline.api.OutputDestination;

public class SampleOutputDestination1 implements OutputDestination<SampleOutputChunk2, SampleResult1> {

	@Override
	public SampleResult1 process(SampleOutputChunk2 outputChunk) {
		System.out.println("Sending SampleOutputChunk1 to Destination and returning SampleResult1");
		return new SampleResult1(outputChunk.dataContext(), outputChunk.outputContext());
	}
}
