package controllers;

import actors.Protocols;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.name.Named;
import models.sensoring.*;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import repos.SensorRepo;
import scala.Option;
import utils.JsonResults;
import utils.JsonViews;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static actors.Protocols.Operation.Type.INSERT;

public class SensorController extends Controller {

    private final SensorRepo sensorRepo;
    private final ActorRef sceneActor;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public SensorController(SensorRepo sensorRepo,
                            @Named("scene") ActorRef sceneActor,
                            HttpExecutionContext httpExecutionContext) {
        this.sensorRepo = sensorRepo;
        this.sceneActor = sceneActor;
        this.httpExecutionContext = httpExecutionContext;
    }

    public CompletionStage<Result> getSensors() {
        ObjectMapper mapper = new ObjectMapper();
        return sensorRepo.getSensors().thenApplyAsync(list -> {
            return JsonResults.ok(JsonViews.toString(list));
        }, httpExecutionContext.current() );
    }

    public CompletionStage<Result> getSensor(Long sensorId) {
        return sensorRepo.getSensorBy(sensorId).thenApplyAsync(
                (sensor) ->
                    (sensor.isPresent()) ? JsonResults.ok(JsonViews.toString(sensor.get())) : JsonResults.notFound("sensor not found"),
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> createSensor() {

        JsonNode sensorNode = request().body().asJson();
        if (!sensorNode.has("label"))
            return CompletableFuture.completedFuture(JsonResults.badRequest("missing label"));

        Sensor sensor = new Sensor().setName(sensorNode.findPath("label").textValue());

        if (sensorNode.has("description"))
            sensor.setDescription(sensorNode.findPath("description").textValue());

        if (sensorNode.has("tag"))
            sensor.setTag(sensorNode.findPath("tag").textValue());

        return sensorRepo.save(sensor).thenApplyAsync(
                s -> {
                    sceneActor.tell(new Protocols.Operation(s, INSERT), null);
                    return JsonResults.ok(JsonViews.toString(s));
                },
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> updateSensor(Long sensorId) {
        return sensorRepo.getById(Sensor.class, sensorId).thenApplyAsync(
                sensor -> {
                    if (sensor.isPresent()) {
                        Sensor updating = (Sensor) sensor.get();
                        JsonNode sensorNode = request().body().asJson();

                        if (!sensorNode.has("label"))
                            return JsonResults.badRequest("missing label");

                        updating.setName(sensorNode.findPath("label").textValue());

                        if (sensorNode.has("description"))
                            updating.setDescription(sensorNode.findPath("description").textValue());

                        if (sensorNode.has("tag"))
                            updating.setTag(sensorNode.findPath("tag").textValue());

                        updating.update();
                        return JsonResults.ok(JsonViews.toString(updating));
                    } else return JsonResults.notFound("sensor not found");
                },
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> deleteSensor(Long sensorId) {

        return sensorRepo.getById(Sensor.class, sensorId).thenApplyAsync(
                sensor -> {
                    if (sensor.isPresent()) {
                        Sensor deleting = (Sensor) sensor.get();
                        deleting.delete();
                        return ok();
                    } else return JsonResults.notFound("sensor not found");
                },
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> createStream(Long sensorId) {
        return sensorRepo.getById(Sensor.class, sensorId).thenApplyAsync(
                sensorOpt ->
                        sensorOpt.map(
                                sensor -> {
                                    JsonNode streamNode = request().body().asJson();

                                    if (!streamNode.has("label"))
                                        return JsonResults.badRequest("missing label");

                                    Stream stream = new Stream((Sensor) sensor).setName(streamNode.findPath("label").textValue());

                                    if (streamNode.has("tag"))
                                        stream.setTag(streamNode.findPath("tag").textValue());

                                    stream.save();
                                    sceneActor.tell(new Protocols.Operation(stream, INSERT), null);
                                    return JsonResults.ok(JsonViews.toString(stream));
                                }
                        ).orElse(
                                JsonResults.notFound("sensor not found")
                        ),
                httpExecutionContext.current()
        );
    }

    public CompletionStage<Result> getStream(Long sensorId, Long streamId) {
        return sensorRepo.getStreamBy(sensorId, streamId, 5).thenApplyAsync(
                streamOpt ->
                        streamOpt
                                .map(stream -> JsonResults.ok(JsonViews.toString(stream, JsonViews.Complete.class)))
                                .orElse(JsonResults.notFound("stream not found"))
                ,
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> updateStream(Long sensorId, Long streamId) {
        return sensorRepo.getStreamBy(sensorId, streamId).thenApplyAsync(
                streamOpt ->
                    streamOpt.map(
                        stream -> {
                            JsonNode streamNode = request().body().asJson();

                            if (!streamNode.has("label"))
                                return JsonResults.badRequest("missing label");

                            stream.setName(streamNode.findPath("label").textValue());
                            stream.update();
                            return JsonResults.ok(JsonViews.toString(stream));
                        }
                    ).orElse(
                        JsonResults.notFound("stream not found")
                    ),
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> deleteStream(Long sensorId, Long streamId) {
        return sensorRepo.getStreamBy(sensorId, streamId).thenApplyAsync(
                streamOpt ->
                        streamOpt.map(
                                stream -> {
                                    Sensor sensor = stream.getSensor();
                                    stream.delete();
                                    return JsonResults.ok(JsonViews.toString(sensor));
                                }
                        ).orElse(
                                JsonResults.notFound("stream not found")
                        ),
                httpExecutionContext.current()
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> putData(String sensorKey, String streamKey) {
        return sensorRepo.getStreamBy(sensorKey, streamKey).thenApplyAsync(
                streamOpt ->
                        streamOpt.map(
                            stream -> {

                                JsonNode dataNode = request().body().asJson();

                                StreamData data = !dataNode.has("timestamp") ?
                                                new StreamData(stream, System.currentTimeMillis()) :
                                                new StreamData(stream, System.currentTimeMillis(), dataNode.get("timestamp").asLong());

                                if (!dataNode.has("values"))
                                    return JsonResults.badRequest("missing values");

                                JsonNode valuesNode = dataNode.get("values");
                                Iterator<String> itr = valuesNode.fieldNames();
                                while (itr.hasNext()) {
                                    String key = itr.next();
                                    data.addTuple(key, valuesNode.get(key).asText());
                                }
                                data.save();
                                sceneActor.tell(new Protocols.Operation(data, INSERT), null);
                                return ok(Json.toJson(data));
                            }
                        ).orElse(
                            JsonResults.notFound("stream not found")
                        ),
                httpExecutionContext.current()
        );
    }

    public CompletionStage<Result> getStreamData(Long sensorId, Long streamId, Option<String> start, Option<String> end, Option<String> length) {
        Map<String, Long> params = new HashMap<String, Long>();
        start.map(p -> params.put("start", new Long(p)));
        end.map(p -> params.put("end", new Long(p)));
        length.map(p -> params.put("length", new Long(p)));
        return sensorRepo.getStreamDataBy(sensorId, streamId, params).thenApplyAsync(
                dataList -> ok(Json.toJson(dataList)),
                httpExecutionContext.current()
        );
    }

}
