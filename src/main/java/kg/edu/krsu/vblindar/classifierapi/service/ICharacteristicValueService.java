package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ICharacteristicValueService {

    void insertPossibleValue(Characteristic characteristic,
                             CharacteristicValue characteristicValue);

    CharacteristicValue searchCharacteristicPossibleValue(Characteristic characteristic,
                                                          CharacteristicValue characteristicValue);

    List<CharacteristicValue> findValuesByCharacteristicId(Long id);

    CharacteristicValue findById(Long id);

}
