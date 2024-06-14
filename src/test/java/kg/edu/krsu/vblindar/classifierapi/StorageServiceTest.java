package kg.edu.krsu.vblindar.classifierapi;

import kg.edu.krsu.vblindar.classifierapi.service.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private VocabularyService vocabularyService;

    @Mock
    private TextCharacteristicService characteristicValueService;

    @Mock
    private ClassifiableTextService classifiableTextService;

    @Mock
    private ImageCharacteristicService imageCharacteristicService;

    @InjectMocks
    private StorageService storageService;

    private File tempDir;
    private File imagesDir;
    private File textsDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("storage_test").toFile();
        imagesDir = new File(tempDir, "images");
        textsDir = new File(tempDir, "texts");

        imagesDir.mkdir();
        textsDir.mkdir();


        Files.createFile(new File(imagesDir, "image1.jpg").toPath());
        Files.createFile(new File(textsDir, "text1.txt").toPath());
    }

    @Test
    void testFillStorageSuccessful() {
        assertDoesNotThrow(() -> storageService.fillStorage(tempDir));

        verify(classifiableTextService, times(1)).saveClassifiableTextsToStorage(anyList());
        verify(imageCharacteristicService, times(1)).saveAllCharacteristics(any(File[].class));
    }

    @Test
    void testFillStorageMissingDirectories() {
        assertThrows(IllegalArgumentException.class, () -> storageService.fillStorage(new File("invalid_path")));
    }

    @Test
    void testFillStorageInvalidImageFormat() throws IOException {
        Files.createFile(new File(imagesDir, "image1.gif").toPath());
        assertThrows(IllegalArgumentException.class, () -> storageService.fillStorage(tempDir));
    }

    @Test
    void testFillStorageInvalidTextFormat() throws IOException {
        Files.createFile(new File(textsDir, "text1.doc").toPath());
        assertThrows(IllegalArgumentException.class, () -> storageService.fillStorage(tempDir));
    }


}

