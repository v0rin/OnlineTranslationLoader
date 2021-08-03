package org.vorin.bestwords.util;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class FileUtil {


    public static String readResourceToString(String path) throws IOException {
        return Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8);
    }


    public static List<String> readResourceToLines(String path) throws IOException {
        return Resources.readLines(Resources.getResource(path), StandardCharsets.UTF_8);
    }


    public static Collection<File> getFilesInDirectory(String dirPath, @Nullable String[] extensions, boolean recursive) {
        return FileUtils.listFiles(new File(dirPath), extensions, recursive);
    }


    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, StandardCharsets.UTF_8);
    }

    public static String readFileToString(File file, Charset charset) throws IOException {
        return FileUtils.readFileToString(file, charset);
    }

    public static List<String> readFileToLines(File file) throws IOException {
        return readFileToLines(file, StandardCharsets.UTF_8);
    }

    public static List<String> readFileToLines(File file, Charset charset) throws IOException {
        return FileUtils.readLines(file, charset);
    }

    public static void printToFile(String path, String fileName, String result) throws IOException {
        Files.deleteIfExists(Paths.get(path, fileName));
        Path file = Files.createFile(Paths.get(path, fileName));
        FileWriter fw = new FileWriter(file.toFile());
        PrintWriter pw = new PrintWriter(fw);
        pw.print(result);
        pw.close();
        fw.close();
    }

}
