package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MoviesApiTest {
    private MoviesStore store;
    private MoviesServer server;
    private Gson gson;
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI moviesUrl = URI.create("http://localhost:8080/movies");

    @BeforeAll
    void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        gson = new Gson();
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @Tag("success")
    @Tag("boundary")
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(moviesUrl).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("application/json;charset=utf-8", response.headers()
                .firstValue("Content-Type")
                .orElse(""));

        List<Movie> movies = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertTrue(movies.isEmpty(), "Expected empty list but got: " + movies);
        assertEquals("[]", response.body(), "Expected empty JSON array string '[]'");
    }

    @Test
    @Tag("success")
    void getMovies_whenMoviesExist_returnsMoviesInInsertionOrder() throws Exception {
        Movie firstMovie = new Movie(0, "The Matrix", 1999);
        Movie secondMovie = new Movie(0, "Inception", 2010);
        store.add(firstMovie);
        store.add(secondMovie);

        HttpRequest request = HttpRequest.newBuilder().uri(moviesUrl).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Movie> movies = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());

        assertEquals(200, response.statusCode());
        assertEquals(List.of(firstMovie, secondMovie), movies);
    }

    @Test
    @Tag("error")
    void moviesEndpoint_whenMethodNotAllowed_returns405AndErrorMessage() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(moviesUrl)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);

        assertEquals(405, response.statusCode());
        assertEquals("Method Not Allowed", error.getMessage());
    }

    @Test
    @Tag("boundary")
    void moviesEndpoint_whenMethodNotAllowed_returnsJsonContentType() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(moviesUrl)
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        assertEquals("application/json;charset=utf-8", response.headers()
                .firstValue("Content-Type")
                .orElse(""));
    }
}
