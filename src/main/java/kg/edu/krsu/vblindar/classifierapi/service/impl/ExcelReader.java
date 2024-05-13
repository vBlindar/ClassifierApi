package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.service.IExcelReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class ExcelReader implements IExcelReader {

    @Override
    public List<ClassifiableText> xlsxToClassifiableTexts(File xlsxFile, int sheetNumber) throws IOException {
        if (xlsxFile == null ||
                sheetNumber < 1) {
            throw new IllegalArgumentException("Excel file is incorrect");
        }

        try (XSSFWorkbook excelFile = new XSSFWorkbook(new FileInputStream(xlsxFile))) {
            XSSFSheet sheet = excelFile.getSheetAt(sheetNumber - 1);

            if (sheet.getLastRowNum() > 0) {
                return getClassifiableTexts(sheet);
            } else {
                throw new IllegalArgumentException("Excel sheet (#" + sheetNumber + ") is empty");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Excel sheet (#" + sheetNumber + ") is empty");
        }
    }

    @Override
    public List<ClassifiableText> getClassifiableTexts(XSSFSheet sheet) {
        List<Characteristic> characteristics = getCharacteristics(sheet);
        List<ClassifiableText> classifiableTexts = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Map<Characteristic, CharacteristicValue> characteristicsValues =
                    getCharacteristicsValues(sheet.getRow(i),
                            characteristics);

            if (!sheet.getRow(i).getCell(0).getStringCellValue().isEmpty()) {
                classifiableTexts.add(ClassifiableText.builder()
//                        .text(readTextFromFile(sheet.getRow(i).getCell(0).getStringCellValue()))
                        .text(sheet.getRow(i).getCell(0).getStringCellValue())
                        .characteristics(characteristicsValues)
                        .build());
            }
        }

        return classifiableTexts;
    }

    @Override
    public Map<Characteristic, CharacteristicValue> getCharacteristicsValues(Row row, List<Characteristic> characteristics) {
            Map<Characteristic, CharacteristicValue> characteristicsValues = new HashMap<>();

            for (int i = 1; i < row.getLastCellNum(); i++) {
                characteristicsValues.put(characteristics.get(i - 1), CharacteristicValue.builder()
                        .value(row.getCell(i).getStringCellValue())
                        .build());

            }

            return characteristicsValues;
    }

    @Override
    public List<Characteristic> getCharacteristics(XSSFSheet sheet) {
        List<Characteristic> characteristics = new ArrayList<>();

        for (int i = 1; i < sheet.getRow(0).getLastCellNum(); i++) {
            characteristics.add(Characteristic.builder()
                    .name(sheet.getRow(0).getCell(i).getStringCellValue())
                    .possibleValues(getPossibleValues(sheet, i))
                    .build());
        }
        return characteristics;
    }

    @Override
    public List<CharacteristicValue> getPossibleValues(XSSFSheet sheet, int index) {
        Set<CharacteristicValue> characteristicValues = new LinkedHashSet<>();
        for (int i = 1; i <=sheet.getLastRowNum(); i++) {
            Cell cell = sheet.getRow(i).getCell(index);
            if (cell != null)
                characteristicValues.add(CharacteristicValue.builder()
                        .value(cell.getStringCellValue())
                        .build());
            else{
                throw new IllegalArgumentException("Excel sheet (#" + index + ") is empty");
            }
        }
        return characteristicValues.stream().toList();
    }

    @Override
    public String readTextFromFile(String path){
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ( (line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
