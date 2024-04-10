package kg.edu.krsu.vblindar.classifierapi.entity;



import kg.edu.krsu.vblindar.classifierapi.entity.embaddableId.TextCharacteristicMappingId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Entity
@Table(name = "classifiable_texts_characteristics")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class TextCharacteristicMapping {

    @EmbeddedId
    TextCharacteristicMappingId id;


}

