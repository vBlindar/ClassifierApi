package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.dto.Answer;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public interface IClassifyService {

    Map<Boolean, String> classifyText(String text, File file) throws IOException;

    List<DL4JClassifier> createClassifiers(File file) throws IOException;

    File getNetworkFile(String type) throws IOException;

    Map<Boolean, String> classifyImage(MultipartFile file, File model) throws IOException;

    File convertMultipartFileToFile(MultipartFile file) throws IOException;

    Answer classify(String text, MultipartFile[] files) throws IOException;
}
