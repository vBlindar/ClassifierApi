package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.entity.ImageCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.repository.ImageCharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.service.IImageCharacteristicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageCharacteristicService implements IImageCharacteristicService {
    private final ImageCharacteristicRepository repository;

    @Override
    public void saveAllCharacteristics(File[] dirs) {
        List<ImageCharacteristic> characteristicList = new ArrayList<>();
        for (File dir : dirs) {
            characteristicList.add(
                    ImageCharacteristic.builder()
                            .value(dir.getName())
                            .build()
            );
        }
        characteristicList.sort(Comparator.comparing(ImageCharacteristic::getValue));
        repository.saveAll(characteristicList);
    }

    @Override
    public int getCharacteristicsCount() {
        return repository.findAll().size();
    }
}
