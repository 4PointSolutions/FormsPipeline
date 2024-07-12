package com._4point.aem.formspipeline.transformations;

import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToOne;
import com._4point.aem.formspipeline.payloads.PdfPayload;
import com._4point.aem.formspipeline.payloads.PsPayload;
import com._4point.aem.formspipeline.payloads.XmlPayload;
import com._4point.aem.formspipeline.api.Message;

/**
 * This class is used to call AEM to generate a Postscript document by first generating a PDF and then converting it to PostScript.
 * 
 * The AEM Print generation has many parameters.  This class looks for them in the incoming data chunk's context.  Typically,
 * these parameters will be supplied from a process upstream in the pipeline or in a context that exposes the environment
 * parameters (usually, the Spring environent will be made available in a Context that is added at the start of the
 * pipeline - so all requests for parameters will default to the Spring environment if no steps in the pipeline otherwise
 * provide a parameter).
 * 
 * This class provides helper classes for getting parameters it needs into and out of a Context.  These are available
 * through the methods on the AemOutputServicePrintGenerationContext object.  contextWriter() (which produces a context with 
 * AEMOutputServicePrintGenerator parameters in it) and contextReader() (which provides a view on an existing context).
 * 
 * Typically, some process upstream of the AemOutputServicePdfGeneration step will use the contextWriter() to create a context 
 * with the parameters it knows about and then it will combine that context with the existing context using an AggregateContext object.
 *
 * When building an AemOutputServicePrintGeneration object, the client application will need to supply parameters (like the
 * location of an AEM server, credentials to talk to that server, etc.).  This is done using a Builder object retrieved 
 * by calling the static builder() method.
 *
 */
public class AemOutputServicePsGenerationViaPdf implements DataTransformationOneToOne<Message<XmlPayload>, Message<PsPayload>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePsGenerationViaPdf.class);

	private final Function<? super Message<XmlPayload>, ? extends Message<PdfPayload>> generatePdfFn;
	private final Function<? super Message<PdfPayload>, ? extends Message<PsPayload>> generatePsFn;
	
	public AemOutputServicePsGenerationViaPdf(
			Function<? super Message<XmlPayload>, ? extends Message<PdfPayload>> generatePdfFn,
			Function<? super Message<PdfPayload>, ? extends Message<PsPayload>> generatePsFn) {
		this.generatePdfFn = generatePdfFn;
		this.generatePsFn = generatePsFn;
	}
	
	public AemOutputServicePsGenerationViaPdf(AemOutputServicePdfGeneration pdfGenerator, AemConvertPdfToPsService pdfToPsConverter) {
		this(pdfGenerator::process, pdfToPsConverter::process);
	}


	@Override
	public Message<PsPayload> process(Message<XmlPayload> msg) {
		return Stream.of(msg)
					 .map(generatePdfFn)
					 .map(generatePsFn)
					 .findFirst()
					 .orElseThrow();
	}
}
