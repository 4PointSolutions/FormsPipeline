package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class FolderOutputDestinationTest {
    private static String mockOutputString = "Test Bytes";
	private static byte[] mockOutputBytes = mockOutputString.getBytes(StandardCharsets.UTF_8); 
	
	@Mock private OutputChunk<EmptyContext, EmptyContext> mockOutputChunk;
	
	//Helper method
	private void writeFile(Path testFolder, final Path filename, final Path fileRename, int counter) throws IOException {
		//Create original file and renamed file in same location to trigger rename
		byte[] emptyBytes = "".getBytes(StandardCharsets.UTF_8); 
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, emptyBytes, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		fileDestination = testFolder.resolve(fileRename);
		Files.write(fileDestination, emptyBytes, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
	
		//Create default renamed files
		String fileName = filename.getFileName().toString();	
		for(int i=0; i<counter; i++) {
	    	String suffix = String.format("%04d", i); //4 digit number with leading zero format									
			int lastDotIndex = fileName.lastIndexOf('.');
			String newFileName;
			if(lastDotIndex > 0) {
				newFileName = fileName.substring(0, lastDotIndex ) + suffix + fileName.substring(lastDotIndex);
			} else {
				newFileName = fileName + suffix;
			}
			
			Path newFile = Path.of(newFileName);
			fileDestination = testFolder.resolve(newFile);			
			Files.write(fileDestination, emptyBytes, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);			
		}
	}	
	
	//Helper Method
	private void validateIndexOutOfBoundException(final FolderOutputDestination<EmptyContext, EmptyContext> underTest) {
		//Rename function keeps setting file to same name therefore reaches limit
		IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);		
		assertThat(msg, allOf(containsString("exists rename attempted")));
	}
	
		
	@Test
	//Original file doesn't exist so writes successfully to destination on first pass through
	void testProcessSuccess(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");	
			
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename)
						  .callProcess()
						  .validateSuccessFileCreated("foo.txt");
	}
	
	@Test
	//Original file exist but rename file doesn't exist so writes successfully to destination upon rename
	void testProcess_withRename_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");	
		
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename)
						  .createFilename()	//Creates original file file so that rename call is triggered when process is called
						  .callProcess()
						  .validateSuccessFileCreated("foo2.txt");
	}
	
	@Test
	void testProcess_withRenameMissingExtension_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2");	
		
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename)
						  .createFilename()	//Creates original file file so that rename call is triggered when process is called
						  .callProcess()
						  .validateSuccessFileCreated("foo2.txt");
	}
	
	
	@Test
	@Tag("slow")
	void testProcess_withMultipleRename_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");		
		
		//Creates original file and renamed file so that rename call is triggered when process is called
		writeFile(testFolder, filename, fileRename,999);		
		
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename)
						  .callProcess()
						  .validateSuccessFileCreated("foo0999.txt");
	}


	
	@Test
	@Tag("slow")
	void testProcess_withRename_IndexOutOfBoundsException(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");		
		//Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates a file with the same name so that rename call is triggered when process is called
		writeFile(testFolder, filename, fileRename,1000);		
		validateIndexOutOfBoundException(underTest);
	}
	
	@Test
	@Tag("slow")
	void testProcess_missingFileExtension_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo");
		final Path fileRename = Path.of("foo2.txt");		
		
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename)
						  .callProcess()
						  .validateSuccessFileCreated("foo");
	}
	
	@Test
	@Tag("slow")
	void testProcess_differentFileExtension_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.xml");
		final Path fileRename = Path.of("foo2.txt");		
		
		tester(testFolder).setFilename(filename)
						  .setFileRename(fileRename) //Creates a file with the same name so that rename call is triggered when process is called
						  .createFilename()
						  .callProcess()
						  .validateSuccessFileCreated("foo2.txt");
	}
	
	@Test
	@Tag("slow")
	void testProcess_missingFileExtension_IndexOutOfBoundsException(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo");
		final Path fileRename = Path.of("foo2.txt");		
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates a file with the same name so that rename call is triggered when process is called
		writeFile(testFolder, filename, fileRename,1000);		
		validateIndexOutOfBoundException(underTest);
	}
	
	@Test
	void testProcess_missingFilename_success(@TempDir Path testFolder) throws IOException {
		final Path filename = Path.of("");	// Intentionally invalid filename

		tester(testFolder).setFilename(filename)
						  .callProcess()
						  .validateSuccessFileCreated("result");
	}
	
	@Test
	@Tag("slow")
	void testProcess_missingFileExtensionWithRename_success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo");
		final Path fileRename = Path.of("foo2");	
		
		tester(testFolder).setFilename(filename)
					   	  .setFileRename(fileRename)
					   	  .createFilename()
					   	  .createRename()
					   	  .callProcess()
					   	  .validateSuccessFileCreated("foo0000");
	}
	
	@Test
	void testProcess_withDefaultRename_success(@TempDir Path testFolder) throws Exception {		
		final Path filename = Path.of("foo.xml");	
				
		tester(testFolder).setFilename(filename)
						  .createFilename()
						  .callProcess()
						  .validateSuccessFileCreated("result.xml");
	}
	
	@Test
	void testProcess_withDefaultRenameMultiple_success(@TempDir Path testFolder) throws Exception {		
		final Path filename = Path.of("foo.txt");	

				
		tester(testFolder).setFilename(filename)
						  .createFilename()
						  .createFile(Path.of("result.txt")) //Create a file with the same name so that rename call is triggered when process is called
						  .callProcess()
						  .validateSuccessFileCreated("foo0000.txt");
	}
	
	private Tester tester(Path testFolder) { return new Tester(testFolder); }
	
	private class Tester {
		private static final byte[] EMPTY_BYTES = "".getBytes(StandardCharsets.UTF_8);
		private final Path testFolder;
		private Path filename = null;
		private Path fileRename = null;
		
		public Tester(Path testFolder) {
			this.testFolder = testFolder;
		}

		public Tester setFilename(Path filename) {
			this.filename = filename;
			return this;
		}

		public Tester setFileRename(Path fileRename) {
			this.fileRename = fileRename;
			return this;
		}

		public Tester createFilename() throws IOException {
			createFile(this.filename);
			return this;
		}

		public Tester createRename() throws IOException {
			createFile(this.fileRename);
			return this;
		}

		Asserter callProcess() {
			Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
			final FolderOutputDestination<EmptyContext, EmptyContext> underTest = fileRename == null 
					? new FolderOutputDestination<>(testFolder, (a, b)->filename) 
					: new FolderOutputDestination<>(testFolder, (a, b)->filename, (a)->fileRename);
				 var result = underTest.process(mockOutputChunk);
			 return new Asserter(result, testFolder);
		}
		
		private static class Asserter {
			private final Result<EmptyContext, EmptyContext, Context> result;
			private final Path testFolder;

			public Asserter(Result<EmptyContext, EmptyContext, Context> result, Path testFolder) {
				this.result = result;
				this.testFolder = testFolder;
			}

			//Helper Method
			private void validateSuccessFileCreated(String expectedFileName)
					throws IOException {
				final Path expectedFileRename = Path.of(expectedFileName);
				Path expectedFile = testFolder.resolve(expectedFileRename);
				assertTrue(Files.exists(expectedFile), "Expected file (" + expectedFile.toString() + ") to exist, but it didn't.");
				assertEquals(expectedFileName,expectedFileRename.getFileName().toString());
				assertEquals(mockOutputString, Files.readString(expectedFile));
				
				assertNull(result.dataContext());		// Should be null because the mock will not return anything
				assertNull(result.outputContext());		// Should be null because the mock will not return anything
				assertEquals(expectedFile, FolderOutputDestination.reader(result.resultContext()).filenameWritten().get());
				assertEquals(expectedFile, FolderOutputDestination.getFilenameWritten(result.resultContext()).get());	// Test both ways of getting the filename.
			}
		}
		
		public Tester createFile(Path filename) throws IOException {
			Path fileDestination = testFolder.resolve(filename);
			Files.write(fileDestination, EMPTY_BYTES, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
			return this;
		}
	}
}
