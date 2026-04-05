package com.enigma.projecthkbackend.services;

import com.enigma.projecthkbackend.entities.Catalog;
import com.enigma.projecthkbackend.entities.Game;
import com.enigma.projecthkbackend.entities.User;
import com.enigma.projecthkbackend.entities.UserGameId;
import com.enigma.projecthkbackend.repos.CatalogRepository;
import com.enigma.projecthkbackend.repos.GameRepository;
import com.enigma.projecthkbackend.repos.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.hibernate.SpringSessionContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final CatalogRepository catalogRepository;
    private final IgdbService igdbService;
    public CatalogService(UserRepository userRepository, GameRepository gameRepository, CatalogRepository catalogRepository, IgdbService igdbService) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.catalogRepository = catalogRepository;
        this.igdbService = igdbService;
    }

    public ResponseEntity<Catalog> saveCatalog(String igdb_id_str) {
        long igdb_id = Long.parseLong(igdb_id_str); // Parse as long

        // Check if game exists in local DB first
        Optional<Game> gameOpt = gameRepository.findByIgdbId(igdb_id);
        Game game;

        if (gameOpt.isEmpty()) {
            // Pass the long id here
            game = igdbService.getGameById(igdb_id).getBody();
            game = gameRepository.save(game);
        } else {
            game = gameOpt.get();
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserGameId catalogId = UserGameId.builder()
                .userId(user.getId())
                .gameId(game.getId())
                .build();

        if (catalogRepository.existsById(catalogId)) {
            throw new RuntimeException("Game already in your catalog");
        }

        Catalog newCatalog = Catalog.builder()
                .id(catalogId)
                .user(user)
                .game(game)
                .addedAt(LocalDateTime.now())
                .status("WANT_TO_PLAY")
                .build();

        return ResponseEntity.ok(catalogRepository.save(newCatalog));
    }
}
