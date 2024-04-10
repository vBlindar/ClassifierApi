package kg.edu.krsu.vblindar.classifierapi.dto;

import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CharacteristicValueDto {

  public static CharacteristicValueDto from(CharacteristicValue characteristicValue){
    return CharacteristicValueDto.builder()
            .id(characteristicValue.getId())
            .value(characteristicValue.getValue())
            .build();
  }

  private final String value;
  private long id;


  @Override
  public boolean equals(Object o) {
    return ((o instanceof CharacteristicValueDto) && (this.value.equals(((CharacteristicValueDto) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}