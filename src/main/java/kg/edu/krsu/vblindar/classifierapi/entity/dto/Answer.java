package kg.edu.krsu.vblindar.classifierapi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class Answer {

    private Map<Boolean,String> text;
    private List<Map<Boolean,String>> images;

}
