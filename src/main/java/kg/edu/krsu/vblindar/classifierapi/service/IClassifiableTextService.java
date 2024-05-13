package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public interface IClassifiableTextService {
    void saveClassifiableTextsToStorage(List<ClassifiableText> classifiableTexts);

    void addText(ClassifiableText text) throws IllegalArgumentException;


    void insertToClassifiableTextsCharacteristicsTable(ClassifiableText classifiableText, Characteristic characteristic
            , CharacteristicValue characteristicValue);


    List<ClassifiableText> getAllTexts();

    List<ClassifiableText> converting(List<ClassifiableText> texts, List<Characteristic> characteristics);

    List<ClassifiableText> collectAndShuffleTexts(Map<String, Map<String, List<ClassifiableText>>> splitMap,
                                                     String key);

    Map<String, Map<String, List<ClassifiableText>>> splitTextsForTrainingAndTesting();

}
