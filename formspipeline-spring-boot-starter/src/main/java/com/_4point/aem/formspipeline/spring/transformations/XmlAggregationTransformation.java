package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationManyToOne;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

public class XmlAggregationTransformation extends XmlEventManipulation	implements DataTransformationManyToOne<XmlDataChunk, XmlDataChunk> {
	private static final Logger logger = LoggerFactory.getLogger(XmlAggregationTransformation.class);

	private final int wrapperLevels;

	public XmlAggregationTransformation(int wrapperLevels) {
		this.wrapperLevels = wrapperLevels;
	}

	@Override
	public XmlDataChunk process(Stream<XmlDataChunk> dataChunks) {
	 	try {
			List<XmlDataChunk> chunkList = dataChunks.toList();
			List<TransactionInfo> transactions = chunkList.stream().map(this::readToListUnchecked).toList();
			TransactionInfo t1 = transactions.get(0);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			try (os; var osw = new OutputStreamWriter(os, StandardCharsets.UTF_8); var ew = AutoCloseableXmlEventWriter.of(osw)) {
				t1.writePreamble(ew);
				for (var currTrans : transactions) {
				// Create ByteArrayOuputStream to capture output, OutputStreamWriter to force UTF-8 output, then XMLEventWriter that is AutoCloseable 
					currTrans.writeTransaction(ew, 0);
				}
				t1.writePostamble(ew);
			}
			return XmlDataChunk.create(os.toByteArray(), chunkList.get(0).dataContext());
		} catch (XMLStreamException | IOException e) {
			throw new IllegalStateException("Failed to combine XML file.  %s".formatted(e.getMessage()), e);
		}
	}

	private TransactionInfo readToListUnchecked(XmlDataChunk dataChunk) {
		try {
			return convertToTransactions(readToList(dataChunk), wrapperLevels);
		} catch (XMLStreamException | IOException e) {
			throw new IllegalStateException("Failed to read XML file.  %s".formatted(e.getMessage()), e);
		}
	}
}
