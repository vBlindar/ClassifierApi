package kg.edu.krsu.vblindar.classifierapi;

import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class ClassifierApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassifierApiApplication.class, args);
    }

}
