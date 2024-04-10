package kg.edu.krsu.vblindar.classifierapi.ngram;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FilteredUnigram {

    public Set<String> getNGram(String text) {

        String[] words = clean(text).split("[ \n\t\r$+<>№=]");

        for (int i = 0; i < words.length; i++) {
            words[i] = PorterStemmer.doStem(words[i]);
        }

        Set<String> uniqueValues = new LinkedHashSet<>(Arrays.asList(words));
        uniqueValues.removeIf(String::isEmpty);

        return uniqueValues;
    }

    private String clean(String text) {

        if (text != null) {
            return text.toLowerCase().replaceAll("[\\pP\\d]", " ");
        } else {
            return "";
        }
    }
}
