package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface OutputTransformationOneToMany<T extends OutputChunk> extends OutputTransformation {
	public Stream<? extends OutputChunk> process(T outputChunk);
}
