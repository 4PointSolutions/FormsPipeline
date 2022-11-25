package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationManyToMany;
import com._4point.aem.formspipeline.api.OutputTransformation.OutputTransformationOneToOne;

public class IdentityOutputTransformation<T extends OutputChunk<? extends Context, ? extends Context>>
		implements OutputTransformationManyToMany<T, T>, OutputTransformationOneToOne<T, T> {
	
	@Override
	public T process(T outputChunk) {
		return outputChunk;
	}

	@Override
	public Stream<T> process(Stream<T> outputChunks) {
		return outputChunks;
	}
}
