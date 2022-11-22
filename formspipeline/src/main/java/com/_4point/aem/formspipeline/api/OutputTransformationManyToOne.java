package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface OutputTransformationManyToOne<T extends OutputChunk> extends OutputTransformation {
	T process(Stream<? extends OutputChunk> outputChunks);
}
