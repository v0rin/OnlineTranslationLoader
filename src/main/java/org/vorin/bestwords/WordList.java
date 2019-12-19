package org.vorin.bestwords;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "wordList")
public class WordList {

    private List<Translation> translations;

    public WordList() {
        this.translations = new ArrayList<>();
    }

    public void add(Translation t) {
        translations.add(t);
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
}
