package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface IClassifiableTextService {
    void saveClassifiableTextsToStorage(List<ClassifiableText> classifiableTexts);


    List<ClassifiableText> collectAndShuffleTexts(Map<String, List<ClassifiableText>> splitMap, String key);

    Map<String, List<ClassifiableText>> splitTextsForTrainingAndTesting();

}
