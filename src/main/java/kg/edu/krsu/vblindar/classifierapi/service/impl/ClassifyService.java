package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.service.IClassifyService;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassifyService implements IClassifyService {

    private final CharacteristicService characteristicService;
    private final VocabularyService vocabularyService;

    @Override
    public String classifyText(String text,File file) {
        ClassifiableTextDto classifiableText = ClassifiableTextDto.builder().text(text).build();
        StringBuilder classifiedCharacteristics = new StringBuilder();
        var classifiers = createClassifiers(file);
        try {
            for (Classifier classifier : classifiers) {
                CharacteristicValueDto classifiedValue = classifier.classify(classifiableText);

                classifiedCharacteristics.append(classifier.getCharacteristic().getName()).append(": ").append(classifiedValue.getValue()).append("\n");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());

        }

        return classifiedCharacteristics.toString();
    }

    @Override
    public List<Classifier> createClassifiers(File file) {
        var characteristics = characteristicService.getAllCharacteristics();
        var vocabulary = vocabularyService.getAllVocabulary();
        List<Classifier> classifiers = new ArrayList<>();
        for (CharacteristicDto characteristic : characteristics) {
            Classifier classifier = new Classifier(file, characteristic, vocabulary);
            classifiers.add(classifier);
        }
        return classifiers;
    }


    public String classifyText2(String text,File file) throws IOException {
        ClassifiableTextDto classifiableText = ClassifiableTextDto.builder().text(text).build();
        StringBuilder classifiedCharacteristics = new StringBuilder();
        var classifiers = createClassifiers2(file);
        try {
            for (DL4JClassifier classifier : classifiers) {
                CharacteristicValueDto classifiedValue = classifier.classify(classifiableText);

                classifiedCharacteristics.append(classifier.getCharacteristic().getName()).append(": ").append(classifiedValue.getValue()).append("\n");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());

        }

        return classifiedCharacteristics.toString();
    }


    public List<DL4JClassifier> createClassifiers2(File file) throws IOException {
        var characteristics = characteristicService.getAllCharacteristics();
        var vocabulary = vocabularyService.getAllVocabulary();
        List<DL4JClassifier> classifiers = new ArrayList<>();
        for (CharacteristicDto characteristic : characteristics) {
            DL4JClassifier classifier = new DL4JClassifier(file, characteristic, vocabulary);
            classifiers.add(classifier);
        }
        return classifiers;
    }

    @Override
    public File getNetworkFile(String type) {
        String folderPath = "./models";
        if (type.equals("img"))
            folderPath += "/imgClassifier";
        else
            folderPath += "/textClassifier";

        File folder = new File(folderPath);

        File[] files = folder.listFiles();

        if (files != null && files.length > 0) {
            return files[0];
        }
        return null;
    }

}
