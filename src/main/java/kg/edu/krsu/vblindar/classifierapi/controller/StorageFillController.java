package kg.edu.krsu.vblindar.classifierapi.controller;

import kg.edu.krsu.vblindar.classifierapi.service.impl.TextStorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageFillController {

    private final TextStorageService textStorageService;

    @PostMapping("/text")
    ResponseEntity<Boolean> textTraining(MultipartFile file) throws IOException {
        if(file==null)
            throw new IOException("File is null");
        textStorageService.dataClassification(file);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
