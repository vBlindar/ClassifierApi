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

    private final TextCharacteristicService characteristicValueService;
    private final ClassifiableTextService classifiableTextService;
    private final ImageCharacteristicService imageCharacteristicService;

    @Override
    public void fillStorage(File file) {
        File[] dirs = file.listFiles(((dir, name) -> !name.equals(".DS_Store")));


        if (dirs == null || dirs.length < 2) {
            throw new IllegalArgumentException("Expected two directories: images and texts");
        }

        File imagesDir = dirs[0];
        if (!imagesDir.isDirectory()) {
            throw new IllegalArgumentException("Expected a directory for images");
        }
        checkImageFiles(imagesDir);

        File textsDir = dirs[1];
        if (!textsDir.isDirectory()) {
            throw new IllegalArgumentException("Expected a directory for texts");
        }
        checkTextFiles(textsDir);

        List<ClassifiableText> classifiableText = getClassifiableTexts(dirs[1]);
        saveTexts(classifiableText);
        fillImagesCharacteristic(dirs[0]);

    }

    private void checkImageFiles(File imagesDir) {
        File[] imageFiles = imagesDir.listFiles();
        if (imageFiles == null) {
            throw new IllegalArgumentException("Images directory is empty or cannot be read");
        }

        for (File imageFile : imageFiles) {
            if (imageFile.isDirectory()) {
                checkImageFiles(imageFile);
            } else {
                String fileName = imageFile.getName().toLowerCase();
                if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
                    throw new IllegalArgumentException("Invalid image file format: " + fileName);
                }
            }
        }
    }

    private void checkTextFiles(File textsDir) {
        File[] textFiles = textsDir.listFiles();
        if (textFiles == null) {
            throw new IllegalArgumentException("Texts directory is empty or cannot be read");
        }

        for (File textFile : textFiles) {
            if (textFile.isDirectory()) {
                checkTextFiles(textFile);
            } else {
                String fileName = textFile.getName().toLowerCase();
                if (!fileName.endsWith(".txt")) {
                    throw new IllegalArgumentException("Invalid text file format: " + fileName);
                }
            }
        }
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

        if (characteristicsDir == null || characteristicsDir.length == 0) {
            throw new IOException("Directory with texts is empty!");
        }

        List<TextCharacteristic> characteristicsEntity =
                characteristicValueService.saveAllCharacteristic(characteristicsDir);

        for (File characteristic : characteristicsDir) {
            List<ClassifiableText> temp = new ArrayList<>();
            TextCharacteristic tc =
                    characteristicsEntity.stream()
                            .filter(c -> c.getValue().equals(characteristic.getName())).findFirst()
                            .orElse(null);

            File[] texts = characteristic.listFiles((dir, name) ->
                    FileNameUtils.getExtension(name.toLowerCase()).equals("txt"));

            if (texts != null) {
                for (File text : texts) {
                    ClassifiableText ct = ClassifiableText.builder()
                            .characteristic(tc)
                            .text(readAllLine(text))
                            .build();
                    temp.add(ct);
                }
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
        File[] dirs = file.listFiles();
        for (File dir : dirs) {
            if(dir.getName().contains(".DS_Store")){
                dir.delete();
            }
        }
        File[] dirs2 = file.listFiles(((dir, name) -> !name.equals(".DS_Store")));
        imageCharacteristicService.saveAllCharacteristics(dirs2);
    }


}
