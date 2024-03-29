package com._4point.aem.formspipeline.destinations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.OutputDestination;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.SingletonContext;
import com._4point.aem.formspipeline.results.SimpleResult;

/**
 * FolderOutputDestination - Writes the output to a local file folder.
 * 
 * It will not overwrite an existing file, instead it will calls a provided rename function (or a default rename function
 * if a rename function was not provided).  If the provided rename function does not produce a name that does not already
 * exist, then this object will append a numerical value to the renamed name to try and create a file that does not yet
 * exist.  After 9999 attempts at this, it will give up and throw an exception. 
 *
 * @param <D> - DataContext - The type of context created from the Data Transformation steps.
 * @param <O> - OutputContext - The type of context created from the Output Generation step.
 */
public class FolderOutputDestination<D extends Context, O extends Context> implements OutputDestination<OutputChunk<D, O>, 
															Result<D, O, ? extends Context>> {
	private static final Logger logger = LoggerFactory.getLogger(FolderOutputDestination.class);

	private static final String CONTEXT_PREFIX = "com._4point.aem.formspipeline.destinations.FolderOutputDestination"; 
	private static final String FILENAME_CONTEXT_KEY = CONTEXT_PREFIX + ".filename"; 
	private static final int RENAME_MAX_LIMIT = 1000;
	private final Path destinationFolder;
	private final BiFunction<D, O, Path> filenameFn;
	private final UnaryOperator<Path> renameFn;
	
	public static final UnaryOperator<Path> DEFAULT_RENAME_FUNCTION = (a)-> Path.of("result");
			
	/**
	 * Constructor
	 * 
	 * @param destinationFolder - Folder to write to
	 * @param filenameFn - Function that provides the initial filename
	 * @param renameFn - Function that provides a new filename if the previous one exists.
	 */
	public FolderOutputDestination(Path destinationFolder, BiFunction<D, O, Path> filenameFn, UnaryOperator<Path> renameFn) {		
		this.destinationFolder = destinationFolder;
		this.filenameFn = filenameFn;
		this.renameFn = renameFn; 
	}
	
	/**
	 * Constructor
	 * 
	 * Provides a default rename function.  The default function returns a filename of "result".
	 * 
	 * @param destinationFolder - Folder to write to
	 * @param filenameFn - Function that provides the initial filename
	 */
	public FolderOutputDestination(Path destinationFolder, BiFunction<D, O, Path> filenameFn) {		
		this.destinationFolder = destinationFolder;
		this.filenameFn = filenameFn;
		this.renameFn = DEFAULT_RENAME_FUNCTION; 
	}

	//Intended to be used by the class itself and unit test
	//Rename the file with the new name added leading zero for less than 4 digit numbers.
    private static String getNewFileNameWithSuffix(Path originalFile, int counter) {
    	String suffix = String.format("%04d", counter); //4 digit number with leading zero format
		String fileName = originalFile.getFileName().toString();								
		Optional<String> extension = getFileExtension(fileName);	
		if(extension.isPresent()) {
			return getFileNameExcludingExtension(fileName)+suffix+"."+extension.get();
		}
		return fileName + suffix;
    }
		
	private static boolean shouldRename(Path destination) {
		//As long as destination exist whether it's a file or folder same logic applies
		return destination!=null && Files.exists(destination);	
	}
	
	//Intended to be used by the class itself and unit test
	private Path getDestinationFolder(OutputChunk<D, O> outputChunk) {
		return destinationFolder.resolve(filenameFn.apply(outputChunk.dataContext(), outputChunk.outputContext()));
	}	

	private static String getFileNameExcludingExtension(String filename) {
	    return filename.substring(0, filename.lastIndexOf("."));	    		
	}

	private static Optional<String> getFileExtension(String filename) {
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}
	
	private Path applyLimitedRename(Path destination) {
		String originalFilename = destination.getFileName().toString();
		//Using the default rename has no file extension		
		Path newDestination = getRenamedFile(originalFilename, destinationFolder.resolve(renameFn.apply(destination)));
		logger.debug("applyLimitedRename newDestination {}", newDestination);
		if(!Files.exists(newDestination)) {
			logger.debug("applyLimitedRename newDestination {} doesn't exist", newDestination);
			return newDestination;			
		}
		
		for(int renameCounter=0;renameCounter<RENAME_MAX_LIMIT;renameCounter++) {
			String renamedFile = getNewFileNameWithSuffix(Path.of(originalFilename), renameCounter);
			logger.debug("applyLimitedRename renamedFile {}.", renamedFile);
			Path rnFile = destination.getParent().resolve(Path.of(renamedFile));			
			if(!Files.exists(rnFile)) {
				return rnFile;
			} 
		}
		throw new IndexOutOfBoundsException(String.format("Maximum %s rename reached for %s.",RENAME_MAX_LIMIT,destination.toString()));	
	}

	//If the rename file is missing the extension than give it the same extension as the original file.
	private static Path getRenamedFile(String originalFilename, Path newDestination) {
		Optional<String> renameExtension = getFileExtension(newDestination.getFileName().toString());
		Optional<String> origExtension = getFileExtension(originalFilename);
		if(renameExtension.isPresent() || !origExtension.isPresent()) {
			return newDestination;
		}
		return newDestination.resolve(Path.of(newDestination.toAbsolutePath().toString()+"."+origExtension.get()));			
	}

	/**
	 * Process an OutputChunk and write the contents out to the destination folder.
	 * 
	 *
	 */
	@Override
	public Result<D, O, Context> process(OutputChunk<D, O> outputChunk) {
		Path destination = getDestinationFolder(outputChunk);
		try {
			if(shouldRename(destination)) {
				destination = applyLimitedRename(destination);
			} 		
			if (logger.isDebugEnabled()) {
				logger.debug("writing to destination {}", (destination != null?destination.toAbsolutePath().toString():null));					
			}
			Files.write(destination, outputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
			return new SimpleResult<>(outputChunk.dataContext(), outputChunk.outputContext(), SingletonContext.of(FILENAME_CONTEXT_KEY, destination.toString()));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write file (" + (destination!=null?destination.toAbsolutePath().toString():null) + ").", e);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException(String.format("Unable to write file (%s). File %s exists rename attempted. %s", 
					(destination != null ?destination.toAbsolutePath().toString():""), 
					getDestinationFolder(outputChunk).toAbsolutePath().toString(), e));
		} 
	}

	/**
	 * Returns a ContextReader used for reading the result context from a FolderOutputDestination.
	 * 
	 * This is provided for standardization, but since the only thing written to the result context is
	 * the filename, you can use the static getFilenameWritten() method instead and save some code.
	 * 
	 * @param c
	 * @return
	 */
	public static ContextReader reader(Context c) { return new ContextReader(c); }
	
	/**
	 * An object for reading the result Context from the FolderOutputDestination object. 
	 *
	 */
	public static class ContextReader {
		private final Optional<Path> filenameWritten;

		public ContextReader(Context context) {
			this.filenameWritten = getFilenameWritten(context);
		}

		public Optional<Path> filenameWritten() {
			return filenameWritten;
		}
	}
	
	/**
	 * Convenience function to extract the filename written by a FolderOutputDestination from the 
	 * result Context of the FolderOutputDestination process() step. 
	 * 
	 * @param context - Result Context from a FolderOutputDestination pipeline step. 
	 * @return - Filename written.  It should always return a result if the FolderOutputDestination step worked.
	 */
	public static Optional<Path> getFilenameWritten(Context context) {
		return context.getString(FILENAME_CONTEXT_KEY).map(Path::of);
	}
}
