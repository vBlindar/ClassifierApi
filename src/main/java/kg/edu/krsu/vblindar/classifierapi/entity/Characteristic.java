package kg.edu.krsu.vblindar.classifierapi.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "characteristics")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@ToString(of = {"name"})
public class Characteristic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(length = 5000)
  private final String name;

  @OneToMany(mappedBy = "characteristic", cascade = CascadeType.ALL)
  private List<CharacteristicValue> possibleValues;

  @Override
  public boolean equals(Object o) {
    return ((o instanceof Characteristic) && (this.name.equals(((Characteristic) o).getName())));
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}