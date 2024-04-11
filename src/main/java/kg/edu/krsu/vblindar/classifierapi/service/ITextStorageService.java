package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public interface ITextStorageService {

    void dataClassification(MultipartFile file) throws IOException;

    List<ClassifiableTextDto> getClassifiableTexts(File file);

    void fillData(List<ClassifiableTextDto> classifiableText,File file);
    File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException;
    void deleteTempFile(File file);
}
