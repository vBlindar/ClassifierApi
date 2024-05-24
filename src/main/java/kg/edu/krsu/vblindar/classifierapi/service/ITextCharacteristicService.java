package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public interface ITextCharacteristicService {
    List<TextCharacteristic> getAllCharacteristics();

    List<TextCharacteristic> saveAllCharacteristic(File[] characteristicsDir);


}
