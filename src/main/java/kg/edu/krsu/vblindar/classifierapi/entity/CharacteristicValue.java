package kg.edu.krsu.vblindar.classifierapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "characteristic_value")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CharacteristicValue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 5000)
  private final String value;

  @ManyToOne
  @JoinColumn(name = "characteristic_id")
  private Characteristic characteristic;

  @Override
  public boolean equals(Object o) {
    return ((o instanceof CharacteristicValue) && (this.value.equals(((CharacteristicValue) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}