package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final Gson gson = new Gson();

    protected void sendText(HttpExchange exchange, String responseText, int statusCode) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, gson.toJson(new ErrorResponse(message)), 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, gson.toJson(new ErrorResponse(message)), 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, gson.toJson(new ErrorResponse(message)), 405);
    }

    protected void sendInternalServerError(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, gson.toJson(new ErrorResponse(message)), 500);
    }
}
