package com._4point.aem.formspipeline.utils;

import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.PrinterName;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will send the output from AEM to a printer using a printerService.print() call.
 *
 */
public class JavaPrinterService {
	private static final Logger log = LoggerFactory.getLogger(JavaPrinterService.class);
	private final PrintService printService;
	private final DocFlavor docFlavor;
	
	public enum PrinterLanguage {
		Pcl(DocFlavor.BYTE_ARRAY.PCL, "application/vnd.hp-pcl"),
		Postscript(DocFlavor.BYTE_ARRAY.POSTSCRIPT, "application/postscript"),
		Pdf(DocFlavor.BYTE_ARRAY.AUTOSENSE, "application/pdf"),
		Other(DocFlavor.BYTE_ARRAY.AUTOSENSE, "application/octet-stream")
		;
		
		private final DocFlavor docFlavor;
		private final String contentType;

		private PrinterLanguage(DocFlavor docFlavor, String contentType) {
			this.docFlavor = docFlavor;
			this.contentType = contentType;
		}
		
		// Package private for unit testing.
		static PrinterLanguage fromContentType(String contentType) {
			String testString = contentType.toLowerCase();
			for (PrinterLanguage value : values()) {
				if (value.contentType.equals(testString)) {
					return value;
				}
			}
			return Other;
		}
	}

	// Package private for unit testing.
	JavaPrinterService(PrintService printService, DocFlavor docFlavor) {
		this.printService = printService;
		this.docFlavor = docFlavor;
	}

	public JavaPrinterService(String printerName, String contentType) {
		this(locatePrintService(printerName), PrinterLanguage.fromContentType(contentType).docFlavor);
	}

	public JavaPrinterService(String printerName) {
		this(locatePrintService(printerName), DocFlavor.BYTE_ARRAY.AUTOSENSE);
	}

	public void print(byte[] printBytes, String printFilename) {
		DocPrintJob job = printService.createPrintJob();
		job.addPrintJobListener(new PrintJobAdapter() {
			public void printDataTransferCompleted(PrintJobEvent event) {
				log.info("Data transfer complete (" + printFilename + ").");
			}

			public void printJobCompleted(PrintJobEvent pje) {
				log.info("Printing complete (" + printFilename + ").");
			}

			public void printJobRequiresAttention(PrintJobEvent pje) {
				log.error("An error occurred during print output (" + printFilename + ").");
			}
		});

		Doc doc = new SimpleDoc(printBytes, this.docFlavor, docAttributes(printFilename));
		try {
			job.print(doc, printRequestAttributes(printFilename));
		} catch (PrintException e) {
			throw new JavaPrinterServiceException("An error occurred while attempting to print '" + printFilename + "' on '" + this.printService.getName() + "'.", e);
		}
	}

	private static DocAttributeSet docAttributes(String printFilename) {
		DocAttributeSet docAttrib = new HashDocAttributeSet();
		docAttrib.add(new DocumentName(printFilename, Locale.getDefault()));
		return docAttrib;
	}

	private static PrintRequestAttributeSet printRequestAttributes(String printFilename) {
		PrintRequestAttributeSet attrib = new HashPrintRequestAttributeSet();
		attrib.add(new Copies(1));
		attrib.add(new JobName(printFilename, Locale.getDefault()));
		return attrib;
	}

	private static PrintService locatePrintService(String printerName) {
		HashPrintServiceAttributeSet attributeSet = new HashPrintServiceAttributeSet();
		attributeSet.add(new PrinterName(printerName, null));
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, attributeSet);

		if (printServices.length > 0) {
			return printServices[0];
		}
		throw new JavaPrinterServiceException("An error occurred while locating printer '" + printerName + "'. The printer could not be found.");
	}

	@SuppressWarnings("serial")
	public static class JavaPrinterServiceException extends RuntimeException {

		public JavaPrinterServiceException() {
		}

		public JavaPrinterServiceException(String message, Throwable cause) {
			super(message, cause);
		}

		public JavaPrinterServiceException(String message) {
			super(message);
		}

		public JavaPrinterServiceException(Throwable cause) {
			super(cause);
		}
	}
}
