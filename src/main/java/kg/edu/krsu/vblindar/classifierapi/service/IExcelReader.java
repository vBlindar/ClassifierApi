package kg.edu.krsu.vblindar.classifierapi.service;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
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

    List<ClassifiableTextDto> xlsxToClassifiableTexts(File xlsxFile, int sheetNumber) throws IOException;

    List<ClassifiableTextDto> getClassifiableTextsDto(XSSFSheet sheet);

    Map<CharacteristicDto, CharacteristicValueDto> getCharacteristicsValuesDto(Row row, List<CharacteristicDto> characteristics);

    List<CharacteristicDto> getCharacteristicsDto(XSSFSheet sheet);

    Set<CharacteristicValueDto> getPossibleValues(XSSFSheet sheet, int index);

    String readTextFromFile(String path);
}
