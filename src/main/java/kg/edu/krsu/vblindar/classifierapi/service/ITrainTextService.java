package kg.edu.krsu.vblindar.classifierapi.service;


import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;

import kg.edu.krsu.vblindar.classifierapi.textClassifier.DL4JClassifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public interface ITrainTextService {


    void trainAndSaveTextClassifier(List<ClassifiableText> classifiableTextForTrain,
                                 DL4JClassifier classifier) throws IOException;

    void startClassification() throws IOException;

    void checkTextClassifierAccuracy(
            List<ClassifiableText> classifiableTexts,
            DL4JClassifier classifier);

    DL4JClassifier createTextClassifier() throws IOException;
}
