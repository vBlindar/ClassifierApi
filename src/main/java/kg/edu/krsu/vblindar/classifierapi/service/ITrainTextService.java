package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;

import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public interface ITrainTextService {


    void trainAndSaveClassifiers(List<ClassifiableText> classifiableTextForTrain,
                                 List<DL4JClassifier> classifiers) throws IOException;

    void startClassification() throws IOException;

    void checkClassifiersAccuracy(
            List<ClassifiableText> classifiableTexts,
            List<DL4JClassifier> classifiers);

    List<DL4JClassifier> createClassifiers() throws IOException;
}
