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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return converting(texts,characteristics);
    }

    @Override
    public List<ClassifiableTextDto> converting(List<ClassifiableText> texts, List<CharacteristicDto> characteristics){
        List<ClassifiableTextDto> dtos = new ArrayList<>();
        for (ClassifiableText text : texts) {
            Map<CharacteristicDto, CharacteristicValueDto> map = new HashMap<>();
            for(TextCharacteristicMapping id : text.getTextCharacteristicMappings()){
                var characteristic = characteristicService.findById(id.getId().getCharacteristicId().getId());
                var characteristicValue =
                        characteristicValueService.findById(id.getId().getCharacteristicValueId().getId());
                map.put(characteristic,characteristicValue);
            }
            dtos.add(ClassifiableTextDto.builder()
                    .text(text.getText())
                    .characteristics(map)
                    .build());
        }

        return dtos;
    }
}
