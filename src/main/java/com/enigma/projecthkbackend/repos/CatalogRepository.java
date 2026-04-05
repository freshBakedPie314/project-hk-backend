package com.enigma.projecthkbackend.repos;

import com.enigma.projecthkbackend.entities.Catalog;
import com.enigma.projecthkbackend.entities.UserGameId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatalogRepository extends JpaRepository<Catalog, UserGameId> {

}
