package com.enigma.projecthkbackend.entities;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(name = "igdb_id", unique = true)
    private long igdbId;

    @Type(ListArrayType.class) // Use Hibernate @Type
    @Column(name = "genres", columnDefinition = "text[]")
    private List<String> genres;

    @Type(ListArrayType.class) // Use Hibernate @Type
    @Column(name = "platforms", columnDefinition = "text[]")
    private List<String> platforms;

    @Column(name = "igdb_rating")
    private float igdbRating;

    @Column(name = "user_rating_avg")
    private float userRatingAvg;

    @Column(name = "user_rating_count")
    private long userRatingCount;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "cover_url")
    private String coverUrl;
}
