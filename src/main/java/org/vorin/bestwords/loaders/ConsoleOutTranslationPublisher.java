package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

public class ConsoleOutTranslationPublisher implements TranslationPublisher {

	private static final Logger LOG = Logger.get(ConsoleOutTranslationPublisher.class);

	private final Wordlist wordlist;

	public ConsoleOutTranslationPublisher() {
		this.wordlist = new Wordlist();
	}

	@Override
	public void addMeaning(String foreignWord, String meaning, String wordType, String source) {
		addMeaning(foreignWord, meaning, wordType, source, null);
	}

	@Override
	public void addMeaning(String foreignWord, String wordMeaning, String wordType, String source, String comment) {
		var meaning = wordlist.addMeaning(foreignWord, wordMeaning, wordType, source);
		meaning.addComment(comment);
		LOG.info(format("added meaning from [%s]: foreignWord=%s, meaning=%s, wordType=%s, comment=%s, total words=%s",
				source, foreignWord, wordMeaning, wordType, comment, wordlist.size()));
	}

	@Override
	public void addExampleSentence(String foreignWord,
								   String wordMeaning,
								   String wordType,
								   String exampleSentence,
								   String source) {
		LOG.info(format("added example sentence from [%s]: foreignWord=%s, meaning=%s, wordType=%s, exampleSentence=%s",
						source, foreignWord, wordMeaning, wordType, exampleSentence));
		wordlist.addExampleSentence(foreignWord, wordMeaning, wordType, exampleSentence, source);
	}

	@Override
	public boolean exampleSentenceExists(String foreignWord, String wordMeaning, String wordType) {
		var meaning = wordlist.findMeaning(foreignWord, wordMeaning, wordType);

		if (meaning != null && !isNullOrEmpty(meaning.getExampleSentence())) {
			return true;
		}
		return false;
	}

	@Override
	public Wordlist getWordlist() {
		return wordlist;
	}

	@Override
	public void writeToTarget() {
		wordlist.getTranslations().forEach(System.out::println);
	}

}