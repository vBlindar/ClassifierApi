package kg.edu.krsu.vblindar.classifierapi.repository;

import kg.edu.krsu.vblindar.classifierapi.entity.Characteristic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CharacteristicRepository  extends JpaRepository<Characteristic, Long> {
    public boolean existsByName(String name);

    Characteristic findByName(String name);

    @Query("select c from Characteristic c")
    List<Characteristic> getAll();
}
