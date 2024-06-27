package com._4point.aem.formspipeline.spring.transformations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

abstract class XmlEventManipulation {
	private static final Logger logger = LoggerFactory.getLogger(XmlEventManipulation.class);
	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

	protected static List<EventInfo> readToList(XmlDataChunk dataChunk) throws XMLStreamException, IOException {
		try (InputStream os = dataChunk.asInputStream()) {
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(os);
			return EventInfo.readToList(reader);
		}
	}

	protected enum State {
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
	
	protected static TransactionInfo convertToTransactions(List<EventInfo> eventList, int transactionDepth) {
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

	protected record TransactionInfo(List<EventInfo> preamble, List<List<EventInfo>> transactions, List<EventInfo> postamble) {
		
		protected TransactionInfo writePreamble(XMLEventWriter ew) throws XMLStreamException {
			EventInfo.writeList(ew, preamble);
			return this;
		}

		protected TransactionInfo writeTransaction(XMLEventWriter ew, int transactionNumber) throws XMLStreamException {
			List<EventInfo> eventList = transactions.get(transactionNumber);
			logger.atDebug().addArgument(eventList.size()).addArgument(transactionNumber).log("Replaying {} events for transaction #{}.");
			EventInfo.writeList(ew, eventList);
			return this;
		}

		protected TransactionInfo writePostamble(XMLEventWriter ew) throws XMLStreamException {
			EventInfo.writeList(ew, postamble);
			return this;
		}

	}

	/**
	 * Reads an XML Data Chunk and creates a list of EventInfo objects.
	 * 
	 * Each EventInfo has an XMLEvent and a depth in the XML tree where it occurs.
	 * 
	 * The depth information is useful later when we determine what is pre/post amble
	 * and what are actualy transactions.
	 *
	 */
	protected record EventInfo(XMLEvent xmlEvent, int depth){
		/**
		 * Read the whole XML in as a list of events so that we can replay them.
		 *  
		 * @param dataChunk
		 * @return List of EventInfo objects
		 * @throws XMLStreamException
		 * @throws IOException
		 */
		protected static List<EventInfo> readToList(XMLEventReader reader) throws XMLStreamException {
			List<EventInfo> eventList = new ArrayList<>(1024);
			int currentLevel = 0;
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) { currentLevel++; }
				eventList.add(new EventInfo(event, currentLevel));
				if (event.isEndElement()) { currentLevel--; }
			}
			return eventList;
		}

		protected static void writeList(XMLEventWriter writer, List<EventInfo> eventList) throws XMLStreamException {
			for(EventInfo eventInfo : eventList) {
				writer.add(eventInfo.xmlEvent);
			}
		}
	};

	/**
	 * Stupid boilerplate class because XMLEventWriter does not implement AutoCloseable.
	 *
	 */
	protected static class AutoCloseableXmlEventWriter implements XMLEventWriter, AutoCloseable {
		private final XMLEventWriter xmlEventWriter;

		public AutoCloseableXmlEventWriter(XMLEventWriter xmlEventWriter) {
			this.xmlEventWriter = xmlEventWriter;
		}

		public void flush() throws XMLStreamException {
			xmlEventWriter.flush();
		}

		public void close() throws XMLStreamException {
			xmlEventWriter.close();
		}

		public void add(XMLEvent event) throws XMLStreamException {
			xmlEventWriter.add(event);
		}

		public void add(XMLEventReader reader) throws XMLStreamException {
			xmlEventWriter.add(reader);
		}

		public String getPrefix(String uri) throws XMLStreamException {
			return xmlEventWriter.getPrefix(uri);
		}

		public void setPrefix(String prefix, String uri) throws XMLStreamException {
			xmlEventWriter.setPrefix(prefix, uri);
		}

		public void setDefaultNamespace(String uri) throws XMLStreamException {
			xmlEventWriter.setDefaultNamespace(uri);
		}

		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
			xmlEventWriter.setNamespaceContext(context);
		}

		public NamespaceContext getNamespaceContext() {
			return xmlEventWriter.getNamespaceContext();
		}
		
		public static AutoCloseableXmlEventWriter of(OutputStreamWriter osw) throws XMLStreamException {
			return new AutoCloseableXmlEventWriter(xmlOutputFactory.createXMLEventWriter(osw));
		}
	}
}
