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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
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
	Path filename = Path.of("foo.txt");
    Path fileRename = Path.of("foo2.txt");

    private String mockOutputString = "Test Bytes";
	private byte[] mockOutputBytes = mockOutputString.getBytes(StandardCharsets.UTF_8); 

	@TempDir
	private Path testFolder;
	
	@Mock private OutputChunk<EmptyContext, EmptyContext> mockOutputChunk;
	
	private FolderOutputDestination<EmptyContext, EmptyContext> spyOutputDestination;
		
	@BeforeEach
	void setup() {
		spyOutputDestination = Mockito.spy(new FolderOutputDestination<>(
				testFolder, (a, b)->filename, 
				(a)-> { 
					if(Files.exists(a)) {
						return testFolder.resolve(fileRename);
					}
					else return a;												
				})
		);			
	}
	
	@Test
	void testProcessSuccess() throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");	
			
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		Result<EmptyContext, EmptyContext, EmptyContext> result = underTest.process(mockOutputChunk);
		
		Path expectedFile = testFolder.resolve(filename);
		assertTrue(Files.exists(expectedFile), "Expected file (" + expectedFile.toString() + ") to exist, but it didn't.");
		assertEquals(mockOutputString, Files.readString(expectedFile));
		
		assertNull(result.dataContext());		// Should be null because the mock will not return anything
		assertNull(result.outputContext());		// Should be null because the mock will not return anything
		assertSame(EmptyContext.emptyInstance(), result.resultContext());
	}
	
	@Test
	void testProcess_withRename_IndexOutOfBoundsException() throws Exception {
		final Path filename = Path.of("foo.txt");
		final Path fileRename = Path.of("foo2.txt");		
		BiFunction<Path, Path, Path> nameFunc = ((a, b)->filename);	
		
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,(a)-> fileRename);
		
		//Creates a file with the same name so that rename call is triggered when process is called
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		
		//Rename function keeps setting file to same name therefore reaches limit
		IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);		
		assertThat(msg, allOf(containsString("exists rename attempted")));
	}
	
	@Test
	void testProcessFailure_throwsIllegalStateException() {
		final Path filename = Path.of("");	// Intentionally invalid filename
		final Path fileRename = Path.of("foo.txt");
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename, (a)->fileRename);
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);		
		assertThat(msg, allOf(containsString("Unable to write file"), containsString(testFolder.toAbsolutePath().toString())));
	}
	
	@Test
	void testProcess_withDefaultRename_success() throws Exception {
		Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		final FolderOutputDestination<EmptyContext, EmptyContext> underTest = new FolderOutputDestination<>(
				testFolder, (a, b)->filename,FolderOutputDestination.DEFAULT_RENAME_FUNCTION);
				
		//Creates a file with the same name so that rename call is triggered when process is called
		Path filename = Path.of("foo.txt");		
		Path fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);
		filename = Path.of("foo0000.txt");		
		fileDestination = testFolder.resolve(filename);
		Files.write(fileDestination, mockOutputChunk.bytes(), StandardOpenOption.WRITE,StandardOpenOption.CREATE_NEW);

		underTest.process(mockOutputChunk);

		final Path fileRename = Path.of("foo0001.txt");
		Path expectedFile = testFolder.resolve(fileRename);
		assertTrue(Files.exists(expectedFile), "Expected file (" + expectedFile.toString() + ") to exist, but it didn't.");
		assertEquals("foo0001.txt",fileRename.getFileName().toString());
	}
}