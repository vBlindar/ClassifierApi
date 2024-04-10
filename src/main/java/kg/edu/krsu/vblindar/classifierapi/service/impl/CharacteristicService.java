package kg.edu.krsu.vblindar.classifierapi.service.impl;

import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicDto;
import kg.edu.krsu.vblindar.classifierapi.dto.CharacteristicValueDto;
import kg.edu.krsu.vblindar.classifierapi.dto.ClassifiableTextDto;
import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicRepository;
import kg.edu.krsu.vblindar.classifierapi.repository.CharacteristicValueRepository;
import kg.edu.krsu.vblindar.classifierapi.service.ICharacteristicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.websocket.OnClose;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CharacteristicService implements ICharacteristicService {
    private final CharacteristicRepository characteristicRepository;
    private final CharacteristicValueService characteristicValueService;
    @Override
    public void saveCharacteristicsToStorage(List<ClassifiableTextDto> classifiableTexts) {
        Set<CharacteristicDto> characteristics = getCharacteristicsCatalog(classifiableTexts);


        for (CharacteristicDto characteristic : characteristics) {
            try {
                addNewCharacteristic(characteristic);
            } catch (IllegalArgumentException e) {
                System.out.println("Empty of Already");
            }
        }

    }

    @Override
    public List<CharacteristicDto> getAllCharacteristics() {

        var characteristics = characteristicRepository.findAll();
        for (Characteristic characteristic : characteristics) {
            characteristic.setPossibleValues(characteristicValueService.findValuesByCharacteristicId(characteristic.getId()));
        }

        return characteristics.stream().map(CharacteristicDto::from).toList();
    }

    @Override
    public Set<CharacteristicDto> getCharacteristicsCatalog(List<ClassifiableTextDto> classifiableTexts) {
        Set<CharacteristicDto> characteristics = new LinkedHashSet<>();

        for (ClassifiableTextDto classifiableText : classifiableTexts) {
            for (Map.Entry<CharacteristicDto, CharacteristicValueDto> entry :
                    classifiableText.getCharacteristics().entrySet()) {
                characteristics.add(entry.getKey());
            }
        }

        return characteristics;
    }

    @Override
    public void addNewCharacteristic(CharacteristicDto characteristicDto) throws IllegalArgumentException {
        if (characteristicDto == null ||
                characteristicDto.getName().isEmpty() ||
                characteristicDto.getPossibleValues() == null ||
                characteristicDto.getPossibleValues().isEmpty()) {
            throw new IllegalArgumentException("Characteristic and/or Possible values are null or empty");
        }

        if (characteristicRepository.existsByName(characteristicDto.getName())) {
            throw new IllegalArgumentException("Characteristic already exists");
        }


        var characteristic = Characteristic.builder()
                .name(characteristicDto.getName())
                .build();
        characteristicRepository.saveAndFlush(characteristic);

        for (CharacteristicValueDto possibleValue : characteristicDto.getPossibleValues()) {
            characteristicValueService.insertPossibleValue(characteristic, possibleValue);
        }


    }


    @Override
    public CharacteristicDto findById(Long id) {
        var characteristic = characteristicRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        return CharacteristicDto.from(characteristic);
    }
}
