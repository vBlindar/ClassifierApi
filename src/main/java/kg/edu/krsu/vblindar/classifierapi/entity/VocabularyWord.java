package kg.edu.krsu.vblindar.classifierapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "vocabulary")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class VocabularyWord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(length = 5000)
  private final String value;


  @Override
  public boolean equals(Object o) {
    return ((o instanceof VocabularyWord) && (this.value.equals(((VocabularyWord) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}