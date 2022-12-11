package com._4point.aem.formspipeline.destinations;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import javax.naming.LimitExceededException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.OutputChunk;
import com._4point.aem.formspipeline.api.Result;
import com._4point.aem.formspipeline.contexts.EmptyContext;

@ExtendWith(MockitoExtension.class)
class FolderOutputDestinationTest {
	Path filename;// = Path.of("foo.txt");
    Path fileRename;// = Path.of("foo2.txt");

    private String mockOutputString;// = "Test Bytes";
	private byte[] mockOutputBytes;// = mockOutputString.getBytes(StandardCharsets.UTF_8); 

	@TempDir
	private Path testFolder;
	
	@Mock private OutputChunk<EmptyContext, EmptyContext> mockOutputChunk;
	
	private FolderOutputDestination<EmptyContext, EmptyContext> spyOutputDestination;
		
	@BeforeEach
	void setup() {
		filename = Path.of("foo.txt");
	    fileRename = Path.of("foo2.txt");

	    mockOutputString = "Test Bytes";
		mockOutputBytes = mockOutputString.getBytes(StandardCharsets.UTF_8); 
		
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
	void testApplyRename_success() throws LimitExceededException, IOException {
		String fileName = "foo2.txt";
		Path rfn = Path.of(fileName);
		Path destination = spyOutputDestination.applyRename(rfn);
		System.out.println(testFolder.toRealPath() + "\n" + 
							testFolder.toRealPath(LinkOption.NOFOLLOW_LINKS) + "\n" +
							testFolder.toAbsolutePath());
		assertEquals(1,spyOutputDestination.getRenameCounter());
		//assertEquals(testFolder.getFileSystem().+File.separator+fileName, 
		//			destination.toAbsolutePath());
	}
	
	@Test
	void testApplyRename_ThrowsLimitExceededException() { 		
		Mockito.when(spyOutputDestination.hasReachedRenameLimit()).thenReturn(true);
			
		Path rfn = Path.of("foo2.txt");
		LimitExceededException ex = assertThrows(LimitExceededException.class, ()->spyOutputDestination.applyRename(rfn));
		String msg = ex.getMessage();
		System.out.println(msg);
		assertNotNull(msg);
		assertThat(msg, allOf(containsString("Maximum 1000 rename reached.")));
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
	void testProcessSuccess_throwsLimitExceeeded() throws Exception {		
		//Mockito.when(mockOutputChunk.bytes()).thenReturn(mockOutputBytes);
		Mockito.when(spyOutputDestination.hasReachedRenameLimit()).thenReturn(true);
		Mockito.when(spyOutputDestination.getDestinationFolder(mockOutputChunk)).thenReturn(Path.of("foo.txt"));
		Mockito.when(spyOutputDestination.shouldRename(Mockito.any())).thenReturn(true);
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->spyOutputDestination.process(mockOutputChunk));
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
				testFolder, (a, b)->filename, (a)-> { 
					if(Files.exists(a)) {
						return testFolder.resolve(fileRename);
					}
					else return a;												
				});
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, ()->underTest.process(mockOutputChunk));
		String msg = ex.getMessage();
		assertNotNull(msg);
		
		assertThat(msg, allOf(containsString("Unable to write file"), containsString(testFolder.toAbsolutePath().toString())));
	}

}
