package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicValueRepository;
import kg.edu.krsu.vblindar.classifierapi.service.ICharacteristicValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacteristicValueService implements ICharacteristicValueService {
    private final CharacteristicValueRepository characteristicValueRepository;

    @Override
    public void insertPossibleValue(Characteristic characteristic,
                                    CharacteristicValue characteristicValue) {

        if (characteristicValue != null &&
                !characteristicValue.getValue().isEmpty()) {

            if (!characteristicValueRepository.existsByValueAndCharacteristicId(characteristicValue.getValue(),
                    characteristic.getId())) {
                characteristicValue.setCharacteristic(characteristic);
            }

        }
    }

    @Override
    public CharacteristicValue searchCharacteristicPossibleValue(Characteristic characteristic,
                                                                 CharacteristicValue characteristicValue) {

        return characteristicValueRepository.findCharacteristicValue(characteristicValue.getId(), characteristic.getId()).orElse(null);
    }

    @Override
    public List<CharacteristicValue> findValuesByCharacteristicId(Long id) {
        return characteristicValueRepository.findAllByCharacteristicId(id);
    }

    @Override
    public CharacteristicValue findById(Long id) {
        return characteristicValueRepository.findById(id).orElseThrow(IllegalArgumentException::new);
    }
}
