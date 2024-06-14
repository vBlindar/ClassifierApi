package kg.edu.krsu.vblindar.classifierapi.imageClassify;


import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;

import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;


@Slf4j
public class ImageModel {
    private static final int IMAGE_HEIGHT = 32;
    private static final int IMAGE_WIDTH = 32;
    private static final int IMAGE_CHANNELS = 3;
    private static int IMAGE_CLASSES;
    private static final int NETWORK_SEED = 666;
    private static final String NETWORK_FILEPATH = "models/imgClassifier/Network.zip";

    private static final int CONVOLUTION_FILTER_SIZE = 3;
    private static final int CONVOLUTION_FILTER_SHIFT = 1;
    private static final int CONVOLUTION_POOL_SIZE = 2;
    private static final int CONVOLUTION_POOL_SHIFT = 2;

    private static final int LEARNING_NUMBER_OF_EPOCHS = 20;

    private final DataSetIterator trainSet;
    private final DataSetIterator testSet;
    private MultiLayerConfiguration configuration;
    private MultiLayerNetwork network;
    public ImageModel(DataSetIterator trainSet, DataSetIterator testSet, File model) throws IOException {
        this.network = ModelSerializer.restoreMultiLayerNetwork(model);
        this.trainSet = trainSet;
        this.testSet = testSet;
    }

    public ImageModel(DataSetIterator trainSet, DataSetIterator testSet, int classesCount) {
        this.trainSet = trainSet;
        this.testSet = testSet;
        IMAGE_CLASSES = classesCount;
        this.initConfig();
        this.initNetwork();
    }


    private void initConfig() {



        configuration = new NeuralNetConfiguration.Builder()
                .seed(NETWORK_SEED)
                .updater(new Nesterovs(1e-2, 0.9))
                .weightInit(WeightInit.XAVIER)
                .l2(1e-4)
                .list()
                .layer(0, new ConvolutionLayer.Builder()
                        .kernelSize(CONVOLUTION_FILTER_SIZE, CONVOLUTION_FILTER_SIZE)
                        .stride(CONVOLUTION_FILTER_SHIFT, CONVOLUTION_FILTER_SHIFT)
                        .nIn(IMAGE_CHANNELS)
                        .nOut(64)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder()
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(CONVOLUTION_POOL_SIZE, CONVOLUTION_POOL_SIZE)
                        .stride(CONVOLUTION_POOL_SHIFT, CONVOLUTION_POOL_SHIFT)
                        .build())
                .layer(2, new ConvolutionLayer.Builder()
                        .kernelSize(CONVOLUTION_FILTER_SIZE, CONVOLUTION_FILTER_SIZE)
                        .stride(CONVOLUTION_FILTER_SHIFT, CONVOLUTION_FILTER_SHIFT)
                        .nOut(128)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(3, new SubsamplingLayer.Builder()
                        .poolingType(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(CONVOLUTION_POOL_SIZE, CONVOLUTION_POOL_SIZE)
                        .stride(CONVOLUTION_POOL_SHIFT, CONVOLUTION_POOL_SHIFT)
                        .build())
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU).nOut(4800).build())
                .layer(5, new DenseLayer.Builder().activation(Activation.RELU).nOut(1200).build())
                .layer(6, new OutputLayer.Builder().lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nOut(IMAGE_CLASSES).build())
                .setInputType(InputType.convolutional(IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_CHANNELS))
                .build();
        configuration.setBackpropType(BackpropType.Standard);


    }


    private void initNetwork() {
        network = new MultiLayerNetwork(configuration);
        network.init();
        StatsStorage statsStorage = new FileStatsStorage(new File("statistics/images-stats.dl4j"));
        network.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(200));
    }

    public void train() {
        DataSet trainDataSet = convertIteratorToDataSet(trainSet);
        for (int i = 0; i < LEARNING_NUMBER_OF_EPOCHS; i++) {
            network.fit(trainSet);
            Evaluation eval = new Evaluation();
            INDArray output = network.output(trainDataSet.getFeatures(), false);
            eval.eval(trainDataSet.getLabels(), output);
            logStatsToFile(eval.stats(),"/Users/vlad_557/Desktop/инс/ClassifierApi/logs/img.txt","EPOCH: "+ (i+1));
            log.info("image model training - " + i + " epoch");
        }
    }

    public static void logStatsToFile(String stats, String filePath,String epoch) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(epoch);
            writer.newLine();
            writer.write(stats);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataSet convertIteratorToDataSet(DataSetIterator iterator) {
        List<DataSet> dataSets = new ArrayList<>();

        while (iterator.hasNext()) {
            dataSets.add(iterator.next());
        }

        if (!dataSets.isEmpty()) {
            return combineDataSets(dataSets);
        }

        return null;
    }

    private DataSet combineDataSets(List<DataSet> dataSets) {
        DataSet all = dataSets.get(0);
        for (int i = 1; i < dataSets.size(); i++) {
            DataSet next = dataSets.get(i);
            all.setFeatures(Nd4j.vstack(all.getFeatures(), next.getFeatures()));
            all.setLabels(Nd4j.vstack(all.getLabels(), next.getLabels()));
        }
        return all;
    }

    public void test() {
        Evaluation evaluationOnTrain = network.evaluate(trainSet);
        System.out.println(evaluationOnTrain.stats());
        logStatsToFile(evaluationOnTrain.stats(),"/Users/vlad_557/Desktop/инс/ClassifierApi/logs/img.txt","RESULTS ON TRAINING DATA");
        Evaluation evaluationOnTest = network.evaluate(testSet);
        System.out.println(evaluationOnTest.stats());
        logStatsToFile(evaluationOnTest.stats(),"/Users/vlad_557/Desktop/инс/ClassifierApi/logs/img.txt","RESULTS ON TESTING DATA");
    }

    public void save() throws IOException {
        File locationToSave = new File(NETWORK_FILEPATH);
        ModelSerializer.writeModel(network, locationToSave, true);
    }

    public void load() throws IOException {
        File locationToSave = new File(NETWORK_FILEPATH);
        network = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
    }
}
