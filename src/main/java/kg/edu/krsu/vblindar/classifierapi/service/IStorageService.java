package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public interface IStorageService {

    void fillStorage(File file);

    List<ClassifiableText> getClassifiableTexts(File file);

    void saveTexts(List<ClassifiableText> classifiableText);

    List<ClassifiableText> parseFile(File file) throws IOException;

    String readAllLine(File text);

    void fillImagesCharacteristic(File file);

}
