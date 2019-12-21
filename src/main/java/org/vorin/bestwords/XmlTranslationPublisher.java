package org.vorin.bestwords;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Logger;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

public class XmlTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(XmlTranslationPublisher.class);

	private final File xmlFile;
	private final WordList wordlist;

	public XmlTranslationPublisher(File xmlFile) {
		this.xmlFile = xmlFile;
		this.wordlist = new WordList();
	}

	public XmlTranslationPublisher(File xmlFile, WordList wordList) {
		this.xmlFile = xmlFile;
		this.wordlist = wordList;
	}

	@Override
	public void addMeaning(String foreignWord,
						   String wordMeaning,
						   String source) {
		LOG.info(format("added meaning from [%s]: foreignWord=%s, meaning=%s", source, foreignWord, wordMeaning));
		wordlist.addMeaning(foreignWord, wordMeaning, source);
	}

	@Override
	public void addExampleSentence(String foreignWord,
								   String wordMeaning,
								   String exampleSentence,
								   String source) {
		LOG.info(format("added example sentence from [%s]: foreignWord=%s, meaning=%s, exampleSentence=%s",
						source, foreignWord, wordMeaning, exampleSentence));
		wordlist.addExampleSentence(foreignWord, wordMeaning, exampleSentence, source);
	}

	@Override
	public void writeToTarget() throws IOException {
		wordlist.writeToXml(xmlFile);
	}

}