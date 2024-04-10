package kg.edu.krsu.vblindar.classifierapi.dto;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;


@Data
@Builder
public class CharacteristicDto {

  public static CharacteristicDto from(Characteristic characteristic){
    return CharacteristicDto.builder()
            .id(characteristic.getId())
            .name(characteristic.getName())
            .possibleValues(characteristic.getPossibleValues().stream().map(CharacteristicValueDto::from).collect(Collectors.toSet()))
            .build();
  }


  private final String name;
  private long id;
  private Set<CharacteristicValueDto> possibleValues;


  public void addPossibleValue(CharacteristicValueDto value) {
    possibleValues.add(value);
  }

  @Override
  public boolean equals(Object o) {
    return ((o instanceof CharacteristicDto) && (this.name.equals(((CharacteristicDto) o).getName())));
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}