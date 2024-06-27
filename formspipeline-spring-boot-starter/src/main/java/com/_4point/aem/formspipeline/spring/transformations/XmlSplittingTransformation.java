package com._4point.aem.formspipeline.spring.transformations;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToMany;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;
import com._4point.aem.formspipeline.spring.transformations.XmlEventManipulation.AutoCloseableXmlEventWriter;

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
				final int index = i;
				byte[] replayTransactions = transactionInfo.replayTransaction((t,w)->writeTransactionWithPrePostAmble(index, t, w));
				streamBuilder.accept(XmlDataChunk.create(replayTransactions, dataChunk.dataContext()));
			}
			return streamBuilder.build();
		} catch (XMLStreamException | TransformerFactoryConfigurationError | IOException e) {
			throw new IllegalStateException("Failed to split XML file.  %s".formatted(e.getMessage()), e);
		} 
	}
	
	private void writeTransactionWithPrePostAmble(int transactionNumber, TransactionInfo ti, AutoCloseableXmlEventWriter ew) {
		try {
			ti.writePreamble(ew);
			ti.writeTransaction(ew, transactionNumber);
			ti.writePostamble(ew);
		} catch (XMLStreamException e) {
			throw new IllegalStateException("Failed to construct split XML file.  %s".formatted(e.getMessage()), e);
		}
	}
}
