package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.service.IExcelReader;
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
public class ExcelReader implements IExcelReader {

    @Override
    public List<ClassifiableTextDto> xlsxToClassifiableTexts(File xlsxFile, int sheetNumber) throws IOException {
        if (xlsxFile == null ||
                sheetNumber < 1) {
            throw new IllegalArgumentException("Excel file is incorrect");
        }

        try (XSSFWorkbook excelFile = new XSSFWorkbook(new FileInputStream(xlsxFile))) {
            XSSFSheet sheet = excelFile.getSheetAt(sheetNumber - 1);

            if (sheet.getLastRowNum() > 0) {
                return getClassifiableTextsDto(sheet);
            } else {
                throw new IllegalArgumentException("Excel sheet (#" + sheetNumber + ") is empty");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Excel sheet (#" + sheetNumber + ") is empty");
        }
    }

    @Override
    public List<ClassifiableTextDto> getClassifiableTextsDto(XSSFSheet sheet) {
        List<CharacteristicDto> characteristics = getCharacteristicsDto(sheet);
        List<ClassifiableTextDto> classifiableTexts = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Map<CharacteristicDto, CharacteristicValueDto> characteristicsValues =
                    getCharacteristicsValuesDto(sheet.getRow(i),
                            characteristics);

            if (!sheet.getRow(i).getCell(0).getStringCellValue().isEmpty()) {
                classifiableTexts.add(ClassifiableTextDto.builder()
                        .text(sheet.getRow(i).getCell(0).getStringCellValue())
                        .characteristics(characteristicsValues)
                        .build());
            }
        }

        return classifiableTexts;
    }

    @Override
    public Map<CharacteristicDto, CharacteristicValueDto> getCharacteristicsValuesDto(Row row, List<CharacteristicDto> characteristics) {
            Map<CharacteristicDto, CharacteristicValueDto> characteristicsValues = new HashMap<>();

            for (int i = 1; i < row.getLastCellNum(); i++) {
                characteristicsValues.put(characteristics.get(i - 1), CharacteristicValueDto.builder()
                        .value(row.getCell(i).getStringCellValue())
                        .build());

            }

            return characteristicsValues;
    }

    @Override
    public List<CharacteristicDto> getCharacteristicsDto(XSSFSheet sheet) {
        List<CharacteristicDto> characteristics = new ArrayList<>();

        for (int i = 1; i < sheet.getRow(0).getLastCellNum(); i++) {
            characteristics.add(CharacteristicDto.builder()
                    .name(sheet.getRow(0).getCell(i).getStringCellValue())
                    .possibleValues(getPossibleValues(sheet, i))
                    .build());
        }
        return characteristics;
    }

    @Override
    public Set<CharacteristicValueDto> getPossibleValues(XSSFSheet sheet, int index) {
        Set<CharacteristicValueDto> characteristicValues = new LinkedHashSet<>();
        for (int i = 1; i <=sheet.getLastRowNum(); i++) {
            Cell cell = sheet.getRow(i).getCell(index);
            if (cell != null)
                characteristicValues.add(CharacteristicValueDto.builder()
                        .value(cell.getStringCellValue())
                        .build());
            else{
                throw new IllegalArgumentException("Excel sheet (#" + index + ") is empty");
            }
        }
        return characteristicValues;
    }
}
