package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.dto.VocabularyWordDto;
import kg.edu.krsu.vblindar.classifierapi.service.ITrainTextService;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifierWithEmbedding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainTextService implements ITrainTextService {
    private final CharacteristicService characteristicService;
    private final VocabularyService vocabularyService;
    private final ClassifiableTextService classifiableTextService;

    @Override
    public List<Classifier> createClassifiers() {
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
        checkClassifiersAccuracy(texts, classifiers);

    }

    @Override
    public void trainAndSaveClassifiers(List<ClassifiableTextDto> classifiableTextForTrain, List<Classifier> classifiers) {
        for (Classifier classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/" + classifier.toString()));
        }

        Classifier.shutdown();
    }

    public void trainAndSaveClassifiers2(List<ClassifiableTextDto> classifiableTextForTrain,
                                         List<DL4JClassifier> classifiers) throws IOException {
        for (DL4JClassifier classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/" + classifier.toString()));
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


    public void startClassification2() throws IOException {
        // Создаем классификаторы
        List<DL4JClassifier> classifiers = createClassifiers2();

        // Получаем все тексты для классификации
        var data = classifiableTextService.splitTextsForTrainingAndTesting();
        var training = classifiableTextService.collectAndShuffleTexts(data, "training");
        var testing = classifiableTextService.collectAndShuffleTexts(data, "testing");
        // Тренируем классификаторы и сохраняем их
        trainAndSaveClassifiers2(training, classifiers);

        checkClassifiersAccuracy2(testing, classifiers);
    }

    public void checkClassifiersAccuracy2(
            List<ClassifiableTextDto> classifiableTexts,
            List<DL4JClassifier> classifiers) {

        // read second sheet from a file

        for (DL4JClassifier classifier : classifiers) {
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


    public List<DL4JClassifier> createClassifiers2() throws IOException {
        // Получаем все характеристики
        List<CharacteristicDto> characteristics = characteristicService.getAllCharacteristics();
        // Получаем весь словарь
        List<VocabularyWordDto> vocabulary = vocabularyService.getAllVocabulary();

        // Список для хранения классификаторов
        List<DL4JClassifier> classifiers = new ArrayList<>();

        // Проходим по всем характеристикам, создавая классификаторы
        for (CharacteristicDto characteristic : characteristics) {
            // Создаем DL4JClassifier с соответствующей характеристикой и словарем
            DL4JClassifier classifier = new DL4JClassifier(null, characteristic, vocabulary);
            classifiers.add(classifier); // добавляем в список классификаторов
        }

        return classifiers; // возвращаем список классификаторов
    }

    public void startClassification3() throws IOException {
        // Создаем классификаторы
        List<DL4JClassifierWithEmbedding> classifiers = createClassifiers3();

        // Получаем все тексты для классификации
        var data = classifiableTextService.splitTextsForTrainingAndTesting();
        var training = classifiableTextService.collectAndShuffleTexts(data, "training");
        var testing = classifiableTextService.collectAndShuffleTexts(data, "testing");
        // Тренируем классификаторы и сохраняем их
        trainAndSaveClassifiers3(training, classifiers);

        checkClassifiersAccuracy3(testing, classifiers);
    }

    public void checkClassifiersAccuracy3(
            List<ClassifiableTextDto> classifiableTexts,
            List<DL4JClassifierWithEmbedding> classifiers) {

        // read second sheet from a file

        for (DL4JClassifierWithEmbedding classifier : classifiers) {
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


    public List<DL4JClassifierWithEmbedding> createClassifiers3() throws IOException {
        // Получаем все характеристики
        List<CharacteristicDto> characteristics = characteristicService.getAllCharacteristics();
        // Получаем весь словарь
        List<VocabularyWordDto> vocabulary = vocabularyService.getAllVocabulary();

        // Список для хранения классификаторов
        List<DL4JClassifierWithEmbedding> classifiers = new ArrayList<>();

        // Проходим по всем характеристикам, создавая классификаторы
        for (CharacteristicDto characteristic : characteristics) {
            // Создаем DL4JClassifier с соответствующей характеристикой и словарем
            DL4JClassifierWithEmbedding classifier = new DL4JClassifierWithEmbedding(null, characteristic, vocabulary);
            classifiers.add(classifier); // добавляем в список классификаторов
        }

        return classifiers; // возвращаем список классификаторов
    }

    public void trainAndSaveClassifiers3(List<ClassifiableTextDto> classifiableTextForTrain,
                                         List<DL4JClassifierWithEmbedding> classifiers) throws IOException {
        for (DL4JClassifierWithEmbedding classifier : classifiers) {
            classifier.train(classifiableTextForTrain);
            classifier.saveTrainedClassifier(new File("./models/textClassifier/" + classifier.toString()));
        }

        Classifier.shutdown();
    }


}
