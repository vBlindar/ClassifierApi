package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicValueRepository;
import kg.edu.krsu.vblindar.classifierapi.service.ICharacteristicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.websocket.OnClose;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CharacteristicService implements ICharacteristicService {
    private final CharacteristicRepository characteristicRepository;
    private final CharacteristicValueService characteristicValueService;
    @Override
    public void saveCharacteristicsToStorage(List<ClassifiableText> classifiableTexts) {
        Set<Characteristic> characteristics = getCharacteristicsCatalog(classifiableTexts);
        for (Characteristic characteristic : characteristics) {
            try {

                addNewCharacteristic(characteristic);
            } catch (IllegalArgumentException e) {
                System.out.println("Empty of Already");
            }
        }

    }

    @Override
    public List<Characteristic> getAllCharacteristics() {

        var characteristics = characteristicRepository.findAll();
        for (Characteristic characteristic : characteristics) {
            characteristic.setPossibleValues(characteristicValueService.findValuesByCharacteristicId(characteristic.getId()));
        }

        return characteristics;
    }

    @Override
    public Set<Characteristic> getCharacteristicsCatalog(List<ClassifiableText> classifiableTexts) {
        Set<Characteristic> characteristics = new LinkedHashSet<>();

        for (ClassifiableText  classifiableText : classifiableTexts) {
            for (Map.Entry<Characteristic , CharacteristicValue> entry :
                    classifiableText.getCharacteristics().entrySet()) {
                characteristics.add(entry.getKey());
            }
        }

        return characteristics;
    }

    @Override
    public void addNewCharacteristic(Characteristic  characteristic ) throws IllegalArgumentException {
        if (characteristic  == null ||
                characteristic .getName().isEmpty() ||
                characteristic .getPossibleValues() == null ||
                characteristic .getPossibleValues().isEmpty()) {
            throw new IllegalArgumentException("Characteristic and/or Possible values are null or empty");
        }

        if (characteristicRepository.existsByName(characteristic .getName())) {
            throw new IllegalArgumentException("Characteristic already exists");
        }

        for (CharacteristicValue  possibleValue : characteristic.getPossibleValues()) {
            characteristicValueService.insertPossibleValue(characteristic, possibleValue);
        }
        characteristicRepository.saveAndFlush(characteristic);



    }


    @Override
    public Characteristic  findById(Long id) {
        return characteristicRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }
}
