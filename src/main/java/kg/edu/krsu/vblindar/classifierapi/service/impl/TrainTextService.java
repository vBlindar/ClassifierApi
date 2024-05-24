package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import kg.edu.krsu.vblindar.classifierapi.service.ITrainTextService;

import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrainTextService implements ITrainTextService {

    private final VocabularyService vocabularyService;
    private final ClassifiableTextService classifiableTextService;
    private final TextCharacteristicService characteristicValueService;

    @Override
    public void trainAndSaveClassifiers(List<ClassifiableText> classifiableTextForTrain,
                                        List<DL4JClassifier> classifiers) throws IOException {
        for (DL4JClassifier classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/" + classifier.toString()));
        }

    }

    @Override
    public void startClassification() throws IOException {

        List<DL4JClassifier> classifiers = createClassifiers();


        Map<String, List<ClassifiableText>> data = classifiableTextService.splitTextsForTrainingAndTesting();
        List<ClassifiableText> training = classifiableTextService.collectAndShuffleTexts(data, "training");
        List<ClassifiableText> testing = classifiableTextService.collectAndShuffleTexts(data, "testing");

        trainAndSaveClassifiers(training, classifiers);

        checkClassifiersAccuracy(testing, classifiers);
    }

    @Override
    public void checkClassifiersAccuracy(
            List<ClassifiableText> classifiableTexts,
            List<DL4JClassifier> classifiers) {

        for (DL4JClassifier classifier : classifiers) {

            int correctlyClassified = 0;

            for (ClassifiableText classifiableText : classifiableTexts) {
                TextCharacteristic idealValue = classifiableText.getCharacteristic();
                TextCharacteristic classifiedValue = classifier.classify(classifiableText);

                if (classifiedValue.getValue().equals(idealValue.getValue())) {
                    correctlyClassified++;
                }
            }

            double accuracy = ((double) correctlyClassified / classifiableTexts.size()) * 100;
            System.out.println((String.format("Accuracy of Classifier for Theme " +
                            "characteristic: %.2f%%",
                    accuracy)));
        }
    }


    @Override
    public List<DL4JClassifier> createClassifiers() throws IOException {

        List<TextCharacteristic> characteristics = characteristicValueService.getAllCharacteristics();

        List<VocabularyWord> vocabulary = vocabularyService.getAllVocabulary();


        List<DL4JClassifier> classifiers = new ArrayList<>();

        DL4JClassifier classifier = new DL4JClassifier(null, vocabulary, characteristics);
        classifiers.add(classifier);

        return classifiers;
    }


}


