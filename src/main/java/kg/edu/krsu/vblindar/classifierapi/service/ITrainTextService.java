package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.textClassifier.Classifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public interface ITrainTextService {

    List<Classifier> createClassifiers();
    void startClassification();
    void trainAndSaveClassifiers(List<ClassifiableTextDto> classifiableTextForTrain,
                                 List<Classifier> classifiers);

    void checkClassifiersAccuracy(
            List<ClassifiableTextDto> classifiableTexts,
            List<Classifier> classifiers);

}
