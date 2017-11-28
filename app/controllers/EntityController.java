package controllers;

import actors.Protocols;
import actors.WsSubscriberActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.name.Named;
import models.Entity;
import models.Sensor;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import repos.EntityRepo;
import utils.JsonResults;
import utils.JsonViews;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class EntityController extends Controller {

    private final ActorRef sceneActor;
    private final HttpExecutionContext httpExecutionContext;
    private final EntityRepo entityRepo;

    @Inject
    public EntityController(EntityRepo entityRepo,
                            HttpExecutionContext httpExecutionContext,
                            @Named("scene") ActorRef sceneActor) {
        this.entityRepo = entityRepo;
        this.httpExecutionContext = httpExecutionContext;
        this.sceneActor = sceneActor;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> addEntity() {

        JsonNode entityNode = request().body().asJson();
        if (!entityNode.has("name"))
            return CompletableFuture.completedFuture(JsonResults.badRequest("missing name"));

        if (!entityNode.has("type"))
            return CompletableFuture.completedFuture(JsonResults.badRequest("missing type"));

        Entity entity = new Entity(entityNode.findPath("type").textValue(), entityNode.findPath("name").textValue());

        return entityRepo.save(entity).thenApplyAsync(
                ent -> {
                    sceneActor.tell(new Protocols.Operation(ent, Protocols.Operation.Type.INSERT), null );
                    return JsonResults.ok(JsonViews.toString(ent));
                },
                httpExecutionContext.current()
        );

    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> addSensor(Long entityId) {

        return entityRepo.getById(Entity.class, entityId).thenApplyAsync(
            maybeEntity ->
                    maybeEntity.map(
                            entity -> {
                                JsonNode sensorNode = request().body().asJson();

                                if (!sensorNode.has("label"))
                                    return JsonResults.badRequest("missing label");

                                if (!sensorNode.has("default"))
                                    return JsonResults.badRequest("missing default value");

                                Sensor sensor = new Sensor((Entity) entity, sensorNode.findPath("label").textValue(), sensorNode.findPath("default").doubleValue() );
                                sensor.save();
                                sceneActor.tell(new Protocols.Operation(sensor, Protocols.Operation.Type.INSERT), null );
                                return ok(Json.toJson(sensor));
                            }
                    ).orElse(
                            JsonResults.notFound("entity not found")
                    ),
            httpExecutionContext.current()
        );
    }

    public CompletionStage<Result> updateSensor(Long EntityId, Long sensorId) {

        return entityRepo.getSensorByEntity(EntityId, sensorId).thenApplyAsync(
            maybeEntity ->
                maybeEntity.map(
                    sensor -> {
                        JsonNode sensorNode = request().body().asJson();
                        if (!sensorNode.has("value"))
                            return JsonResults.badRequest("missing value");

                        sensor.setValue(sensorNode.findPath("value").doubleValue());

                        sensor.save();
                        sceneActor.tell(new Protocols.Operation(sensor, Protocols.Operation.Type.UPDATE), null );
                        return ok(Json.toJson(sensor));
                    }
                ).orElse(
                        JsonResults.notFound("sensor not found")
                ),
            httpExecutionContext.current()
        );

    }

}
            
