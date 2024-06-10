package kg.edu.krsu.vblindar.classifierapi.ngram;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FilteredUnigram {

    private final PorterStemmer stemmer = new PorterStemmer();

    public Set<String> getUnigram(String text) {

        String[] words = clean(text).split("[ \n\t\r$+<>№=]");

        for (int i = 0; i < words.length; i++) {
            words[i] = stemmer.stem(words[i]);
        }

        Set<String> uniqueValues = new LinkedHashSet<>(Arrays.asList(words));
        uniqueValues.removeIf(String::isEmpty);

        return uniqueValues;
    }

    private String clean(String text) {
        if (text != null) {
            return text.toLowerCase()
                    .replaceAll("[\\pP\\d]", " ")
                    .replaceAll("[\\x{1F600}-\\x{1F64F}]", " ")
                    .replaceAll("[\\x{1F300}-\\x{1F5FF}]", " ")
                    .replaceAll("[\\x{1F680}-\\x{1F6FF}]", " ")
                    .replaceAll("[\\x{1F700}-\\x{1F77F}]", " ")
                    .replaceAll("[\\x{1F780}-\\x{1F7FF}]", " ")
                    .replaceAll("[\\x{1F800}-\\x{1F8FF}]", " ")
                    .replaceAll("[\\x{1F900}-\\x{1F9FF}]", " ")
                    .replaceAll("[\\x{1FA00}-\\x{1FA6F}]", " ")
                    .replaceAll("[\\x{1FA70}-\\x{1FAFF}]", " ")
                    .replaceAll("[\\x{2600}-\\x{26FF}]", " ")
                    .replaceAll("[\\x{2700}-\\x{27BF}]", " ")
                    .replaceAll("[\\x{1F100}-\\x{1F1FF}]", " ")
                    .replaceAll("[\\x{2190}-\\x{21FF}]", " ");
        } else {
            return "";
        }
    }

}
