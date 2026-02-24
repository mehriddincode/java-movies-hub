package ru.practicum.moviehub.http;

enum RequestMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH;

    static RequestMethod from(String methodName) {
        try {
            return RequestMethod.valueOf(methodName);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }
}
