package org.vorin.bestwords.loaders;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.hamcrest.CoreMatchers.is;

public class SynonymStoreTest {

    @Test
    public void removeSynonyms() {
        // given
        var synonymStore = new SynonymStore();
        synonymStore.addMeaning("duren", "idiota", "");
        synonymStore.addMeaning("dryndac", "telefonowac", "");

        synonymStore.addMeaning("auto", "", "");
        synonymStore.addMeaning("idiota", "", "");
        synonymStore.addMeaning("telefonowac", "", "");

        // when & then
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "dryndac", "telefonowac")),
                is(Arrays.asList("auto", "idiota", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("duren", "idiota", "auto", "dryndac")),
                   is(Arrays.asList("duren", "auto", "dryndac")));
        assertThat(synonymStore.removeSynonyms(Arrays.asList("auto", "idiota", "telefonowac", "duren", "dryndac")),
                is(Arrays.asList("auto", "duren", "dryndac")));
    }

}