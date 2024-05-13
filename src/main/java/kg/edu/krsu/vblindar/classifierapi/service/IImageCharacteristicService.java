package kg.edu.krsu.vblindar.classifierapi.service;

import java.io.File;

public interface IImageCharacteristicService {
    void saveAllCharacteristics(File[] dirs);

    int getCharacteristicsCount();
}
