package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
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
                                    CharacteristicValueDto characteristicValue) {

        if (characteristicValue != null &&
                !characteristicValue.getValue().isEmpty()) {


            if (!characteristicValueRepository.existsByValueAndCharacteristicId(characteristicValue.getValue(),
                    characteristic.getId())) {
                var newCharacteristicValue = CharacteristicValue.builder()
                        .characteristic(characteristic)
                        .value(characteristicValue.getValue())
                        .build();
                characteristicValueRepository.saveAndFlush(newCharacteristicValue);
            }

        }
    }

    @Override
    public CharacteristicValue searchCharacteristicPossibleValue(Characteristic characteristic,
                                                                 CharacteristicValueDto characteristicValue) {

        return characteristicValueRepository.findCharacteristicValue(characteristicValue.getId(), characteristic.getId()).orElse(null);
    }
    @Override
    public List<CharacteristicValue> findValuesByCharacteristicId(Long id) {
        return characteristicValueRepository.findAllByCharacteristicId(id);
    }

    @Override
    public CharacteristicValueDto findById(Long id) {
        var value = characteristicValueRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        return CharacteristicValueDto.from(value);
    }
}
