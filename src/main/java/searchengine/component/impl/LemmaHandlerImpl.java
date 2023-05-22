package searchengine.component.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.component.LemmaHandler;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Component
@Slf4j
public class LemmaHandlerImpl implements LemmaHandler {

    @Override
    public Map<String, Integer> getLemmasFromText(String html) throws IOException {
        Map<String, Integer> lemmasInText = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        String text = Jsoup.parse(html).text();
        List<String> words = new ArrayList<>(List.of(text.replaceAll("(?U)\\pP","").toLowerCase().split(" ")));
        words.forEach(w -> determineLemma(w, luceneMorph,lemmasInText));
        return lemmasInText;
    }
    @Override
    public List<String> getLemma(String word) throws IOException {

        LuceneMorphology russianLuceneMorphology = new RussianLuceneMorphology();
        List<String> lemmaList = new ArrayList<>();
        try {
            List<String> baseRusForm = russianLuceneMorphology.getNormalForms(word);
            if (!isServiceWord(word)) {
                lemmaList.addAll(baseRusForm);
            }
        } catch (Exception e) {
        }
        return lemmaList;
    }
    private boolean isServiceWord(String word) throws IOException {

        LuceneMorphology russianLuceneMorphology = new RussianLuceneMorphology();
        List<String> morphForm = russianLuceneMorphology.getMorphInfo(word);
        for (String l : morphForm) {
            if (l.contains("ПРЕДЛ")
                    || l.contains("СОЮЗ")
                    || l.contains("МЕЖД")
                    || l.contains("МС")
                    || l.contains("ЧАСТ")
                    || l.length() <= 3) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Integer> findLemmaIndexInText(String content, String lemma) throws IOException {
        List<Integer> lemmaIndexList = new ArrayList<>();
        String[] elements = content.toLowerCase(Locale.ROOT).split("\\p{Punct}|\\s");
        int index = 0;
        for (String el : elements) {
            List<String> lemmas = getLemma(el);
            for (String lem : lemmas) {
                if (lem.equals(lemma)) {
                    lemmaIndexList.add(index);
                }
            }
            index += el.length() + 1;
        }
        return lemmaIndexList;
    }


    private void determineLemma(String word, LuceneMorphology luceneMorphology,Map<String,Integer> lemmasInText) {
        try{
            if (word.isEmpty() || String.valueOf(word.charAt(0)).matches("[a-z]") || String.valueOf(word.charAt(0)).matches("[0-9]")) {
                return;
            }
            List<String> normalWordForms = luceneMorphology.getNormalForms(word);
            String wordInfo = luceneMorphology.getMorphInfo(word).toString();
            if (wordInfo.contains("ПРЕДЛ") || wordInfo.contains("СОЮЗ") || wordInfo.contains("МЕЖД")) {
                return;
            }
            normalWordForms.forEach(w -> {
                if (!lemmasInText.containsKey(w)) {
                    lemmasInText.put(w,1);
                } else {
                    lemmasInText.replace(w,lemmasInText.get(w) + 1);
                }
            });
        } catch (RuntimeException ex) {
            log.debug(ex.getMessage());
        }

    }

    @Override
    public void getLemmasFromUrl(URL url) throws IOException {
        org.jsoup.Connection connect = Jsoup.connect(String.valueOf(url));
        Document doc = connect.timeout(60000).get();
        Map<String,Integer> res = getLemmasFromText(doc.body().html());
    }
}