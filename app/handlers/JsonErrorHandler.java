package handlers;

import com.fasterxml.jackson.databind.JsonNode;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class JsonErrorHandler implements HttpErrorHandler {

    private JsonNode jsonError(String msg) {
        return Json.newObject().put("error", msg);
    }

    public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(
                Results.status(statusCode, jsonError(message)).as("application/json")
        );
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        return CompletableFuture.completedFuture(
                Results.internalServerError(jsonError(exception.getMessage())).as("application/json")
        );
    }

}
