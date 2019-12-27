package org.vorin.bestwords.loaders;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.vorin.bestwords.util.LangUtil;

public class WordInfo {
    private final String foreignWord;
    private final String wordMeaning;

    public WordInfo(String foreignWord, String wordMeaning) {
        this.foreignWord = foreignWord;
        this.wordMeaning = wordMeaning;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public String getParsedForeignWord() {
        return LangUtil.getParsedForeignWord(foreignWord);
    }

    public String getWordMeaning() {
        return wordMeaning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WordInfo wordInfo = (WordInfo) o;

        return new EqualsBuilder()
                .append(foreignWord, wordInfo.foreignWord)
                .append(wordMeaning, wordInfo.wordMeaning)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(foreignWord)
                .append(wordMeaning)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("foreignWord", foreignWord)
                .append("wordMeaning", wordMeaning)
                .toString();
    }
}