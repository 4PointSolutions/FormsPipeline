package com._4point.aem.formspipeline.destinations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import javax.naming.LimitExceededException;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.results.SimpleResult;

public class FolderOutputDestination<T extends Context, U extends Context> implements OutputDestination<OutputChunk<T, U>, 
															Result<T, U, ? extends Context>
															> {
	private static final int RENAME_MAX_LIMIT = 1000;
	private final Path destinationFolder;
	private final BiFunction<T, U, Path> filenameFn;
	private UnaryOperator<Path> renameFn;
	private int renameCounter=0;
	
	public int getRenameCounter() {
		return renameCounter;
	}

	public FolderOutputDestination(Path destinationFolder,
			 BiFunction<T, U, Path> filenameFn, UnaryOperator<Path> renameFn) {
		
		this.destinationFolder = destinationFolder;
		this.filenameFn = filenameFn;
		this.renameFn = renameFn; 
	}
	
	public boolean hasReachedRenameLimit() {
		return renameCounter>=RENAME_MAX_LIMIT;
	}
		
	public Path applyRename(Path destination) throws LimitExceededException {
		Path newDestination = null;
		while(!hasReachedRenameLimit()) {
			renameCounter++;
			newDestination = renameFn.apply(destination);
			if(!Files.exists(newDestination)) {
				return newDestination;
			}
		}
		throw new LimitExceededException(String.format("Maximum %s rename reached.",RENAME_MAX_LIMIT));	
	}

	@Override
	public Result<T, U, EmptyContext> process(OutputChunk<T, U> outputChunk) {
		Path destination = getDestinationFolder(outputChunk);
		try {
			if(shouldRename(destination)) {
				destination = applyRename(destination);
			} 
			Files.write(destination, outputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);						
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write file (" + destination.toAbsolutePath().toString() + ").", e);
		} catch (LimitExceededException e) {
			throw new IllegalStateException(String.format("Unable to write file (%s). File %s exists rename attempted. %s", 
					destination.toAbsolutePath().toString(), getDestinationFolder(outputChunk).toAbsolutePath().toString(), e));
		} 
		return new SimpleResult<>(outputChunk.dataContext(), outputChunk.outputContext(), EmptyContext.emptyInstance());
	}

	public boolean shouldRename(Path destination) {
		return destination!=null && !Files.isDirectory(destination)&& Files.exists(destination);	
	}
	
	public Path getDestinationFolder(OutputChunk<T, U> outputChunk) {
		return destinationFolder.resolve(filenameFn.apply(outputChunk.dataContext(), outputChunk.outputContext()));
	}	
}
