package kg.edu.krsu.vblindar.classifierapi.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "texts_characteristics")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@ToString(of = {"value"})
public class TextCharacteristic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 5000)
  private final String value;

  @OneToMany(mappedBy = "characteristic", cascade = CascadeType.ALL)
  private List<ClassifiableText> texts;


}