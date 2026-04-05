package com.enigma.projecthkbackend.repos;

import com.enigma.projecthkbackend.entities.Catalog;
import com.enigma.projecthkbackend.entities.Game;
import com.enigma.projecthkbackend.entities.UserGameId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByIgdbId(long igdbId);
}
