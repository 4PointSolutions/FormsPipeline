package com._4point.aem.formspipeline.transformations;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.Context;
import com._4point.aem.formspipeline.api.DataChunk;
import com._4point.aem.formspipeline.api.OutputGeneration;
import com._4point.aem.formspipeline.chunks.PsPayload;

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
 * @param <D>
 * @param <T>
 */
public class AemOutputServicePsGenerationViaPdf <D extends Context, T extends DataChunk<D>> implements OutputGeneration<T, PsPayload<D>> {
	private static final Logger logger = LoggerFactory.getLogger(AemOutputServicePsGenerationViaPdf.class);

	private final AemOutputServicePdfGeneration<D, T> pdfGenerator;
	private final AemConvertPdfToPsService<D> pdfToPsConverter;
	
	
	public AemOutputServicePsGenerationViaPdf(AemOutputServicePdfGeneration<D, T> pdfGenerator, AemConvertPdfToPsService<D> pdfToPsConverter) {
		this.pdfGenerator = pdfGenerator;
		this.pdfToPsConverter = pdfToPsConverter;
	}

	@Override
	public PsPayload<D> process(T dataChunk) {
		return Stream.of(dataChunk)
					 .map(pdfGenerator::process)
					 .map(pdfToPsConverter::process)
					 .findFirst()
					 .orElseThrow();
	}
}
