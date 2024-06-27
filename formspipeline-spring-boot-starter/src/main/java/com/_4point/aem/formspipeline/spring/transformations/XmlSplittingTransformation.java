package com._4point.aem.formspipeline.spring.transformations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
				byte[] replayTransactions = transactionInfo.replayTransactions(i);
				streamBuilder.accept(XmlDataChunk.create(replayTransactions, dataChunk.dataContext()));
			}
			return streamBuilder.build();
		} catch (XMLStreamException | TransformerFactoryConfigurationError | IOException e) {
			throw new IllegalStateException("Failed to split XML file.  %s".formatted(e.getMessage()), e);
		} 
	}

	enum State {
		InPreamble,				// Beginning of file up to the point where transactions occur
		BeginTransaction,		// Start/inside a transaction
		EndTransaction,			// End element of a transaction
		BetweenTransactions,	// Character data, etc. outside of a transaction
		InPostamble;			// After the end of the list transaction until the end of the file

		// Checks to see if the current state should be changed.
		private static State checkState(State currentState, boolean isStartElementEvent, boolean isEndElementEvent, int currentDepth, int transactionDepth) {
			return switch (currentState) {
			case InPreamble -> currentDepth <= transactionDepth ? State.InPreamble : State.BeginTransaction;
			case BeginTransaction -> isEndElementEvent && currentDepth == transactionDepth + 1 ? State.EndTransaction : State.BeginTransaction;
			case EndTransaction  -> currentDepth <= transactionDepth ? (isEndElementEvent ? State.InPostamble : State.BetweenTransactions) : State.BeginTransaction;
			case BetweenTransactions -> isStartElementEvent ? (currentDepth <= transactionDepth ? State.InPostamble : State.BeginTransaction) : State.BetweenTransactions;  
			case InPostamble -> State.InPostamble;
			};
		}
	}
	
	private static TransactionInfo convertToTransactions(List<EventInfo> eventList, int transactionDepth) {
		List<EventInfo> preambleList = null;
		List<EventInfo> postAmbleList;
		List<List<EventInfo>> transactionList = new ArrayList<>(1024);
		State currentState = State.InPreamble;
		int transactionStartIndex = 0;
		for (int i = 0; i < eventList.size(); i++) {
			EventInfo currentEvent = eventList.get(i);
			State newState = State.checkState(currentState, currentEvent.xmlEvent().isStartElement(), currentEvent.xmlEvent().isEndElement(), currentEvent.depth(), transactionDepth);
			if (currentState != newState) {
				if (currentState == State.InPreamble && newState == State.BeginTransaction) {
					// Hit first transaction, so save what we've processed so far to preamble.
					preambleList = eventList.subList(0, i);
					transactionStartIndex = i;
				} else if (currentState == State.BeginTransaction && newState == State.EndTransaction) {
					// We've completed a transaction, so store it away.
					transactionList.add(eventList.subList(transactionStartIndex, i));
					transactionStartIndex = i;
				} else if ((currentState == State.EndTransaction || currentState == State.BetweenTransactions) && (newState == State.InPostamble || newState == State.BetweenTransactions|| newState == State.BeginTransaction)) {
					// We are between transactions and so, store the incoming data
					transactionStartIndex = i;
				} else if (currentState == State.BetweenTransactions && newState == State.BeginTransaction) {
					// We have started a new state, stop ignoring characters
				} else {
					throw new IllegalStateException("Unexpected State transition from '" + currentState.toString() + "' to '" + newState.toString() + "'.");
				}
				currentState = newState;
			}
		}
		postAmbleList = eventList.subList(transactionStartIndex, eventList.size());
		
		return new TransactionInfo(preambleList, transactionList, postAmbleList);
	}

}
