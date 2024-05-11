package kg.edu.krsu.vblindar.classifierapi.controller;

import kg.edu.krsu.vblindar.classifierapi.service.impl.TrainTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainTextService trainTextService;

    @PostMapping("/text")
    ResponseEntity<Boolean> textTraining() throws IOException {
        trainTextService.startClassification2();
        return ResponseEntity.ok(Boolean.TRUE);
    }


}
