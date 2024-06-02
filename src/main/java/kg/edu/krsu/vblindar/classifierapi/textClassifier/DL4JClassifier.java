package kg.edu.krsu.vblindar.classifierapi.textClassifier;



import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
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
    private final MultiLayerNetwork network; // заменяем BasicNetwork
    private List<VocabularyWord> vocabulary;
    List<TextCharacteristic> allCharacteristic;
    private final int inputLayerSize;
    private final int outputLayerSize;
    private final FilteredUnigram nGramStrategy = new FilteredUnigram();

    public DL4JClassifier(File trainedNetwork, List<VocabularyWord> vocabulary,
                          List<TextCharacteristic> allCharacteristic) throws IOException {
        this.allCharacteristic = allCharacteristic;
        this.inputLayerSize = vocabulary.size();
        this.vocabulary = vocabulary;
        this.outputLayerSize = allCharacteristic.size();

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
        StatsStorage statsStorage = new FileStatsStorage(new File("statistics/ui-texts-stats.dl4j"));
        network.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(10));

        return network;
    }


    public void train(List<ClassifiableText> classifiableTexts) {
        INDArray input = getInput(classifiableTexts);
        INDArray labels = getIdeal(classifiableTexts);
        DataSet dataSet = new DataSet(input, labels);


        int epoch = 0;

        while (epoch<50) {
            network.fit(dataSet);
            Evaluation eval = new Evaluation();
            INDArray output = network.output(dataSet.getFeatures(), false);
            eval.eval(dataSet.getLabels(), output);
            System.out.println(eval.stats());
            epoch++;
        }


        System.out.println("Training complete");
    }




    private INDArray getInput(List<ClassifiableText> classifiableTexts) {
        double[][] input = new double[classifiableTexts.size()][inputLayerSize];

        int i = 0;
        for (ClassifiableText classifiableText : classifiableTexts) {
            input[i++] = getTextAsVectorOfWords(classifiableText);
        }

        return Nd4j.create(input);
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
        long id = classifiableText.getCharacteristic().getId();
        vector[(int)(id - 1)] = 1; // Set the index corresponding to the characteristic value to 1
        return vector;
    }



    public void saveTrainedClassifier(File trainedNetwork) throws IOException {
        ModelSerializer.writeModel(network, trainedNetwork, true); // сохранение модели
    }




    public TextCharacteristic classify(ClassifiableText classifiableText) {

        double[] inputArray = getTextAsVectorOfWords(classifiableText);
        INDArray input = Nd4j.create(new double[][]{inputArray});

        INDArray output = network.output(input);
        checkVector(output.data());
        int index = output.argMax(1).getInt(0);
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
    private TextCharacteristic getCharacteristicValueByIndex(int index) {
        for (TextCharacteristic value : allCharacteristic) {
            if ((value.getId() - 1) == index) {
                return value;
            }
        }
        return null;
    }
    private double[] getTextAsVectorOfWords(ClassifiableText text) {
        double[] vector = new double[inputLayerSize];
        Set<String> words = nGramStrategy.getUnigram(text.getText());
        for (String word : words) {
            VocabularyWord vw = findWordInVocabulary(word);
            if (vw != null) {
                vector[(int) (vw.getId() - 1)] = 1.0;
            }
        }
        return vector;
    }


    private void checkVector(DataBuffer vector) {
        for (long i = 0; i < vector.length(); i++) {
            if(vector.getDouble(i) >= 0.33){
               return;
            }
        }
        throw new IllegalArgumentException("This text does not belong to any topic from the training data");
    }
}
