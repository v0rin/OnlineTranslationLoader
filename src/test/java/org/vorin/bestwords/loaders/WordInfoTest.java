package org.vorin.bestwords.loaders;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WordInfoTest {

    @Test
    public void getParsedForeignWord() {
        assertThat(new WordInfo("a / an", null).getParsedForeignWord(), is("a"));
        assertThat(new WordInfo("okay / OK", null).getParsedForeignWord(), is("okay"));
        assertThat(new WordInfo("almost = nearly", null).getParsedForeignWord(), is("almost"));
        assertThat(new WordInfo("bitch", null).getParsedForeignWord(), is("bitch"));
    }
}