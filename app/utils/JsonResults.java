package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;
import  play.mvc.Results;

import static play.mvc.Http.Status.BAD_REQUEST;

public class JsonResults {

    private static JsonNode JsonError(String msg) {
        return Json.newObject().put("error", msg);
    }

    public static Result badRequest(String content) {
        return Results.badRequest(JsonError(content)).as("application/json");
    }

    public static Result unauthorized(String content) {
        return Results.unauthorized(JsonError(content)).as("application/json");
    }

    public static Result notFound(String content) {
        return Results.notFound(JsonError(content)).as("application/json");
    }

    public static Result ok(String content) {
        return Results.ok(content).as("application/json");
    }

}
