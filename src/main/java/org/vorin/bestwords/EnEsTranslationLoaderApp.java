package org.vorin.bestwords;

import org.vorin.bestwords.loaders.GoogleTranslateDownloader;
import org.vorin.bestwords.loaders.GoogleTranslateParser;
import org.vorin.bestwords.loaders.TranslationLoader;
import org.vorin.bestwords.loaders.WordInfo;
import org.vorin.bestwords.loaders.XmlTranslationPublisher;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.util.FileUtil;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Sources;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.AppConfig.RES_DIR;


public class EnEsTranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    // ### ES CONFIG ###############
    private static final Dictionary DICT = Dictionary.EN_ES;
//    private static final Dictionary DICT = Dictionary.ES_EN;
    private static final Dictionary REVERSE_DICT = Dictionary.ES_EN;
    private static final int MAX_MEANING_COUNT_FROM_SRC = 6;
    // ##########################

    public static void main(String... args) throws IOException {
        var inputWordlistFilePath = RES_DIR + DICT.name() + "-InputEnglishWordlist.txt";
//        var inputWordlistFilePath = RES_DIR + DICT.name() + "-InputSpanishWordlist.txt";
        var outputWordlistFilePath = RES_DIR + DICT.name() + "-CombinedWordlist.xml";
        var outputPrettyPrintFilePath = RES_DIR + DICT.name() + "-CombinedWordlistPrettyPrint.txt";
        var processedWordlistFilePath = RES_DIR + DICT.name() + "-ProcessedWordlist.xml";
        var googleWordlistFilePath = RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml";
        var lingueeWordlistFilePath =  RES_DIR + DICT.name() + "-LingueeWordlist.xml";
        var contextPhrasesToTranslateFilePath = RES_DIR + DICT.name() + "-ContextPhrasesToTranslate.txt";
        var translatedContextPhrasesFilePath = RES_DIR + DICT.name() + "-TranslatedContextPhrases.xml";
        var booksWordlistContent = FileUtil.readFileToString(new File(RES_DIR + "books-10_000-words.txt"), Charset.forName("cp1252"));
        var subtitlesWordlistContent = FileUtil.readFileToString(new File(RES_DIR + "subtitles-10_000-words.txt"), Charset.forName("cp1252"));

        List<WordInfo> wordInfos = Util.loadWordsFromTxtFile2(new File(inputWordlistFilePath)).stream().flatMap(inLine -> {
            var outputWordInfos = new ArrayList<WordInfo>();

            // clean -> workout / training;noun;sports & healthy / sick;adjective;medicine by splitting and duplicating
            var lines = Arrays.asList(inLine.split(" */ *"));
            String lastLine = lines.get(lines.size() - 1);
            String linePartToDuplicate = lastLine.substring(lastLine.indexOf(";") >= 0 ? lastLine.indexOf(";") : 0);
            lines = lines.stream().limit(lines.size()-1).map(l -> l + linePartToDuplicate).collect(toList());
            lines.add(lastLine);
            for (var line : lines) {
                // clean -> "to " for verbs by removing
                line = line.replaceAll("^to ", "");

                String[] items = line.split(";");
                outputWordInfos.add(new WordInfo(items[0], null, items.length >= 2 ? items[1] : "", items.length >= 3 ? items[2] : ""));
            }
            return outputWordInfos.stream();
        }).collect(toList());

        createWordlists(wordInfos, googleWordlistFilePath, lingueeWordlistFilePath);

        combineWordlists(
                wordInfos,
                Map.of(
                        Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml(new File(googleWordlistFilePath)),
                        Sources.LINGUEE_SOURCE, Wordlist.loadFromXml(new File(lingueeWordlistFilePath))),
                outputWordlistFilePath);

//        findTranslationsForContextPhrases(FileUtil.readFileToLines(new File(contextPhrasesToTranslateFilePath), Charset.forName("cp1252")), translatedContextPhrasesFilePath);


        printoutWordlistAsEasyToCheckList(Wordlist.loadFromXml(new File(outputWordlistFilePath)), wordInfos, outputPrettyPrintFilePath,
                contextPhrasesToTranslateFilePath, translatedContextPhrasesFilePath, booksWordlistContent,
                subtitlesWordlistContent, 2);

        // unused for now - needs checking and working on (possibly) if to be used
//        processWordlist(inputWordlist, processedWordlistFilePath);
    }


    public static void findTranslationsForContextPhrases(List<String> phrases, String translatedContextPhrasesFilePath) throws IOException {
        var wordInfos = phrases.stream().map(phrase -> new WordInfo(phrase, null)).distinct().collect(toList());

        var xmlPublisher = new XmlTranslationPublisher(new File(translatedContextPhrasesFilePath));
        var downloader = new GoogleTranslateDownloader(REVERSE_DICT);
        var parser = new GoogleTranslateParser(0.01, 2, true);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void printoutWordlistAsEasyToCheckList(Wordlist wordlist, List<WordInfo> wordInfos, String outputPrettyPrintFilePath, String contextPhrasesToTranslateFilePath,
                                                          String translatedContextPhrasesFilePath, String booksWordlistContent,
                                                          String subtitlesWordlistContent, int maxMeaningCount) throws IOException {
        var content = new StringJoiner("\n");
        var contextsToTranslate = new StringJoiner("\n");
        var translatedContextPhrases = Wordlist.loadFromXml(new File(translatedContextPhrasesFilePath));
        int i = 0;
        for(var t : wordlist.getTranslations()) {
            var wordInfo = wordInfos.get(i++);
            content.add(format("%s (%s - %s)", t.getForeignWord(), wordInfo.getWordType(), wordInfo.getComment()));
            int meaningCount = 0;
            for (var m : t.getMeanings()) {
                if ((wordInfo.getWordType().equals("verb") && !m.getWordType().equals("verb")) ||
                        (!wordInfo.getWordType().equals("verb") && m.getWordType().equals("verb"))) {
                    // it seems to me that the only words that can be confused are verbs (with nouns or adverbs or adjectives)
                    // that's why the input words (wordinfos) need to have defined if they are verbs and that is enough
                    // we don't necessarily need to define other types, because nouns and adjectives e.g. cannot be confused
                    continue;
                }
                var wordInBooks = parseContentForWord(booksWordlistContent, m.getWordMeaning(), contextsToTranslate, translatedContextPhrases);
                var wordInSubtitles = parseContentForWord(subtitlesWordlistContent, m.getWordMeaning(), contextsToTranslate, translatedContextPhrases);
                content.add(format("%s;%s;%s;sub%s;bks%s;=HYPERLINK(\"https://www.google.es/search?tbm=isch&q=%s\",\"google search: %s\") ;=HYPERLINK(\"https://www.amazon.es/s?k=%s\",\"amazon search: %s\") ",
                        m.getWordMeaning(), m.getWordType(), m.getWordMeaningSource(), wordInSubtitles, wordInBooks, m.getWordMeaning(), m.getWordMeaning(), m.getWordMeaning(), m.getWordMeaning())
                        .replaceAll("google\\-translate", "goog")
                    .replaceAll("linguee", "ling"));
                if(++meaningCount >= maxMeaningCount) {
                    break;
                }
            }
            content.add("");
        }
        // below line needs to be uncommented to generate the list of context phrases
        // then the list is being translated and used here to replace phrases in spanish to in english
//        FileUtil.printToFile("", contextPhrasesToTranslateFilePath, contextsToTranslate.toString());
        FileUtil.printToFile("", outputPrettyPrintFilePath, content.toString());
    }


    private static void combineWordlists(List<WordInfo> wordInfos, Map<String, Wordlist> sourceWordlists, String outputWordlistFile) throws IOException {
        var wordlist = new Wordlist();
        for (var wordInfo : wordInfos) {
            var translation = new Translation(wordInfo.getForeignWord(), "", "", new ArrayList<>());
            WordlistProcessor.combineMeanings(translation, sourceWordlists);
            wordlist.getTranslations().add(translation);
        }

        wordlist.writeToXml(new File(outputWordlistFile));
    }


    private static String parseContentForWord(String content, String word, StringJoiner contextPhrasesToTranslateSj, Wordlist translatedContextPhrases) throws IOException {
        Pattern wordPattern = Pattern.compile(format("\\(\\d{4}\\): %s .*?\n", word));
        Pattern excludedWordPattern = Pattern.compile(format("(?ms)^%s +\\-.*?\n", word));

        Matcher matcher = wordPattern.matcher(content);
        String line = "";
        if (matcher.find()) {
            line = matcher.group(0);
            Pattern linePattern = Pattern.compile("\\((\\d{4})\\):.+?\\- +?(\\d+).+?context: (.+?) +\\-");
            matcher = linePattern.matcher(line);
            matcher.find();
            // #2432@138 (he stained the carpet)
            var contextPhrase = matcher.group(3).replaceAll("\\(\\d+\\) ", "");
            if (!contextPhrase.isBlank()) {
                contextPhrasesToTranslateSj.add(contextPhrase);
            }
            var translation = translatedContextPhrases.findTranslationForWord(contextPhrase);
            var fullContext = matcher.group(3);
            if (translation != null) {
                fullContext = fullContext + " - " + translation.getMeanings().get(0).getWordMeaning();
            }
            return format("#%s@%s - %s", matcher.group(1), matcher.group(2), fullContext);
        } else {
            matcher = excludedWordPattern.matcher(content);
            if (matcher.find()) {
                line = matcher.group(0);
                Pattern linePattern = Pattern.compile(".+\\- +?(\\d+)");
                matcher = linePattern.matcher(line);
                matcher.find();
                // #excl@138
                return format("#excl@%s", matcher.group(1));
            }
        }

        return "#n/a";
    }


    private static void processWordlist(Wordlist wordlist, String processedWordlistFilePath) throws IOException {
        var wordlistProcessor = new WordlistProcessor(DICT, null);
        int wordsWithProblemsCount = 0;
        for (var t : wordlist.getTranslations()) {
            if(!wordlistProcessor.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordlistProcessor.verifyWordlist(wordlist);

        wordlist.writeToXml(new File(processedWordlistFilePath));
    }


    private static void createWordlists(List<WordInfo> wordInfos, String googleWordlistFilePath, String lingueeWordlistFilePath) throws IOException {

        TranslationLoaderApp.createGoogleWordlist(DICT, wordInfos, googleWordlistFilePath, MAX_MEANING_COUNT_FROM_SRC);

        TranslationLoaderApp.createLingueeWordlist(DICT, wordInfos, lingueeWordlistFilePath, MAX_MEANING_COUNT_FROM_SRC);
    }

}
