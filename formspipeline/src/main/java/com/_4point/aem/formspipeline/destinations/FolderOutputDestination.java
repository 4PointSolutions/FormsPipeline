package com._4point.aem.formspipeline.destinations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.results.SimpleResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderOutputDestination<T extends Context, U extends Context> implements OutputDestination<OutputChunk<T, U>, 
															Result<T, U, ? extends Context>> {
	
	private static final Logger log = LoggerFactory.getLogger(FolderOutputDestination.class);
	
	private static final int RENAME_MAX_LIMIT = 1000;
	private final Path destinationFolder;
	private final BiFunction<T, U, Path> filenameFn;
	private UnaryOperator<Path> renameFn;
	private int renameCounter=0;
	
	protected static final UnaryOperator<Path> DEFAULT_RENAME_FUNCTION =
		(a)-> {
			if(a!=null && !Files.isDirectory(a)){
				if(!Files.exists(a)) {
					return a;
				}
				return processDefaultRename(a);
			} 
			return a;
		};
			
	public FolderOutputDestination(Path destinationFolder,
			 BiFunction<T, U, Path> filenameFn, UnaryOperator<Path> renameFn) {
		
		this.destinationFolder = destinationFolder;
		this.filenameFn = filenameFn;
		this.renameFn = renameFn; 
	}
	
	//Intended to be used by the class itself and unit test
	//Should use destinationFolder folder instead of DEFAULT_FOLDER	
	protected static Path processDefaultRename(Path a) {
		int i = 0;
		Path newDestination;
		Path DEFAULT_FOLDER = Path.of("C:\\TempPath");
		while(i<RENAME_MAX_LIMIT) {
			try {
				String newFileName = getNewDefaultFileName(a,i++);
				Files.move(a, a.resolveSibling(newFileName));						
				newDestination = DEFAULT_FOLDER.resolve(a);
				if(!Files.exists(newDestination)) {
					return newDestination;
				}
			} catch (IOException e) {
				log.info(String.format("Rename %s to %s failed.", a.getFileName().toString(), e.getMessage()));
			}
		}	
		throw new IndexOutOfBoundsException(String.format("Maximum %s rename reached for %s.",RENAME_MAX_LIMIT,a.getFileName().toString()));
	}
	

	//Intended to be used by the class itself and unit test
	//Rename the file with the new name added leading zero for less than 4 digit numbers.
    private static String getNewDefaultFileName(Path originalFile, int counter) {
    	String suffix = String.format("%04d", counter); //4 digit number with leading zero format
		String fileName = originalFile.getFileName().toString();								
		int lastDotIndex = fileName.lastIndexOf('.');			
		return fileName.substring(0, lastDotIndex ) + suffix + fileName.substring(lastDotIndex);
    }
		
	private boolean shouldRename(Path destination) {
		return destination!=null && !Files.isDirectory(destination)&& Files.exists(destination);	
	}
	
	//Intended to be used by the class itself and unit test
	private Path getDestinationFolder(OutputChunk<T, U> outputChunk) {
		return destinationFolder.resolve(filenameFn.apply(outputChunk.dataContext(), outputChunk.outputContext()));
	}	
	
	private Path applyLimitedRename(Path destination) {
		Path newDestination = null;
		while(renameCounter<RENAME_MAX_LIMIT) {
			renameCounter++;
			newDestination = renameFn.apply(destination);
			if(!Files.exists(newDestination)) {
				return newDestination;
			}
		}
		throw new IndexOutOfBoundsException(String.format("Maximum %s rename reached for %s.",RENAME_MAX_LIMIT,destination.toString()));	
	}

	@Override
	public Result<T, U, EmptyContext> process(OutputChunk<T, U> outputChunk) {
		Path destination = getDestinationFolder(outputChunk);
		try {
			if(shouldRename(destination)) {
				destination = applyLimitedRename(destination);
			} 
			Files.write(destination, outputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);						
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write file (" + destination.toAbsolutePath().toString() + ").", e);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException(String.format("Unable to write file (%s). File %s exists rename attempted. %s", 
					destination.toAbsolutePath().toString(), getDestinationFolder(outputChunk).toAbsolutePath().toString(), e));
		} 
		return new SimpleResult<>(outputChunk.dataContext(), outputChunk.outputContext(), EmptyContext.emptyInstance());
	}

}
