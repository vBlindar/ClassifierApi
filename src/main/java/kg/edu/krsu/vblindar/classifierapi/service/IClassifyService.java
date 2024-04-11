package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public interface IClassifyService {
    String classifyText(String text,File file);

    List<Classifier> createClassifiers(File file);
    File getNetworkFile(String type);

}
