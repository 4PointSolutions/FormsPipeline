package com._4point.aem.formspipeline.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.utils.JavaPrinterService.JavaPrinterServiceException;
import com._4point.aem.formspipeline.utils.JavaPrinterService.PrinterLanguage;

@ExtendWith(MockitoExtension.class)
class JavaPrinterServiceTest {

	@Captor ArgumentCaptor<PrintRequestAttributeSet> printAttibs;
	@Captor ArgumentCaptor<Doc> doc;

	@BeforeEach
	void setUp() throws Exception {
	}

	enum PrinterLanguageScenario {
		Pcl(PrinterLanguage.Pcl, List.of("application/vnd.hp-pcl", "application/Vnd.HP-PCL")),
		Postscript(PrinterLanguage.Postscript, List.of("application/postscript", "application/PostScript")),
		Pdf(PrinterLanguage.Pdf, List.of("application/pdf", "application/PDF")),
		Other(PrinterLanguage.Other, List.of("application/octet-stream", "foo/bar"))
		;
		
		private final PrinterLanguage expectedResult;
		private final List<String> contentTypes;
		
		private PrinterLanguageScenario(PrinterLanguage expectedResult, List<String> contentTypes) {
			this.expectedResult = expectedResult;
			this.contentTypes = contentTypes;
		}
	}
	@ParameterizedTest
	@EnumSource
	void testPrinterLanguageFormContentType(PrinterLanguageScenario scenario) {
		for (String contentType : scenario.contentTypes) {
			assertEquals(scenario.expectedResult, PrinterLanguage.fromContentType(contentType));
		}
	}

	@DisplayName("PrinterLanguageScenario enum should have 1:1 mapping to PrinterLanguage")
	@Test
	void testPrinterLanguageScenarios() {
		assertEquals(asSetofNames(PrinterLanguage.values()), asSetofNames(PrinterLanguageScenario.values()));
	}

	private static <E extends Enum<E>> Set<String> asSetofNames(E[] array) {
		return Arrays.stream(array).map(E::toString).collect(Collectors.toSet());
	}
	
	@Test
	void testPrint(@Mock PrintService mockPrintService, @Mock DocPrintJob mockPrintJob) throws Exception {
		byte[] expectedBytes = "expected print bytes".getBytes(StandardCharsets.UTF_8);
		String expectedPrintName = "expected Print Name";
		DocFlavor expectedDocFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		
		Mockito.when(mockPrintService.createPrintJob()).thenReturn(mockPrintJob);
		Mockito.doNothing().when(mockPrintJob).print(doc.capture(), printAttibs.capture());
		
		JavaPrinterService underTest = new JavaPrinterService(mockPrintService, expectedDocFlavor);
		underTest.print(expectedBytes, expectedPrintName);
	
		Doc actualDoc = doc.getValue();
		assertAll(
				()->assertArrayEquals(expectedBytes, actualDoc.getStreamForBytes().readAllBytes()),
				()->assertEquals(expectedDocFlavor, actualDoc.getDocFlavor())
				);
	}

	@Test
	void testPrint_Exception(@Mock PrintService mockPrintService, @Mock DocPrintJob mockPrintJob) throws Exception {
		byte[] expectedBytes = "expected print bytes".getBytes(StandardCharsets.UTF_8);
		String expectedPrintName = "expected Print Name";
		String expectedPrinterName = "expected Printer Name";
		DocFlavor expectedDocFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		PrintException printException = new PrintException();
		
		Mockito.when(mockPrintService.createPrintJob()).thenReturn(mockPrintJob);
		Mockito.when(mockPrintService.getName()).thenReturn(expectedPrinterName);
		Mockito.doThrow(printException).when(mockPrintJob).print(doc.capture(), printAttibs.capture());
		
		JavaPrinterService underTest = new JavaPrinterService(mockPrintService, expectedDocFlavor);
		JavaPrinterServiceException ex = assertThrows(JavaPrinterServiceException.class, ()->underTest.print(expectedBytes, expectedPrintName));
		String msg = ex.getMessage();
		assertNotNull(msg);

		assertSame(printException, ex.getCause());
		assertThat(msg, allOf(
							containsString(expectedPrintName), 
							containsString(expectedPrinterName), 
							containsString("error occurred while attempting to print")
							));
		
		// Validate the parameters passed into print.
		Doc actualDoc = doc.getValue();
		assertAll(
				()->assertArrayEquals(expectedBytes, actualDoc.getStreamForBytes().readAllBytes()),
				()->assertEquals(expectedDocFlavor, actualDoc.getDocFlavor())
				);
	}

}
