package org.vorin.bestwords.loaders;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.vorin.bestwords.util.LangUtil;

@Data
@AllArgsConstructor
public class WordInfo {
    private final String foreignWord;
    private final String wordMeaning;
    private String wordType;
    private String comment;

    public WordInfo(String foreignWord, String wordMeaning) {
        this.foreignWord = foreignWord;
        this.wordMeaning = wordMeaning;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public String getParsedForeignWord() {
        return LangUtil.sanitizeWord(foreignWord);
    }

    public String getWordMeaning() {
        return wordMeaning;
    }
}