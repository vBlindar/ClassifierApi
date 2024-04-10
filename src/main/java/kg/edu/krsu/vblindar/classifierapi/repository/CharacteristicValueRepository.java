package kg.edu.krsu.vblindar.classifierapi.repository;

import kg.edu.krsu.vblindar.classifierapi.entity.CharacteristicValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CharacteristicValueRepository extends JpaRepository<CharacteristicValue, Long> {

    @Query("select v.id from CharacteristicValue as v where v.id = :id and v.characteristic.id = :characteristicId")
    Optional<CharacteristicValue> findCharacteristicValue(@Param("id") Long id, @Param("characteristicId") Long characteristicId);

    List<CharacteristicValue> findAllByCharacteristicId(Long characteristic_id);
    CharacteristicValue findByValue(String value);

    boolean existsByValueAndCharacteristicId(String value, Long characteristic_id);

    boolean existsById(Long id);
    boolean existsByIdAndCharacteristicId(Long id, Long characteristic_id);
}
