package kg.edu.krsu.vblindar.classifierapi.controller;

import kg.edu.krsu.vblindar.classifierapi.service.impl.ClassifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/classify")
public class ClassifyController {

    private final ClassifyService classifyService;

    @PostMapping("/text")
    ResponseEntity<String> classifyText(String text) throws IOException {
        var model = classifyService.getNetworkFile("text");
        if(model==null)
            throw new IllegalArgumentException("The text classification model is not trained");
        var response = classifyService.classifyText2(text,model);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image")
    ResponseEntity<String> classifyImage(MultipartFile file) throws IOException{
        var model = classifyService.getNetworkFile("img");
        if(model==null)
            throw new IllegalArgumentException("The text classification model is not trained");

        return ResponseEntity.ok(classifyService.classifyImage(file,model));
    }

}
