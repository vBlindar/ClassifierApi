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
    public void trainAndSaveTextClassifier(List<ClassifiableText> classifiableTextForTrain,
                                        DL4JClassifier classifier) throws IOException {

            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/" + classifier.toString()));


    }

    @Override
    public void startClassification() throws IOException {

        DL4JClassifier classifier = createTextClassifier();


        Map<String, List<ClassifiableText>> data = classifiableTextService.splitTextsForTrainingAndTesting();
        List<ClassifiableText> training = classifiableTextService.collectAndShuffleTexts(data, "training");
        List<ClassifiableText> testing = classifiableTextService.collectAndShuffleTexts(data, "testing");

        trainAndSaveTextClassifier(training, classifier);

        checkTextClassifierAccuracy(testing, classifier);
    }

    @Override
    public void checkTextClassifierAccuracy(
            List<ClassifiableText> classifiableTexts,
            DL4JClassifier classifier) {


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


    @Override
    public DL4JClassifier createTextClassifier() throws IOException {

        List<TextCharacteristic> characteristics = characteristicValueService.getAllCharacteristics();

        List<VocabularyWord> vocabulary = vocabularyService.getAllVocabulary();

       return new DL4JClassifier(null, vocabulary, characteristics);

    }


}


