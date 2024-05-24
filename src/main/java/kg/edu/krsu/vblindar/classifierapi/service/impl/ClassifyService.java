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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassifyService implements IClassifyService {


    private final VocabularyService vocabularyService;
    private final ImageCharacteristicRepository imageCharacteristicRepository;
    private final TextCharacteristicService characteristicValueService;


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
        throw new IOException("Network file for "+type+" not found");
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
    public File convertMultipartFileToFile(MultipartFile file) throws IOException {

        InputStream inputStream = file.getInputStream();
        BufferedImage originalImage = ImageIO.read(inputStream);

        // Изменение размера изображения до 32x32
        BufferedImage resizedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH), 0, 0, null);
        graphics2D.dispose();

        // Конвертация BufferedImage в File
        File outputFile = new File("resizedImage.png");
        ImageIO.write(resizedImage, "png", outputFile);

        return outputFile;
//
//        File convertedFile = new File(file.getOriginalFilename());
//        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
//            fos.write(file.getBytes());
//
//        } catch (IOException e) {
//            log.error("Error converting multipartFile to file", e);
//        }
//
//        return convertedFile;
    }

    @Override
    public String classify(String text, MultipartFile[] files) throws IOException {
        StringBuilder sb = new StringBuilder();
        File textNetwork = getNetworkFile("text");
        File imageNetwork = getNetworkFile("image");
        sb.append(classifyText(text,textNetwork));
        sb.append(System.lineSeparator());
        for (MultipartFile file : files) {
            sb.append(file.getOriginalFilename())
                    .append(":")
                    .append(classifyImage(file,imageNetwork))
                    .append(System.lineSeparator());
        }

        return sb.toString();
    }

}
