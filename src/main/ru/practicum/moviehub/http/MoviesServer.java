package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesStore store;

    public MoviesServer(MoviesStore store, int port) {
        this.store = store;
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/movies", new MoviesHandler());
    }

    public void start() {
        System.out.println("Starting MoviesServer on port " + server.getAddress().getPort());
        server.start();
    }

    public void stop() {
        System.out.println("Stopping MoviesServer");
        server.stop(0);
    }

    private class MoviesHandler extends BaseHttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if ("GET".equals(method)) {
                    String response = gson.toJson(store.getAll());
                    sendText(exchange, response, 200);
                } else {
                    sendNotFound(exchange, "Method Not Allowed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendText(exchange, "{\"message\":\"Internal Server Error\"}", 500);
            } finally {
                exchange.close();
            }
        }
    }
}