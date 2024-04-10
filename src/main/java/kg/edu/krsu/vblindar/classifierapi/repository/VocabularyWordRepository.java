package kg.edu.krsu.vblindar.classifierapi.repository;

import kg.edu.krsu.vblindar.classifierapi.entity.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {
    boolean existsByValue(String value);
}
