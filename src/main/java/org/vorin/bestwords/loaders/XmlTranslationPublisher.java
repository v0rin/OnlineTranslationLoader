package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
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

	@Override
	public void addMeaning(String foreignWord,
						   String wordMeaning,
						   String source) {
		wordlist.addMeaning(foreignWord, wordMeaning, source);
		LOG.info(format("added meaning from [%s]: foreignWord=%s, meaning=%s, total words=%s",
				source, foreignWord, wordMeaning, wordlist.size()));
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
	public boolean exampleSentenceExists(String foreignWord, String wordMeaning) {
		var meaning = wordlist.findMeaning(foreignWord, wordMeaning);

		if (meaning != null && !isNullOrEmpty(meaning.getExampleSentence())) {
			return true;
		}
		return false;
	}

	@Override
	public WordList getWordList() {
		return wordlist;
	}

	@Override
	public void writeToTarget() throws IOException {
		wordlist.writeToXml(xmlFile);
	}

}