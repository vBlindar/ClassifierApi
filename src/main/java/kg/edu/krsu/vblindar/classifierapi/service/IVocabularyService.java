package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.dto.VocabularyWordDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IVocabularyService {
    List<VocabularyWordDto> getAllVocabulary();
    List<VocabularyWordDto> getVocabulary(List<ClassifiableTextDto> classifiableTexts);

    void saveVocabularyToStorage(List<ClassifiableTextDto> classifiableText);

    void saveWithVerification(List<VocabularyWordDto> words);
}
