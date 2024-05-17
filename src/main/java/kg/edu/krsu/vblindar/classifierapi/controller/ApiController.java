package kg.edu.krsu.vblindar.classifierapi.controller;


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
    ResponseEntity<Boolean> fillImagesStorage(String filePath){
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
        return CompletableFuture.supplyAsync(()->ResponseEntity.ok("Text training completed successfully"));
    }

    @PostMapping("/training/image")
    CompletableFuture<ResponseEntity<String>>  imageTraining() throws IOException {
        int count = imageCharacteristicService.getCharacteristicsCount();
        System.out.println("privet");
        ImageDataset imageDataset = new ImageDataset(count);
        ImageModel imageModel = new ImageModel(imageDataset.getTrainDataSetIterator(),
                imageDataset.getTestDataSetIterator(), count);
        imageModel.train();
        imageModel.test();
        imageModel.save();
        return CompletableFuture.supplyAsync(()->ResponseEntity.ok("Image training completed successfully"));
    }

    @PostMapping("/classify/text")
    ResponseEntity<String> classifyText(String text) throws IOException {
        File model = classifyService.getNetworkFile("text");
        if(model==null)
            throw new IOException("The text classification model is not trained");
        String response = classifyService.classifyText(text,model);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/classify/image")
    ResponseEntity<String> classifyImage(MultipartFile file) throws IOException{
        File model = classifyService.getNetworkFile("img");
        if(model==null)
            throw new IllegalArgumentException("The text classification model is not trained");
        return ResponseEntity.ok(classifyService.classifyImage(file,model));
    }


}
