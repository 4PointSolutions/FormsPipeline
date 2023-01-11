package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class FolderOutputDestinationTest {
    private String mockOutputString = "Test Bytes";
	private byte[] mockOutputBytes = mockOutputString.getBytes(StandardCharsets.UTF_8); 
	
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
	private void validateSuccessFileCreated(Path testFolder, Result<EmptyContext, EmptyContext, EmptyContext> result, String expectedFileName)
			throws IOException {
		final Path expectedFileRename = Path.of(expectedFileName);
		Path expectedFile = testFolder.resolve(expectedFileRename);
		assertTrue(Files.exists(expectedFile), "Expected file (" + expectedFile.toString() + ") to exist, but it didn't.");
		assertEquals(expectedFileName,expectedFileRename.getFileName().toString());
		assertEquals(mockOutputString, Files.readString(expectedFile));
		
		assertNull(result.dataContext());		// Should be null because the mock will not return anything
		assertNull(result.outputContext());		// Should be null because the mock will not return anything
		assertSame(EmptyContext.emptyInstance(), result.resultContext());
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
			
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);

		validateSuccessFileCreated(testFolder, result, "foo.txt");
	}
	
	@Test
	//Original file exist but rename file doesn't exist so writes successfully to destination upon rename
	void testProcess_withRename_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");	
		byte[] emptyBytes = "".getBytes(StandardCharsets.UTF_8); 
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates original file file so that rename call is triggered when process is called
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, emptyBytes, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
				
		validateSuccessFileCreated(testFolder, result, "foo2.txt");
	}
	
	@Test
	void testProcess_withRenameMissingExtension_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2");	
		byte[] emptyBytes = "".getBytes(StandardCharsets.UTF_8); 
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates original file file so that rename call is triggered when process is called
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, emptyBytes, StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
				
		validateSuccessFileCreated(testFolder, result, "foo2.txt");
	}
	
	
	@Test
	@Tag("slow")
	void testProcess_withMultipleRename_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");		
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates original file and renamed file so that rename call is triggered when process is called
		writeFile(testFolder, filename, fileRename,999);		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		validateSuccessFileCreated(testFolder, result, "foo0999.txt");
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
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		validateSuccessFileCreated(testFolder, result, "foo");
	}
	
	@Test
	@Tag("slow")
	void testProcess_differentFileExtension_Success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo.xml");
		final Path fileRename = Path.of("foo2.txt");		
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates a file with the same name so that rename call is triggered when process is called		
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		validateSuccessFileCreated(testFolder, result, "foo2.txt");
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
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		validateSuccessFileCreated(testFolder, result, "result");
	}
	
	@Test
	@Tag("slow")
	void testProcess_missingFileExtensionWithRename_success(@TempDir Path testFolder) throws Exception {
		final Path filename = Path.of("foo");
		final Path fileRename = Path.of("foo2");	
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates a file with the same name so that rename call is triggered when process is called		
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		fileDestination = testFolder.resolve(fileRename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		validateSuccessFileCreated(testFolder, result, "foo0000");
	}
	
	@Test
	void testProcess_withDefaultRename_success(@TempDir Path testFolder) throws Exception {		
		final Path filename = Path.of("foo.xml");	
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename);
				
		//Creates a file with the same name so that rename call is triggered when process is called		
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		validateSuccessFileCreated(testFolder, result, "result.xml");
	}
	
	@Test
	void testProcess_withDefaultRenameMultiple_success(@TempDir Path testFolder) throws Exception {		
		final Path filename = Path.of("foo.txt");	
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename);
				
		//Creates a file with the same name so that rename call is triggered when process is called		
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		fileDestination = testFolder.resolve(Path.of("result.txt"));
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);

		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		validateSuccessFileCreated(testFolder, result, "foo0000.txt");
	}
	

}
