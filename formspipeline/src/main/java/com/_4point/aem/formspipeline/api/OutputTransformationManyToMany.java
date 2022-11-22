package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface OutputTransformationManyToMany extends OutputTransformation {
	Stream<? extends OutputChunk> process(Stream<? extends OutputChunk> outputChunks);
}
