package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToMany;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

/**
 * Splits an XmlChunk into multiple XmlChunks.
 * 
 * Assumes that the root level will be preserved and replicated in each resulting XmlChunk. One XmlChunk will be
 * created per element below the root.  Characters between transaction elements will be ignored. 
 * 
 */
public class XmlSplittingTransformation extends XmlEventManipulation implements DataTransformationOneToMany<XmlDataChunk, XmlDataChunk> {
	private static final Logger logger = LoggerFactory.getLogger(XmlSplittingTransformation.class);

	private final int wrapperLevels;

	public XmlSplittingTransformation(int wrapperLevels) {
		this.wrapperLevels = wrapperLevels;
	}
	
	@Override
	public Stream<XmlDataChunk> process(XmlDataChunk dataChunk) {
		try {
			TransactionInfo transactionInfo = convertToTransactions(readToList(dataChunk), wrapperLevels);

			Builder<XmlDataChunk> streamBuilder = Stream.builder();
			int transactionsSize = transactionInfo.transactions().size();
			for (int i = 0; i < transactionsSize; i++) {
				byte[] replayTransactions = replayTransaction(transactionInfo, i);
				streamBuilder.accept(XmlDataChunk.create(replayTransactions, dataChunk.dataContext()));
			}
			return streamBuilder.build();
		} catch (XMLStreamException | TransformerFactoryConfigurationError | IOException e) {
			throw new IllegalStateException("Failed to split XML file.  %s".formatted(e.getMessage()), e);
		} 
	}
	
	private byte[] replayTransaction(TransactionInfo ti, int transactionNumber) throws XMLStreamException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Create ByteArrayOuputStream to capture output, OutputStreamWriter to force UTF-8 output, then XMLEventWriter that is AutoCloseable 
		try (os; var osw = new OutputStreamWriter(os, StandardCharsets.UTF_8); var ew = AutoCloseableXmlEventWriter.of(osw)) {
			ti.writePreamble(ew);
			ti.writeTransaction(ew, transactionNumber);
			ti.writePostamble(ew);
		}
		return os.toByteArray();
	}
}
