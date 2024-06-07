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
    private Boolean checked;

    public static Boolean checkForFalse(Map<Boolean,String> text, List<Map<Boolean,String> > images) {
        if (text != null && text.containsKey(Boolean.FALSE)) {
            return Boolean.FALSE;
        }
        if (images != null) {
            for (Map<Boolean, String> imageMap : images) {
                if (imageMap.containsKey(Boolean.FALSE)) {
                    return Boolean.FALSE;
                }
            }
        }
       return Boolean.TRUE;
    }

}
