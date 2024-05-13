package kg.edu.krsu.vblindar.classifierapi.imageClassify;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

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

    public ImageModel(DataSetIterator trainSet, DataSetIterator testSet, int classesCount) {
        this.trainSet = trainSet;
        this.testSet = testSet;
        IMAGE_CLASSES = classesCount;
        this.initConfig();
        this.initNetwork();
    }

    private static Layer convLayer(int nIn, int nOut) {
        return new ConvolutionLayer.Builder()
                .kernelSize(CONVOLUTION_FILTER_SIZE, CONVOLUTION_FILTER_SIZE)
                .stride(CONVOLUTION_FILTER_SHIFT, CONVOLUTION_FILTER_SHIFT)
                .nIn(nIn)
                .nOut(nOut)
                .activation(Activation.IDENTITY)
                .build();
    }

    private static Layer convLayer(int nOut) {
        return new ConvolutionLayer.Builder()
                .kernelSize(CONVOLUTION_FILTER_SIZE, CONVOLUTION_FILTER_SIZE)
                .stride(CONVOLUTION_FILTER_SHIFT, CONVOLUTION_FILTER_SHIFT)
                .nOut(nOut)
                .activation(Activation.IDENTITY)
                .build();
    }

    private static Layer downsamplingLayer() {
        return new SubsamplingLayer.Builder()
                .poolingType(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(CONVOLUTION_POOL_SIZE, CONVOLUTION_POOL_SIZE)
                .stride(CONVOLUTION_POOL_SHIFT, CONVOLUTION_POOL_SHIFT)
                .build();
    }

    private void initConfig() {

        configuration = new NeuralNetConfiguration.Builder()
                .seed(NETWORK_SEED)
                .updater(new Nesterovs(1e-2, 0.9)) // Integrated learning rate and momentum
                .weightInit(WeightInit.XAVIER)
                .l2(1e-4)
                .list()
                .layer(0, convLayer(IMAGE_CHANNELS, 64))
                .layer(1, downsamplingLayer())
                .layer(2, convLayer(128))
                .layer(3, downsamplingLayer())
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
    }

    public void train() {
        for (int i = 0; i < LEARNING_NUMBER_OF_EPOCHS; i++) {
            network.fit(trainSet);
            log.info("Completed epoch " + i);
        }
    }

    public void test() {
        Evaluation evaluationOnTrain = network.evaluate(trainSet);
        System.out.println(evaluationOnTrain.stats());

        Evaluation evaluationOnTest = network.evaluate(testSet);
        System.out.println(evaluationOnTest.stats());
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
