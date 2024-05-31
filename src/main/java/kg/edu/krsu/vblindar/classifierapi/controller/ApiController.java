package kg.edu.krsu.vblindar.classifierapi.controller;


import kg.edu.krsu.vblindar.classifierapi.entity.dto.Answer;
import kg.edu.krsu.vblindar.classifierapi.imageClassify.ImageModel;
import kg.edu.krsu.vblindar.classifierapi.imageClassify.dataset.ImageDataset;
import kg.edu.krsu.vblindar.classifierapi.service.impl.ClassifyService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.ImageCharacteristicService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.StorageService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.TrainTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/classifier")
@RequiredArgsConstructor
public class ApiController {


    private final StorageService storageService;
    private final TrainTextService trainTextService;
    private final ClassifyService classifyService;
    private final ImageCharacteristicService imageCharacteristicService;


    @PostMapping("/storage")
    ResponseEntity<Boolean> fillStorage(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        storageService.fillStorage(file);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PostMapping("/storage/image")
    ResponseEntity<Boolean> fillImagesStorage(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        storageService.fillImagesCharacteristic(file);
        return null;
    }


    @PostMapping("/training/text")
    CompletableFuture<ResponseEntity<String>> textTraining() throws IOException {
        trainTextService.startClassification();
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok("Text training completed successfully"));
    }


    @PostMapping("/training/image")
    CompletableFuture<ResponseEntity<String>> imageTraining() throws IOException {
        int count = imageCharacteristicService.getCharacteristicsCount();
        CompletableFuture.runAsync(() -> {
            ImageDataset imageDataset = new ImageDataset(count);
            ImageModel imageModel = new ImageModel(imageDataset.getTrainDataSetIterator(),
                    imageDataset.getTestDataSetIterator(), count);
            imageModel.train();
            imageModel.test();
            try {
                imageModel.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok("Image training started successfully"));
    }

    @PostMapping("/classify/text")
    ResponseEntity<Map<Boolean,String>> classifyText(String text) throws IOException {
        File model = classifyService.getNetworkFile("text");
        return ResponseEntity.ok(classifyService.classifyText(text, model));
    }

    @PostMapping("/classify/image")
    ResponseEntity<Map<Boolean,String>> classifyImage(MultipartFile file) throws IOException {
        File model = classifyService.getNetworkFile("img");
        return ResponseEntity.ok(classifyService.classifyImage(file, model));
    }

    @PostMapping("/classify")
    ResponseEntity<Answer> classify(String text, MultipartFile[] files) throws IOException {

        return ResponseEntity.ok(classifyService.classify(text, files));


    }


}
