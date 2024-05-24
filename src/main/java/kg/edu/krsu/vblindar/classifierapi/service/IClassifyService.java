package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public interface IClassifyService {

    String classifyText(String text, File file) throws IOException;

    List<DL4JClassifier> createClassifiers(File file) throws IOException;

    File getNetworkFile(String type) throws IOException;

    String classifyImage(MultipartFile file, File model) throws IOException;

    File convertMultipartFileToFile(MultipartFile file) throws IOException;

    String classify(String text, MultipartFile[] files) throws IOException;
}
