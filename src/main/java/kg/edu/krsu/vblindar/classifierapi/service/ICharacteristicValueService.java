package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ICharacteristicValueService {

    void insertPossibleValue(Characteristic characteristic,
                             CharacteristicValueDto characteristicValue);

    CharacteristicValue searchCharacteristicPossibleValue(Characteristic characteristic,
                                                          CharacteristicValueDto characteristicValue);

    List<CharacteristicValue> findValuesByCharacteristicId(Long id);

    CharacteristicValueDto findById(Long id);

}
