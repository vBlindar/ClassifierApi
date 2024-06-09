package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.*;
import kg.edu.krsu.vblindar.classifierapi.entity.dto.Ad;
import kg.edu.krsu.vblindar.classifierapi.entity.dto.Answer;
import kg.edu.krsu.vblindar.classifierapi.repository.ImageCharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.service.IClassifyService;

import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassifyService implements IClassifyService {


    private final VocabularyService vocabularyService;
    private final ImageCharacteristicRepository imageCharacteristicRepository;
    private final TextCharacteristicService characteristicValueService;


    @Override
    public Map<Boolean, String> classifyText(String text, File file) throws IOException {
        Map<Boolean, String> map = new HashMap<>();
        ClassifiableText classifiableText = ClassifiableText.builder().text(text).build();
        StringBuilder classifiedCharacteristics = new StringBuilder();
        List<DL4JClassifier> classifiers = createClassifiers(file);
        boolean bool=true;
        try {
            for (DL4JClassifier classifier : classifiers) {
                TextCharacteristic classifiedValue = classifier.classify(classifiableText);

                classifiedCharacteristics.append(classifiedValue.getValue());
                bool=classifiedValue.getCheck();
            }
        } catch (Exception e) {
            map.put(Boolean.FALSE, e.getMessage());
            return map;

        }
        map.put(Boolean.valueOf(bool),classifiedCharacteristics.toString());
        return map;
    }


    @Override
    public List<DL4JClassifier> createClassifiers(File file) throws IOException {
        List<TextCharacteristic> characteristics = characteristicValueService.getAllCharacteristics();
        List<VocabularyWord> vocabulary = vocabularyService.getAllVocabulary();
        List<DL4JClassifier> classifiers = new ArrayList<>();

        DL4JClassifier classifier = new DL4JClassifier(file, vocabulary, characteristics);
        classifiers.add(classifier);

        return classifiers;
    }

    @Override
    public File getNetworkFile(String type) throws IOException {
        String folderPath = "./models";
        if (type.equals("img"))
            folderPath += "/imgClassifier";
        else
            folderPath += "/textClassifier";

        File folder = new File(folderPath);

        File[] files = folder.listFiles((dir, name) -> !name.equals(".DS_Store"));

        if (files != null && files.length > 0) {
            return files[0];
        }
        throw new IOException("Network file for " + type + " not found");
    }

    @Override
    public Map<Boolean, String> classifyImage(MultipartFile file, File neural) throws IOException {
        Map<Boolean,String> map = new HashMap<>();
        File img = convertMultipartFileToFile(file);
        List<ImageCharacteristic> characteristics = imageCharacteristicRepository.findAll();

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(neural);

        NativeImageLoader loader = new NativeImageLoader(32, 32, 3);
        INDArray image = loader.asMatrix(img);

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(image);

        INDArray output = model.output(image);

        int classIdx = output.argMax(1).getInt(0) + 1;
        img.delete();
        for (ImageCharacteristic characteristic : characteristics) {
            if (characteristic.getId() == classIdx){
                map.put(Boolean.TRUE,characteristic.getValue());
                return map;
            }
        }

        map.put(Boolean.FALSE,"This image does not belong to any topic from the training data");
        return map;
    }
    public Map<Boolean, String> classifyImage(File file, File neural) throws IOException {
        Map<Boolean,String> map = new HashMap<>();
        List<ImageCharacteristic> characteristics = imageCharacteristicRepository.findAll();

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(neural);

        NativeImageLoader loader = new NativeImageLoader(32, 32, 3);
        INDArray image = loader.asMatrix(file);

        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(image);

        INDArray output = model.output(image);

        int classIdx = output.argMax(1).getInt(0) + 1;

        for (ImageCharacteristic characteristic : characteristics) {
            if (characteristic.getId() == classIdx){
                map.put(characteristic.getCheck(),characteristic.getValue());
                return map;
            }
        }

        map.put(Boolean.FALSE,"This image does not belong to any topic from the training data");
        return map;
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile file) throws IOException {

        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());

        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }

        return convertedFile;
    }

    @Override
    public Answer classify(Ad ad) throws IOException {
        File textNetwork = getNetworkFile("text");
        File imageNetwork = getNetworkFile("img");
        Map<Boolean,String> textAnswer = classifyText(ad.getText(),textNetwork);
        List<Map<Boolean,String>> imgsAnswer = new ArrayList<>();
        for (String path : ad.getFiles()) {
            var file = getFileFromPath(path);
            imgsAnswer.add(classifyImage(file,imageNetwork));
        }

        return new Answer(ad.getId(),textAnswer,imgsAnswer,Answer.checkForFalse(textAnswer,imgsAnswer));
    }
    public  File getFileFromPath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be null or blank");
        }

        File file = new File(path);

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist at path: " + path);
        }

        return file;
    }

}
