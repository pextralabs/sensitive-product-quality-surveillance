package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;
import  play.mvc.Results;

public class JsonResults {

    static final String __JSON = "application/json";

    private JsonResults() {
        throw new IllegalStateException("Utility class");
    }


    private static JsonNode jsonError(String msg) {
        return Json.newObject().put("error", msg);
    }

    public static Result badRequest(String content) {
        return Results.badRequest(jsonError(content)).as(__JSON);
    }

    public static Result unauthorized(String content) {
        return Results.unauthorized(jsonError(content)).as(__JSON);
    }

    public static Result notFound(String content) {
        return Results.notFound(jsonError(content)).as(__JSON);
    }

    public static Result ok(String content) {
        return Results.ok(content).as(__JSON);
    }

}
