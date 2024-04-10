package kg.edu.krsu.vblindar.classifierapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "classifiable_texts")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ClassifiableText {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 5000)
  private final String text;

  @OneToMany(mappedBy = "id.classifiableTextId", cascade = CascadeType.ALL)
  private List<TextCharacteristicMapping> textCharacteristicMappings;


}