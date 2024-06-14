package kg.edu.krsu.vblindar.classifierapi.controller;


import io.swagger.annotations.ApiParam;
import kg.edu.krsu.vblindar.classifierapi.entity.dto.Ad;
import kg.edu.krsu.vblindar.classifierapi.entity.dto.Answer;
import kg.edu.krsu.vblindar.classifierapi.entity.dto.ApiResponse;
import kg.edu.krsu.vblindar.classifierapi.imageClassify.ImageModel;
import kg.edu.krsu.vblindar.classifierapi.imageClassify.dataset.ImageDataset;
import kg.edu.krsu.vblindar.classifierapi.service.impl.ClassifyService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.ImageCharacteristicService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.StorageService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.TrainTextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/classifier")
@RequiredArgsConstructor
@Slf4j
public class ApiController {

    private final StorageService storageService;
    private final TrainTextService trainTextService;
    private final ClassifyService classifyService;
    private final ImageCharacteristicService imageCharacteristicService;


    @PostMapping("/dataset-upload")
    ResponseEntity<ApiResponse> fillStorage(
            @ApiParam(value = "Dataset file", required = true) @RequestPart MultipartFile dataset
    ) throws IOException {
        File file = classifyService.convertMultipartFileToFile(dataset);
        if (!file.exists()) {
            throw new IOException("File not found");
        }
     storageService.fillStorage(file);
        return ResponseEntity.ok(new ApiResponse(Boolean.TRUE, "dataset loaded successfully"));
    }


    @PostMapping("/training/text")
    CompletableFuture<ResponseEntity<ApiResponse>> textTraining() throws IOException {
        trainTextService.startClassification();
        trainTextService.test();
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(new ApiResponse(Boolean.TRUE, "Text training " +
                "completed " +
                "successfully")));
    }


    @PostMapping("/training/image")
    CompletableFuture<ResponseEntity<ApiResponse>> imageTraining() throws IOException {
        int count = imageCharacteristicService.getCharacteristicsCount();
        ImageDataset imageDataset = new ImageDataset(count);
        ImageModel imageModel = new ImageModel(imageDataset.getTrainDataSetIterator(),imageDataset.getTestDataSetIterator(),
                classifyService.getNetworkFile("img"));
        imageModel.test();
//        CompletableFuture.runAsync(() -> {
//            ImageDataset imageDataset = new ImageDataset(count);
//            ImageModel imageModel = new ImageModel(imageDataset.getTrainDataSetIterator(),
//                    imageDataset.getTestDataSetIterator(), count);
//            imageModel.train();
//            imageModel.test();
//            try {
//                imageModel.save();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(new ApiResponse(Boolean.TRUE, "Image training " +
                "started " +
                "successfully")));
    }

    @PostMapping("/training")
    CompletableFuture<ResponseEntity<ApiResponse>> training() throws IOException {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.ok(new ApiResponse(Boolean.TRUE, "Training " +
                "completed " +
                "successfully")));
    }


    @PostMapping("/classify")
    ResponseEntity<Answer> classify(@RequestBody Ad ad) throws IOException {
        var response = classifyService.classify(ad);
        log.info(ad.toString());
        log.info(response.toString());
        return ResponseEntity.ok(response);


    }


}

