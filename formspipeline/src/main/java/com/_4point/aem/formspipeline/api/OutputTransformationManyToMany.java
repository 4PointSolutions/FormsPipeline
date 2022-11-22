package com._4point.aem.formspipeline.api;

import java.util.stream.Stream;

public non-sealed interface OutputTransformationManyToMany extends OutputTransformation {
	Stream<? extends OutputChunk<? extends Context, ? extends Context>> process(Stream<? extends OutputChunk<? extends Context, ? extends Context>> outputChunks);
}
