package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
public interface IExcelReader {

    List<ClassifiableText> xlsxToClassifiableTexts(File xlsxFile, int sheetNumber) throws IOException;

    List<ClassifiableText> getClassifiableTexts(XSSFSheet sheet);

    Map<Characteristic, CharacteristicValue> getCharacteristicsValues(Row row, List<Characteristic> characteristics);

    List<Characteristic> getCharacteristics(XSSFSheet sheet);

    List<CharacteristicValue> getPossibleValues(XSSFSheet sheet, int index);

    String readTextFromFile(String path);
}
