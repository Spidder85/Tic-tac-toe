package app.datasource.repository;

import app.datasource.model.CurrentGameData;
import app.domain.model.GameStatus;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface CurrentGameJpaRepository extends CrudRepository<CurrentGameData, UUID> {
    List<CurrentGameData> findByStatusAndComputerOpponent(GameStatus status, boolean computerOpponent);
}
