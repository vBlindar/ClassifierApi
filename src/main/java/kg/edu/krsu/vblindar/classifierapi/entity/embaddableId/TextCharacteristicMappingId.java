package kg.edu.krsu.vblindar.classifierapi.entity.embaddableId;


import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextCharacteristicMappingId implements Serializable {


    @ManyToOne
    @JoinColumn(name = "classifiable_text_pr_id")
    private ClassifiableText classifiableTextId;

    @ManyToOne
    @JoinColumn(name = "characteristic_pr_id")
    private Characteristic characteristicId;

    @ManyToOne
    @JoinColumn(name = "characteristic_value_pr_id")
    private CharacteristicValue characteristicValueId;

}