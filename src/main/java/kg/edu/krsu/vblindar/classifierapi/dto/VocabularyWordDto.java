package kg.edu.krsu.vblindar.classifierapi.dto;


import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VocabularyWordDto {

  public static VocabularyWordDto from (VocabularyWord word){
    return VocabularyWordDto.builder()
            .id(word.getId())
            .value(word.getValue())
            .build();
  }
  public static VocabularyWord on(VocabularyWordDto word){
    return VocabularyWord.builder()
            .id(null)
            .value(word.value)
            .build();
  }
  private long id;
  private String value;



  @Override
  public boolean equals(Object o) {
    return ((o instanceof VocabularyWordDto) && (this.value.equals(((VocabularyWordDto) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}