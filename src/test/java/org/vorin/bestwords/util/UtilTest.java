package org.vorin.bestwords.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void stripTrailingDot() {
        assertThat(Util.trimAndStripTrailingDot("some sentence."), is("some sentence"));
        assertThat(Util.trimAndStripTrailingDot("some. sentence."), is("some. sentence"));
        assertThat(Util.trimAndStripTrailingDot("some. sentence"), is("some. sentence"));
    }
}