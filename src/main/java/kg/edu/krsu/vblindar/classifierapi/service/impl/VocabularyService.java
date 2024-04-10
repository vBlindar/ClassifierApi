package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.dto.VocabularyWordDto;
import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
import kg.edu.krsu.vblindar.classifierapi.repository.VocabularyWordRepository;
import kg.edu.krsu.vblindar.classifierapi.service.IVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VocabularyService implements IVocabularyService {

    private final VocabularyWordRepository vocabularyWordRepository;

    private final FilteredUnigram filteredUnigram;
    @Override
    public List<VocabularyWordDto> getAllVocabulary(){
        return vocabularyWordRepository.findAll().stream().map(VocabularyWordDto::from).toList();
    }
    @Override
    public List<VocabularyWordDto> getVocabulary(List<ClassifiableTextDto> classifiableTexts) {
        if (classifiableTexts == null ||
                classifiableTexts.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Map<String, Integer> uniqueValues = new HashMap<>();
        List<VocabularyWordDto> vocabulary = new ArrayList<>();


        for (ClassifiableTextDto classifiableText : classifiableTexts) {
            for (String word : filteredUnigram.getNGram(classifiableText.getText())) {
                if (uniqueValues.containsKey(word)) {
                    uniqueValues.put(word, uniqueValues.get(word) + 1);
                } else {
                    uniqueValues.put(word, 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : uniqueValues.entrySet()) {
            if (entry.getValue() > 3) {
                vocabulary.add(VocabularyWordDto.builder()
                        .value(entry.getKey())
                        .build());
            }
        }

        return vocabulary;
    }



    @Override
    public void saveVocabularyToStorage(List<ClassifiableTextDto> classifiableText) {
        var vocabulary = getVocabulary(classifiableText);
        vocabulary
                .forEach(this::saveWithVerification);
    }

    @Override
    public void saveWithVerification(VocabularyWordDto word) {
        boolean existingWord = vocabularyWordRepository.existsByValue(word.getValue());
        if (!existingWord) {
            var newWord = VocabularyWordDto.on(word);
            vocabularyWordRepository.saveAndFlush(newWord);
            word.setId(newWord.getId());
        }
    }
}
