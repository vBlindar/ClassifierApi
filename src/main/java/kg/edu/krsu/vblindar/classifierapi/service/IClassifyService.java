package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public interface IClassifyService {
    String classifyText(String text,File file);

    List<Classifier> createClassifiers(File file);
    File getNetworkFile(String type);

    String classifyImage(MultipartFile file, File model) throws IOException;
}
