package kg.edu.krsu.vblindar.classifierapi.controller;

import kg.edu.krsu.vblindar.classifierapi.imageClassify.ImageModel;
import kg.edu.krsu.vblindar.classifierapi.imageClassify.dataset.ImageDataset;
import kg.edu.krsu.vblindar.classifierapi.service.impl.ImageCharacteristicService;
import kg.edu.krsu.vblindar.classifierapi.service.impl.TrainTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.io.IOException;

@Controller
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainTextService trainTextService;

    private final ImageCharacteristicService imageCharacteristicService;

    @PostMapping("/text")
    ResponseEntity<Boolean> textTraining() throws IOException {
        trainTextService.startClassification2();
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PostMapping("/image")
    ResponseEntity<Boolean> imageTraining() throws IOException {

        int count = imageCharacteristicService.getCharacteristicsCount();
        ImageDataset imageDataset = new ImageDataset(count);
        ImageModel imageModel = new ImageModel(imageDataset.getTrainDataSetIterator(),
                imageDataset.getTestDataSetIterator(), count);
       // imageModel.train();
        //imageModel.load();
        imageModel.test();
        //imageModel.save();
        return null;
    }


}
