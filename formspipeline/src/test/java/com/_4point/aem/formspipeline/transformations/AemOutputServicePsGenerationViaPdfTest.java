package com._4point.aem.formspipeline.transformations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com._4point.aem.formspipeline.api.Message;
import com._4point.aem.formspipeline.api.MessageBuilder;
import com._4point.aem.formspipeline.contexts.EmptyContext;
import com._4point.aem.formspipeline.payloads.PdfPayload;
import com._4point.aem.formspipeline.payloads.PsPayload;
import com._4point.aem.formspipeline.payloads.XmlPayload;

@ExtendWith(MockitoExtension.class)
class AemOutputServicePsGenerationViaPdfTest {
	private static final byte[] MOCK_XML_BYTES = "Mock Xml Bytes".getBytes();
	private static final byte[] MOCK_PDF_BYTES = "Mock Pdf Bytes".getBytes();
	private static final byte[] MOCK_PS_BYTES = "Mock Ps Bytes".getBytes();
	private static final XmlPayload MOCK_XML_PAYLOAD = new XmlPayload(MOCK_XML_BYTES);
	private static final PdfPayload MOCK_PDF_PAYLOAD = new PdfPayload(MOCK_PDF_BYTES);
	private static final PsPayload MOCK_PS_PAYLOAD = new PsPayload(MOCK_PS_BYTES);
	private static final Message<XmlPayload> MOCK_XML_MSG = MessageBuilder.createMessage(MOCK_XML_PAYLOAD, EmptyContext.emptyInstance());
	private static final Message<PdfPayload> MOCK_PDF_MSG = MessageBuilder.createMessage(MOCK_PDF_PAYLOAD, EmptyContext.emptyInstance());
	private static final Message<PsPayload> MOCK_PS_MSG = MessageBuilder.createMessage(MOCK_PS_PAYLOAD, EmptyContext.emptyInstance());
	
	private final AemOutputServicePdfGeneration pdfGemerator;
	private final  AemConvertPdfToPsService pdfToPsConverter;
	
	public AemOutputServicePsGenerationViaPdfTest(@Mock AemOutputServicePdfGeneration pdfGemerator, @Mock AemConvertPdfToPsService pdfToPsConverter) {
		this.pdfGemerator = pdfGemerator;
		this.pdfToPsConverter = pdfToPsConverter;
	}

	@BeforeEach
	void setup() {
		when(this.pdfGemerator.process(same(MOCK_XML_MSG))).thenReturn(MOCK_PDF_MSG);
		when(this.pdfToPsConverter.process(same(MOCK_PDF_MSG))).thenReturn(MOCK_PS_MSG);
	}
	
	@Test
	void testProcess() {
		AemOutputServicePsGenerationViaPdf underTest = new AemOutputServicePsGenerationViaPdf(pdfGemerator, pdfToPsConverter);
		Message<PsPayload> result = underTest.process(MOCK_XML_MSG);
		
		assertSame(MOCK_PS_MSG, result);
	}

}
