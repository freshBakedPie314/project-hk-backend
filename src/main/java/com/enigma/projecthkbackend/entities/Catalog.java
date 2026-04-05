package com.enigma.projecthkbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_catalogs", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Catalog {
    @EmbeddedId
    private UserGameId id; // Composite Key

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("gameId")
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "added_at", insertable = false, updatable = false)
    private LocalDateTime addedAt;

    private String status; // e.g., "WANT_TO_PLAY", "PLAYING", "COMPLETED"
}

