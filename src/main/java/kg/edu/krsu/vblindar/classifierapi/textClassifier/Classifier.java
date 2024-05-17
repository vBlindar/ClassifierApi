//package kg.edu.krsu.vblindar.classifierapi.textClassifier;
//
//
//import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
//import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
//import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
//import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
//import kg.edu.krsu.vblindar.classifierapi.ngram.FilteredUnigram;
//import lombok.Data;
//import org.encog.Encog;
//import org.encog.engine.network.activation.ActivationSigmoid;
//import org.encog.ml.data.basic.BasicMLDataSet;
//import org.encog.neural.networks.BasicNetwork;
//import org.encog.neural.networks.layers.BasicLayer;
//import org.encog.neural.networks.training.propagation.Propagation;
//import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
//import org.encog.persist.PersistError;
//
//import java.io.File;
//import java.util.List;
//import java.util.Set;
//
//import static org.encog.persist.EncogDirectoryPersistence.loadObject;
//import static org.encog.persist.EncogDirectoryPersistence.saveObject;
//
//
//@Data
//public class Classifier {
//    private final Characteristic characteristic;
//    private final int inputLayerSize;
//    private final int outputLayerSize;
//    private final BasicNetwork network;
//    private List<VocabularyWord> vocabulary;
//    private final FilteredUnigram nGramStrategy = new FilteredUnigram();
//
//    public Classifier(File trainedNetwork, Characteristic characteristic, List<VocabularyWord> vocabulary) {
//        if (characteristic == null ||
//                characteristic.getName().isEmpty() ||
//                characteristic.getPossibleValues() == null ||
//                characteristic.getPossibleValues().isEmpty() ||
//                vocabulary == null ||
//                vocabulary.isEmpty()) {
//            throw new IllegalArgumentException();
//        }
//
//        this.characteristic = characteristic;
//        this.vocabulary = vocabulary;
//        this.inputLayerSize = vocabulary.size();
//        this.outputLayerSize = characteristic.getPossibleValues().size();
//
//        if (trainedNetwork == null) {
//            this.network = createNeuralNetwork();
//        } else {
//            // load neural network from file
//            try {
//                this.network = (BasicNetwork) loadObject(trainedNetwork);
//            } catch (PersistError e) {
//                throw new IllegalArgumentException();
//            }
//        }
//    }
//
//    public Classifier(Characteristic characteristic, List<VocabularyWord> vocabulary) {
//        this(null, characteristic, vocabulary);
//    }
//
//
//    public static void shutdown() {
//        Encog.getInstance().shutdown();
//    }
//
//    private BasicNetwork createNeuralNetwork() {
//        BasicNetwork network = new BasicNetwork();
//
//        // input layer
//        network.addLayer(new BasicLayer(null, true, inputLayerSize));
//
//        // hidden layer
//        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputLayerSize / 6));
//        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputLayerSize / 6 / 4));
//
//        // output layer
//        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputLayerSize));
//
//        network.getStructure().finalizeStructure();
//        network.reset();
//
//        return network;
//    }
//
//    public CharacteristicValue classify(ClassifiableText classifiableText) {
//        double[] output = new double[outputLayerSize];
//
//        // calculate output vector
//        network.compute(getTextAsVectorOfWords(classifiableText), output);
//        Encog.getInstance().shutdown();
//
//        return convertVectorToCharacteristic(output);
//    }
//
//    private CharacteristicValue convertVectorToCharacteristic(double[] vector) {
//        int idOfMaxValue = getIdOfMaxValue(vector);
//
//        for (CharacteristicValue c : characteristic.getPossibleValues()) {
//            if (c.getId() == idOfMaxValue) {
//                return c;
//            }
//        }
//        return null;
//    }
//    private int getIdOfMaxValue(double[] vector) {
//        int indexOfMaxValue = 0;
//        double maxValue = vector[0];
//        if (!checkVector(vector)) {
//            return -1;
//        }
//
//        for (int i = 1; i < vector.length; i++) {
//            if (vector[i] > maxValue) {
//                maxValue = vector[i];
//                indexOfMaxValue = i;
//            }
//        }
//
//        return indexOfMaxValue + 1;
//    }
//
//    private boolean checkVector(double[] vector) {
//        for (double value : vector) {
//            if (value >= 0.58)
//                return true;
//        }
//        throw new IllegalArgumentException("This text does not belong to any topic from the training data");
//    }
//
//    public void saveTrainedClassifier(File trainedNetwork) {
//        saveObject(trainedNetwork, network);
//
//    }
//
//    public Characteristic getCharacteristic() {
//        return characteristic;
//    }
//
//    public void train(List<ClassifiableText> classifiableTexts) {
//        double[][] input = getInput(classifiableTexts);
//        double[][] ideal = getIdeal(classifiableTexts);
//
//        Propagation train = new ResilientPropagation(network, new BasicMLDataSet(input, ideal));
//        train.setThreadCount(32);
//        int c =0;
//        do {
//            train.iteration();
//            c++;
//        } while (train.getError() > 0.01);
//        System.out.println(c);
//        System.out.println("train finish ");
//        train.finishTraining();
//
//    }
//
//    private double[][] getInput(List<ClassifiableText> classifiableTexts) {
//        double[][] input = new double[classifiableTexts.size()][inputLayerSize];
//
//
//        int i = 0;
//
//        for (ClassifiableText classifiableText : classifiableTexts) {
//            input[i++] = getTextAsVectorOfWords(classifiableText);
//        }
//
//        return input;
//    }
//
//    private double[][] getIdeal(List<ClassifiableText> classifiableTexts) {
//        double[][] ideal = new double[classifiableTexts.size()][outputLayerSize];
//
//
//        int i = 0;
//
//        for (ClassifiableText classifiableText : classifiableTexts) {
//            ideal[i++] = getCharacteristicAsVector(classifiableText);
//        }
//
//        return ideal;
//    }
//
//    private double[] getCharacteristicAsVector(ClassifiableText classifiableText) {
//        double[] vector = new double[outputLayerSize];
//        var value = classifiableText.getCharacteristicValue(characteristic).getId();
//        var id = (int)(value - 1);
//        vector[id] = 1;
//        return vector;
//    }
//
//    private double[] getTextAsVectorOfWords(ClassifiableText classifiableText) {
//        double[] vector = new double[inputLayerSize];
//
//        Set<String> uniqueValues = nGramStrategy.getUnigram(classifiableText.getText());
//
//
//        for (String word : uniqueValues) {
//            VocabularyWord vw = findWordInVocabulary(word);
//
//            if (vw != null) { // word found in vocabulary
//                vector[(int) (vw.getId() - 1)] = 1;
//            }
//        }
//
//        return vector;
//    }
//
//    private VocabularyWord findWordInVocabulary(String word) {
//        try {
//
//            return vocabulary.get(vocabulary.indexOf(VocabularyWord.builder().value(word).build()));
//        } catch (NullPointerException | IndexOutOfBoundsException e) {
//            return null;
//        }
//    }
//
//    @Override
//    public String toString() {
//        return characteristic.getName() + "NeuralNetworkClassifier";
//    }
//
//
//}