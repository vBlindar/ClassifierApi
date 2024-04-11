package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.service.ITrainTextService;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainTextService implements ITrainTextService {
    private final CharacteristicService characteristicService;
    private final VocabularyService vocabularyService;
    private final ClassifiableTextService classifiableTextService;

    @Override
    public List<Classifier> createClassifiers(){
        var characteristics = characteristicService.getAllCharacteristics();
        var vocabulary = vocabularyService.getAllVocabulary();
        List<Classifier> classifiers = new ArrayList<>();
        for (CharacteristicDto characteristic : characteristics) {
            Classifier classifier = new Classifier(characteristic, vocabulary);
            classifiers.add(classifier);
        }
        return classifiers;
    }
    @Override
    public void startClassification() {
        var classifiers = createClassifiers();
        var texts = classifiableTextService.getAllTexts();
        trainAndSaveClassifiers(texts, classifiers);
        checkClassifiersAccuracy(texts,classifiers);

    }

    @Override
    public void trainAndSaveClassifiers(List<ClassifiableTextDto> classifiableTextForTrain, List<Classifier> classifiers) {
        for (Classifier classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/"+ classifier.toString()));
        }

        Classifier.shutdown();
    }

    @Override
    public void checkClassifiersAccuracy(
            List<ClassifiableTextDto> classifiableTexts,
            List<Classifier> classifiers) {

        // read second sheet from a file

        for (Classifier classifier : classifiers) {
            CharacteristicDto characteristic = classifier.getCharacteristic();
            int correctlyClassified = 0;

            for (ClassifiableTextDto classifiableText : classifiableTexts) {
                CharacteristicValueDto idealValue = classifiableText.getCharacteristicValue(characteristic);
                CharacteristicValueDto classifiedValue = classifier.classify(classifiableText);

                if (classifiedValue.getValue().equals(idealValue.getValue())) {
                    correctlyClassified++;
                }
            }

            double accuracy = ((double) correctlyClassified / classifiableTexts.size()) * 100;
            System.out.println((String.format("Accuracy of Classifier for '" + characteristic.getName() + "' characteristic: %.2f%%",
                    accuracy)));
        }
    }


}
