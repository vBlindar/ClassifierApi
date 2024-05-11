package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristicMapping;
import kg.edu.krsu.vblindar.classifierapi.entity.embaddableId.TextCharacteristicMappingId;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicValueRepository;
import kg.edu.krsu.vblindar.classifierapi.repository.ClassifiableTextRepository;
import kg.edu.krsu.vblindar.classifierapi.repository.TextCharacteristicMappingRepository;
import kg.edu.krsu.vblindar.classifierapi.service.IClassifiableTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassifiableTextService implements IClassifiableTextService {
    private final CharacteristicValueRepository characteristicValueRepository;
    private final ClassifiableTextRepository classifiableTextRepository;
    private final CharacteristicRepository characteristicRepository;
    private final TextCharacteristicMappingRepository textCharacteristicMappingRepository;
    private final CharacteristicService characteristicService;
    private final CharacteristicValueService characteristicValueService;

    @Override
    public void saveClassifiableTextsToStorage(List<ClassifiableTextDto> classifiableTexts) {
        for (ClassifiableTextDto text : classifiableTexts) {
            try {
                addText(text);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    @Override
    public void addText(ClassifiableTextDto textDto) throws IllegalArgumentException {
        if (textDto == null ||
                textDto.getText().isEmpty() ||
                textDto.getCharacteristics() == null ||
                textDto.getCharacteristics().isEmpty()) {
            throw new IllegalArgumentException("Classifiable text is null or empty");
        }
        if (classifiableTextRepository.existsByText(textDto.getText())) {
            throw new IllegalArgumentException("Classifiable text is already exists");
        }


        var text = ClassifiableText.builder()
                .text(textDto.getText())
                .build();
        classifiableTextRepository.saveAndFlush(text);

        for (Map.Entry<CharacteristicDto, CharacteristicValueDto> entry :
                textDto.getCharacteristics().entrySet()) {
            insertToClassifiableTextsCharacteristicsTable(text, entry.getKey(),
                    entry.getValue());
        }


    }

    @Override
    public void insertToClassifiableTextsCharacteristicsTable(ClassifiableText classifiableText,
                                                              CharacteristicDto characteristicDto
            , CharacteristicValueDto characteristicValueDto) {

        var text = classifiableTextRepository.findById(classifiableText.getId()).orElse(null);
        var characteristic = characteristicRepository.findByName(characteristicDto.getName());
        var characteristicValue = characteristicValueRepository.findByValue(characteristicValueDto.getValue());

        var mappingId = TextCharacteristicMappingId.builder()
                .classifiableTextId(text)
                .characteristicId(characteristic)
                .characteristicValueId(characteristicValue)
                .build();

        var mapping = TextCharacteristicMapping.builder()
                .id(mappingId)
                .build();

        textCharacteristicMappingRepository.saveAndFlush(mapping);

    }

    @Override
    public List<ClassifiableTextDto> getAllTexts() {
        var texts = classifiableTextRepository.findAll();
        var characteristics = characteristicService.getAllCharacteristics();
        return converting(texts, characteristics);
    }

    @Override
    public List<ClassifiableTextDto> converting(List<ClassifiableText> texts, List<CharacteristicDto> characteristics) {
        List<ClassifiableTextDto> dtos = new ArrayList<>();
        for (ClassifiableText text : texts) {
            Map<CharacteristicDto, CharacteristicValueDto> map = new HashMap<>();
            for (TextCharacteristicMapping id : text.getTextCharacteristicMappings()) {
                var characteristic = characteristicService.findById(id.getId().getCharacteristicId().getId());
                var characteristicValue =
                        characteristicValueService.findById(id.getId().getCharacteristicValueId().getId());
                map.put(characteristic, characteristicValue);
            }
            dtos.add(ClassifiableTextDto.builder()
                    .text(text.getText())
                    .characteristics(map)
                    .build());
        }

        return dtos;
    }
    @Override
    public Map<String, Map<String, List<ClassifiableTextDto>>> splitTextsForTrainingAndTesting() {
        var texts = getAllTexts();
        // Группировка текстов по каждому значению каждой характеристики
        Map<String, Map<String, List<ClassifiableTextDto>>> groupedByCharacteristicValue = new HashMap<>();

        // Инициализация мапы для тренировочных и тестовых данных
        Map<String, Map<String, List<ClassifiableTextDto>>> splitMap = new HashMap<>();
        splitMap.put("training", new HashMap<>());
        splitMap.put("testing", new HashMap<>());

        // Перебираем тексты и группируем их по значениям характеристик
        for (ClassifiableTextDto text : texts) {
            for (Map.Entry<CharacteristicDto, CharacteristicValueDto> entry : text.getCharacteristics().entrySet()) {
                String charValue = entry.getValue().getValue();
                String charName = entry.getKey().getName();

                if (!groupedByCharacteristicValue.containsKey(charName)) {
                    groupedByCharacteristicValue.put(charName, new HashMap<>());
                }
                groupedByCharacteristicValue.get(charName).computeIfAbsent(charValue, k -> new ArrayList<>()).add(text);
            }
        }

        // Разделение каждой группы на обучающую и тестовую выборки
        for (Map.Entry<String, Map<String, List<ClassifiableTextDto>>> entry : groupedByCharacteristicValue.entrySet()) {
            String charName = entry.getKey();
            Map<String, List<ClassifiableTextDto>> valueGroups = entry.getValue();

            for (Map.Entry<String, List<ClassifiableTextDto>> groupEntry : valueGroups.entrySet()) {
                List<ClassifiableTextDto> groupTexts = groupEntry.getValue();
                int splitIndex = (int) (groupTexts.size() * 0.8); // 65% для обучения

                List<ClassifiableTextDto> trainingTexts = new ArrayList<>(groupTexts.subList(0, splitIndex));
                List<ClassifiableTextDto> testingTexts = new ArrayList<>(groupTexts.subList(splitIndex, groupTexts.size()));

                if (!splitMap.get("training").containsKey(charName)) {
                    splitMap.get("training").put(charName, new ArrayList<>());
                    splitMap.get("testing").put(charName, new ArrayList<>());
                }
                splitMap.get("training").get(charName).addAll(trainingTexts);
                splitMap.get("testing").get(charName).addAll(testingTexts);
            }
        }

        return splitMap;
    }

    @Override
    public List<ClassifiableTextDto> collectAndShuffleTexts(Map<String, Map<String, List<ClassifiableTextDto>>> splitMap, String key) {
        // Собираем все тексты из подмап по ключу 'training' или 'testing'
        List<ClassifiableTextDto> collectedTexts = splitMap.get(key).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Перемешиваем собранные тексты
        Collections.shuffle(collectedTexts);
        return collectedTexts;
    }
}
