package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public interface IClassifiableTextService {
    void saveClassifiableTextsToStorage(List<ClassifiableTextDto> classifiableTexts);

    void addText(ClassifiableTextDto textDto) throws IllegalArgumentException;


    void insertToClassifiableTextsCharacteristicsTable(ClassifiableText classifiableText, CharacteristicDto characteristicDto
            ,CharacteristicValueDto characteristicValueDto);


    List<ClassifiableTextDto> getAllTexts();

    List<ClassifiableTextDto> converting(List<ClassifiableText> texts, List<CharacteristicDto> characteristics);
}
