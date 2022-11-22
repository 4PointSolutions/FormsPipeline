package com._4point.aem.formspipeline.destinations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.results.SimpleResult;
import com._4point.aem.formspipeline.contexts.EmptyContext;

public class FolderDestination<T extends Context, U extends Context> implements OutputDestination<OutputChunk<T, U>, 
															Result<T, U, ? extends Context>
															> {
	private final Path destinationFolder;
	private final BiFunction<T, U, Path> filenameFn;
	
	public FolderDestination(Path destinationFolder,
							 BiFunction<T, U, Path> filenameFn) {
		this.destinationFolder = destinationFolder;
		this.filenameFn = filenameFn;
	}

	@Override
	public Result<T, U, EmptyContext> process(OutputChunk<T, U> outputChunk) {
		Path destination = destinationFolder.resolve(filenameFn.apply(outputChunk.dataContext(), outputChunk.outputContext()));
		try {
			Files.write(destination, outputChunk.bytes());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write file (" + destination.toAbsolutePath().toString() + ").", e);
		}
		return new SimpleResult<T, U, EmptyContext>(outputChunk.dataContext(), outputChunk.outputContext(), EmptyContext.emptyInstance());
	}
}
