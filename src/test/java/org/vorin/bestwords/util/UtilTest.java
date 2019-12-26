package org.vorin.bestwords.util;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void stripTrailingDot() {
        assertThat(Util.trimAndStripTrailingDot("some sentence."), is("some sentence"));
        assertThat(Util.trimAndStripTrailingDot("some. sentence."), is("some. sentence"));
        assertThat(Util.trimAndStripTrailingDot("some. sentence"), is("some. sentence"));
    }

    @Test
    public void chooseShortestString() {
        assertThat(Util.chooseShortestString(List.of("", "22", "333")), is(""));
        assertThat(Util.chooseShortestString(List.of("22", "", "333")), is(""));
        assertThat(Util.chooseShortestString(List.of("22", "333", "")), is(""));

        assertThat(Util.chooseShortestString(List.of("1", "22", "333")), is("1"));
        assertThat(Util.chooseShortestString(List.of("22", "1", "333")), is("1"));
        assertThat(Util.chooseShortestString(List.of("22", "333", "1")), is("1"));
    }

}