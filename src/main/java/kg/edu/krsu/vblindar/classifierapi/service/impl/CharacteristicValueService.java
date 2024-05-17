package kg.edu.krsu.vblindar.classifierapi.service.impl;



import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicValueRepository;
import kg.edu.krsu.vblindar.classifierapi.service.ICharacteristicValueService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.analysis.function.Abs;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacteristicValueService implements ICharacteristicValueService {
    private final CharacteristicValueRepository characteristicValueRepository;

    @Override
    public List<TextCharacteristic> getAllCharacteristics() {
        return characteristicValueRepository.findAll();
    }

    @Override
    public List<TextCharacteristic> saveAllCharacteristic(File[] characteristicsDir) {
        List<TextCharacteristic> characteristics = new ArrayList<>();
        for (File file : characteristicsDir) {
            characteristics.add(
              TextCharacteristic.builder()
                      .value(file.getName())
                      .build()
            );
        }

        return characteristicValueRepository.saveAll(characteristics);
    }

}
