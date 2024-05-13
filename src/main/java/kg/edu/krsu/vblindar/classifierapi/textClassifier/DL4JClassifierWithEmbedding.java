package kg.edu.krsu.vblindar.classifierapi.textClassifier;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DL4JClassifierWithEmbedding {

    private final Characteristic characteristic;
    private final MultiLayerNetwork network; // заменяем BasicNetwork
    private List<VocabularyWord> vocabulary;
    private final int inputLayerSize;
    private final int outputLayerSize;
    private final FilteredUnigram nGramStrategy = new FilteredUnigram();

    public DL4JClassifierWithEmbedding(File trainedNetwork, Characteristic characteristic, List<VocabularyWord> vocabulary) throws IOException {
        this.inputLayerSize = vocabulary.size();
        this.characteristic = characteristic;
        this.vocabulary = vocabulary;
        this.outputLayerSize = characteristic.getPossibleValues().size();

        if (trainedNetwork == null) {
            this.network = createNeuralNetwork();
        } else {
            this.network = MultiLayerNetwork.load(trainedNetwork, false);
        }


    }

    private MultiLayerNetwork createNeuralNetwork() {
        int embeddingSize = 100; // Размер вектора встраивания

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam())
                .list()
                .layer(0, new EmbeddingLayer.Builder()
                        .nIn(inputLayerSize)  // размер словаря
                        .nOut(embeddingSize)  // размер вектора встраивания
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(embeddingSize)
                        .nOut(200) // количество LSTM единиц
                        .activation(Activation.TANH)
                        .build())
                .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX) // Используйте softmax для многоклассовой классификации
                        .nIn(200)
                        .nOut(outputLayerSize)  // Количество классов
                        .build())
                .build();


        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        return network;
    }

    public void train(List<ClassifiableText> classifiableTexts) {
        int maxLength = 100; // Определите максимальную длину
        INDArray input = getInput(classifiableTexts, maxLength);
        INDArray labels = getIdeal(classifiableTexts);

        DataSet dataSet = new DataSet(input, labels);
        for (int epoch = 0; epoch < 100; epoch++) {
            System.out.println("================ "+epoch + "===========");
            network.fit(dataSet);
            Evaluation eval = new Evaluation();
            INDArray output = network.output(dataSet.getFeatures(), false);
            eval.eval(dataSet.getLabels(), output);
            System.out.println(eval.stats());

        }
        System.out.println("Training complete");
    }

//    public void train(List<ClassifiableText> classifiableTexts) {
//
//        INDArray input = getInput(classifiableTexts,100);
//        INDArray labels = getIdeal(classifiableTexts);
//        DataSet dataSet = new DataSet(input, labels);
//
//        double errorThreshold = 0.01;  // Порог ошибки
//        double lastError = Double.MAX_VALUE;
//        int epoch = 0;
//
//        while (lastError > errorThreshold) {
//            network.fit(dataSet);  // Обучение сети
//
//            // Вычисляем ошибку после каждой эпохи
//            Evaluation eval = new Evaluation();
//            INDArray output = network.output(dataSet.getFeatures(), false);
//            eval.eval(dataSet.getLabels(), output);
//            System.out.println(eval.stats());  // Или другая метрика, соответствующая вашей задаче
//
//            System.out.println("Epoch " + epoch + ": Error = " + lastError);
//            epoch++;
//
//            // Условие выхода для предотвращения бесконечного цикла
//            if (epoch > 100) {
//                break;
//            }
//        }
//
//
//        System.out.println("Training complete");
//    }



//    private INDArray getInput(List<ClassifiableText> classifiableTexts) {
//        int[][] input = new int[classifiableTexts.size()][inputLayerSize];
//
//        int i = 0;
//        for (ClassifiableText classifiableText : classifiableTexts) {
//            input[i++] = getTextAsVectorOfWords(classifiableText);
//        }
//
//        return Nd4j.create(input);
//    }

//    private INDArray getInput(List<ClassifiableText> classifiableTexts, int maxLength) {
//        int[][] input = new int[classifiableTexts.size()][maxLength];
//
//        int i = 0;
//        for (ClassifiableText classifiableText : classifiableTexts) {
//            input[i] = getTextAsVectorOfWords(classifiableText, maxLength);
//            i++;
//        }
//
//        return Nd4j.create(input);
//    }

    private INDArray getInput(List<ClassifiableText> classifiableTexts, int maxLength) {
        List<INDArray> inputs = new ArrayList<>();
        for (ClassifiableText classifiableText : classifiableTexts) {
            int[] indices = getTextAsVectorOfWords(classifiableText, maxLength);
            inputs.add(Nd4j.create(indices, new int[]{1, maxLength}));  // Создание 2D INDArray для каждого примера
        }
        return Nd4j.concat(0, inputs.toArray(new INDArray[0]));  // Объединяем все INDArray в один для подачи в сеть
    }
    private INDArray getIdeal(List<ClassifiableText> classifiableTexts) {
        double[][] ideal = new double[classifiableTexts.size()][outputLayerSize];

        int i = 0;
        for (ClassifiableText classifiableText : classifiableTexts) {
            ideal[i++] = getCharacteristicAsVector(classifiableText);
        }

        return Nd4j.create(ideal);
    }



    private double[] getCharacteristicAsVector(ClassifiableText classifiableText) {
        double[] vector = new double[outputLayerSize];
        long id = classifiableText.getCharacteristicValue(characteristic).getId();
        vector[(int)(id - 1)] = 1; // Set the index corresponding to the characteristic value to 1
        return vector;
    }
    private int getWordIndex(ClassifiableText text) {
        VocabularyWord vw = findWordInVocabulary(text.getText()); // нахождение слова в словаре
        return (vw != null) ? Integer.parseInt(String.valueOf(vw.getId())) : 0; // возвращает индекс или 0, если слово не найдено
    }



    public void saveTrainedClassifier(File trainedNetwork) throws IOException {
        ModelSerializer.writeModel(network, trainedNetwork, true); // сохранение модели
    }


    public Characteristic getCharacteristic() {
        return characteristic;
    }



    public CharacteristicValue classify(ClassifiableText classifiableText) {
        // Преобразуем текст в вектор признаков
        int[] inputArray = getTextAsVectorOfWords(classifiableText,100);
        // Преобразуем одномерный массив в двумерный, добавив дополнительное измерение
        INDArray input = Nd4j.create(new int[][]{inputArray});
        //INDArray input = Nd4j.create(getTextAsVectorOfWords(classifiableText));

        // Получаем вывод сети
        INDArray output = network.output(input);

        // Определяем индекс максимального значения в выходном векторе
        int index = output.argMax(1).getInt(0);

        // Возвращаем соответствующее значение характеристики
        return getCharacteristicValueByIndex(index);
    }

    private VocabularyWord findWordInVocabulary(String word) {
        for (VocabularyWord vw : vocabulary) {
            if (vw.getValue().equals(word)) {
                return vw;
            }
        }
        return null; // Возвращаем null, если слово не найдено
    }
    private CharacteristicValue getCharacteristicValueByIndex(int index) {
        for (CharacteristicValue value : characteristic.getPossibleValues()) {
            if ((value.getId() - 1) == index) {
                return value;
            }
        }
        return null; // Или выбросить исключение, если ничего не найдено
    }
//    private int[] getTextAsVectorOfWords(ClassifiableText text) {
//        Set<String> words = nGramStrategy.getNGram(text.getText());
//        int[] indices = new int[words.size()];
//        int i = 0;
//        for (String word : words) {
//            VocabularyWord vw = findWordInVocabulary(word);
//            if (vw != null) {
//                indices[i++] = (int) vw.getId() - 1; // Индекс для EmbeddingLayer
//            }
//        }
//        return indices;
//    }

    private int[] getTextAsVectorOfWords(ClassifiableText text, int maxLength) {
        Set<String> words = nGramStrategy.getNGram(text.getText());
        int[] indices = new int[maxLength];  // Initialize with maxLength
        Arrays.fill(indices, 0);  // Fill with zeros for padding

        int i = 0;
        for (String word : words) {
            if (i >= maxLength) break;  // Prevent exceeding the maximum length
            VocabularyWord vw = findWordInVocabulary(word);
            if (vw != null) {
                indices[i++] = (int) (vw.getId() - 1);  // Set index, subtract 1 if your IDs start from 1
            }
        }
        return indices;
    }


}
