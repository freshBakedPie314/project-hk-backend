package com.enigma.projecthkbackend.services;

import com.enigma.projecthkbackend.entities.Game;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IgdbService {

    private String baseUrl = "https://api.igdb.com/v4";
    private String accessToken = null;
    private long expiryTimeMillis;

    @Value("${igbd.client-id}")
    private String CLIENT_ID;
    @Value("${igdb.client-secret}")
    private String CLIENT_SECRET;

    RestClient restClient;
    long currentSystemTimeSeconds;

    private IgdbService()
    {
        this.restClient = RestClient.create();
        this.currentSystemTimeSeconds = System.currentTimeMillis()/1000;
    }

    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() >= expiryTimeMillis) {
            refreshAccessToken();
        }
        return accessToken;
    }

    public void refreshAccessToken()
    {
        String url = "https://id.twitch.tv/oauth2/token?"
                + "client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET
                + "&grant_type=client_credentials";

        Map<String, String> response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });

        accessToken = response.get("access_token");
        long second = Long.parseLong(response.get("expires_in"));
        this.expiryTimeMillis = System.currentTimeMillis() + (second * 1000) - 60000; //60s safety buffer
    }

    /// Get game info by game_id
    public ResponseEntity<List<Map<String, Object>>> getGame(String game_id)
    {
        List<Map<String, Object>> response = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID",  CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, summary, cover.url;" +
                        "where id = " + game_id + "; limit 10;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>(){});

        return  ResponseEntity.ok(response);
    }

    /// Recent games filtered by hype(normalised) and current playing(normalised)
    // TODO: Make sure to always return 10 games
    public ResponseEntity<List<Map<String, Object>>> getTrending()
    {
        long oneMonthAgo = (currentSystemTimeSeconds) - (30L * 24 * 60 * 60);

        // Get High Hype + Recent Games
        List<Map<String, Object>> games = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID",  CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, id, cover.url, hypes; where first_release_date >= " + oneMonthAgo + "& first_release_date < " + currentSystemTimeSeconds + "; sort hypes desc; limit 100;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (games.isEmpty()) return ResponseEntity.ok(Collections.emptyList());

        double maxHype = Double.parseDouble(games.get(0).get("hypes").toString());
        double minHype = Double.parseDouble(games.get(games.size()-1).get("hypes").toString());



        String ids = games.stream().map(g ->
                g.get("id").toString())
                .collect(Collectors.joining(","));

        // Get Playing for these specific games
        List<Map<String, Object>> playing = restClient.post()
                .uri(baseUrl + "/popularity_primitives")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID",  CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields game_id, value; where popularity_type = 3 & game_id = (" + ids + "); sort value desc;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        double maxValue = Double.parseDouble(playing.get(0).get("value").toString());
        double minValue = Double.parseDouble(playing.get(playing.size()-1).get("value").toString());

        Map<Object, Double> weightMap = new HashMap<>();

        for (Map<String, Object> entry : playing) {
            double value = Double.parseDouble(entry.get("value").toString());
            double normalisedValue = (maxValue == minValue) ? 1.0 : (value - minValue) / (maxValue - minValue);
            weightMap.put(entry.get("game_id"), normalisedValue);
        }

        for (Map<String, Object> game : games) {
            double hype = Double.parseDouble(game.getOrDefault("hypes", "0").toString());
            double normalisedHype = (maxHype == minHype) ? 1.0 : (double)(hype - minHype) / (maxHype - minHype);
            weightMap.merge(game.get("id"), normalisedHype, Double::sum);
        }

        games.sort((a, b) -> {
            double valA = weightMap.getOrDefault(a.get("id"), 0.0);
            double valB = weightMap.getOrDefault(b.get("id"), 0.0);
            return Double.compare(valB, valA);
        });

        return ResponseEntity.ok(games.subList(0, Math.min(games.size(), 10)));
    }

    /// Recently released games in 1 month (hype > hypeSortValue), ordered by date
    public ResponseEntity<List<Map<String, Object>>> getRecentlyReleased(int hypeSortValue) {

        long monthAgo = currentSystemTimeSeconds - (30*24*60*60);

        List<Map<String, Object>> response = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, id, cover.url, hypes; " +
                        "where first_release_date >= " + monthAgo + "& first_release_date < " + currentSystemTimeSeconds
                        + "& hypes >="+ hypeSortValue + ";" +
                        "sort first_release_date desc;" +
                        "limit 10;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        return  ResponseEntity.ok(response);
    }

    public ResponseEntity<List<Map<String, Object>>> getMostAnticipated() {
        List<Map<String, Object>> response = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, id, cover.url, hypes; " +
                        "where first_release_date > " + currentSystemTimeSeconds
                         + ";" +
                        "sort hypes desc;" +
                        "limit 10;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        return  ResponseEntity.ok(response);
    }

    public ResponseEntity<List<Map<String, Object>>> getGamesBasedOnName(String query) {
        List<Map<String, Object>> response = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, id, cover.url, hypes; " +
                        "search " + query + ";" +
                        "limit 10;")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        return  ResponseEntity.ok(response);
    }

    public ResponseEntity<Game> getGameById(long id) {
        List<Map<String, Object>> game = restClient.post()
                .uri(baseUrl + "/games")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Client-ID", CLIENT_ID)
                .header("Authorization", "Bearer " + getAccessToken())
                .body("fields name, id, cover.url, genres, platforms, rating, summary; " +
                        "where id =" + id
                        + ";"
                )
            .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        Map<String, Object> gameData = game.get(0);

        String coverUrl = "";
        if (gameData.get("cover") instanceof Map<?, ?> cover) {
            Object urlObj = cover.get("url");
            if (urlObj != null) {
                coverUrl = "https:" + urlObj.toString().replace("t_thumb", "t_cover_big");
            }
        }

        Object platformData = gameData.get("platforms");
        List<String> platformStrings = (platformData instanceof List<?> list)
                ? list.stream().map(Object::toString).toList()
                : Collections.emptyList();

        Object genreData = gameData.get("genres");
        List<String> genreStrings = (platformData instanceof List<?> list)
                ? list.stream().map(Object::toString).toList()
                : Collections.emptyList();

        float rating = 0.0f;
        if (gameData.get("rating") != null) {
            rating = Float.parseFloat(gameData.get("rating").toString());
        }

        Game gameResponse = Game.builder()
                .igdbId(Long.parseLong(gameData.get("id").toString()))
                .name(gameData.get("name").toString())
                .coverUrl(coverUrl)
                .igdbRating(rating)
                .summary(gameData.getOrDefault("summary", "").toString())
                .platforms(platformStrings)
                .genres(genreStrings)
                .build();

        return ResponseEntity.ok(gameResponse);
    }
}
