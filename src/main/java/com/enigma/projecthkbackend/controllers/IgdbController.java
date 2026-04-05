package com.enigma.projecthkbackend.controllers;

import com.enigma.projecthkbackend.services.IgdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/games")
@CrossOrigin(origins = "*")
public class IgdbController {

    private final IgdbService igdbService;
    public IgdbController(IgdbService igdbService)
    {
        this.igdbService = igdbService;
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<List<Map<String, Object>>> games(@PathVariable String id)
    {
        return igdbService.getGame(id);
    }

    @GetMapping("/discovery/trending")
    public ResponseEntity<List<Map<String, Object>>> trending()
    {
        return igdbService.getTrending();
    }

    @GetMapping("/discovery/recently-released")
    public ResponseEntity<List<Map<String, Object>>> recentlyReleased(@RequestParam int hypeSortValue)
    {
        return igdbService.getRecentlyReleased(hypeSortValue);
    }

    @GetMapping("/discovery/most-anticipated")
    public ResponseEntity<List<Map<String, Object>>> mostAnticipated()
    {
        return igdbService.getMostAnticipated();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> serach(@RequestParam String query)
    {
        return igdbService.getGamesBasedOnName(query);
    }
}
