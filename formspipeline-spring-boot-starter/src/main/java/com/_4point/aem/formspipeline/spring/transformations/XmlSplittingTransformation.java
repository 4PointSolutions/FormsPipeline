package com._4point.aem.formspipeline.spring.transformations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com._4point.aem.formspipeline.api.DataTransformation.DataTransformationOneToMany;
import com._4point.aem.formspipeline.spring.chunks.XmlDataChunk;

/**
 * Splits an XmlChunk into multiple XmlChunks.
 * 
 * Assumes that the root level will be discarded and one XmlChunk will be
 * created per element below the root.
 * 
 * code based on
 * https://stackoverflow.com/questions/5169978/split-1gb-xml-file-using-java
 *
 */
public class XmlSplittingTransformation implements DataTransformationOneToMany<XmlDataChunk, XmlDataChunk> {

	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
	private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

	@Override
	public Stream<XmlDataChunk> process(XmlDataChunk dataChunk) {
		try {
			XMLEventReader xsr = xmlInputFactory.createXMLEventReader(dataChunk.asInputStream());
			var event = xsr.nextTag(); // Advance to second level element
			event = xsr.nextTag(); // Advance to second level element

			Builder<XmlDataChunk> streamBuilder = Stream.builder();
			while (event.isStartElement()) {
				System.out.println("Found Tag " + event.asStartElement().getName().getLocalPart());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				XMLEventWriter ew = xmlOutputFactory.createXMLEventWriter(os);
				event = copyTag(event, xsr, ew);
				os.close();
				streamBuilder.accept(XmlDataChunk.create(os.toByteArray(), dataChunk.dataContext()));
			}
			return streamBuilder.build();
		} catch (XMLStreamException | TransformerFactoryConfigurationError | IOException e) {
			throw new IllegalStateException("Failed to split XML file.  %s".formatted(e.getMessage()), e);
		}
	}

	XMLEvent copyTag(XMLEvent currentEvent, XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
		writer.add(currentEvent);
		for(currentEvent = reader.nextEvent();!currentEvent.isEndElement();currentEvent = reader.nextEvent()) {
			if (currentEvent.isStartElement()) {
				currentEvent = copyTag(currentEvent, reader, writer);
			}
			writer.add(currentEvent);
		}
		writer.add(currentEvent);	// Write out the endElement event
		return reader.nextTag();
	}
}
