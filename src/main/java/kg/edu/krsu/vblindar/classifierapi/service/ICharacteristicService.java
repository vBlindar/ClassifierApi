package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface ICharacteristicService {

    void saveCharacteristicsToStorage(List<ClassifiableText> classifiableTexts);

    List<Characteristic> getAllCharacteristics();

    Set<Characteristic> getCharacteristicsCatalog(List<ClassifiableText> classifiableTexts);
    void addNewCharacteristic(Characteristic characteristicDto) throws IllegalArgumentException;

    Characteristic findById(Long id);


}
