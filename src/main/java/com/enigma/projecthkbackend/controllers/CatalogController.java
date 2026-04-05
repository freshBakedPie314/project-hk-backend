package com.enigma.projecthkbackend.controllers;

import com.enigma.projecthkbackend.entities.Catalog;
import com.enigma.projecthkbackend.services.CatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("catalog")
public class CatalogController {

    private final CatalogService catalogService;

    CatalogController(CatalogService catalogService)
    {
        this.catalogService = catalogService;
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<Catalog> addToCatalog(@PathVariable String id){
        return catalogService.saveCatalog(id);
    }

}
