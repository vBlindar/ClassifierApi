package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
import kg.edu.krsu.vblindar.classifierapi.repository.VocabularyWordRepository;
import kg.edu.krsu.vblindar.classifierapi.service.IVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VocabularyService implements IVocabularyService {

    private final VocabularyWordRepository vocabularyWordRepository;

    private final FilteredUnigram filteredUnigram;
    @Override
    public List<VocabularyWord> getAllVocabulary(){
        return vocabularyWordRepository.findAll();
    }
    @Override
    public List<VocabularyWord> getVocabulary(List<ClassifiableText> classifiableTexts) {
        if (classifiableTexts == null ||
                classifiableTexts.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Map<String, Integer> uniqueValues = new HashMap<>();
        List<VocabularyWord> vocabulary = new ArrayList<>();


        for (ClassifiableText classifiableText : classifiableTexts) {
            for (String word : filteredUnigram.getUnigram(classifiableText.getText())) {
                if (uniqueValues.containsKey(word)) {
                    uniqueValues.put(word, uniqueValues.get(word) + 1);
                } else {
                    uniqueValues.put(word, 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : uniqueValues.entrySet()) {
            if (entry.getValue() > 3) {
                vocabulary.add(VocabularyWord.builder()
                        .value(entry.getKey())
                        .build());
            }
        }

        return vocabulary;
    }



    @Override
    public void saveVocabularyToStorage(List<ClassifiableText> classifiableTexts) {
        List<VocabularyWord> vocabulary = getVocabulary(classifiableTexts);
        var vocabularyInDb = vocabularyWordRepository.findAll();
        vocabulary.removeIf(word -> vocabularyInDb.stream().anyMatch(dbWord -> dbWord.getValue().equals(word.getValue())));
        saveWithVerification(vocabulary);

    }

    @Override
    public void saveWithVerification(List<VocabularyWord> words) {
       var vocabulary = new HashSet<>(words);
       vocabularyWordRepository.saveAll(vocabulary);
    }
}
