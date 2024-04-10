package kg.edu.krsu.vblindar.classifierapi.dto;


import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ClassifiableTextDto {

  private String text;
  private Map<CharacteristicDto, CharacteristicValueDto> characteristics;

  public CharacteristicValueDto getCharacteristicValue(CharacteristicDto characteristic) {
    return characteristics.get(characteristic);
  }
  public static ClassifiableText on(ClassifiableTextDto text){
    return ClassifiableText.builder()
            .text(text.getText())
            .build();
  }


}