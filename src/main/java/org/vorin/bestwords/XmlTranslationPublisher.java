package org.vorin.bestwords;

import static java.lang.String.format;

import static org.vorin.bestwords.Util.print;

public class XmlTranslationPublisher implements TranslationPublisher {

	@Override
	public void addTranslation(String foreignWord, String meaning, String exampleForeignSentence, String exampleTranslatedSentence) {
        print(format("Added translation: foreightWord=%s, meaning=%s, exampleForeignSentence=%s, exampleTranslatedSentence=%s",
                    foreignWord, meaning, exampleForeignSentence, exampleTranslatedSentence));
	}

}