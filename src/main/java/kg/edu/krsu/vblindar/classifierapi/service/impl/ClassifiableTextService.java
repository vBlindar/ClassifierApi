package kg.edu.krsu.vblindar.classifierapi.service.impl;


import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;

import kg.edu.krsu.vblindar.classifierapi.repository.ClassifiableTextRepository;

import kg.edu.krsu.vblindar.classifierapi.service.IClassifiableTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassifiableTextService implements IClassifiableTextService {
    private final ClassifiableTextRepository classifiableTextRepository;


    @Override
    public void saveClassifiableTextsToStorage(List<ClassifiableText> classifiableTexts) {
        for (ClassifiableText classifiableText : classifiableTexts) {
            classifiableTextRepository.save(classifiableText);
        }

    }

    @Override
    public List<ClassifiableText> getAllTexts() {
        List<ClassifiableText> texts = classifiableTextRepository.findAll();

        return texts;
    }


    @Override
    public Map<String, List<ClassifiableText>> splitTextsForTrainingAndTesting() {
        List<ClassifiableText> texts = getAllTexts();

        Map<String, List<ClassifiableText>> groupedByCharacteristic = texts.stream()
                .collect(Collectors.groupingBy(text -> text.getCharacteristic().getValue()));

        Map<String, List<ClassifiableText>> splitMap = new HashMap<>();
        splitMap.put("training", new ArrayList<>());
        splitMap.put("testing", new ArrayList<>());

        groupedByCharacteristic.forEach((characteristicValue, groupTexts) -> {
            Collections.shuffle(groupTexts);
            int splitPoint = (int) (groupTexts.size() * 0.8);

            splitMap.get("training").addAll(groupTexts.subList(0, splitPoint));
            splitMap.get("testing").addAll(groupTexts.subList(splitPoint, groupTexts.size()));
        });

        return splitMap;
    }

    @Override
    public List<ClassifiableText> collectAndShuffleTexts(Map<String, List<ClassifiableText>> splitMap, String key) {
        List<ClassifiableText> texts = new ArrayList<>();

        if (splitMap.containsKey(key)) {
            texts = splitMap.get(key);
        }

        Collections.shuffle(texts);

        return texts;
    }
}
