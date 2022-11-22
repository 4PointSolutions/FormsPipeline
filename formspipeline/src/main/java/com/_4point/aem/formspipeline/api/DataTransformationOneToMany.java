package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface DataTransformationOneToMany<T extends DataChunk<? extends Context>> extends DataTransformation {
	public Stream<? extends DataChunk<? extends Context>> process(T dataChunk);
}
