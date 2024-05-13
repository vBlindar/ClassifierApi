package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
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
    public void saveClassifiableTextsToStorage(List<ClassifiableText> classifiableTexts) {
        for (ClassifiableText text : classifiableTexts) {
            try {
                addText(text);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    @Override
    public void addText(ClassifiableText text) throws IllegalArgumentException {
        if (text == null ||
                text.getText().isEmpty() ||
                text.getCharacteristics() == null ||
                text.getCharacteristics().isEmpty()) {
            throw new IllegalArgumentException("Classifiable text is null or empty");
        }
        if (classifiableTextRepository.existsByText(text.getText())) {
            throw new IllegalArgumentException("Classifiable text is already exists");
        }


        classifiableTextRepository.saveAndFlush(text);

        for (Map.Entry<Characteristic, CharacteristicValue> entry :
                text.getCharacteristics().entrySet()) {
            insertToClassifiableTextsCharacteristicsTable(text, entry.getKey(),
                    entry.getValue());
        }


    }

    @Override
    public void insertToClassifiableTextsCharacteristicsTable(ClassifiableText classifiableText,
                                                              Characteristic characteristic
            , CharacteristicValue characteristicValue) {

        var text = classifiableTextRepository.findById(classifiableText.getId()).orElse(null);
        var newCharacteristic = characteristicRepository.findByName(characteristic.getName());
        var newCharacteristicValue = characteristicValueRepository.findByValue(characteristicValue.getValue());

        var mappingId = TextCharacteristicMappingId.builder()
                .classifiableTextId(text)
                .characteristicId(newCharacteristic)
                .characteristicValueId(newCharacteristicValue)
                .build();

        var mapping = TextCharacteristicMapping.builder()
                .id(mappingId)
                .build();

        textCharacteristicMappingRepository.saveAndFlush(mapping);

    }

    @Override
    public List<ClassifiableText> getAllTexts() {
        var texts = classifiableTextRepository.findAll();
        var characteristics = characteristicService.getAllCharacteristics();
        return converting(texts, characteristics);
    }

    @Override
    public List<ClassifiableText> converting(List<ClassifiableText> texts, List<Characteristic> characteristics) {
        List<ClassifiableText> s = new ArrayList<>();
        for (ClassifiableText text : texts) {
            Map<Characteristic, CharacteristicValue> map = new HashMap<>();
            for (TextCharacteristicMapping id : text.getTextCharacteristicMappings()) {
                var characteristic = characteristicService.findById(id.getId().getCharacteristicId().getId());
                var characteristicValue =
                        characteristicValueService.findById(id.getId().getCharacteristicValueId().getId());
                map.put(characteristic, characteristicValue);
            }
            s.add(ClassifiableText.builder()
                    .text(text.getText())
                    .characteristics(map)
                    .build());
        }

        return s;
    }
    @Override
    public Map<String, Map<String, List<ClassifiableText>>> splitTextsForTrainingAndTesting() {
        var texts = getAllTexts();
        // Группировка текстов по каждому значению каждой характеристики
        Map<String, Map<String, List<ClassifiableText>>> groupedByCharacteristicValue = new HashMap<>();

        // Инициализация мапы для тренировочных и тестовых данных
        Map<String, Map<String, List<ClassifiableText>>> splitMap = new HashMap<>();
        splitMap.put("training", new HashMap<>());
        splitMap.put("testing", new HashMap<>());

        // Перебираем тексты и группируем их по значениям характеристик
        for (ClassifiableText text : texts) {
            for (Map.Entry<Characteristic, CharacteristicValue> entry : text.getCharacteristics().entrySet()) {
                String charValue = entry.getValue().getValue();
                String charName = entry.getKey().getName();

                if (!groupedByCharacteristicValue.containsKey(charName)) {
                    groupedByCharacteristicValue.put(charName, new HashMap<>());
                }
                groupedByCharacteristicValue.get(charName).computeIfAbsent(charValue, k -> new ArrayList<>()).add(text);
            }
        }

        // Разделение каждой группы на обучающую и тестовую выборки
        for (Map.Entry<String, Map<String, List<ClassifiableText>>> entry : groupedByCharacteristicValue.entrySet()) {
            String charName = entry.getKey();
            Map<String, List<ClassifiableText>> valueGroups = entry.getValue();

            for (Map.Entry<String, List<ClassifiableText>> groupEntry : valueGroups.entrySet()) {
                List<ClassifiableText> groupTexts = groupEntry.getValue();
                int splitIndex = (int) (groupTexts.size() * 0.8); // 65% для обучения

                List<ClassifiableText> trainingTexts = new ArrayList<>(groupTexts.subList(0, splitIndex));
                List<ClassifiableText> testingTexts = new ArrayList<>(groupTexts.subList(splitIndex, groupTexts.size()));

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
    public List<ClassifiableText> collectAndShuffleTexts(Map<String, Map<String, List<ClassifiableText>>> splitMap, String key) {
        // Собираем все тексты из подмап по ключу 'training' или 'testing'
        List<ClassifiableText> collectedTexts = splitMap.get(key).values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Перемешиваем собранные тексты
        Collections.shuffle(collectedTexts);
        return collectedTexts;
    }
}
