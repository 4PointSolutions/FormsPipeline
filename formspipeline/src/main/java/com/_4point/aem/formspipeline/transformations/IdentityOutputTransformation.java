package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationManyToMany;
import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationOneToOne;

public class IdentityOutputTransformation<T extends OutputChunk<? extends Context, ? extends Context>>
		implements OutputTransformationManyToMany, OutputTransformationOneToOne<T, T> {
	
	@Override
	public T process(T outputChunk) {
		return outputChunk;
	}

	@Override
	public Stream<? extends OutputChunk<? extends Context, ? extends Context>> process(Stream<? extends OutputChunk<? extends Context, ? extends Context>> outputChunks) {
		return outputChunks;
	}
}
