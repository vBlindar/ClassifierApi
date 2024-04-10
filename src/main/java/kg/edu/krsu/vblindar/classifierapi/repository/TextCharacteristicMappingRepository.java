package kg.edu.krsu.vblindar.classifierapi.repository;

import kg.edu.krsu.vblindar.classifierapi.entity.TextCharacteristicMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextCharacteristicMappingRepository extends JpaRepository<TextCharacteristicMapping,
        Long> {
}
