package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.service.ITextStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextStorageService implements ITextStorageService {
    private final ExcelReader excelReader;
    private final VocabularyService vocabularyService;
    private final CharacteristicService characteristicService;
    private final ClassifiableTextService classifiableTextService;

    @Override
    public void dataClassification(MultipartFile file) throws IOException {
        var xlsxFile = convertMultipartFileToFile(file);
        var classifiableText = getClassifiableTexts(xlsxFile);
        fillData(classifiableText,xlsxFile);

    }

    @Override
    public void fillData(List<ClassifiableTextDto> classifiableText,File file){
        vocabularyService.saveVocabularyToStorage(classifiableText);
        characteristicService.saveCharacteristicsToStorage(classifiableText);
        classifiableTextService.saveClassifiableTextsToStorage(classifiableText);
        deleteTempFile(file);

    }

    @Override
    public List<ClassifiableTextDto> getClassifiableTexts(File file){
        List<ClassifiableTextDto> classifiableTexts = new ArrayList<>();

        try {
            classifiableTexts = excelReader.xlsxToClassifiableTexts(file, 1);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Problem with excel file");
        }

        return classifiableTexts;
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File("tempFile.xlsx");
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        }
        return file;
    }

    @Override
    public void deleteTempFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Файл успешно удален.");
            } else {
                System.out.println("Не удалось удалить файл.");
            }
        } else {
            System.out.println("Файл не существует.");
        }
    }


}