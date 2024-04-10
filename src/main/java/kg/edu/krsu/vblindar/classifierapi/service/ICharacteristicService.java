package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface ICharacteristicService {

    void saveCharacteristicsToStorage(List<ClassifiableTextDto> classifiableTexts);

    List<CharacteristicDto> getAllCharacteristics();

    Set<CharacteristicDto> getCharacteristicsCatalog(List<ClassifiableTextDto> classifiableTexts);
    void addNewCharacteristic(CharacteristicDto characteristicDto) throws IllegalArgumentException;

    CharacteristicDto findById(Long id);


}
