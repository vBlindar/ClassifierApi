package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.*;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassifyService implements IClassifyService {


    private final VocabularyService vocabularyService;
    private final ImageCharacteristicRepository imageCharacteristicRepository;
    private final CharacteristicValueService characteristicValueService;


    @Override
    public String classifyText(String text, File file) throws IOException {
        ClassifiableText classifiableText = ClassifiableText.builder().text(text).build();
        StringBuilder classifiedCharacteristics = new StringBuilder();
        List<DL4JClassifier> classifiers = createClassifiers(file);
        try {
            for (DL4JClassifier classifier : classifiers) {
                TextCharacteristic classifiedValue = classifier.classify(classifiableText);

                classifiedCharacteristics.append(classifiedValue.getValue()).append("\n");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());

        }

        return classifiedCharacteristics.toString();
    }


    @Override
    public List<DL4JClassifier> createClassifiers(File file) throws IOException {
        List<TextCharacteristic> characteristics = characteristicValueService.getAllCharacteristics();
        List<VocabularyWord> vocabulary = vocabularyService.getAllVocabulary();
        List<DL4JClassifier> classifiers = new ArrayList<>();

            DL4JClassifier classifier = new DL4JClassifier(file, vocabulary,characteristics);
            classifiers.add(classifier);

        return classifiers;
    }

    @Override
    public File getNetworkFile(String type) {
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
        return null;
    }

    @Override
    public String classifyImage(MultipartFile file, File neural) throws IOException {
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
            if(characteristic.getId()==classIdx)
                return characteristic.getValue();
        }


        return null;
    }

    @Override
    public File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());

        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }

        return convertedFile;
    }

}
