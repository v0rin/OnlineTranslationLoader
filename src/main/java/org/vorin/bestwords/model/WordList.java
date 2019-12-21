package org.vorin.bestwords.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.vorin.bestwords.AppConfig.RES_DIR;

@XmlRootElement(name = "wordList")
public class WordList {

    private static final Map<String, Translation> originalWordList = new HashMap<>();

    static {
        try {
            var wl = loadFromXml(new File(RES_DIR + "EnglishWordList1k.xml"));
            wl.getTranslations().stream().forEach(t -> {
               originalWordList.put(t.foreignWord, t);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Translation> translations;

    public WordList() {
        this.translations = new ArrayList<>();
    }

    public static WordList loadFromXml(File xmlFile) throws IOException {
        return JAXB.unmarshal(new FileInputStream(xmlFile), WordList.class);
    }

    public void writeToXml(File xmlFile) throws IOException {
        JAXB.marshal(this, new FileOutputStream(xmlFile));
    }

    public void addTranslation(String foreignWord,
                               String wordMeaning,
                               String exampleForeignSentence,
                               String exampleTranslatedSentence) {

        var translation = findTranslationForWord(foreignWord);
        if (translation == null) {
            translation = new Translation(foreignWord,
                                          getPronunciation(foreignWord),
                                          getAudioName(foreignWord),
                                          new ArrayList<>());
            translations.add(translation);
        }

        String exampleSentence = null;
        if (!isNullOrEmpty(exampleForeignSentence) && !isNullOrEmpty(exampleTranslatedSentence)) {
            exampleSentence = exampleForeignSentence + " - " + exampleTranslatedSentence;
        }

        translation.getMeanings().add(new Meaning(wordMeaning, exampleSentence, null));
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    @XmlElement(name = "translation")
    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WordList wordList = (WordList) o;

        return new EqualsBuilder()
                .append(translations, wordList.translations)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(translations)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("translations", translations)
                .toString();
    }


    private Translation findTranslationForWord(String foreignWord) {
        return translations.stream().filter(t -> t.getForeignWord().equals(foreignWord)).findFirst().orElse(null);
    }

    private String getAudioName(String foreignWord) {
        return originalWordList.get(foreignWord).getAudioName();
    }

    private String getPronunciation(String foreignWord) {
        return originalWordList.get(foreignWord).getPronunciation();
    }
}
