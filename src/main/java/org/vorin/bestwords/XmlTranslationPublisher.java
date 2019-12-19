package org.vorin.bestwords;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class XmlTranslationPublisher implements TranslationPublisher {

    private static final Logger LOG = LogManager.getLogger();

    private File xmlFile;

	public XmlTranslationPublisher(File xmlFile) {
		this.xmlFile = xmlFile;
    }

	@Override
	public void addTranslation(String foreignWord, String meaning, String exampleForeignSentence, String exampleTranslatedSentence) {
        LOG.info(format("Added translation: foreightWord=%s, meaning=%s, exampleForeignSentence=%s, exampleTranslatedSentence=%s",
                    foreignWord, meaning, exampleForeignSentence, exampleTranslatedSentence));
	}

	@Override
	public void write() throws IOException {
		// TODO @af write to the xml file
	}

}