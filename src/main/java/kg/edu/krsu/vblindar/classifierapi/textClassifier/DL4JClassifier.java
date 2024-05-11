package kg.edu.krsu.vblindar.classifierapi.textClassifier;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.dto.VocabularyWordDto;
import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DL4JClassifier {
    private final CharacteristicDto characteristic;
    private final MultiLayerNetwork network; // заменяем BasicNetwork
    private List<VocabularyWordDto> vocabulary;
    private final int inputLayerSize;
    private final int outputLayerSize;
    private final FilteredUnigram nGramStrategy = new FilteredUnigram();

    public DL4JClassifier(File trainedNetwork, CharacteristicDto characteristic, List<VocabularyWordDto> vocabulary) throws IOException {
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
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam())
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputLayerSize)  // количество входов соответствует размеру входного слоя
                        .nOut(inputLayerSize / 6)
                        .activation(Activation.SIGMOID)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nOut(inputLayerSize / 6 / 4)
                        .activation(Activation.SIGMOID)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nOut(outputLayerSize)
                        .activation(Activation.SIGMOID)
                        .build())
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        return network;
    }


    public void train(List<ClassifiableTextDto> classifiableTexts) {
        // Преобразование текстов в DataSet
        INDArray input = getInput(classifiableTexts);
        INDArray labels = getIdeal(classifiableTexts);
        DataSet dataSet = new DataSet(input, labels);


        double errorThreshold = 0.01;  // Порог ошибки
        double lastError = Double.MAX_VALUE;
        int epoch = 0;

        while (lastError > errorThreshold) {
            network.fit(dataSet);  // Обучение сети

            // Вычисляем ошибку после каждой эпохи
            Evaluation eval = new Evaluation();
            INDArray output = network.output(dataSet.getFeatures(), false);
            eval.eval(dataSet.getLabels(), output);
            System.out.println(eval.stats());  // Или другая метрика, соответствующая вашей задаче
            System.out.println("Epoch " + epoch + ": Error = " + lastError);
            epoch++;

            // Условие выхода для предотвращения бесконечного цикла
            if (epoch > 100) {
                break;
            }
        }


        System.out.println("Training complete");
    }




    private INDArray getInput(List<ClassifiableTextDto> classifiableTexts) {
        double[][] input = new double[classifiableTexts.size()][inputLayerSize];

        int i = 0;
        for (ClassifiableTextDto classifiableText : classifiableTexts) {
            input[i++] = getTextAsVectorOfWords(classifiableText);
        }

        return Nd4j.create(input);
    }

    private INDArray getIdeal(List<ClassifiableTextDto> classifiableTexts) {
        double[][] ideal = new double[classifiableTexts.size()][outputLayerSize];

        int i = 0;
        for (ClassifiableTextDto classifiableText : classifiableTexts) {
            ideal[i++] = getCharacteristicAsVector(classifiableText);
        }

        return Nd4j.create(ideal);
    }


    private double[] getCharacteristicAsVector(ClassifiableTextDto classifiableText) {
        double[] vector = new double[outputLayerSize];
        long id = classifiableText.getCharacteristicValue(characteristic).getId();
        vector[(int)(id - 1)] = 1; // Set the index corresponding to the characteristic value to 1
        return vector;
    }
    private int getWordIndex(ClassifiableTextDto text) {
        VocabularyWordDto vw = findWordInVocabulary(text.getText()); // нахождение слова в словаре
        return (vw != null) ? (int) vw.getId() : 0; // возвращает индекс или 0, если слово не найдено
    }




    public void saveTrainedClassifier(File trainedNetwork) throws IOException {
        ModelSerializer.writeModel(network, trainedNetwork, true); // сохранение модели
    }


    public CharacteristicDto getCharacteristic() {
        return characteristic;
    }



    public CharacteristicValueDto classify(ClassifiableTextDto classifiableText) {
        // Преобразуем текст в вектор признаков
        double[] inputArray = getTextAsVectorOfWords(classifiableText);
        // Преобразуем одномерный массив в двумерный, добавив дополнительное измерение
        INDArray input = Nd4j.create(new double[][]{inputArray});
        //INDArray input = Nd4j.create(getTextAsVectorOfWords(classifiableText));

        // Получаем вывод сети
        INDArray output = network.output(input);

        checkVector(output.data());
        // Определяем индекс максимального значения в выходном векторе
        int index = output.argMax(1).getInt(0);

        // Возвращаем соответствующее значение характеристики
        return getCharacteristicValueByIndex(index);
    }

    private VocabularyWordDto findWordInVocabulary(String word) {
        for (VocabularyWordDto vw : vocabulary) {
            if (vw.getValue().equals(word)) {
                return vw;
            }
        }
        return null; // Возвращаем null, если слово не найдено
    }
    private CharacteristicValueDto getCharacteristicValueByIndex(int index) {
        for (CharacteristicValueDto value : characteristic.getPossibleValues()) {
            if ((value.getId() - 1) == index) {
                return value;
            }
        }
        return null;
    }
    private double[] getTextAsVectorOfWords(ClassifiableTextDto text) {
        double[] vector = new double[inputLayerSize];
        Set<String> words = nGramStrategy.getNGram(text.getText());
        for (String word : words) {
            VocabularyWordDto vw = findWordInVocabulary(word);
            if (vw != null) {
                vector[(int) vw.getId() - 1] = 1.0;
            }
        }
        return vector;
    }


    private void checkVector(DataBuffer vector) {
        for (long i = 0; i < vector.length(); i++) {
            if(vector.getDouble(i) <= 0.33){
                throw new IllegalArgumentException("This text does not belong to any topic from the training data");
            }
        }
    }
}