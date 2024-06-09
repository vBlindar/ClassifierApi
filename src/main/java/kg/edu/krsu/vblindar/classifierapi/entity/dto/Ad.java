package kg.edu.krsu.vblindar.classifierapi.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class Ad {
    private Long id;
    private String text;
    private List<String> files;
}
