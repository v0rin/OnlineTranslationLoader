package org.vorin.bestwords.util;

import java.util.Map;

public class CacheFileNameProvider {

    private static final Map<String, String> restrictedFileNameConversionMap = Map.of("con", "con_mapped");

    public String getFileName(String name) {
        var mappedName = restrictedFileNameConversionMap.get(name);
        return (mappedName == null ? name : mappedName);
    }
}
