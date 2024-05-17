package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.service.IStorageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StorageService implements IStorageService {

    private final VocabularyService vocabularyService;

    private final CharacteristicValueService characteristicValueService;
    private final ClassifiableTextService classifiableTextService;
    private final ImageCharacteristicService imageCharacteristicService;

    @Override
    public void fillStorage(File file) {
        File[] dirs = file.listFiles(((dir, name) -> !name.equals(".DS_Store")));
        List<ClassifiableText> classifiableText = getClassifiableTexts(dirs[1]);
        saveTexts(classifiableText);
        Arrays.stream(dirs).forEach(System.out::println);
    }

    @Override
    public void saveTexts(List<ClassifiableText> classifiableText) {
        vocabularyService.saveVocabularyToStorage(classifiableText);
        classifiableTextService.saveClassifiableTextsToStorage(classifiableText);

    }

    @Override
    public List<ClassifiableText> getClassifiableTexts(File file) {
        List<ClassifiableText> classifiableTexts = new ArrayList<>();

        try {
            classifiableTexts = parseFile(file);

        } catch (IllegalArgumentException e) {
            System.out.println("Problem with excel file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return classifiableTexts;
    }

    @Override
    public List<ClassifiableText> parseFile(File file) throws IOException {
        List<ClassifiableText> classifiableTexts = new ArrayList<>();
        File[] characteristicsDir = file.listFiles(((dir, name) -> !name.equals(".DS_Store")));
        List<TextCharacteristic> characteristicsEntity =
                characteristicValueService.saveAllCharacteristic(characteristicsDir);
        if (characteristicsDir != null && characteristicsDir.length == 0) {
            throw new IOException("Directory with texts is empty!");
        }
        for (File characteristic : characteristicsDir) {
            List<ClassifiableText> temp = new ArrayList<>();
            TextCharacteristic tc =
                    characteristicsEntity.stream()
                            .filter(c -> c.getValue().equals(characteristic.getName())).findFirst()
                            .orElse(null);
            File[] texts =
                    characteristic.listFiles((dir, name) ->
                            FileNameUtils.getExtension(name.toLowerCase()).equals("txt"));
            for (File text : texts) {
                ClassifiableText ct = ClassifiableText.builder()
                        .characteristic(tc)
                        .text(readAllLine(text))
                        .build();
                temp.add(ct);
            }
            classifiableTexts.addAll(temp);
        }
        return classifiableTexts;
    }

    @Override
    public String readAllLine(File text) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(text))) {

            String line;
            while ((line = reader.readLine()) != null) {

                result.append(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + text.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error reading from file: " + text.getAbsolutePath());
        }
        return result.toString();
    }


    @Override
    public void fillImagesCharacteristic(File file) {
        File[] dirs = file.listFiles(((dir, name) -> !name.equals(".DS_Store")));
        imageCharacteristicService.saveAllCharacteristics(dirs);
    }


}
