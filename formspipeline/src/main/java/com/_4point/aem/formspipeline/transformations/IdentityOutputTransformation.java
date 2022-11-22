package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputTransformationManyToMany;
import com._4point.aem.formspipeline.api.OutputTransformationOneToOne;

public class IdentityOutputTransformation<T extends OutputChunk>
		implements OutputTransformationManyToMany, OutputTransformationOneToOne<T, T> {
	
	@Override
	public T process(T outputChunk) {
		return outputChunk;
	}

	@Override
	public Stream<? extends OutputChunk> process(Stream<? extends OutputChunk> outputChunks) {
		return outputChunks;
	}
}
