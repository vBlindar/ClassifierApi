package kg.edu.krsu.vblindar.classifierapi.service.impl;



import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.repository.TextCharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.service.ITextCharacteristicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextCharacteristicService implements ITextCharacteristicService {
    private final TextCharacteristicRepository textCharacteristicRepository;

    @Override
    public List<TextCharacteristic> getAllCharacteristics() {
        return textCharacteristicRepository.findAll();
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

        return textCharacteristicRepository.saveAll(characteristics);
    }

}
