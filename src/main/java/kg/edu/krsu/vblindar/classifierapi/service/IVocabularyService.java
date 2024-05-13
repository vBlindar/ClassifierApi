package kg.edu.krsu.vblindar.classifierapi.service;



import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IVocabularyService {
    List<VocabularyWord> getAllVocabulary();
    List<VocabularyWord> getVocabulary(List<ClassifiableText> classifiableTexts);

    void saveVocabularyToStorage(List<ClassifiableText> classifiableText);

    void saveWithVerification(List<VocabularyWord> words);
}
