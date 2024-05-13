package kg.edu.krsu.vblindar.classifierapi.controller;


import kg.edu.krsu.vblindar.classifierapi.service.impl.StorageService;

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

@Controller
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageFillController {

    private final StorageService storageService;


    @PostMapping("/text")
    ResponseEntity<Boolean> fillTextsStorage(MultipartFile file) throws IOException {
        if(file==null)
            throw new IOException("File is null");
        storageService.dataClassification(file);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PostMapping("/image")
    ResponseEntity<Boolean> fillImagesStorage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        storageService.fillImagesCharacteristic(file);
        return null;
    }
}
