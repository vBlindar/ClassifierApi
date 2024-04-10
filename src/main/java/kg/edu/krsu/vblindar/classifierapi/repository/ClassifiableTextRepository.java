package kg.edu.krsu.vblindar.classifierapi.repository;

import kg.edu.krsu.vblindar.classifierapi.entity.ClassifiableText;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassifiableTextRepository extends JpaRepository<ClassifiableText,Long> {
    boolean existsByText(String text);
}
